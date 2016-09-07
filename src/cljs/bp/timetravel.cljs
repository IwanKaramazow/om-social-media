(ns bp.timetravel)

(defonce app-history (atom nil))

(defn setup-timetravel [app-state]
  (reset! app-history [@app-state])
  (add-watch app-state :history
           (fn [_ _ _ new]
             (when-not (= (last @app-history) new)
               (swap! app-history conj new))))
  (aset js/window "undo"
        (fn []
          (when (> (count @app-history) 1)
            (swap! app-history pop)
            (reset! app-state (last @app-history))))))



