(ns forms-bootstrap.server
  (:require [compojure.core :refer (GET)]
            [compojure.route :as route]
            [forms-bootstrap.core :as core]
            [forms-bootstrap.test.core :as c]
            [noir.util.middleware :as nm]
            [org.httpkit.server :refer (run-server)]
            [ring.middleware.reload :as rl]))

;; Noir's default "app handler" takes a sequence of routes; it's NOT
;; compatible with the defroutes function provided by compojure.core.
(def all-routes
  (nm/app-handler
   [(GET "/" [] (c/index-page))
    (GET "/make-form" [] (c/make-form-page))
    (GET "/form-helper" [:as {m :params}] (c/form-helper m))
    (GET "/form-helper/:user" [:as {m :params}]
         (let [user (:user m)
               default-values {:username user
                               :birthday-day 12
                               :gender "male"
                               :first-name 12345
                               :colors ["red" "blue"]}]
           (c/test-layout
            {:form-tests
             [[(c/helper-example-user default-values (str "/" user "/action") "/")
               "Form-helper Example"
               "Uses the form-helper macro for easy validation."]]})))
    c/helper-example-post
    c/post-helper-example
    (route/not-found "<p>Page not found.</p>")]))

;; Note that this library requires nm/app-handler to work, since we
;; mess with sessions.
(defn -main [& _]
  (println "Firing up an HTTP-Kit server on port 8080.")
  (run-server (rl/wrap-reload #'all-routes) {:port 8080}))
