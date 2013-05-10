(defproject sleuth "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"  
  :license {:name "GNU Affero General Public License"
            :url "http://www.gnu.org/licenses/agpl.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-jetty-adapter "1.1.0"]
                 [compojure "1.1.5"]
                 [ring-edn "0.1.0"]
                 [clj-http "0.7.2"]
                 [ring/ring-json "0.2.0"]
                 [com.novemberain/monger "1.5.0"]
                 [prismatic/dommy "0.1.1"]
                 [com.cemerick/bandalore "0.0.3"]
                 [bdisraeli/rotary "0.3.0-SNAPSHOT"]
                 [com.amazonaws/aws-java-sdk "1.4.3"]
                 [clj-time "0.5.0"]
                 [jayq "2.3.0"]
                 [secretary "0.2.0-SNAPSHOT"]
                 [org.clojure/data.json "0.2.2"]
                 [org.clojure/data.codec "0.1.0"]]
  :plugins [[lein-cljsbuild "0.3.0"]
            [lein-ring "0.8.2"]
            [speclj "2.5.0"]]
  :profiles {:dev {:dependencies [[speclj "2.5.0"]
                                  [ring-mock "0.1.3"]]}}
  :test-paths ["spec/"]
  :cljsbuild {
              :builds [{
                        :source-paths ["src-cljs"]
                        :compiler {
                                   :output-to "resources/public/javascripts/app.js"
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  :ring {:handler sleuth.handler/app}
  :min-lein-version "2.0.0")
