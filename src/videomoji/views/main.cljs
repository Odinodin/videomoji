(ns videomoji.views.main
  (:require [shadow.css :refer (css)]
            [videomoji.components :as c]
            [videomoji.video :as video]))

;; TODO
(defn perform-action [state [action]]
  (when (= ::start-video action)
    #_(video/render-video {:size (-> state ::view :size)})
    []))

(defn view [state]
  (let [started (-> state ::view :video-started)
        video-paused? (-> state ::view :video-paused?)]

    [:div {:class (css :max-w-6xl :m-auto :p-4)}
     [:div {:class (css :flex :flex-col)}
      [:header]
      [:main {:class (css :flex-1)}
       [:div
        [:video {:playsinline true :muted true :style {:position "sticky" :top "10px" :display "none"}}]
        [:div {:class (css :flex :flex-row)}
         [:div {:class (css {:flex "1 1 auto"})}
          [:div {:class (css :flex :flex-col {:gap "20px"})}

           ;; Logo
           [:div {:class (css :p-8 :flex :flex-col {:gap "10px"})}
            [:h1 {:class (css :text-5xl :flex :justify-center)} "\uD83C\uDFA5️Videomoji 🎥"]
            [:h3 {:class (css :text-2xl :flex :justify-center)} "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25"]]

           [:div {}
            (c/button {:label (if video-paused? "Play" "Pause") :on-click [[:action/assoc-in [::view :video-paused?] (not video-paused?)]]})]

           (c/radio {:label "Size:"
                     :on-select [[:action/assoc-in [::view :size] :selected-value]]
                     :radio-name "size"
                     :values [{:id "small" :label "S" :checked (-> state ::view :size (= "small"))}
                              {:id "medium" :label "M" :checked (-> state ::view :size (= "medium"))}
                              {:id "large" :label "L" :checked (-> state ::view :size (= "large"))}]})]]
         [:div {:id "content"
                :class (css {:flex "4 1 0"} :text-center :border-2 :rounded-3xl :p-4)}
          (when (not started)
            [:div {:class (css :flex :justify-center :items-center :h-full)}
             (c/button {:label "Start the video" :on-click [[:action/assoc-in [::view :video-started] true]]})])]]]]]]))