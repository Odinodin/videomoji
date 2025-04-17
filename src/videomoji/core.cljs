(ns videomoji.core
  (:require [replicant.dom :as r]
            [videomoji.video :as video]
            [videomoji.views.main :as main]))

(def views
  [{:id :main}])

(defn get-current-view [state]
  (:current-view state :main))

(defn render-ui [state]
  (let [current-view (get-current-view state)]
    [:div
     (case current-view
           :main
           (main/view state))]))

(defn process-effect [store [effect & args]]
  (case effect
        :effect/assoc-in
        (apply swap! store assoc-in args)))

(defn perform-actions [state event-data]
  (mapcat
    (fn [action]
      (prn "Action: " (first action) (rest action))
      (or (main/perform-action state action)
          (case (first action)
                :action/assoc-in
                [(into [:effect/assoc-in] (rest action))]

                (prn "Unknown action"))))
    event-data))

(defn init [store]
  (add-watch store ::render (fn [_ _ _ new-state]
                              ;; TODO consider using an effect to update video-state
                              (video/start-video new-state)
                              (r/render
                                js/document.body
                                (render-ui new-state))))

  (r/set-dispatch!
    (fn [_ event-data]
      (->> (perform-actions @store event-data)
           (run! #(process-effect store %)))))

  (swap! store assoc ::loaded-at (.getTime (js/Date.))))
