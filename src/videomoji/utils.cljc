(ns videomoji.utils
  (:require [clojure.walk :refer [postwalk]]))

;; Sniped https://parenteser.mattilsynet.io/interpolering/
(defn interpolate [data replacements]
  (postwalk
    (fn [form]
      (or (replacements form)
          form))
    data))