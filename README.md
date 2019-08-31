## swiza-sh 

Clojure library designed to wrap the execution of shell script to make it easy to manipulate the result.

The library rely on the awesome power of [clj-common-exec][1] which wrap the functionality of [apache-common-exec][2]

[1]: https://github.com/hozumi/clj-commons-exec
[2]: http://commons.apache.org/proper/commons-exec

### Installation

Just include the following line in your dependency vector

```clojure
[b12n.swiza.sh "0.1.0"]
```

### Example Usage

Then to use this in your Clojure project try 

- Run multiple shell commands and stop on the first error.

```clojure
(require '[b12n.swiza.commons :refer [expand-path]])
(require '[b12n.swiza.sh :refer [sh-cmd sh-cmds sh-exec]])

;; Build multiple Clojure projects using Leiningen 
(sh-cmds [{:cmd ["lein" "deps" ":tree"] :opts {:dir (expand-path "~/apps/swiza/swiza-commons")}}
          {:cmd ["lein" "deps" ":tree"] :opts {:dir (expand-path "~/apps/swiza/swiza-jenkins")}}
          {:cmd ["lein" "deps" ":tree"] :opts {:dir (expand-path "~/apps/swiza/swiza-aws")}}])
```



- Build and run GraalVM native image from Clojure 

```clojure
;; Build and run the GraalVM project using `lein native-image`
(let [base-dir (expand-path "~/apps/swizz/swiza-graal")]
  (sh-cmds [{:cmd ["lein" "deps" ":tree"] :opts {:dir base-dir}}
            {:cmd ["lein" "native-image"] :opts {:dir base-dir}}
            {:cmd [(format "%s/target/default+uberjar/swiza-graal" base-dir)]}]))
```

You will get output like:

```
(["drwxr-xr-x" "7" "bchoomnuan" "staff" "224" "Aug" "31" "11:01" "."] 
 ["drwxr-xr-x" "60" "bchoomnuan" "staff" "1920" "Aug" "31" "09:22" ".."] 
 ["-rw-r--r--" "1" "bchoomnuan" "staff" "5" "Aug" "31" "09:53" ".nrepl-port"] 
 ["-rw-r--r--" "1" "bchoomnuan" "staff" "1328" "Aug" "31" "11:01" "README.md"] 
 ["-rw-r--r--" "1" "bchoomnuan" "staff" "2691" "Aug" "30" "22:56" "project.clj"] 
 ["drwxr-xr-x" "4" "bchoomnuan" "staff" "128" "Aug" "23" "22:34" "src"] 
 ["drwxr-xr-x" "4" "bchoomnuan" "staff" "128" "Aug" "24" "01:16" "target"])

```

- If you like to post-process the result of a single output then try `sh-exec`

```clojure
(sh-exec ["ls" "-al"]
         {:opts {:dir (expand-path ".")}
          :success-fn (fn [x]
                        (if-let [lines (clojure.string/split x #"\n")]
                          (map #(clojure.string/split % #"\s+") (rest lines))))})
```

```
;; More example
(if-let [files (sh-exec ["find" (expand-path "~/apps/pro-scripts")
                           "-type" "f"
                           "-iname" "*.sh"]
                          {:success-fn (fn [line]
                                         (if-let [result (str/split line #"\n")]
                                           result))
                           :opts {:dir (expand-path "~/apps/pro-scripts")}})]
    (doseq [file files]
      (println file)))
```

### Development

```shell
git clone git@github.com:agilecreativity/swiza-sh.git
cd swiza-sh && lein deps :tree
lein repl
;; then hackaway
```
