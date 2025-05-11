(ns videomoji.video
  (:require
    ["@mediapipe/tasks-vision" :as mp-vision]
    [videomoji.emoji :as emoji]
    [shadow.cljs.modern :refer (js-await)]))

(def local-state (atom {:emoji-kind :emoji-colored}))

(defonce imageSegmenter (atom nil))

(defn create-image-segmenter []
  (try
    (js-await [fileset-resolver (.forVisionTasks mp-vision/FilesetResolver "/wasm")] ;; wasm-dir copied from @mediapipe node_modules
      (let [segmenter-options #js {:baseOptions #js {:modelAssetPath "/selfie_segmenter_landscape.tflite"
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
          (reset! imageSegmenter segmenter)
          true)))

    ;; --- Error Handling ---
    (catch js/Error e
      (js/console.error "Failed to create Image Segmenter:" e)
      ;; Indicate failure (optional)
      false)))

(create-image-segmenter)

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

        mask-length (.-length mask)]

    (loop [i 0                                              ; Index for mask array
           j 0]                                             ; Index for image-data array (steps by 4)
      (when (< i mask-length)                               ; Loop while mask index is in bounds
        (when (not= (aget mask i) 0)
          (let [leg-r 255
                leg-g 255
                leg-b 255
                leg-a 255]

            ;; Clear out the pixel
            (aset image-data j leg-r)
            (aset image-data (+ j 1) leg-g)
            (aset image-data (+ j 2) leg-b)
            (aset image-data (+ j 3) leg-a)))

        ;; Move to the next mask value and the next pixel (4 steps in image-data)
        (recur (inc i) (+ j 4))))

    (let [clamped (js/Uint8ClampedArray. (.-buffer image-data))
          data-new (js/ImageData. clamped width height)
          ascii-dom-element (emoji/convert-to-dom-element data-new js/document emoji-kind font-size-in-px)]
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
  (when (-> state :videomoji.pages.main/view :video-initialized?)
    (let [size (-> state :videomoji.pages.main/view :size)
          emoji-kind (-> state :videomoji.pages.main/view :emoji-kind)
          running? (-> state :videomoji.pages.main/view :video-paused? not)]

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
