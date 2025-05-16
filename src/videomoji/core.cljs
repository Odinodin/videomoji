(ns videomoji.core
  {:dev/always true
   :shadow.css/include ["videomoji/main.css"]}
  (:require [clojure.walk :as walk]
            [replicant.dom :as r]
            [replicant.alias :as alias]
            [videomoji.pages.main :as main]
            [videomoji.router :as router]
            [videomoji.ui :as ui]
            [videomoji.video :as video]))

(defn routing-anchor [attrs children]
  (let [routes (-> attrs :replicant/alias-data :routes)]
    (into [:a (cond-> attrs
                      (:ui/location attrs)
                      (assoc :href (router/location->url routes
                                     (:ui/location attrs))))]
      children)))

(alias/register! :ui/a routing-anchor)

(defn find-target-href [e]
  (some-> e .-target
          (.closest "a")
          (.getAttribute "href")))

(defn get-current-location []
  (->> js/location.href
       (router/url->location router/routes)))

(defn interpolate [event data]
  (walk/postwalk
    (fn [x]
      (case x
            :event.target/value-as-number
            (some-> event .-target .-valueAsNumber)

            :event.target/value-as-keyword
            (some-> event .-target .-value keyword)

            :event.target/value
            (some-> event .-target .-value)

            x))
    data))

(defn process-effect [store [effect & args]]
  (case effect
        :effect/assoc-in
        (apply swap! store assoc-in args)
        ::main/update-video
        (video/update-state @store)))

(defn perform-actions [state event-data]
  (mapcat
    (fn [action]
      (prn (first action) (rest action))
      (or #_(main/perform-action state action)
          (case (first action)
                :action/assoc-in
                [(into [:effect/assoc-in] (rest action))]
                ::main/update-video
                [action]
                (prn "Unknown action"))))
    event-data))

(defn route-click [e store routes]
  (let [href (find-target-href e)]
    (when-let [location (router/url->location routes href)]
      (.preventDefault e)
      (if (router/essentially-same? location (:location @store))
        (.replaceState js/history nil "" href)
        (.pushState js/history nil "" href))
      (swap! store assoc :location location))))

(defn main [store el]
  (add-watch
    store ::render
    (fn [_ _ _ state]
      (r/render el (ui/render-page state) {:alias-data {:routes router/routes}})))

  (r/set-dispatch!
    (fn [{:replicant/keys [dom-event]} event-data]
      (->> (interpolate dom-event event-data)
           (perform-actions @store)
           (run! #(process-effect store %)))))

  (js/document.body.addEventListener
    "click"
    #(route-click % store router/routes))

  (js/window.addEventListener
    "popstate"
    (fn [_] (swap! store assoc :location (get-current-location))))

  ;; Trigger the initial render

  (swap! store assoc
    :app/started-at (js/Date.)
    :location (get-current-location)))