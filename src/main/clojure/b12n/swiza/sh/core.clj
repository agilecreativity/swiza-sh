(ns b12n.swiza.sh.core
  (:require [b12n.swiza.commons.core-utils :refer [expand-path]]
            [clj-commons-exec :as exec :refer [sh]]
            [clojure.string :as str]))

(set! *warn-on-reflection* true)

(defn- parse-cmd [s]
  (clojure.string/split s #"\s+"))

(defn sh-cmd
  "Execute a single shell command.

  (sh-cmd {:cmd [\"ls\"]}))
  (sh-cmd {:cmd [\"ls\"]
           :opts {:dir (expand-path \"~/projects\")}}))"
  [& [{:keys [cmd opts]}]]
  (try
    @(sh cmd opts)
    (catch Exception e
      (.getMessage e))))

(defn run-cmds
  "Run multiple commands with simple api.

  Examples:

  ;; a) Run multiple commands, using the shared `dir` options (ignore error by default)
  (run-cmds {:dir \".\"
             :cmds [\"ls -alt\"
                    \"find . -type f -iname \"*.clj\"]})

  ;; b) Similar to the first usage, but stop on the first error
  (run-cmds {:dir \".\"
             :ignore-error? false
             :cmds [\"ls -alt\"
                    \"invalid-command\"
                    \"find . -type f -iname \"*.clj\"]})

  ;; c) Run multiple command using that run in start from different directory (ignore error)
  ;; e.g. don't specify `:dir` option
  (run-cmds {:cmds [\"ls -alt\"
                    \"find . -type f -iname \"*.clj\"]})

  ;; d) Same as above but stop on first error
  (run-cmds {:ignore-error? false
             :cmds [\"ls -alt\"
                    \"find . -type f -iname \"*.clj\"]})"
  [& [{:keys [ignore-error?
              dir
              cmds]
       :or {ignore-error? true}}]]
  (loop [cmds (if dir
                (partition 2 (interleave cmds (take (count cmds) (repeat (expand-path dir)))))
                (partition 2 cmds))
         error false
         result []]
    (if (and (seq cmds)
             (or ignore-error? (not error)))
      (let [[c & cs] cmds
            [cmd d] c
            {:keys [exit out err]} (sh-cmd {:cmd (parse-cmd cmd) :opts {:dir d}})]
        (recur cs
               (not (zero? exit))
               (conj result {:cmd cmd
                             :exit exit
                             :out (if out out "")
                             :err (if err err "")})))
      result)))

;; External

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

(comment
  ;; Scratch Area
  ;; --------------------------- ;;
  (sh-cmd {:cmd ["ls"]})
  ;; --------------------------- ;;
  (sh-exec ["ls" "-alt"]
           {:opts {:dir (expand-path ".")}
            :success-fn (fn [x]
                          (if-let [lines (clojure.string/split x #"\n")]
                            (map #(clojure.string/split % #"\s+") (rest lines))))})
  ;; --------------------------- ;;
  (->>
   (run-cmds {:dir "."
              :ignore-error? false
              :cmds ["ls -alt"
                     "find . -type f -iname \"*.clj\""]})
   (map :out))
  ;; --------------------------- ;;
  (run-cmds {:cmds ["ls -alt"
                    "."
                    "find . -type f -iname \"*.clj\"" "."]})
  ;; --------------------------- ;;
  )
