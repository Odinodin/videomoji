(ns videomoji.video)

(def local-state (atom {}))

(def dark-to-bright-emoji ["1F5A4", "1F977", "1F98D", "1F9BE", "1F993", "1F463", "1F47b", "1F480", "1F440", "1F9B4", "1F90D", "1F4AC", "1F5EF"])

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

(defn convert-to-dom-element [image-data document dark-to-bright-array & [font-size]]
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
                        (let [character (brightness-to-char dark-to-bright-array
                                          (brightness-at image-data col row))]
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
  (let [aspect-ratio (/ (.-videoHeight js/video) (.-videoWidth js/video))
        {:keys [width height font-size-in-px]} (dimensions
                                                 (.-offsetWidth js/content)
                                                 aspect-ratio
                                                 (or (:size @local-state) "small")
                                                 )]

    (let [canvas (.createElement js/document "canvas")
          context (.getContext canvas "2d")]
      (.drawImage context js/video 0 0 width height)

      (let [image-data (.getImageData context 0 0 width height)
            ascii-dom-element (convert-to-dom-element image-data js/document dark-to-bright-emoji font-size-in-px)]
        (.replaceChildren js/content ascii-dom-element)))))

;; TODO replace js/variable with state atom
(defn start-video [state]
  (when (-> state :videomoji.views.main/view :video-started)
    (let [size (-> state :videomoji.views.main/view :size)]
      (prn "START VIDEO " size)
      (swap! local-state assoc :size size)
      (when-let [interval-id (@local-state :interval-id)]
        (js/clearInterval interval-id))

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
            (.catch handle-error))))))

