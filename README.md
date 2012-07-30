# Forms-Bootstrap #
Forms-Bootstrap is a utility for creating web forms in Clojure, styled using 
[Twitter's Bootstrap CSS](http://twitter.github.com/bootstrap/). It is built to be used with the 
[Noir web framework](https://github.com/noir-clojure/noir), 
[Enlive](https://github.com/cgrand/enlive) HTML templating, and validation using 
[Sandbar](https://github.com/brentonashworth/sandbar).
You can use forms-bootstrap to quickly make nicely styled forms for your web app. It is easy to validate 
your forms and display well formatted error messages next to the appropriate fields. You can also repopulate 
a form with default data from a source of your choice.

I am working on this library in my spare time, and there is much that still needs to be done!

## Usage ##
Supported forms: input (text or password), select dropdown, text area, checkboxes,
radio buttons, inline inputs (several text inputs on one line), file
input, error and success messages.

## Tests ##
Once you have cloned the project, navigate to the project directory and:
`lein run`

Open your favorite browser and go to:
`http://localhost:8080/`

## Snippets ##
We define the following snippets (see 
[Enlive](https://github.com/cgrand/enlive) Templating) for web forms: (Snippets are functions that return 
a collection of nodes. You can use them with Enlive functions such as 'content' that take a sequence of nodes 
and return a transformation function, which you can use in a 'deftemplate' to get a sequence of strings in HTML 
to use in a Noir defpage.) Sounds complicated but its actually simple to use and makes templating a cinch!

basic-form: Takes in an action, a submit button, class, encode type, and a collection of transformed content 
called fields. 
The following form field snippets are defined:

* input-field
* text-area-field
* dropdown-menu
* checkbox-or-radio
* file-input
* make-submit-button

## Form-Helper Macro ##
The form-helper macro is really convenient for making forms very quickly. You can call the macro with a 
collection of form fields, a validator function, a submit button label, a url to POST to, and functions to 
execute on successful validation and on failed validation. Form-Helper will then create a function for you 
that you can call with an action (ie users/dpetrovics/edit), a cancel link (ie: /users), and a form parameters 
map (used to pre-populate the form after a page render on failed validation, or to provide default values). 
When called, this function will return the enlive node representation of your form. You can use this Enlive map 
in a deftamplate to put the form into your desired html page, ie: (content your-form-name-here). The form-helper 
macro also creates a POST handler using Noir's defpage-- it takes in the form params, validates it, and executes 
either the on-success or on-failure function.

Each 'field' in the :fields portion of a form-helper macro call can contain: 
1. name: This assigns a name to the form element
2. type: Supported types: text, password, checkbox, radio, file. 
3. size: Supported sizes: input-mini, input-small, input-medium, input-large, input-xlarge, input-xxlarge, as well as span1, span2, span3, etc
4. label: What to display next to the form element
5. inputs: A vector of [name1 content1 name2 content2 ...]. Used by
   select, checkbox and radio.
6. rows: Defines the number of rows for a text area
7. text-area-content: A string of default text in a text area

Here is an example:

   (form-helper example-form
      :validator user/edit-validator
      :post-url "/users/:username/edit"
      :submit-label "Edit"
      :fields [{:name "first-name"
                :label "First Name"
                :type "text"}
               {:name "last-name"
                :label "Last Name"
                :type "text"}
               {:name "gender"
                :label "Gender"
                :type "radio"
                :inputs [["male" "Male"]
                         ["female" "Female"]]}
               {:name "email"
                :label "Email Address"
                :type "text"}
               {:name "username"
                :label "Username"
                :type "text"}
               {:name "password"
                :label "Password"
                :type "password"}]
      :on-success (fn [{uname :username :as user-map}]
                     (user/add! user-map)
                     (user/login! uname)
                     (session/flash-put! :flash "User created successfully.")
                     (response/redirect "/"))
      :on-failure (fn [form-data]
                     (session/flash-put! :flash "Please Fix Errors")
                     (render "/signup" form-data)))

Then to use the generated 'example-form' function:

(defpage "/users/:username/edit"
  {:keys[username] :as m}
  (your-enlive-template-here (example-form m (str "users/" username "/edit") "/users"))) 

Your Enlive template should make sure to link to Twitter Bootstrap CSS to make use of their styling.


## TO DO ##
-Should we stick with Sandbar, or move the error handling over to Noir?
-Deal with empty checkboxes, radios, or dropdowns. In this situations the name of the form field is not present in the form params map, so we need a new way of searching for errors (in create-errors-defaults-map).
-More testing.
-Formatting seems to be an issue if we bump from Twitter Bootstrap 2.0.0 to 2.0.4, figure out what's changed


## License ##

Copyright (C) 2012 David Petrovics

Distributed under the Eclipse Public License, the same as Clojure.
