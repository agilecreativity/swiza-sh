## swiza-sh

[![Clojars Project](https://img.shields.io/clojars/v/net.b12n/swiza-sh.svg)](https://clojars.org/net.b12n/swiza-sh)
[![Dependencies Status](https://jarkeeper.com/agilecreativity/swiza-sh/status.png)](https://jarkeeper.com/agilecreativity/swiza-sh)
![ClojarsDownloads](https://img.shields.io/clojars/dt/net.b12n/swiza-sh)

Clojure library designed to wrap the execution of shell script to make it easy to manipulate the result.

The library rely on the awesome power of [clj-common-exec][1] which wrap the functionality of [apache-common-exec][2]

[1]: https://github.com/hozumi/clj-commons-exec
[2]: http://commons.apache.org/proper/commons-exec

### Installation

Just include the following line in your dependency vector

```clojure
[net.b12n/swiza-sh "0.1.3"]
```

### Available Apis

- sh-cmd - run a single shell command
- sh-cmds - run multiple shell commands
- run-cmds - run multiple shell commands (alternative api)
- sh-exec - run command with callback function

### Example Usages

```clojure
(require '[b12n.swiza.commons :refer [expand-path]])
(require '[b12n.swiza.sh.core :refer [sh-cmd sh-cmds run-cmds sh-exec]])
```

- `run-cmds` : run multiple commands optionally stop on first error

```clojure
;; a)
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
```

- `sh-cmds` : run multiple command using the original api

Build multiple Clojure projects using Leiningen

```clojure
(sh-cmds [{:cmd ["lein" "deps" ":tree"] :opts {:dir (expand-path "~/apps/swiza/swiza-commons")}}
          {:cmd ["lein" "deps" ":tree"] :opts {:dir (expand-path "~/apps/swiza/swiza-jenkins")}}
          {:cmd ["lein" "deps" ":tree"] :opts {:dir (expand-path "~/apps/swiza/swiza-aws")}}])
```

Build and run GraalVM native image from Clojure

```clojure
;; Build and run the GraalVM project using `lein native-image`
(let [base-dir (expand-path "~/apps/swizz/swiza-graal")]
  (sh-cmds [{:cmd ["lein" "deps" ":tree"] :opts {:dir base-dir}}
            {:cmd ["lein" "native-image"] :opts {:dir base-dir}}
            {:cmd [(format "%s/target/default+uberjar/swiza-graal" base-dir)]}]))
```

You will get output like:

```clojure
(["drwxr-xr-x" "7" "bchoomnuan" "staff" "224" "Aug" "31" "11:01" "."]
 ["drwxr-xr-x" "60" "bchoomnuan" "staff" "1920" "Aug" "31" "09:22" ".."]
 ["-rw-r--r--" "1" "bchoomnuan" "staff" "5" "Aug" "31" "09:53" ".nrepl-port"]
 ["-rw-r--r--" "1" "bchoomnuan" "staff" "1328" "Aug" "31" "11:01" "README.md"]
 ["-rw-r--r--" "1" "bchoomnuan" "staff" "2691" "Aug" "30" "22:56" "project.clj"]
 ["drwxr-xr-x" "4" "bchoomnuan" "staff" "128" "Aug" "23" "22:34" "src"]
 ["drwxr-xr-x" "4" "bchoomnuan" "staff" "128" "Aug" "24" "01:16" "target"])
```

If you like to post-process the result of a single output then try `sh-exec`

```clojure
(sh-exec ["ls" "-al"]
         {:opts {:dir (expand-path ".")}
          :success-fn (fn [x]
                        (if-let [lines (clojure.string/split x #"\n")]
                          (map #(clojure.string/split % #"\s+") (rest lines))))})
```

```clojure
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

### Tips 

- https://hashids.org/java/#how-does-it-work
