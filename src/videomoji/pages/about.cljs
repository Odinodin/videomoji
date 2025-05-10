(ns videomoji.pages.about
  (:require [shadow.css :refer (css)]
            [videomoji.components :as c]))

(defn render [state]
  (c/layout-page-with-header
    [:div {:class (css :px-4)}

     [:div {:class (css :flex {:gap "10px"} :mt-4)}
      [:h2 {:class (css :text-2xl)} "Everyone loves emojis ❤️"]
      [:h2 {:class (css :text-2xl)} "Everyone loves selfies 🤪"]]

     [:h3 {:class (css :text-4xl :mt-6)} "🎥 Videomoji combines both of them! ❤️‍🔥"]

     [:p {:class (css :my-6 {:max-width "800px"})} "If you like Videomoji or have any comments or suggestions; I'd be really happy to hear about them."]

     [:p {:class (css :my-6 {:max-width "800px"})}
      "Some years ago I wrote a blog post about how you could turn bitmaps into ASCII on the web. The basic principle is
      to figure out what ASCII character that most closely matches the brightness of each pixel."]
     [:p {:class (css :my-6 {:max-width "800px"})}
      "That got me thinking, what if you used emojis instead of ASCII-characters? Turns out, that works really well.
      It's not even that hard to implement in the browser."]
     [:p {:class (css :my-6 {:max-width "800px"})}
      "Some time later, I was tinkering with video streams, and had an epiphany.
      A video is just a stream of pictures (duh)! What if I just hooked up the picture-to-emoji-converter to the video stream,
      what would that look like?"]

     [:p {:class (css :my-6 {:max-width "800px"})}
      "The result was pretty entertaining, but it was hard to make out the subject of the video
      in busy backgrounds."]

     [:p {:class (css :my-6 {:max-width "800px"})}
      "Detecting a subject in a video stream, that has to be hard, right? Turns out, it's dead simple. People have
      already solved this problem for us."]
     [:div
      " If you are interested in the techincal details, check out the " [:ui/a {:ui/location {:location/page-id :pages/tech}} "tech page"]]]))