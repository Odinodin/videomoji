(ns videomoji.ui
  (:require [videomoji.views.main :as main]))


(defn render-frontpage [state]
  [:main
   "FRONT"
   [:ui/a {:ui/location {:location/page-id :pages/about}} "About"]
   ]
  )

(defn render-about [state]
  [:main
   "About page"
   [:ui/a {:ui/location {:location/page-id :pages/tech}} "What"]])

(defn render-what [state]
  [:main
   "WAT"
   [:ui/a {:ui/location {:location/page-id :pages/about}} "About"]])

(defn render-not-found [state]
  [:main
   "NA"]
  )


(defn render-page [state]
  (let [f (case (:location/page-id (:location state))
                :pages/frontpage main/view
                :pages/tech render-what
                :pages/about render-about
                render-not-found)]
    (f state)))