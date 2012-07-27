(ns forms-bootstrap.test.core
  (:use forms-bootstrap.core
        clojure.test
        forms-bootstrap.core
        noir.core
        net.cgrand.enlive-html))

(def test-template "forms_bootstrap/test/test-page.html")

(deftemplate test-layout
  test-template
  [{:keys [make-form-tests]}]
  [:section] (clone-for [[form-test header descr] make-form-tests]
                        [:div.page-header :h3] (html-content
                                                (str header " <small>" descr "</small"))
                        [:div.formhere] (content form-test)))

;;This first example uses the make-form function 
(defpage "/" [] 
  (test-layout {:make-form-tests
                [[(make-form
                 :action "someaction"
                 :submit-label "Send it!"  
                 :fields [{:type "text" :name "nickname" :label "Nick Name" :size "input-large"}
                          {:type "password" :name "password" :label "Password"}
                          {:type "text" :name "city" :label "City" :placeholder "Placeholder!"}
                          {:type "text-area" :name "description" :label "Favorite Quote"}
                          {:type "select" :name "colors" :label "Favorite Color"
                           :inputs [["blue" "Blue"]
                                    ["red" "Red"]
                                    ["yellow" "Yellow"]]}
                          {:type "radio" :name "cars" :label "Favorite Car"
                           :inputs [["honda" "Honda"]
                                    ["toyota" "Toyota"]
                                    ["chevy" "Chevy"]]}
                          {:type "checkbox" :name "languages" :label "Languages"
                           :inputs [["german" "German"]
                                    ["french" "French"]
                                    ["english" "English"]]}
                          {:type "file-input" :name "afile" :label "Choose a pic"}])
                  "Example One"
                  "A Longer Form"]]
                }))
