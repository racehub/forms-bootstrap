(ns forms-bootstrap.validation
  "Code copied out of the old Sandbar library.")

(defn append-to-keyword [k s]
  (keyword (str (name k) s)))

(defn property-lookup
  "Return the value of the key in the passed map or the name of the key."
  [p k]
  (k p (name k)))

;; Update this function to use ::validation-errors as a key and then
;; create a get-validation-errors function to get the errors for a
;; map. You may also want to put validation errors in the map's
;; metadata.

(defmacro if-valid [validator m success failure]
  `(let [result# (~validator ~m)]
     (if (= ~m result#)
       (~success ~m)
       (let [errors# (:_validation-errors result#)]
         (~failure (dissoc ~m :_validation-errors) errors#)))))

(defn add-validation-error
  "Add an error message to a map of data. msg may be a string or a map."
  ([m msg] (add-validation-error m :form msg))
  ([m k msg]
     (let [msg (if (map? msg)
                 ((keyword (str (name k) "-validation-error"))
                  msg)
                 (str msg))]
       (assoc m :_validation-errors
              (merge-with (fn [a b] (vec (concat a b)))
                          (:_validation-errors m) {k [msg]})))))

;;
;; The following two functions and the macro build-validator allow you
;; to write the following validator:
;;
;; (defn login-validator []
;;   (fn [m]
;;     (let [v (-> m
;;                 (non-empty-string :username)
;;                 (non-empty-string :password))]
;;       (if (= m v)
;;         (-> m
;;             password-validator)
;;         v))))
;;
;; as
;;
;; (defn login-validator []
;;  (build-validator (non-empty-string :username)
;;                   (non-empty-string :password)
;;                   :ensure
;;                   password-validator))
;;
;; which allows you to create complex validators without having to
;; think about all of the control stuff. You may have any number of
;; :ensure keywords.
;;
;; The :ensure keyword means that everything before it must be valid,
;; if not it will return with only the current errors.
;;

(defn- group-validators [coll]
  (loop [result []
         next coll]
    (if (some #(= % :ensure) next)
      (let [v (split-with #(not (= % :ensure)) next)]
        (recur (conj result (first v)) (rest (first (rest v)))))
      (conj result next))))

(defn- build-validator-control [m- groups]
  (if (> (count groups) 1)
    (let [v- (gensym "v_")]
      (list 'clojure.core/let [v- (build-validator-control m- [(first groups)])]
            (list 'if (list 'clojure.core/= m- v-)
                  (build-validator-control m- (rest groups))
                  v-)))
    (cons 'clojure.core/-> (cons m- (first groups)))))

(defmacro build-validator [& validators]
  (let [v (group-validators validators)]
    (let [m- (gensym "m_")]
      (list 'clojure.core/fn [m-]
            (build-validator-control m- v)))))

;; Update this function to use ::validation-errors as a key and then
;; create a get-validation-errors function to get the errors for a
;; map. You may also want to put validation errors in the map's
;; metadata.

(defn add-validation-error
  "Add an error message to a map of data. msg may be a string or a map."
  ([m msg] (add-validation-error m :form msg))
  ([m k msg]
     (let [msg (if (map? msg)
                 ((keyword (str (name k) "-validation-error"))
                  msg)
                 (str msg))]
       (assoc m :_validation-errors
              (merge-with (fn [a b] (vec (concat a b)))
                          (:_validation-errors m) {k [msg]})))))

;;
;; Validators
;; ==========

(defn multi-value-validator
  "Validate multiple values in a map. args is a list of keys possibly ending
   with a message string or map. v-fn is the validation function which is a
   function of the key to validate. default-message is either a string or a
   function of the message map and key which returns a string."
  [m args v-fn default-message]
  (let [key-seq (take-while keyword? args)
        msg (last args)
        msg (if (keyword? msg) {} msg)]
    (loop [key-seq key-seq
           m m]
      (if (empty? key-seq)
        m
        (let [k (first key-seq)]
          (if (v-fn k)
            (recur (rest key-seq) m)
            (recur (rest key-seq)
                   (add-validation-error
                    m
                    k
                    (if (map? msg)
                      (if-let [custom-error-message
                               (get msg (append-to-keyword
                                         k
                                         "-validation-error"))]
                        custom-error-message
                        (if (fn? default-message)
                          (default-message k msg)
                          default-message))
                      msg)))))))))

(defn non-empty-string [m & args]
  (multi-value-validator m args
                         (fn [k]
                           (let [value (get m k)]
                             (and (string? value)
                                  (not (empty? value)))))
                         (fn [k msg]
                           (str (property-lookup msg k) " cannot be blank!"))))

(defn non-nil [m & args]
  (multi-value-validator m args
                         (fn [k]
                           (let [value (get m k)]
                             (not (nil? value))))
                         (fn [k msg]
                           (str (property-lookup msg k) " cannot be nil!"))))

(defn integer-number [m & args]
  (multi-value-validator m args
                         (fn [k]
                           (let [value (get m k)]
                             (integer? value)))
                         (fn [k msg]
                           (str (property-lookup msg k)
                                " must be an integer number!"))))

(defn zero-or-more-maps [m k & options]
  (let [vfn (first (filter fn? options))
        msg (first (filter #(not (fn? %)) options))
        element-validator (or (fn [m] (= (vfn m) m))
                              map?)
        values (get m k)]
    (if (and (coll? values)
             (every? true? (map element-validator values)))
      m
      (add-validation-error m k (str (property-lookup msg k)
                                     " must be empty or contain maps!")))))

(defn one-or-more-maps [m k & options]
  (let [result (apply zero-or-more-maps m k options)
        msg (first (filter #(not (fn? %)) options))]
    (if (and (= result m)
             (> (count (get m k)) 0))
      m
      (add-validation-error m k (str (property-lookup msg k)
                                     " must be a list of maps!")))))

(defn validation-errors [m]
  (:_validation-errors m))

(defn required-fields [validator]
  (keys (validation-errors (validator {}))))
