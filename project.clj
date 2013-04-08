(defproject sleuth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [ring-edn "0.1.0"]
                 [com.novemberain/monger "1.5.0"]
                 [clj-time "0.5.0"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler sleuth.handler/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
