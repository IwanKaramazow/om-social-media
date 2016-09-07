(ns bp.utils
  (:require [cognitect.transit :as t]
            [om.next :as om])
  (:import [goog.net XhrIo]))



(defn concat-query [& Components]
  (into []
        (distinct
         (mapcat #(om/get-query %) Components))))

(defn deep-merge [& xs]
  "Merges nested maps without overwriting existing keys."
  (cond
    (every? map? xs) (apply merge-with deep-merge xs)
    ;; (and (every? vector? xs) (apply (partial not=) xs)) (into [] (apply (comp distinct concat) xs))
    :else (last xs)))


(defn tempid-migrate [app-state _ tempids _]
  "merge tempids without throwing away app state"
  (clojure.walk/prewalk #(if (-> % type (= om.tempid/TempId))
                           (get tempids %) %)
                        app-state))

(defn transit-post [url]
  (fn [{:keys [remote] :as env} cb]
    (let [{:keys [query rewrite]} (om/process-roots remote)]
      (.send XhrIo url
             (fn [e]
               (this-as this
                        (let [data (rewrite (t/read (t/reader :json) (.getResponseText this)))]
                          (cb data))))
             "POST" (t/write (t/writer :json) query)
             #js {"Content-Type" "application/transit+json"}))))


;; TODO what if multiple mutation results arrive?
;; (defn custom-merge [reconciler state res query]
;;   (let [first-key (-> res keys first)]
;;     (if (symbol? first-key)
;;       (om/default-merge reconciler state (get-in res [first-key :result]) query)
;;       (om/default-merge reconciler state res query))))
