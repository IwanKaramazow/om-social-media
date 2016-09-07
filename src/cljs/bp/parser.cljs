(ns bp.parser
  (:require [om.next :as om]))

(defmulti read om/dispatch)

(defmethod read :path
  [{:keys [state]} key _]
  {:value (:path @state)})


(defmethod read :story
  [{:keys [parser query ast target] :as env} key _]
  (let [remote (parser env query target)]
    (if (and target (not-empty remote))
      {:remote (update-in ast [:query] (fn [query] remote))}
      {:value (parser env query)})))


(defmethod read :route
  [{:keys [parser query ast target] :as env} key _]
  (let [remote (parser env query target)]
    (if (and target (not-empty remote))
      {:remote (update-in ast [:query] (fn [query] remote))}
      {:value (parser env query)})))
























(defmethod read :viewer
  [{:keys [state ast query target]} key _]
  (let [st @state]
    (if (contains? (get-in st [:route]) key)
      {:value (om/db->tree query (get-in st [:route key]) st)}
      {:remote (assoc ast :query-root true)})))


















(defmethod read :new
  [{:keys [state ast query target]} key _]
  (let [st @state]
    (if (contains? (get-in st [:route]) key)
      {:value (om/db->tree query (get-in st [:route key]) st)}
      {:remote (assoc ast :query-root true)}
      )))

(defmethod read :setting
  [{:keys [state ast query target]} key _]
  (let [st @state]
    (if (contains? (get-in st [:route]) key)
      {:value (om/db->tree query (get-in st [:route key]) st)}
      {:remote (assoc ast :query-root true)})))

(defn prepare-form [form query]
  (reduce #(if (contains? form %2)
             (assoc % %2 (%2 form))
             (assoc % %2 "")) {} query))

(defmethod read :form
  [{:keys [parser state query] :as env} key {:keys [form/id form/init] :as params}]
  (let [f (get-in @state [:form id])]
    (if (nil? f)
      {:value (prepare-form (:viewer (parser env init)) query)}
      {:value (prepare-form (merge (:viewer (parser env init)) f) query)})))

(defmethod read :friends
  [{:keys [state ast query]} k _]
  (let [st @state]
    (if (contains? (get st :route) k)
      {:value (map #(assoc % :friends (get-in st [:route :friends])) (om/db->tree query (get-in st [:route k]) st) )}
      {:remote (assoc ast :query-root true)})))

(defmethod read :suggested
  [{:keys [state ast query]} k _]
  (let [st @state]
    (if (contains? (get st :route) k)
      {:value (om/db->tree query (get-in st [:route k]) st)}
      {:remote (assoc ast :query-root true)})))

(defmethod read :dashboard/items
  [{:keys [state ast query]} k _]
  (let [st @state]
    (if (contains? (:route st) k)
      {:value (om/db->tree query (get-in st [:route k]) st)}
      {:remote (assoc ast :query-root true)})))


(defmulti mutate om/dispatch)

(defmethod mutate 'form/update
  [{:keys [state parser] :as env} _ {:keys [form/id form/field form/value]}]
  {:action #(swap! state assoc-in [:form id field] value)})

(defn pull-up [m]
  "Walk one level deeper in a map.
   E.g. {:a {:b :c}} -> {:b :c}"
  (let [k (-> m keys first)]
    (get m k)))

(defmethod mutate 'form/init
  [{:keys [state parser] :as env} _ {:keys [form/id query]}]
  {:action #(let [form (pull-up (parser env query))]
              (swap! state assoc-in [:form id] form))})

(defmethod mutate 'settings/save
  [{:keys [state]} _ _]
  {:action #(let [st @state
                  f (get-in st [:form :settings])
                  user (get-in st [:route :viewer])]
               (swap! state update-in [:route :viewer] merge f)
              ;;(swap! state update-in user merge f)
              )})


(defmethod mutate 'friend/delete
  [{:keys [state]} _ {:keys [user/id]}]
  {:action
   #(swap! state (fn [st]
                   (let [user (get-in st [:users/by-id id])
                         friends (get-in st [:route :friends])
                         updated-friends (into [] (remove #{[:users/by-id id]} friends))
                         suggested (get-in st [:route :suggested])

                         updated-suggested (conj suggested [:users/by-id id])
                         updated-user (update user :user/friendsCount dec)]
                     (-> st
                         (update-in (conj (get-in st [:route :viewer]) :user/friendsCount) dec)
                         (assoc-in [:route :friends] updated-friends)
                         (assoc-in [:route :suggested] updated-suggested)
                         (assoc-in [:users/by-id id] updated-user)))))})

(defmethod mutate 'friend/add
  [{:keys [state]} _ {:keys [user/id]}]
  {:action
   #(swap! state (fn [st]
                   (let [user (get-in st [:users/by-id id])
                         friends (get-in st [:route :friends])
                         updated-friends (conj friends [:users/by-id id])
                         suggested (get-in st [:route :suggested])
                         updated-suggested  (into [] (remove #{[:users/by-id id]} suggested))
                         updated-user (update user :user/friendsCount inc)]
                     (-> st
                         (update-in (conj (get-in st [:route :viewer]) :user/friendsCount) inc)
                         (assoc-in [:route :friends] updated-friends)
                         (assoc-in [:route :suggested] updated-suggested)
                         (assoc-in [:users/by-id id] updated-user)))))})


(defmethod mutate 'story/favorite
  [{:keys [state]} k {:keys [ref]}]
  {:action #(swap! state update-in (conj ref :favorites) inc)})

(defmethod mutate 'path/change
  [{:keys [state] } k {:keys [path]}]
  {:action #(swap! state assoc :path path)})



;; (defn create-post [{:keys [story/text user/id]}]
;;   (let [tid (om/tempid)
;;         story {:db/id tid
;;                :favorites 0
;;                :post/title ""
;;                :post/author [:users/by-id id]
;;                :post/content text}]))

(defmethod mutate 'story/submit
  [{:keys [ast state] } _ {:keys [story/text user/id]}]
  {:action #(swap! state (fn [st]
                           (let [tid (om/tempid)
                                 story {:db/id tid
                                        :favorites 0
                                        :type :dashboard/post
                                        :post/title ""
                                        :post/author [:users/by-id id]
                                        :post/content text}]
                             (-> st
                                 (assoc-in [:dashboard/post tid] story)
                                 (update-in [:route :dashboard/items] conj [:dashboard/post tid])))))
   :remote (merge ast {:params {:story/text text :user/id id}}) })
