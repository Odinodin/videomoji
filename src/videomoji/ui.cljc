(ns videomoji.ui
  (:require [videomoji.pages.about :as about]
            [videomoji.pages.main :as main]
            [videomoji.pages.tech :as tech]))

(defn render-not-found [state]
  [:main
   "NA"])

(defn render-page [state]
  (let [f (case (:location/page-id (:location state))
                :pages/about about/render
                :pages/frontpage main/render
                :pages/tech tech/render
                main/render)]
    (f state)))