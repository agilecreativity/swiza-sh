(ns b12n.swiza.sh.core
  (:require [b12n.swiza.commons.core-utils :refer [expand-path]]
            [clj-commons-exec :as exec :refer [sh]]
            [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn sh-cmd
  "Execute a single shell command.

  (sh-command {:cmd [\"ls\"]}))
  (sh-command {:cmd [\"ls\"]
               :opts {:dir (expand-path \"~/projects\")}}))"
  [& [{:keys [cmd opts]}]]
  (try
    @(sh cmd opts)
    (catch Exception e
      (.getMessage e))))

(defn sh-cmds
  "Execute multiple shell command stop on the first error.

  Example:
  ;; Building multiple Clojure projects with Leiningen
  (sh-cmds [{:cmd [\"lein\" \"deps\" \":tree\"]
             :opts {:dir (expand-path \"~/apps/swiza/swiza-vault\")}}
            {:cmd [\"lein\" \"deps\" \":tree\"]
             :opts {:dir (expand-path \"~/apps/swiza/swiza-jenkins\")}}])"
  [commands]
  (loop [cmds commands
         result []]
    (if cmds
      (let [{:keys [cmd opts]} (first cmds)
            {:keys [exit out err exception]}
            (sh-cmd {:cmd cmd :opts opts})]
        (if (= 0 exit)
          (recur (next cmds)
                 (conj result {:cmd cmd
                               :err err
                               :out out}))
          (conj result {:cmd cmd
                        :err (.getMessage exception)
                        :out out})))
      result)))

(defn sh-exec
  "Execute a shell command and process the result using the callback style function.

  Example:
  ;; Run the command `ls` and parse the result on success.
  (sh-exec [\"ls\" \"-alt\"]
  {:opts {:dir (expand-path \".\")}
   :success-fn (fn [x]
                 (if-let [lines (clojure.string/split x #\"\n\")]
                   (map #(clojure.string/split % #\"\\s+\") (rest lines))))})"
  [cmd & [{:keys [opts
                  success-fn
                  error-fn]
           :or {success-fn identity
                error-fn identity}}]]
  (let [{:keys [out exit err exception]}
        (sh-cmd {:cmd cmd
                 :opts opts})]
    (if (and (= 0 exit) out)
      (success-fn out)
      (error-fn {:err err
                 :exit exit}))))
