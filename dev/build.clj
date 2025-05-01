(ns build
  (:require
    [clojure.java.io :as io]
    [shadow.css.build :as cb]))

(defn css-release [& args]
  (let [build-state
        (-> (cb/start)
            (cb/index-path (io/file "src") {})
            (assoc-in [:aliases :gap-2] {:gap "2px"})
            (assoc-in [:aliases :gap-4] {:gap "4px"})
            (assoc-in [:aliases :gap-6] {:gap "6px"})
            (cb/generate
              '{:styles
                {:entries [videomoji.core
                           videomoji]}})
            (cb/minify)
            (cb/write-outputs-to (io/file "resources" "public")))]

    (doseq [mod (vals (:chunks build-state))
            {:keys [warning-type] :as warning} (:warnings mod)]
      (prn [:CSS (name warning-type) (dissoc warning :warning-type)]))))