(ns bp.parser
  (:refer-clojure :exclude [read])
  (:require [bp.ws :as ws] ))

(defn dispatch
  [_ key _] key)


(defmulti read dispatch)


(def userdb (atom {100 {:user/id 100
                        :user/name "Sebastian"
                        :user/username "SebastianMarkbage"
                        :user/role "Developer"
                        :user/birthdate "25/03/1980"
                        :user/friends [101]
                        :user/avatar "https://pbs.twimg.com/profile_images/762857977036480513/G6HPkHDy_bigger.jpg"
                        :user/background "https://pbs.twimg.com/profile_banners/668863/1455061590/600x200"}
              101 {:user/id 101
                   :user/name "Maxim"
                   :user/username "MaximIsNotAFunction"
                   :user/role "Admin"
                   :user/friends [100 102]
                   :user/birthdate "13/10/1975"
                   :user/avatar "https://pbs.twimg.com/profile_images/539087534530121728/amtAYNfY_bigger.jpeg"
                   :user/background "https://pbs.twimg.com/profile_banners/522120048/1406982347/600x200"
                   }
              102 {:user/id 102
                   :user/name "Jesse"
                   :user/username "Jessemthomas"
                   :user/role "Developer"
                   :user/friends [101]
                   :user/birthdate"12/04/1981"
                   :user/avatar "https://pbs.twimg.com/profile_images/728413909971931136/L7OeoCZt_bigger.jpg"
                   :user/background "https://pbs.twimg.com/profile_banners/28169211/1462501166/600x200"
                   }}))

(def dashboard (atom [{:db/id 1 :type :dashboard/photo
                  :photo/image "https://scontent-mrs1-1.cdninstagram.com/t51.2885-15/s640x640/sh0.08/e35/14145358_892561300847794_634103119_n.jpg?ig_cache_key=MTMyOTQ5MTc4MjM5NjE2ODgyMw%3D%3D.2"
                  :photo/caption "Double ride + run meant lots of time doing this today. Got to try out some new prototype #descente cycling gear. Good stuff!"
                  :photo/author {:user/id 102
                                 :user/name "Jesse"
                                 :user/username "Jessemthomas"
                                 :user/role "Developer"
                                 :user/friends [101]
                                 :user/avatar "https://pbs.twimg.com/profile_images/728413909971931136/L7OeoCZt_bigger.jpg"
                                 :user/background "https://pbs.twimg.com/profile_banners/28169211/1462501166/600x200"
                                 }
                  :favorites 0}
                 {:db/id 2 :type :dashboard/post
                  :favorites 23
                  :post/title "Strange realization"
                  :post/author {:user/id 100
                                :user/name "Sebastian"
                                :user/username "SebastianMarkbage"
                                :user/role "Developer"
                                :user/friends [101]
                                :user/avatar "https://pbs.twimg.com/profile_images/762857977036480513/G6HPkHDy_bigger.jpg"
                                :user/background "https://pbs.twimg.com/profile_banners/668863/1455061590/600x200"}
                  :post/content "The best performance tool you have is the slow-mo video camera mode on your phone. You can gather so much interesting data from it."}]))


;; [:db/id :type :post/title :post/author :post/content]



(defn prepare-profile [user]
  (let [friendsCount (count (:user/friends user))]
    (-> user
        (dissoc :user/friends)
        (assoc :user/friendsCount friendsCount))))























(defmethod read :viewer
  [{:keys [viewer/id]} key params]
  {:value (prepare-profile (get @userdb id))})






















(defmethod read :new
  [{:keys [viewer/id]} key params]
  {:value (prepare-profile (get @userdb id))})

(defmethod read :friends
  [{:keys [viewer/id]} key params]
  (let [friends (get-in @userdb [id :user/friends])]
    {:value (into [] (map #(-> (get @userdb %) prepare-profile) friends))}))

(defmethod read :suggested
  [{:keys [viewer/id]} key params]
  (let [users @userdb
        friends (into #{} (get-in users [id :user/friends]))
        ids (into #{} (keys users))
        diff (into [] (clojure.set/difference  ids friends #{id}))]
    {:value (into []
                  (map #(-> (get users %) prepare-profile) diff))}))

(defmethod read :dashboard/items
  [env key params]
  {:value @dashboard})

(defmulti mutate dispatch)


(defn broadcast-post [text id]
  (let [uids (:any @ws/connected-uids)]
    (doseq [uid uids]
      (ws/chsk-send! uid
                     [:posts/new (conj @dashboard {:db/id (rand-int 100000)
                                           :favorites 0
                                           :type :dashboard/post
                                           :post/title ""
                                           :post/author (get @userdb id)
                                           :post/content text})]))))

(defn add-posts [text id]
  (swap! dashboard conj {:db/id (rand-int 100000)
                                      :favorites 0
                                      :type :dashboard/post
                                      :post/title ""
                                      :post/author (get @userdb id)
                                      :post/content text}))

(defmethod mutate 'story/submit
  [_ _ {:keys [story/text user/id]}]
  {:action (fn []
             (let [_ (broadcast-post text id)
                   _ (add-posts text id)]
               (+ 1 1)))})

[;; broadcast!
 ;;        (fn [i]
 ;;          (let [uids (:any @connected-uids)]
 ;;            (debugf "Broadcasting server>user: %s uids" (count uids))
 ;;            (doseq [uid uids]
 ;;              (chsk-send! uid
 ;;                [:some/
 ;;                 {:what-is-this "An async broadcast pushed from server"
 ;;                  :how-often "Every 10 seconds"
 ;;                  :to-whom uid
 ;;                  :i i}]))))
 ]


;; (defn start-example-broadcaster!
;;   "As an example of server>user async pushes, setup a loop to broadcast an
;;   event to all connected users every 10 seconds"
;;   []
;;   (let [broadcast!
;;         (fn [i]
;;           )]

;;     (go-loop [i 0]
;;       (<! (async/timeout 10000))
;;       (broadcast! i)
;;       (recur (inc i)))))

;; {:db/id 2 :type :dashboard/post
;;                   :favorites 23
;;                   :post/title "Strange realization"
;;                   :post/author {:user/id 100
;;                                 :user/name "Sebastian"
;;                                 :user/username "SebastianMarkbage"
;;                                 :user/role "Developer"
;;                                 :user/friends [101]
;;                                 :user/avatar "https://pbs.twimg.com/profile_images/762857977036480513/G6HPkHDy_bigger.jpg"
;;                                 :user/background "https://pbs.twimg.com/profile_banners/668863/1455061590/600x200"}
;;                   :post/content "The best performance tool you have is the slow-mo video camera mode on your phone. You can gather so much interesting data from it."}
