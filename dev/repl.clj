(ns repl
  (:require
    [clojure.java.io :as io]
    [build]
    [shadow.cljs.devtools.api :as shadow]
    [shadow.cljs.devtools.server.fs-watch :as fs-watch]))


(defonce css-watch-ref (atom nil))

(defn start
  {:shadow/requires-server true}
  []

  ;; this is optional
  ;; if using shadow-cljs you can start a watch from here
  ;; same as running `shadow-cljs watch your-build` from the command line
  (shadow/watch :app)

  ;; build css once on start
  (build/css-release)

  ;; then setup the watcher that rebuilds everything on change
  (reset! css-watch-ref
    (fs-watch/start
      {}
      [(io/file "src")]
      ["cljs" "cljc" "clj"]
      (fn [_]
        (try
          (build/css-release)
          (catch Exception e
            (prn [:css-failed e]))))))

  ::started)

(defn stop []
  (when-some [css-watch @css-watch-ref]
    (fs-watch/stop css-watch))

  ::stopped)

(defn go []
  (stop)
  (start))