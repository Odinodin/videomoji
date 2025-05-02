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

    [:div {:class (css {:max-width "1200px"} :m-auto :p-4 {:font-family "Pally-Variable" :font-weight "500"})}
     [:div {:class (css :flex :flex-row :gap-6)}

      ;; Video
      [:div {:class (css {:flex "5 1 auto"} :p-4 :relative)}
       [:div {:class (css :absolute :top-4 {:display "none"})}
        [:canvas]
        [:video {:playsinline true :muted true :style {:position "sticky" :top "10px" :display "none"}}]]

       [:div {:id "content"}
        (when (not started)
          [:div {:class (css :flex :justify-center :items-center :h-full)}
           (c/button {:label "Start the video" :on-click [[:action/assoc-in [::view :video-started] true]]})])]]

      ;; Menu
      [:div {:class (css :pt-10 {:flex "1 1 200px"})}
       [:div {:class (css :flex :flex-col {:gap "20px"})}

        ;; Logo
        [:div {:class (css :flex :flex-col :gap-4 :mb-8)}
         [:h1 {:class (css :text-5xl)} "Videomoji"]
         [:h3 {:class (css :text-2xl)} "\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25\uD83D\uDD25"]]

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
                           {:id :emoji-colored-grayed :label "🥖" :checked (-> state ::view :emoji-kind (= :emoji-colored-grayed))}]})]]
      ]]))