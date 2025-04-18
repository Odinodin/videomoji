(ns videomoji.components
  (:require [shadow.css :refer (css)]
            [videomoji.utils :as u]))

(defn button [{:keys [label on-click]}]
  [:button {:class (css :bg-blue-500 :rounded :p-2 [:hover :bg-blue-300])
            :on {:click on-click}}
   label])

(defn radio [{:keys [label radio-name on-select values]}]
  [:div
   [:div label]
   [:ul {:class (css :flex :gap-2)}
    (for [{:keys [id label checked]} values]
      [:li {:on {:click (u/interpolate on-select {:selected-value id})}
            :class (css
                     ["&:hover" :border-gray-500]
                     ["&:has(input[type='radio']:checked)" :border-gray-700]
                     {:width "40px" :height "40px"}
                     :items-center
                     :justify-center
                     :rounded :border-2 :flex :p-2 :cursor-pointer :border-gray-400)}
       [:input {:type "radio" :id id :value id :name radio-name :checked checked :class (css {:opacity 0 :position "absolute" :pointer-events "none"})}]
       [:label {:for id :class (css {:pointer-events "none"})} label]])]])