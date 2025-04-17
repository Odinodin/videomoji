(ns videomoji.dev
  (:require [videomoji.core :as videomoji]))

(defonce store (atom {:number 0}))

(defn main []
  (videomoji/init store)
  (println "Loaded!"))

(defn ^:dev/after-load reload []
  (videomoji/init store)
  (println "Reloaded!!"))
