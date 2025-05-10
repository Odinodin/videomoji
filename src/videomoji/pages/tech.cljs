(ns videomoji.pages.tech
  (:require [shadow.css :refer (css)]
            [videomoji.components :as c]))

(defn render [state]
  (c/layout-page-with-header
    [:div {:class (css :px-4)}
     [:div {:class (css :flex {:gap "10px"} :mt-4)}
      [:h2 {:class (css :text-2xl)} "Tech that make emojis move"]]]))