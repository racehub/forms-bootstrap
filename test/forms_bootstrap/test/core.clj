(ns forms-bootstrap.test.core
  (:use forms-bootstrap.core
        clojure.test
        forms-bootstrap.util
        noir.core
        net.cgrand.enlive-html)
  (:require [noir.response :as response]
            [noir.session :as session]
            [clojure.data.json :as json]
            [sandbar.validation :as v]))

(def test-template "forms_bootstrap/test/test-page.html")

(deftemplate test-layout
  test-template
  [{:keys [form-tests links]}]
  [:p.example-link] (clone-for [[href link-name] links]
                                 [:a] (do-> (set-attr :href href)
                                            (content link-name)))
  [:section] (clone-for [[form-test header descr] form-tests]
                        [:div.page-header :h3] (html-content
                                                (str header " <small>" descr "</small"))
                        [:div.formhere] (content form-test)))

;;This first example uses the make-form function
(defpage "/" []
  (test-layout {:links
                [["/make-form" "Make-Form Example"]
                 ["/form-helper" "Form-Helper Example"]]}))

;;you can use make-form in conjunction with
;;create-errors-defaults-map and post-helper.

;;Create-errors-defaults-map will populate fields in the form (on the
;;first page load) with the map that you give it. It can handle
;;checkboxes with multiple values too, just pass in the values in a collection.

;;Use post-helper to help with validation / form submission. Just pass
;;it an on-success and an on-failure fn, just like w. form-helper. If
;;the form fails validation, it will move all the errors over to the
;;flash, and reloads the page. Now create-errors-defaults-map can
;;access the form-data from the last submit and any errors since they
;;are now in the flash. Make the returned map the value to
;;:errors-and-defaults for make-form, and it will show the defaults
;;and errors in the form.

(defpage "/make-form" []
  (test-layout {:form-tests
                [[(make-form
                   :action "/someaction"
                   :submit-label "Send it!"
                   :cancel-link "/"
                   ;;you can pass in any defaults here that you want
                   ;;to preopulate the form with.
                   ;;If you use the post-helper macro (see below),
                   ;;then form validation errors will be stored in the
                   ;;flash for you, and create-errors-defaults-map
                   ;;will grab those and transform them into a format
                   ;;which make-form can use for display.
                   :errors-and-defaults (create-errors-defaults-map
                                         {:city "Something"
                                          :description
                                          "These can come from a db or another stateful place."
                                          :car "honda"
                                          :languages ["german" "french"]
                                          :color "yellow"})
                   :fields [{:type "text"
                             :name "nickname"
                             :label "Nick Name"
                             :size "input-large"}
                            {:type "custom"
                             :html-nodes [{:tag :p
                                           :content
                                           "Some content you might want to put into your form, but not a standard form field."}]}
                            {:type "checkbox"
                             :name "waiver"
                             :label "Waiver"
                             :inputs [["checked" "I agree"]]}
                            {:type "password"
                             :name "password"
                             :label "Password"}
                            {:type "hidden"
                             :name "myhiddenfield"
                             :value "hiddenvalue"}
                            {:type "text"
                             :name "city"
                             :label "City"
                             :placeholder "Placeholder!"}
                            {:type "inline-fields"
                             :name "myfields"
                             :label "FieldsLabel"
                             :columns [{:name "hiddenfield"
                                        :type "hidden"
                                        :value "123"}
                                       {:name "firstthing"
                                        :type "text"
                                        :placeholder "A"}
                                       {:type "button"
                                        :class "btn"
                                        :name "abutton"
                                        :onclick "add('something')"
                                        :value "Do something!"}
                                       {:name "firstcustomthing"
                                        :type "custom"
                                        :html-nodes {:tag :p
                                                     :attrs {:style "display:inline"}
                                                     :content ["Sometext"]}}
                                       {:name "secondthing"
                                        :type "text"
                                        :style "display: none;"
                                        :placeholder "B"}]}
                            {:type "text-area"
                             :name "description"
                             :label "Favorite Quote"}
                            {:type "text"
                             :name "typeahead-test"
                             :label "Typeahead JS"
                             :custom-attrs [:data-provide "typeahead"
                                            :data-source (json/write-str
                                                          ["Alabama" "Alaska" "Arizona"
                                                           "Arkansas"])]}
                            ;;<input value="Add Exercise" onclick="add('time')" class="btn" type="button">
                            {:type "button"
                             :class "btn"
                             :name "abutton"
                             :onclick "add('something')"
                             :value "Do something!"}
                            {:type "select"
                             :name "color"
                             :label "Favorite Color"
                             :inputs [["blue" "Blue"]
                                      ["red" "Red"]
                                      ["yellow" "Yellow"]]}
                            {:type "select"
                             :name "numbers"
                             :label "Favorite Numbers"
                             :custom-inputs [["One" {:value "1" :class "first"}]
                                             ["Two" {:value "2" :class "second"}]
                                             ["Three" {:value "3" :class "third"}]]}
                            {:type "radio"
                             :name "car"
                             :label "Favorite Car"
                             :custom-inputs [["Honda" {:value "honda" :class "Japan"}]
                                             ["Toyota" {:value "toyota" :class "Japan"}]
                                             ["Chevy" {:value "chevy" :class "USA"}]]}
                            {:type "checkbox"
                             :name "languages[]"
                             :label "Languages"
                             :inputs [["german" "German"]
                                      ["french" "French"]
                                      ["english" "English"]]}])
                  "Example One"
                  "How to use make-form"]]}))

(post-helper :post-url "/someaction"
             :validator (v/build-validator (v/non-empty-string :nickname))
             :on-success (fn [m]
                           ;;on success actions here
                           (println "Successful validation. Your form
                           map: " m)
                           (response/redirect "/"))
             :on-failure (fn [m]
                           ;;on success actions here
                           (println "Failed validation. Your form
                           map: " m)
                           (response/redirect "/make-form")))

(defn email-valid?
  [{:keys [email] :as m}]
  (if (= email "blah")
    (v/add-validation-error m :email "Your email cannot be 'blah'!")
    m))

(defn color-selected
  "A sample validator to test if a color has been checked."
  [{:keys [colors] :as m}]
  (if (nil? colors)
    (v/add-validation-error m :colors "Please select a color!")
    m))

(defn gender-selected
  "A sample validator to test if a gender has been selected"
  [{:keys [gender] :as m}]
  (if (nil? gender)
    (v/add-validation-error m :gender "Please select a gender!")
    m))

;;Form Helper Example
(form-helper helper-example
             :validator (v/build-validator (v/non-empty-string :first-name)
                                           (v/non-empty-string :last-name)
                                           (v/non-nil :gender)
                                           (color-selected)
                                           (email-valid?))
             :post-url "/some-post-url"
             :submit-label "Sign Up!"
             :enctype "multipart/form-data"
             :fields [{:name "first-name"
                       :label "First Name"
                       :help-inline "This is 'help-inline' for the first name field."
                       :type "text"}
                      {:name "last-name"
                       :help-block "This is 'help-block' for the last
                       name field. You can fit more stuff like this."
                       :label "Last Name"
                       :type "text"}
                      {:type "button"
                       :class "btn"
                       :name "abutton"
                       :onclick "add('something')"
                       :value "Do something!"}
                      {:type "custom"
                       :html-nodes [{:tag :p
                                     :content
                                     "Some content you might want to put into your form, but not a standard form field."}]}
                      {:name "gender"
                       :label "Gender"
                       :type "radio"
                       :inputs [["male" "Male"]
                                ["female" "Female"]]}
                      {:name "options"
                       :label "Options"
                       :type "select"
                       :inputs [["option1" "Option 1"]
                                ["option2" "Option 2"]
                                ["option3" "Option 3"]]}
                      {:name "email"
                       :label "Email Address"
                       :type "text"
                       :placeholder "Try using 'blah'"}
                      {:type "inline-fields"
                       :name "birthday"
                       :label "Birthday"
                       :columns [{:name "birthday-day"
                                  :type "select"
                                  :size "input-small"
                                  :inputs (let [days (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (range 1 32))]
                                            (insert days 0 ["" "Day"]))}
                                 {:name "birthday-month"
                                  :type "select"
                                  :size "input-small"
                                  :inputs (let [days (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (range 1 13))]
                                            (insert days 0 ["" "Month"]))}
                                 {:name "birthday-year"
                                  :type "select"
                                  :size "input-small"
                                  :inputs (let [year (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (reverse
                                                                 (range 1900 2013)))]
                                            (insert year 0 ["" "Year"]))}]}
                      {:name "colors[]"
                       :label "Favorite Colors"
                       :type "checkbox"
                       :inputs [["blue" "Blue"]
                                ["red" "Red"]
                                ["yellow" "Yellow"]
                                ["green" "Green"]]
                       :note "Pick 2 of the above colors!"}
                      {:name "username"
                       :label "Username"
                       :type "text"}
                      {:name "password"
                       :label "Password"
                       :type "password"}]
             :on-success (fn [{uname :username :as user-map}]
                           ;;on success actions here
                           (println "Successful validation. Your form
                           map: " user-map)
                           (response/redirect "/"))
             :on-failure (fn [form-data]
                           ;;some failure action here
                           (println "failed, form data: " form-data)
                           (println "flash: " (session/flash-get :form-data))
                           (response/redirect "/form-helper")))

;;This example shows how to access the entire request map. Typically
;;we just use 'm' from below, which is just the form params portion of
;;the map. Thats what you can pass in to your form function (ie
;;'helper-example') to populate default values in case of an
;;error. Alternatively, you could use a map with default values from a
;;database or some other data source to prepopulate your form.
(defpage "/form-helper"
  {:as m}
 ;; (println "form-helper m: " m)
;;  (println "flash: " (session/flash-get :form-data))
  (fn [req]
  ;;   (println "Request map: " req)
    (let [default-values {:username "zoey" :birthday-day 12 :first-name 12345
                          :colors ["red" "blue"]}]
      (test-layout
       {:form-tests
        [[(helper-example default-values "some-post-url" "/")
          "Form-helper Example"
          "Uses the form-helper macro for easy validation."]]}))))

(form-helper helper-example-user
             :validator (v/build-validator (v/non-empty-string :first-name)
                                           (v/non-empty-string :last-name)
                                           (email-valid?))
             :post-url "/:user/action"
             :submit-label "Sign Up!"
             :enctype "multipart/form-data"
             :fields [{:name "first-name"
                       :label "First Name"
                       :help-inline "This is 'help-inline' for the first name field."
                       :type "text"}
                      {:name "last-name"
                       :help-block "This is 'help-block' for the last
                       name field. You can fit more stuff like this."
                       :label "Last Name"
                       :type "text"}
                      {:type "button"
                       :class "btn"
                       :name "abutton"
                       :onclick "add('something')"
                       :value "Do something!"}
                      {:name "gender"
                       :label "Gender"
                       :type "radio"
                       :inputs [["male" "Male"]
                                ["female" "Female"]]}
                      {:name "options"
                       :label "Options"
                       :type "select"
                       :inputs [["option1" "Option 1"]
                                ["option2" "Option 2"]
                                ["option3" "Option 3"]]}
                      {:name "email"
                       :label "Email Address"
                       :type "text"
                       :placeholder "Try using 'blah'"}
                      {:type "inline-fields"
                       :name "birthday"
                       :label "Birthday"
                       :columns [{:name "birthday-day"
                                  :type "select"
                                  :size "input-small"
                                  :inputs (let [days (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (range 1 32))]
                                            (insert days 0 ["" "Day"]))}
                                 {:name "birthday-month"
                                  :type "select"
                                  :size "input-small"
                                  :inputs (let [days (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (range 1 13))]
                                            (insert days 0 ["" "Month"]))}
                                 {:name "birthday-year"
                                  :type "select"
                                  :size "input-small"
                                  :inputs (let [year (reduce #(conj %1 [(str %2) (str %2)])
                                                             [] (reverse
                                                                 (range 1900 2013)))]
                                            (insert year 0 ["" "Year"]))}]}
                      {:name "colors[]"
                       :label "Favorite Colors"
                       :type "checkbox"
                       :inputs [["blue" "Blue"]
                                ["red" "Red"]
                                ["yellow" "Yellow"]
                                ["green" "Green"]]
                       :note "Pick 2 of the above colors!"}
                      {:type "file-input"
                       :name "pic"
                       :label "Choose a pic"}
                      {:name "username"
                       :label "Username"
                       :type "text"}
                      {:name "password"
                       :label "Password"
                       :type "password"}]
             :on-success (fn [{uname :username :as user-map}]
                           ;;on success actions here
                           (println "Successful validation. Your form
                           map: " user-map)
                           (response/redirect "/"))
             :on-failure (fn [{:keys [user] :as form-data}]
                           ;;some failure action here
                           (println "failed, form data: " form-data)
                           (println "flash: " (session/flash-get :form-data))
                           (response/redirect (str "/form-helper/" user))))

(defpage "/form-helper/:user"
  {:keys [user] :as m}
  (let [default-values {:username user :birthday-day 12 :gender "male" :first-name 12345
                        :colors ["red" "blue"]}]
    (test-layout
     {:form-tests
      [[(helper-example-user default-values (str "/" user "/action") "/")
        "Form-helper Example"
        "Uses the form-helper macro for easy validation."]]})))
