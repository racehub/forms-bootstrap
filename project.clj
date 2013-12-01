(defproject paddleguru/forms-bootstrap "0.7.0-SNAPSHOT"
  :description "Utility for creating web forms using Twitter's Bootstrap CSS"
  :url "https://github.com/paddleguru/forms-bootstrap"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [lib-noir "0.7.2"]
                 [compojure "1.1.5"]
                 [paddleguru/enlive "1.2.0-alpha1"]
                 [org.clojars.dpetrovics/sandbar "0.4.0-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[http-kit "2.1.13"]
                                  [ring/ring-core "1.2.0"]
                                  [org.clojure/data.json "0.2.1"]]}}
  :main forms-bootstrap.server)
