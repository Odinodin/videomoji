(ns videomoji.components
  (:require [shadow.css :refer (css)]
            [videomoji.utils :as u]))

(defn button [{:keys [label on-click]}]
  [:button {:class (css {:background "white"} :text-left :border-2 :border-gray-400 :rounded :p-2 [:hover :border-gray-700 :bg-gray-200])
            :on {:click on-click}}
   label])

(defn logo [label]
  [:div
   [:h1 {:class (css :text-5xl)} label]])

(defn radio [{:keys [label radio-name on-select values]}]
  [:div
   [:div label]
   [:ul {:class (css :flex :gap-6)}
    (for [{:keys [id label checked]} values]
      [:li {:on {:click (u/interpolate on-select {:selected-value id})}
            :class (css
                     ["&:hover" :border-gray-500]
                     ["&:has(input[type='radio']:checked)" :border-gray-700 :bg-gray-200]
                     {:width "40px" :height "40px"}
                     :items-center
                     :justify-center
                     :rounded :border-2 :flex :p-2 :cursor-pointer :border-gray-400)}
       [:input {:type "radio" :id id :value id :name radio-name :checked checked :class (css {:opacity 0 :position "absolute" :pointer-events "none"})}]
       [:label {:for id :class (css {:pointer-events "none"})} label]])]])

(defn emoji-wall [amount character]
  [:div (take amount (repeat character))])

(defn layout-page-with-header [content]
  [:div {:class (css {:max-width "2000px"} :m-auto :px-4 {:font-family "Pally-Variable" :font-weight "500"})}
   [:header {:class (css :py-4 :pl-4 :flex :gap-10 :items-center)}
    [:ui/a {:ui/location {:location/page-id :pages/frontpage} :class (css {:margin-right "20px"})}
     (logo "🎥 Videomoji")]
    [:ui/a {:ui/location {:location/page-id :pages/about}} "About"]
    [:ui/a {:ui/location {:location/page-id :pages/tech}} "Tech"]]
   content])