(ns videomoji.video
  (:require
    ["@mediapipe/tasks-vision" :as mp-vision]
    [videomoji.emoji :as emoji]
    [shadow.cljs.modern :refer (js-await)]))

(def local-state (atom {:emoji-kind :emoji-colored}))

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

(defn get-pixel-at [image-data x y]
  (let [red-idx (+ (* y (* (.-width image-data) 4)) (* x 4))]
    {:red (aget (.-data image-data) red-idx)
     :green (aget (.-data image-data) (+ red-idx 1))
     :blue (aget (.-data image-data) (+ red-idx 2))
     :alpha (aget (.-data image-data) (+ red-idx 3))}))

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
        :monochrome (closest-emoji (get-pixel-at image-data col row) emoji/monochrome-palette)
        :emoji-squares (closest-emoji (get-pixel-at image-data col row) emoji/square-palette)
        :emoji-colored (closest-emoji (get-pixel-at image-data col row) emoji/colored-palette)
        :emoji-colored-grayed (closest-emoji (get-pixel-at image-data col row) emoji/color-grayed-palette)
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
