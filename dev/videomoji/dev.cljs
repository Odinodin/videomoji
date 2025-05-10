(ns videomoji.dev
  (:require [videomoji.core :as videomoji]))

(defonce store (atom {}))
(defonce el (js/document.getElementById "app"))

(defn ^:dev/after-load main []
  ;; Add additional dev-time tooling here
  (videomoji/main store el))
