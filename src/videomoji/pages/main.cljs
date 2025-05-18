(ns videomoji.pages.main
  (:require [shadow.css :refer (css)]
            [videomoji.components :as c]
            [videomoji.video :as video]))

;; TODO refactor
(defn perform-action [state [action]]
  (when (= ::update-video action)
    (video/update-state state)
    []))

(defn emojivideo [state]
  [:div {:replicant/key "emojivideo"
         :replicant/on-mount (fn [_replicant-event] (video/initialize state))
         ;; Unmount does not work, so need to work around stopping video on page transition
         #_#_:replicant/on-unmount (fn [e] (prn "UNMOUNT" e))}
   [:div {:id "content"}]])

(defn render [state]
  (let [video-paused? (-> state ::view :video-paused?)]
    (c/layout-page-with-header
      [:div {:class (css :flex :flex-col-reverse {:gap "20px"} [:lg :flex-row])}

       ;; Video
       [:div {:class (css {:flex "5 1 0"} :relative)}
        ;; Necessary elements for reading the webcam and for rendering video bitmaps off screen
        ;; Note they are not visible to the user
        [:div {:class (css :absolute :top-4 :left-0 :z-40 {:display "none"})}
         [:canvas {:id "offscreen-canvas"}]
         [:video {:playsinline true :muted true :style {:position "sticky" :top "10px" :display "none"}}]]

        [:div {:class (css :h-full :relative)}
         (when (-> state ::view :video-initialized?)
           (emojivideo state))
         (when (not (-> state ::view :video-initialized?))
           [:div {:class (css :h-full :relative :text-center [:lg :text-right])}
            [:div {:class (css :absolute :overflow-hidden :top-0 :left-0 :right-0 :h-full)}
             (c/emoji-wall 2000 "✋")]
            [:div {:class (css :relative :flex :justify-center :items-center :h-full)}
             (c/button {:label "Start the video" :on-click [[:action/assoc-in [::view :video-initialized?] true]
                                                            [:action/assoc-in [::view :video-paused?] false]
                                                            ]})]])]]

       ;; Menu
       [:div {:class (css :py-2 {:flex "1 1 0"} [:lg :py-10])}
        [:div {:class (css :flex :flex-col :items-center {:gap "4px"} [:lg {:gap "20px"}])}
         [:div {:class (css {:width "100px"})}
          (c/button {:label (if video-paused? "▶️ Play" "⏸️ Pause")
                     :on-click [[:action/assoc-in [::view :video-paused?] (not video-paused?)]
                                [::update-video]]})]

         (c/radio {:label "Size:"
                   :on-select [[:action/assoc-in [::view :size] :selected-value]
                               [::update-video]]
                   :radio-name "size"
                   :values [{:id "small" :label "S" :checked (-> state ::view :size (= "small"))}
                            {:id "medium" :label "M" :checked (-> state ::view :size (= "medium"))}
                            {:id "large" :label "L" :checked (-> state ::view :size (= "large"))}]})

         (c/radio {:label "Style:"
                   :on-select [[:action/assoc-in [::view :emoji-kind] :selected-value]
                               [::update-video]]
                   :radio-name "emoji"
                   :values [{:id :monochrome :label "☠️" :checked (-> state ::view :emoji-kind (= :monochrome))}
                            {:id :emoji-squares :label "🟩" :checked (-> state ::view :emoji-kind (= :emoji-squares))}
                            {:id :emoji-colored :label "🌸" :checked (-> state ::view :emoji-kind (= :emoji-colored))}
                            {:id :emoji-colored-grayed :label "🥖" :checked (-> state ::view :emoji-kind (= :emoji-colored-grayed))}]})]]])))