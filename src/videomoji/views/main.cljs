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
  (let [video-paused? (-> state ::view :video-paused?)]
    [:div {:class (css {:max-width "2000px"} :m-auto :p-4 {:font-family "Pally-Variable" :font-weight "500"})}
     [:div {:class (css :flex :flex-row {:gap "20px"}) }

      ;; Video
      [:div {:class (css {:flex "5 1 0"} :relative)}
       [:div {:class (css :absolute :top-4 :left-0  :z-40 {:display "none"})}
        [:canvas]
        [:video {:playsinline true :muted true :style {:position "sticky" :top "10px" :display "none"}}]]

       [:div {:id "content" :class (css :h-full :relative :text-right)}
        (when (not (-> state ::view :video-initialized?))
          [:div {:class (css :h-full :relative)}
           [:div {:class (css :absolute :overflow-hidden :top-0 :left-0 :right-0 :h-full )}
            (c/emoji-wall 2000 "✋")]
           [:div {:class (css :relative :flex :justify-center :items-center :h-full)}
            (c/button {:label "Start the video" :on-click [[:action/assoc-in [::view :video-initialized?] true]]})]])]]

      ;; Menu
      [:div {:class (css :py-10 {:flex "1 1 0"})}
       [:div {:class (css :flex :flex-col {:gap "20px"})}
        (c/logo "Videomoji")

        [:div {:class (css {:width "100px"})}
         (c/button {:label (if video-paused? "▶️ Play" "⏸️ Pause")
                    :on-click [[:action/assoc-in [::view :video-paused?] (not video-paused?)]]})]

        (c/radio {:label "Size:"
                  :on-select [[:action/assoc-in [::view :size] :selected-value]]
                  :radio-name "size"
                  :values [{:id "small" :label "S" :checked (-> state ::view :size (= "small"))}
                           {:id "medium" :label "M" :checked (-> state ::view :size (= "medium"))}
                           {:id "large" :label "L" :checked (-> state ::view :size (= "large"))}]})

        (c/radio {:label "Style:"
                  :on-select [[:action/assoc-in [::view :emoji-kind] :selected-value]]
                  :radio-name "emoji"
                  :values [{:id :monochrome :label "☠️" :checked (-> state ::view :emoji-kind (= :monochrome))}
                           {:id :emoji-squares :label "🟩" :checked (-> state ::view :emoji-kind (= :emoji-squares))}
                           {:id :emoji-colored :label "🌸" :checked (-> state ::view :emoji-kind (= :emoji-colored))}
                           {:id :emoji-colored-grayed :label "🥖" :checked (-> state ::view :emoji-kind (= :emoji-colored-grayed))}]})]]]]))