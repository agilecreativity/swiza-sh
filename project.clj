(defproject net.b12n.swiza.sh "0.1.0"
  :description "swiza-sh interact with shell from Clojure"
  :url "http://github.com/agilecreativity/swiza-sh"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljfmt "0.6.1"]
            [jonase/eastwood "0.3.5"]
            [lein-auto "0.1.3"]
            [lein-cloverage "1.0.13"]]
  :dependencies [[b12n.swiza.commons "0.1.1"]
                 [org.clojars.hozumi/clj-commons-exec "1.2.0"]
                 [org.clojure/clojure "1.9.0"]]
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :test-paths ["src/test/clojure"
               "src/test/java"]
  :target-path "target/%s"
  :profiles {:dev {:global-vars {*warn-on-reflection* true}
                   :dependencies [[org.clojure/test.check "0.9.0"]
                                  [jonase/eastwood "0.3.6"]]}
             :uberjar {:aot :all}})