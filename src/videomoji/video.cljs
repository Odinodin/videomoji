(ns videomoji.video)

(def local-state (atom {:emoji-kind :emoji-colored}))

(def dark-to-bright-emoji ["1F5A4", "1F977", "1F98D", "1F9BE", "1F993", "1F463", "1F47b", "1F480", "1F440", "1F9B4", "1F90D", "1F4AC", "1F5EF"])

(def emoji-color-grayed-palette
    [;; 🖤 Blacks & dark grays
     {:emoji "⬛" :rgb [0 0 0]}
     {:emoji "🖤" :rgb [30 30 30]}
     {:emoji "⚫" :rgb [50 50 50]}
     {:emoji "🎩" :rgb [60 60 60]}
     {:emoji "🎱" :rgb [80 80 80]}
     {:emoji "🕶" :rgb [100 100 100]}
     {:emoji "💼" :rgb [110 110 110]}

     ;; 🩶 Mid grays
     {:emoji "🩶" :rgb [128 128 128]}
     {:emoji "🧳" :rgb [140 140 140]}
     {:emoji "🧥" :rgb [150 150 150]}
     {:emoji "📎" :rgb [160 160 160]}

     ;; 🤍 Light grays & whites
     {:emoji "🗂" :rgb [180 180 180]}
     {:emoji "📄" :rgb [220 220 220]}
     {:emoji "🧻" :rgb [240 240 240]}
     {:emoji "⚪" :rgb [250 250 250]}
     {:emoji "⬜" :rgb [255 255 255]}

     ;; 🔴 Reds
     {:emoji "🟥" :rgb [255 0 0]}
     {:emoji "🍎" :rgb [230 30 30]}
     {:emoji "🍒" :rgb [222 49 99]}

     ;; 🟠 Oranges
     {:emoji "🟧" :rgb [255 165 0]}
     {:emoji "🍊" :rgb [255 140 0]}
     {:emoji "🥕" :rgb [255 110 40]}

     ;; 🟡 Yellows
     {:emoji "🟨" :rgb [255 255 0]}
     {:emoji "🍌" :rgb [255 240 100]}

     ;; 🟢 Greens
     {:emoji "🟩" :rgb [0 128 0]}
     {:emoji "🥝" :rgb [140 200 70]}
     {:emoji "🥦" :rgb [90 150 80]}

     ;; 🔵 Blues
     {:emoji "🟦" :rgb [0 0 255]}
     {:emoji "🫐" :rgb [70 100 200]}
     {:emoji "🧊" :rgb [160 230 255]}

     ;; 🟣 Purples
     {:emoji "🟪" :rgb [128 0 128]}
     {:emoji "🍇" :rgb [150 60 160]}
     {:emoji "🔮" :rgb [120 0 200]}

     ;; 🟤 Browns
     {:emoji "🟫" :rgb [139 69 19]}
     {:emoji "🍫" :rgb [123 63 0]}
     {:emoji "🥔" :rgb [205 133 63]}
     {:emoji "🪵" :rgb [165 94 54]}])

(def emoji-colored-palette
    [;; Reds
     {:emoji "🟥" :rgb [255 0 0]}
     {:emoji "🍎" :rgb [220 20 60]}
     {:emoji "🍒" :rgb [222 49 99]}

     ;; Oranges
     {:emoji "🟧" :rgb [255 165 0]}
     {:emoji "🍊" :rgb [255 140 0]}
     {:emoji "🧡" :rgb [255 130 80]}

     ;; Yellows
     {:emoji "🟨" :rgb [255 255 0]}
     {:emoji "🍌" :rgb [255 240 0]}
     {:emoji "🌕" :rgb [255 250 180]}

     ;; Greens
     {:emoji "🟩" :rgb [0 128 0]}
     {:emoji "🥝" :rgb [110 190 50]}
     {:emoji "🥦" :rgb [85 130 70]}

     ;; Blues
     {:emoji "🟦" :rgb [0 0 255]}
     {:emoji "🫐" :rgb [60 90 200]}
     {:emoji "🧊" :rgb [150 230 255]}

     ;; Purples
     {:emoji "🟪" :rgb [128 0 128]}
     {:emoji "🍇" :rgb [140 60 180]}
     {:emoji "🔮" :rgb [120 0 255]}

     ;; Browns
     {:emoji "🟫" :rgb [139 69 19]}
     {:emoji "🍫" :rgb [123 63 0]}
     {:emoji "🥔" :rgb [205 133 63]}

     ;; Grayscale
     {:emoji "⬛" :rgb [0 0 0]}
     {:emoji "⚫" :rgb [50 50 50]}
     {:emoji "⚪" :rgb [230 230 230]}
     {:emoji "⬜" :rgb [255 255 255]}

     ;; Pinks
     {:emoji "🌸" :rgb [255 182 193]}
     {:emoji "🎀" :rgb [255 105 180]}

     ;; Neutrals / Misc
     {:emoji "🌈" :rgb [150 150 150]}                       ;; rainbow, used for ambiguous colors
     ])

(def emoji-square-palette
  [{:emoji "🟥" :rgb [255 0 0]}
   {:emoji "🟧" :rgb [255 165 0]}
   {:emoji "🟨" :rgb [255 255 0]}
   {:emoji "🟩" :rgb [0 128 0]}
   {:emoji "🟦" :rgb [0 0 255]}
   {:emoji "🟪" :rgb [128 0 128]}
   {:emoji "⬛" :rgb [0 0 0]}
   {:emoji "⬜" :rgb [255 255 255]}
   {:emoji "🟫" :rgb [165 42 42]}])

(defn get-pixel-at [image-data x y]
  (let [red-idx (+ (* y (* (.-width image-data) 4)) (* x 4))]
    {:red (aget (.-data image-data) red-idx)
     :green (aget (.-data image-data) (+ red-idx 1))
     :blue (aget (.-data image-data) (+ red-idx 2))
     :alpha (aget (.-data image-data) (+ red-idx 3))}))

(defn brightness-at [image-data x y]
  (let [{:keys [red green blue alpha]} (get-pixel-at image-data x y)]
    (if (< alpha 255)
      255
      (/ (js/Math.floor (+ red green blue)) 3))))

(defn brightness-to-char [dark-to-bright-array brightness]
  (let [char-idx (js/Math.floor (* (/ (dec (count dark-to-bright-array)) 255) brightness))
        character (nth dark-to-bright-array char-idx)]
    ;; Force the web page to actually render a space character
    (if (= character " ")
      "&nbsp;"
      (str "&#x" character ";"))))

(defn color-distance [[r1 g1 b1] [r2 g2 b2]]
  (Math/sqrt
    (+ (Math/pow (- r1 r2) 2)
       (Math/pow (- g1 g2) 2)
       (Math/pow (- b1 b2) 2))))

(defn closest-emoji [{:keys [red green blue _alpha]} emoji-palette]
  (let [input-color [red green blue]]
    (:emoji
      (apply min-key #(color-distance input-color (:rgb %)) emoji-palette))))

(defn pixel-to-character [kind image-data col row]
  (case kind
        :monochrome (brightness-to-char dark-to-bright-emoji (brightness-at image-data col row))
        :emoji-squares (closest-emoji (get-pixel-at image-data col row) emoji-square-palette)
        :emoji-colored (closest-emoji (get-pixel-at image-data col row) emoji-colored-palette)
        :emoji-colored-grayed (closest-emoji (get-pixel-at image-data col row) emoji-color-grayed-palette)
        (prn "missing mappping for " kind)))

(defn convert-to-dom-element [image-data document emoji-kind & [font-size]]
  (let [font-size (or font-size 10)
        container (.createElement document "div")
        style (str "font-family: monospace; word-break:keep-all; font-size: " font-size "px")]
    (set! (.-style container) style)

    (let [content
          (loop [row 0
                 result ""]
            (if (>= row (.-height image-data))
              result
              (let [row-content
                    (loop [col 0
                           row-result ""]
                      (if (>= col (.-width image-data))
                        row-result
                        (let [character (pixel-to-character emoji-kind image-data col row)]
                          (recur (inc col) (str row-result character)))))]
                (recur (inc row) (str result row-content "<br/>")))))]

      (set! (.-innerHTML container) content)
      container)))

(defn dimensions [content-width aspect-ratio size]
  (let [font-size-in-px (get {"small" 12
                              "medium" 14
                              "large" 20} size)
        adjustment (get {"small" 0.8
                         "medium" 0.8
                         "large" 0.8} size)
        width (Math/ceil (* adjustment (/ content-width font-size-in-px)))]
    {:width width
     :height (Math/ceil (* aspect-ratio width))
     :font-size-in-px font-size-in-px}))

(defn draw-frame []
  (let [emoji-kind (:emoji-kind @local-state)
        aspect-ratio (/ (.-videoHeight js/video) (.-videoWidth js/video))
        {:keys [width height font-size-in-px]} (dimensions
                                                 (.-offsetWidth js/content)
                                                 aspect-ratio
                                                 (or (:size @local-state) "small")
                                                 )]

    (let [canvas (.createElement js/document "canvas")
          context (.getContext canvas "2d")]
      (.drawImage context js/video 0 0 width height)

      (let [image-data (.getImageData context 0 0 width height)
            ascii-dom-element (convert-to-dom-element image-data js/document emoji-kind font-size-in-px )]
        (.replaceChildren js/content ascii-dom-element)))))

;; TODO replace js/variable with state atom
(defn render-video [state]
  (when (-> state :videomoji.views.main/view :video-started)
    (let [size (-> state :videomoji.views.main/view :size)
          emoji-kind (-> state :videomoji.views.main/view :emoji-kind)
          running? (-> state :videomoji.views.main/view :video-paused? not)]

      (swap! local-state assoc :emoji-kind emoji-kind)
      (swap! local-state assoc :size size)
      (when-let [interval-id (@local-state :interval-id)]
        (js/clearInterval interval-id))

      (when running?
        ;; Video element
        (set! js/video (.querySelector js/document "video"))

        (letfn [(handle-success [stream]
                  (set! (.-srcObject js/video) stream)
                  (.play js/video)
                  (set! js/content (.getElementById js/document "content")))

                (handle-error [error]
                  (.log js/console "Error for getUserMedia: " (.-message error) (.-name error)))]

          (.addEventListener
            js/video
            "canplay"
            (fn [ev] (swap! local-state assoc :interval-id (js/setInterval draw-frame 200)))
            #js {:once true})

          (-> (.-mediaDevices js/navigator)
              (.getUserMedia #js {:audio false
                                  :video true})
              (.then handle-success)
              (.catch handle-error)))))))

