(ns videomoji.video
  (:require
   ["@mediapipe/tasks-vision" :as mp-vision]
   [shadow.cljs.modern :refer (js-await)]
   [videomoji.emoji :as emoji]))

(def local-state (atom {:emoji-kind :emoji-colored
                        :running? true}))

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

    (catch js/Error e
      (js/console.error "Failed to create Image Segmenter:" e)
      false)))

(create-image-segmenter)

(defn video-el [] (.querySelector js/document "video"))
(defn canvas-el [] (.querySelector js/document "#offscreen-canvas"))
(defn context2d [c] (.getContext c "2d" (clj->js {"willReadFrequently" true})))
(defn content-el [] (.getElementById js/document "content"))

(defn should-draw? []
  (and (:running? @local-state)
       (exists? (content-el))
       (not (nil? (content-el)))
       (exists? (video-el))
       (not (nil? (video-el)))
       (exists? (canvas-el))
       (not (nil? (canvas-el)))
       (exists? (context2d (canvas-el)))
       (not (nil? (context2d (canvas-el))))))

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
  (when (should-draw?)

    (let [emoji-kind (:emoji-kind @local-state)
          aspect-ratio (/ (.-videoHeight (video-el)) (.-videoWidth (video-el)))
          {:keys [width height font-size-in-px]} (dimensions
                                                   (.-offsetWidth (content-el))
                                                   aspect-ratio
                                                   (or (:size @local-state) "small"))

          original-data (.getImageData (context2d (canvas-el)) 0 0 width height)
          image-data (.-data original-data)
          mask (-> result .-categoryMask (.getAsUint8Array))
          mask-length (.-length mask)]

      (loop [i 0
             j 0]
        (when (< i mask-length)
          (when (not= (aget mask i) 0)
            ;; Clear out the pixel
            (aset image-data j 255)
            (aset image-data (+ j 1) 255)
            (aset image-data (+ j 2) 255)
            (aset image-data (+ j 3) 255))

          ;; Move to the next mask value and the next pixel (4 steps in image-data)
          (recur (inc i) (+ j 4))))

      (let [clamped (js/Uint8ClampedArray. (.-buffer image-data))
            data-new (js/ImageData. clamped width height)
            ascii-dom-element (emoji/convert-to-dom-element data-new js/document emoji-kind font-size-in-px)]
        (.putImageData (context2d (canvas-el)) data-new 0 0)
        (.replaceChildren (content-el) ascii-dom-element)))))



(defn draw-frame []
  (when (should-draw?)

    (let [video-height (.-videoHeight (video-el))
          video-width (.-videoWidth (video-el))]

      (when (and (< 0 video-height) (< 0 video-width))
        (let [aspect-ratio (/ video-height video-width)
              {:keys [width height]} (dimensions
                                       (.-offsetWidth (content-el))
                                       aspect-ratio
                                       (or (:size @local-state) "small"))]
          (set! (.-width (canvas-el)) width)
          (set! (.-height (canvas-el)) height)
          (.drawImage (context2d (canvas-el)) (video-el) 0 0 width height)
          (.segment @imageSegmenter (canvas-el) image-callback))))))

(defn update-state [state]
  (let [size (-> state :videomoji.pages.main/view :size)
        emoji-kind (-> state :videomoji.pages.main/view :emoji-kind)
        running? (-> state :videomoji.pages.main/view :video-paused? not)]
    (swap! local-state assoc
      :running? running?
      :size size
      :emoji-kind emoji-kind)))

(defn initialize [state]
  (update-state state)

  (when-let [interval-id (@local-state :interval-id)]
    (js/clearInterval interval-id))

  (when (and (exists? (video-el))
             (not (nil? (video-el))))
    (letfn [(handle-success [stream]
              (set! (.-srcObject (video-el)) stream)
              (.play (video-el)))

            (handle-error [error]
              (.log js/console "Error for getUserMedia: " (.-message error) (.-name error)))]

      (.addEventListener
        (video-el)
        "canplay"
        (fn [ev] (swap! local-state assoc :interval-id (js/setInterval draw-frame 50)))
        #js {:once true})

      (-> (.-mediaDevices js/navigator)
          (.getUserMedia #js {:audio false
                              :video true})
          (.then handle-success)
          (.catch handle-error)))))
