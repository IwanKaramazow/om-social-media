(ns bp.core
  (:require-macros
   [cljs.core.async.macros :as asyncm :refer (go go-loop)])
  (:require [goog.dom :as gdom]
            [cljs.core.async :as async :refer (<! >! put! chan)]
            [om.next :as om :refer-macros [defui]]
            [om.dom :as dom]
            [bp.utils :as u]
            [bp.parser :as p]
            [bp.form :as form]
            [bp.ws :as ws]
            [compassus.core :as compassus]
            [bp.history :as h]
            [bp.timetravel :as tt]
            [taoensso.sente  :as sente  :refer (cb-success?)]
            [bp.proptest]))

(enable-console-print!)

(defn handleChange [c k e]
  (let [v (.. e -target -value)]
    (om/update-state! c assoc k v)))


(defonce app-state (atom {:path js/window.location.pathname}))


(defn logo []
  (dom/div #js {:className "circles"}
           (dom/div nil)
           (dom/div nil)
           (dom/div nil)
           (dom/span nil)))

(defn goto [c path]
  (om/transact! c `[(path/change {:path ~path}) :path]))

(defn make-active [current test className]
  (if (= current test)
    (str className " Active")
    className))

(defn navbar [c path props]
  (let [{:keys [user/name]} props]
    (dom/div #js {:className "Navbar-Container"}
             (dom/nav #js {:className "Navbar-Navigation"}
                      (dom/ul #js {:className "Navbar-NavigationList"}
                              (dom/li #js {:className (make-active path "/story" "Navbar-NavigationItem")
                                           :onClick #(goto c "/story")}
                                      (dom/a nil "🏂 Story Feed"))
                              (dom/li #js {:className (make-active path "/friends" "Navbar-NavigationItem")
                                           :onClick #(goto c "/friends")}
                                      (dom/a nil "👫 Friends"))
                              (dom/li #js {:className (make-active path "/settings" "Navbar-NavigationItem")
                                           :onClick #(goto c "/settings")}
                                      (dom/a nil  "⚙ Settings"))))
             (dom/a #js {:className "Navbar-Logo"
                         :onClick #(goto c "/")})
             (dom/div #js {:className "Navbar-Right"}
                      (dom/div nil (str "Welcome " name "! 😄"))))))


(defn statistics [friends-count role]
  (dom/div #js {:className "ProfileCard-StatsContainer"}
           (dom/div #js {:className "ProfileCard-Stats"}
                    (dom/div #js {:className "ProfileCard-StatLabel"} "Friends")
                    (dom/div #js {:className "ProfileCard-StatValue"} friends-count))
           (dom/div #js {:className "ProfileCard-Stats"}
                    (dom/div #js {:className "ProfileCard-StatLabel"} "Role")
                    (dom/div #js {:className "ProfileCard-StatValue"} role))))









(defui ProfileCard
  static om/Ident
  (ident [_ {:keys [user/id]}]
         [:users/by-id id])
  static om/IQuery
  (query [_]
         [:user/id
          :user/username
          :user/role
          :user/name
          :user/friendsCount
          :user/avatar
          :user/background
          :user/birthdate])
  Object
  (render [this]
          (let [{:keys [user/username user/role
                        user/name user/friendsCount
                        user/avatar user/background user/birthdate]} (om/props this)]
            (dom/div #js {:className "ProfileCard"}
                     (dom/div #js {:className "ProfileCard-bg"
                                   :style #js {:backgroundImage (str "url(" background ")")}})
                     (dom/a #js {:className "ProfileCard-Avatar"}
                            (dom/img #js {:className "ProfileCard-AvatarImage"
                                          :src avatar}))
                     (dom/div #js {:className "ProfileCard-User"}
                              
                              (dom/div #js {:className "ProfileCard-Name"} name)
                              (dom/div #js {:className "ProfileCard-Username"} (str "@" username)))
                     (statistics friendsCount role)
                     (dom/div nil birthdate)))))

(def profile-card (om/factory ProfileCard))













(defui Settings
  static om/IQuery
  (query [this]
         [:user/name :user/username])
  Object
  (render [this]
          (let [{:keys [:user/name :user/username]} (om/props this)]
            (dom/div #js {:className "SettingsContainer"}
                     (dom/h2 #js {:className "SettingsTitle"} "Settings")
                     (dom/div #js {:className "SettingsForm"}
                              (dom/label #js {:className "SettingsForm-Label"} "Name: ")
                              (dom/input #js {:placeholder "Name"
                                              :type "text"
                                              :className "SettingsForm-Input"
                                              :value name
                                              :onChange #(form/handleChange
                                                          this
                                                          :settings :user/name %)}))
                     (dom/div #js {:className "SettingsForm"}
                              (dom/label #js {:className "SettingsForm-Label"} "Username: ")
                              (dom/input #js {:placeholder "Username"
                                              :className "SettingsForm-Input"
                                              :type "text"
                                              :value username
                                              :onChange #(form/handleChange
                                                          this
                                                          :settings :user/username %)}))
                     (dom/button #js {:className "SettingsForm-Save"
                                      :onClick
                                      #(om/transact!
                                        this '[(settings/save) :viewer])}
                                 "Opslaan")))))

(def settings (om/factory Settings))

(defui Login
  static om/IQuery
  (query [this]
         [:username :password])
  Object

  (render [this]
          (let [{:keys [username password]} (om/props this)]
            (dom/div nil
                     (dom/input #js {:placeholder "Username"
                                     :type "text"
                                     :value username
                                     :onChange #(form/handleChange this :login :username %)})
                     (dom/input #js {:placeholder "Password"
                                     :type "password"
                                     :value password
                                     :onChange #(form/handleChange this :login :password %)})
                     (dom/button #js {:onClick #(om/transact! this `[(auth/login {:username ~username}) :viewer]) } "Login!")))))

(def login (om/factory Login))

(defn has-friend? 
  "Return true if user-id is in friends"
  [id friends]
  (some #(= [:users/by-id id] %) friends))

(defn friend-button
  [c user-id friends]
  (if (has-friend? user-id friends)
    (dom/button #js {:className "FriendCard-Button"
                     :onClick #(om/transact! c `[(friend/delete {:user/id ~user-id}) :viewer :friends])} "De-friend")
    (dom/button #js {:className "FriendCard-Button"
                     :onClick #(om/transact! c `[(friend/add {:user/id ~user-id}) :viewer :friends])} "Add friend")))

(defui FriendCard
  static om/Ident
  (ident [_ {:keys [user/id]}]
         [:users/by-id id])
  static om/IQuery
  (query [_]
         '[:user/id
          :user/username
          :user/role
          :user/name
          :user/friendsCount
          :user/friends
          :user/avatar
          :user/background])
  Object
  (render [this]
          (let [{:keys [user/id
                        user/username user/role
                        user/name user/friendsCount
                        user/avatar user/background
                        friends]} (om/props this)]
            (dom/div #js {:className "FriendCard"}
                     (dom/img #js {:className "ProfileCard-AvatarImage"
                                   :src avatar})
                     (dom/div #js {:className "FriendCard-Desc"}
                              (dom/div #js {:className "FriendCard-Username"} (str "@" username))
                              (dom/div #js {:className "FriendCard-Count"} (str "Friends: " friendsCount)))
                     (friend-button this id friends)))))

(def friend (om/factory FriendCard {:keyfn :user/id}))


(defn friend-list [friends]
  (dom/div #js {:className "FriendList"}
           (dom/h3 #js {:className "FriendList-Title"} "Friends List:")
           (map friend friends)))

(defn suggestedFriends-list [friends]
  (dom/div #js {:className "FriendList"}
           (dom/h3 #js {:className "FriendList-Title"} "Suggested Friends:")
           (map friend friends)))

(defui PostAuthor
  static om/Ident
  (ident [_ {:keys [:user/id]}]
         [:users/by-id id])
  static om/IQuery
  (query [_]
         [:user/id :user/username])
  Object
  (render [this]
          (let [{:keys [:user/username]} (om/props this)]
            (dom/div #js {:className "Story-PostAuthor"} (str "@" username " says:")))))

(def post-author (om/factory PostAuthor))

(defui Post
  static om/IQuery
  (query [this]
         [:db/id :type :post/title :post/content {:post/author (om/get-query PostAuthor)}])
  Object
  (render [this]
          (let [{:keys [post/title post/author post/content]} (om/props this)]
            (dom/div nil
                     (post-author author)
                     (dom/h3 nil (if (empty? title) "" (str "#" title)))
                     (dom/p nil content)))))

(def post (om/factory Post))

(defui PhotoAuthor
  static om/Ident
  (ident [_ {:keys [:user/id]}]
         [:users/by-id id])
  static om/IQuery
  (query [_]
         [:user/id :user/username])
  Object
  (render [this]
          (let [{:keys [user/username]} (om/props this)]
            (dom/span #js {:className "Story-PhotoAuthor"} (str "@" username " ")))))

(def render-author (om/factory PhotoAuthor))

(defui Photo
  static om/IQuery
  (query [this]
    [:db/id :type :photo/image :photo/caption {:photo/author (om/get-query PhotoAuthor)}])
  Object
  (render [this]
    (let [{:keys [photo/author photo/image photo/caption photo/author]} (om/props this)]
      (dom/div #js {:className "StoryContent"}
               (dom/img #js {:src image
                             :className "Story-Photo"})
               (dom/div #js {:className "Story-PhotoCaptionContainer"}
                        (render-author author)
                        (dom/span #js {:className "Story-PhotoCaption"} caption))))))

(def photo (om/factory Photo))

(defui Graphic
  static om/IQuery
  (query [this]
    [:db/id :type :graphic/title :graphic/image])
  Object
  (render [this]
    (let [{:keys [graphic/title graphic/image]} (om/props this)]
      (dom/div nil
        (dom/h3 nil (str "Graphic: " title))
        (dom/div nil image)))))

(def graphic (om/factory Graphic))

(defui DashboardItem
  static om/Ident
  (ident [this {:keys [db/id type]}]
    [type id])
  static om/IQuery
  (query [this]
    (zipmap
      [:dashboard/post :dashboard/photo :dashboard/graphic]
      (map #(conj % :favorites)
        [(om/get-query Post)
         (om/get-query Photo)
         (om/get-query Graphic)])))
  Object
  (render [this]
          (let [{:keys [db/id type favorites]} (om/props this)]
      (dom/div
       #js {:className "StoryCard"}
        (dom/div nil
          (({:dashboard/post    post
             :dashboard/photo   photo
             :dashboard/graphic graphic} type)
            (om/props this)))
        (dom/div #js {:className "Story-Favorite"}
                 (dom/div #js {:className "FavoriteButton"
                               :onClick #(om/transact! this
                                                       `[(story/favorite
                                                          {:ref [~type ~id]})])} "Favorite")
                 (dom/div nil (str "👍 " favorites)))))))

(def dashboard-item (om/factory DashboardItem))

(defn dashboard-list [items]
  (dom/ul #js {:className "Column"}
          (map dashboard-item (reverse items))))

(defn flex [& children]
  (apply (partial dom/div #js {:className "flex"}) children))

(defui StoryTextBox
  Object
  (initLocalState [_]
                  {:story ""})
  (render [this]
          (let [{:keys [story]} (om/get-state this)
                {:keys [user/avatar user/id ]} (om/props this)
                {:keys [submit]} (om/get-computed this)]
            (dom/div #js {:className "StoryTextBox-Container"}
                     (flex 
                      (dom/img #js {:className "StoryTextBox"
                                    :src avatar})
                      (dom/input #js {:value story
                                      :className "StoryTextBox-Input"
                                      :onChange #(handleChange this :story %)
                                      :placeholder "Qué está pasando?"}))
                     (dom/div #js {:className "StoryTextBox-ButtonContainer"}
                              (dom/button #js {:className  "StoryTextBox-Button"
                                               :onClick #(do
                                                           (submit story id)
                                                           (om/set-state! this {:story ""}))}
                                  "Post!"))))))

(def new-story (om/factory StoryTextBox))

(defui StoryFeed
  static om/IQuery
  (query [this]
         `[{:viewer ~(om/get-query ProfileCard)} 
           {:dashboard/items ~(om/get-query DashboardItem)}])
  Object
  (submitPost [this story id]
              (om/transact! this `[(story/submit {:story/text ~story :user/id ~id})]))
  (render [this]
          (let [{:keys [viewer dashboard/items path]} (om/props this)]
            (dom/div nil
                     (navbar this path viewer)
                     (dom/div #js {:style #js {:position "relative"
                                               :padding "56px 14px 15px"}}
                              (dom/div #js {:className "Dashboard"}
                                       (profile-card viewer))
                              (new-story (om/computed viewer {:submit (fn [story user-id] (.submitPost this story user-id))}))
                              (dashboard-list items))))))


(defui FriendsRoot
  static om/IQuery
  (query [this]
         `[{:viewer ~(om/get-query ProfileCard)} 
           {:friends ~(om/get-query FriendCard)}
           {:suggested ~(om/get-query FriendCard)}])
  Object
  (render [this]
          (let [{:keys [viewer form friends suggested path]} (om/props this)]
            (dom/div nil
                     (navbar this path viewer)
                     (dom/div #js {:style #js {:position "relative"
                                               :padding "56px 14px 15px"}}
                              (dom/div #js {:className "Dashboard"}
                                       (profile-card viewer))
                              (friend-list friends)
                              (suggestedFriends-list suggested))))))

(defui SettingsRoot
  static om/IQuery
  (query [this]
         `[{:viewer ~(om/get-query ProfileCard)}
           ({:form ~(om/get-query Settings)} {:form/id :settings
                                              :form/init [{:viewer [:user/name :user/username]}]})])
  Object
  (render [this]
          (let [{:keys [main/title viewer path form] :as props} (om/props this)
                _ (println "form-props" form)]
            (dom/div nil
                     (navbar this path viewer)
                     (dom/div #js {:style #js {:position "relative"
                                                :padding "56px 14px 15px"}}
                               (dom/div #js {:className "Dashboard"}
                                        (profile-card viewer))
                               (settings form))))))



(defn submit [c]
  (let [{:keys [username]} (om/get-state c)]
    (sente/ajax-lite "/login"
                     {:method :post
                      :params {:username username}}
                     (fn [ajax-resp]
                       (let [_ (println "received ajax-resp" ajax-resp)]
                         (do
                           (sente/chsk-reconnect! ws/chsk)
                           (goto c "/story")))))))

(def ENTER_KEY 13)

(defn key-down [c e]
  (condp == (.-keyCode e)
    ENTER_KEY
    (do
      (submit c)
      (doto e (.preventDefault) (.stopPropagation)))
      nil))

(defui LoginRoot
  static om/IQuery
  (query [this]
         [:path])
  Object
  (initLocalState [this]
                  {:username ""})
  (render [this]
          (let [{:keys [username password]} (om/get-state this)]
            (dom/div #js {}
                     (dom/h1 #js {:style #js {:textAlign "center"}} "Hypothetical Social Media App™")
                     (logo)
                     (dom/div #js {:style #js {:display "flex"
                                               :justifyContent "center"
                                               :marginTop "40px"}}
                              (dom/input #js {:type "text"
                                              :value username
                                              :style #js {:padding "8px"
                                                          :borderRadius "8px"
                                                          :border "1px solid #eee"
                                                          :width "300px"}
                                              :placeholder "Username"
                                              :onChange #(handleChange this :username %)
                                              :onKeyDown #(key-down this %)}))))))


(def url->Component
  {"/" LoginRoot
   "/story" StoryFeed
   "/friends" FriendsRoot
   "/settings" SettingsRoot
   "/login" LoginRoot})

(defui ^:once Root
  static om/IQueryParams
  (params [this]
          {:route/data []})
  static om/IQuery
  (query [this]
         '[:path {:route ?route/data} ])
  Object
  (initLocalState [this]
                  {:history (h/make-history this)})
  (componentDidMount [this]
                      (let [c (get url->Component (:path (om/props this)))]
                        (om/set-query! this {:params
                                             {:route/data (om/get-query c)}})))
  (componentDidUpdate [this prev-props prev-state]
                      (let [{:keys [history]} (om/get-state this)
                            {:keys [path]} (om/props this)]
                        (when-not (= path (.getToken history))
                          (h/push! history path))))
  (componentWillUpdate [this next-props next-state]
                       (let [current (:path (om/props this))
                                   next (:path next-props)]
                               (when (not= current next)
                                 (om/set-query! this {:params
                                                      {:route/data
                                                       (om/get-query (get url->Component (:path next-props)))}}))))
  (componentWillReceiveProps [this next-props]
                             (let [current (:path (om/props this))
                                   next (:path next-props)]
                               (when (not= current next)
                                 (om/set-query! this {:params
                                                      {:route/data
                                                       (om/get-query (get url->Component (:path next-props)))}}))))
  (render [this]
          (let [{:keys [path route] :as props} (om/props this)
                c (get url->Component path)]
            (dom/div nil
                     ((om/factory c) (assoc route :path path))
                     (dom/button #js {:className "TimeTravel"
                                      :onClick #(.undo js/window)} "⇠")))))


(defonce reconciler
  (om/reconciler {:state app-state
                  :parser (om/parser {:read p/read :mutate p/mutate})
                  :merge-tree u/deep-merge
                  :normalize true
                  :send (u/transit-post "/api")}))



(om/add-root! reconciler Root (gdom/getElement "app"))


(tt/setup-timetravel app-state)


(go-loop []
  (let [{:keys [event id ?data send-fn]} (<! ws/ch-chsk)]
    (when (= id :chsk/recv)
      (om/merge! reconciler {:route {:dashboard/items (second ?data)}}))
    (recur)))
