(ns videomoji.video
  (:require
    ["@mediapipe/tasks-vision" :as mp-vision]
    [shadow.cljs.modern :refer (js-await)]))

(def local-state (atom {:emoji-kind :emoji-colored}))

(def dark-to-bright-emoji ["1F5A4", "1F977", "1F98D", "1F9BE", "1F993", "1F463", "1F47b", "1F480", "1F440", "1F9B4", "1F90D", "1F4AC", "1F5EF"])

(defonce imageSegmenter (atom nil))
(defonce labels (atom nil))

(defn create-image-segmenter
  "Initializes the MediaPipe ImageSegmenter."
  []
  (try
    (js-await [fileset-resolver (.forVisionTasks mp-vision/FilesetResolver "/wasm")] ;; wasm-dir copied from @mediapipe node_modules
      (let [;; Define the options for the ImageSegmenter using a JS object literal
            segmenter-options #js {:baseOptions #js {:modelAssetPath "/selfie_segmenter_landscape.tflite"
                                                     :delegate "GPU"}
                                   :runningMode "IMAGE"
                                   :outputCategoryMask true
                                   :outputConfidenceMasks false}

            _ (prn "OPTS " (js->clj segmenter-options))

            ;; Create the segmenter instance
            segmenter (js-await (.createFromOptions mp-vision/ImageSegmenter
                                  fileset-resolver
                                  segmenter-options))]
        (js-await [segmenter (.createFromOptions mp-vision/ImageSegmenter
                               fileset-resolver
                               segmenter-options)]

          ;; --- Update State and UI ---
          ;; Store the created segmenter and labels in atoms
          (reset! imageSegmenter segmenter)
          (reset! labels (.getLabels segmenter))            ; Call instance method

          true)))

    ;; --- Error Handling ---
    (catch js/Error e
      (js/console.error "Failed to create Image Segmenter:" e)
      ;; Indicate failure (optional)
      false)))

(create-image-segmenter)

(def emoji-monochrome-palette
  [{:emoji "&#x1F5A4;" :rgb [15 15 15]},
   {:emoji "&#x1F977;" :rgb [35 35 35]},
   {:emoji "&#x1F98D;" :rgb [55 55 55]},
   {:emoji "&#x1F9BE;" :rgb [75 75 75]},
   {:emoji "&#x1F993;" :rgb [95 95 95]},
   {:emoji "&#x1F463;" :rgb [115 115 115]},
   {:emoji "&#x1F47b;" :rgb [135 135 135]},
   {:emoji "&#x1F480;" :rgb [155 155 155]},
   {:emoji "&#x1F440;" :rgb [175 175 175]},
   {:emoji "&#x1F9B4;" :rgb [195 195 195]},
   {:emoji "&#x1F90D;" :rgb [215 215 215]},
   {:emoji "&#x1F4AC;" :rgb [235 235 235]},
   {:emoji "&#x1F5EF;" :rgb [255 255 255]}])

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
   {:emoji "🌈" :rgb [150 150 150]}                         ;; rainbow, used for ambiguous colors
   ])

(def emoji-square-palette
  [{:emoji "🟥" :rgb [255 0 0]}
   {:emoji "🟧" :rgb [255 165 0]}
   {:emoji "🟨" :rgb [255 255 0]}
   {:emoji "🟩" :rgb [0 128 0]}
   {:emoji "🟦" :rgb [0 0 255]}
   {:emoji "🟪" :rgb [128 0 128]}
   {:emoji "⬛" :rgb [0 0 0]}
   {:emoji "🟨" :rgb [254 254 254]}
   {:emoji "⬜" :rgb [255 255 255]}
   {:emoji "🟫" :rgb [155 15 15]}])

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
        :monochrome (closest-emoji (get-pixel-at image-data col row) emoji-monochrome-palette) #_(brightness-to-char dark-to-bright-emoji (brightness-at image-data col row))
        :emoji-squares (closest-emoji (get-pixel-at image-data col row) emoji-square-palette)
        :emoji-colored (closest-emoji (get-pixel-at image-data col row) emoji-colored-palette)
        :emoji-colored-grayed (closest-emoji (get-pixel-at image-data col row) emoji-color-grayed-palette)
    (prn "missing mappping for " kind)))

(defn convert-to-dom-element [image-data document emoji-kind & [font-size]]
  (let [font-size (or font-size 10)
        container (.createElement document "div")
        style (str "font-family: monospace; word-break:keep-all; line-height: 1; font-size: " font-size "px")]
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
  (let [font-size-in-px (get {"small" 10
                              "medium" 14
                              "large" 20} size)
        adjustment (get {"small" 0.9
                         "medium" 0.9
                         "large" 0.9} size)
        width (Math/ceil (* adjustment (/ content-width font-size-in-px)))]
    {:width width
     :height (Math/ceil (* aspect-ratio width))
     :font-size-in-px font-size-in-px}))

(defn image-callback [result]
  (let [context js/the-context
        emoji-kind (:emoji-kind @local-state)
        aspect-ratio (/ (.-videoHeight js/video) (.-videoWidth js/video))
        {:keys [width height font-size-in-px]} (dimensions
                                                 (.-offsetWidth js/content)
                                                 aspect-ratio
                                                 (or (:size @local-state) "small"))

        original-data (.getImageData context 0 0 width height)
        ^js image-data (.-data original-data)

        ;; --- 2. Get Mask Data ---
        ;; Access the category mask and get it as a Float32Array
        ^js mask (-> result .-categoryMask (.getAsUint8Array))

        mask-length (.-length mask)
        ]



    (loop [i 0                                              ; Index for mask array
           j 0]                                             ; Index for image-data array (steps by 4)
      (when (< i mask-length)                               ; Loop while mask index is in bounds
        (when (not= (aget mask i) 0)
          (let [;; Get legend RGBA values
                leg-r 255 #_(aget legend-color 0)
                leg-g 255 #_(aget legend-color 1)
                leg-b 255 #_(aget legend-color 2)
                leg-a 255 #_(aget legend-color 3)]

            ;; Clear out the pixel
            (aset image-data j leg-r)
            (aset image-data (+ j 1) leg-g)
            (aset image-data (+ j 2) leg-b)
            (aset image-data (+ j 3) leg-a)
            ))

        ;; Move to the next mask value and the next pixel (4 steps in image-data)
        (recur (inc i) (+ j 4))))

    (let [clamped (js/Uint8ClampedArray. (.-buffer image-data))
          data-new (js/ImageData. clamped width height)
          ascii-dom-element (convert-to-dom-element data-new js/document emoji-kind font-size-in-px)]
      (.putImageData context data-new 0 0)
      (.replaceChildren js/content ascii-dom-element))))

(defn draw-frame []
  (let [aspect-ratio (/ (.-videoHeight js/video) (.-videoWidth js/video))
        {:keys [width height font-size-in-px]} (dimensions
                                                 (.-offsetWidth js/content)
                                                 aspect-ratio
                                                 (or (:size @local-state) "small"))]

    (let [canvas (.querySelector js/document "canvas")
          context (.getContext canvas "2d" (clj->js {"willReadFrequently" true}))]

      (set! (.-width canvas) width)
      (set! (.-height canvas) height)

      (.drawImage context js/video 0 0 width height)
      (set! js/the-context context)
      (.segment @imageSegmenter canvas image-callback))))

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
            (fn [ev] (swap! local-state assoc :interval-id (js/setInterval draw-frame 50)))
            #js {:once true})

          (-> (.-mediaDevices js/navigator)
              (.getUserMedia #js {:audio false
                                  :video true})
              (.then handle-success)
              (.catch handle-error)))))))

