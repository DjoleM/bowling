(defproject bowling-scorecard "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.1"]
                 [ring/ring-json "0.1.2"]
                 [cheshire "4.0.3"]]
  :plugins [[lein-ring "0.7.3"]]
  :ring {:handler bowling-scorecard.core/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
