(defproject forms-bootstrap "0.4.5-SNAPSHOT"
  :description "Utility for creating web forms using Twitter's Bootstrap CSS"
  :url "https://github.com/dpetrovics/forms-bootstrap"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [noir "1.3.0-beta10"]
                 [paddleguru/enlive "1.2.0-alpha1"]
                 [org.clojars.dpetrovics/sandbar "0.4.0-SNAPSHOT"]
                 [ring/ring-core "1.1.1"]]
  :plugins [[lein-swank "1.4.4"]
            [lein-ring "0.7.4"]]
  :main  forms-bootstrap.server)
