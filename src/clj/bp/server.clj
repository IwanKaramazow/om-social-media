(ns bp.server
  (:require [clojure.java.io :as io]
            [om.next.server :as om]
            [bp.parser :as parser]
            [bp.middleware :as m]
            [ring.util.response :as res]
            [bidi.bidi :as bidi]
            [bidi.ring :refer (make-handler)]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.defaults :refer [wrap-defaults api-defaults]]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [ring.middleware.gzip :refer [wrap-gzip]]
            [ring.middleware.logger :refer [wrap-with-logger]]
            [ring.middleware.resource :as resource]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.content-type :as ct]
            [ring.middleware.not-modified :as nm]
            [org.httpkit.server :as httpkit]
            [environ.core :refer [env]]
            [buddy.sign.jwt :as jwt]
            [compojure.core :refer [ANY GET PUT POST DELETE defroutes]]
            [compojure.route :refer [resources]]
            [clojure.core.async :as async  :refer (<! <!! >! >!! put! chan go go-loop)]
            [bp.ws :as ws]
            ))



;; We can watch this atom for changes if we like
(add-watch ws/connected-uids :connected-uids
  (fn [_ _ old new]
    (when (not= old new)
      (println "Connected uids change: " new))))



(def server-parser
  (om/parser {:read parser/read :mutate parser/mutate}))

(def anonymous {:viewer/id 0
                :viewer/role "Anonymous"})

(def secret "secret")

(def anonymous-jwt (jwt/sign anonymous secret))

(defn cookies->auth [req]
  (let [cookies (:cookies req)]
    (if-not (empty? cookies)
      (jwt/unsign (get-in cookies ["token" :value]) secret)
      anonymous)))



(defn make-token
  ([req]
   (if-let [cookies (:cookies req)]
     cookies
     {"token" {:value anonymous-jwt}})))

(defn index-handler [req]
  {:status 200
   :cookies (make-token req)
   :headers {"Content-Type" "text/html; charset=utf-8"}
   :body (io/input-stream (io/resource "public/index.html"))})


(defn pull-cookies [req payload]
  (let [k (-> payload keys first)
        val (get payload k)]
    (if (contains? val :cookies)
      {:status  200
       :headers {}
       :cookies {"token" {:value (jwt/sign (:cookies val) secret)}}
       :body    {k (dissoc val :cookies)}}
      {:status  200
       :headers {}
       :cookies (:cookies req)
       :body payload})))

(defn pull-up [m]
  "Walk one level deeper in a map.
   E.g. {:a {:b :c}} -> {:b :c}"
  (let [k (-> m keys first)]
    (get m k)))

(defn logger [handler]
  (fn [req]
    ;; (println "incoming: " (:transit-params req))
    ;; (println "=====================================")
    ;; (println "=====================================")
    ;; (println (:session req))
    ;; (println "=====================================")
    ;; (println "=====================================")
    (handler req)))

(defn api-handler [req]
  (if-let [uid (:uid (:session req))]
    (let [res (server-parser {:viewer/id uid} (:transit-params req))]
      (pull-cookies req res))
    (res/response {})))

(defn w-session [handler]
  (fn [req]
    (let [uid (:uid (jwt/unsign (get-in req [:cookies "app" :value]) secret))]
      (handler (assoc-in req [:session :uid] uid)))))


;; (GET  "/chsk" req (ring-ajax-get-or-ws-handshake req))
;; (POST "/chsk" req (ring-ajax-post                req))

;; (def routes
;;   ["" { "/api" {:post {[""] api-handler}}
;;        "/chsk" {:post {[""] ring-ajax-post}
;;                 :get {[""] ring-ajax-get-or-ws-handshake}}
;;        true index-handler}])

;; (defn login-handler
;;   "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
;;   In our simplified example we'll just always successfully authenticate the user
;;   with whatever user-id they provided in the auth request."
;;   [req]
;;   (let [{:keys [session params]} req
;;         {:keys [username]} params
;;         id (case username
;;              "Maxim" 101
;;              "Sebastian" 100
;;              "Jesse" 102)]
;;     (println "login-request: " params)
;;     {:status 200
;;      :session (assoc session :uid id)}))

(defn login-handler
  "Here's where you'll add your server-side login/auth procedure (Friend, etc.).
  In our simplified example we'll just always successfully authenticate the user
  with whatever user-id they provided in the auth request."
  [req]
  (let [{:keys [session params]} req
        {:keys [username]} params
        id (case username
             "Maxim" 101
             "Sebastian" 100
             "Jesse" 102)]
    {:status 200
     :cookies {:app {:value (jwt/sign {:uid id} secret)}}}))

(defroutes routes
  (GET "/" _
       index-handler)
  (GET "/login" _ index-handler)
  (GET "/story" _ index-handler)
  (GET "/friends" _ index-handler)
  (GET "/settings" _ index-handler)
  (POST "/api" _ api-handler)
  (POST "/chsk" _ ws/ring-ajax-post)
  (GET "/chsk" _ ws/ring-ajax-get-or-ws-handshake)
  (POST "/login" _ login-handler)
  (resources "/"))

;; (def handler
;;   (make-handler routes))

(def http-handler
  (-> routes
      w-session
      wrap-cookies
   
   (ring.middleware.keyword-params/wrap-keyword-params)
   (ring.middleware.params/wrap-params)
   
      m/wrap-transit-params 
      m/wrap-transit-response
      wrap-gzip))

;; (defn -main [& [port]]
;;   (let [port (or port (Integer. 10555))]
;;     (run-jetty
;;      (wrap-reload #'bp.server/http-handler)
;;      {:port (Integer. 10555) :join? false})))

(defn start-server! []
  (httpkit/run-server (wrap-reload #'http-handler) {:port 10555}))

(defn -main []
  (start-server!))



