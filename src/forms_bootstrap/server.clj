(ns forms-bootstrap.server
  (:require [noir.server :as server]
            [ring.middleware.reload :as rl]))

(server/load-views "test/forms_bootstrap/test/")

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (when (= mode :dev)
      (server/add-middleware rl/wrap-reload))
    (server/start port {:mode mode
                        :ns 'forms-bootstrap})))
