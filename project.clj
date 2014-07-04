(defproject paddleguru/forms-bootstrap "0.9.0-SNAPSHOT"
  :description "Utility for creating web forms using Twitter's Bootstrap CSS"
  :url "https://github.com/paddleguru/forms-bootstrap"
  :license {:name "Apache License, Version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [lib-noir "0.8.4" :exclusions [ring/ring]]
                 [enlive "1.1.5"]]
  :main forms-bootstrap.server
  :profiles {:dev {:plugins [[paddleguru/lein-gitflow "0.1.2"]]
                   :dependencies [[compojure "1.1.8"]
                                  [http-kit "2.1.18"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [ring/ring-core "1.3.0"]
                                  [ring/ring-devel "1.3.0"]
                                  [org.clojure/data.json "0.2.1"]]}})
