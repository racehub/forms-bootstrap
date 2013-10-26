(ns forms-bootstrap.util
  (:require [clojure.string :as s]))

(defn remove-spaces
  "Remove spaces from a string"
  [input]
  (s/replace input " " ""))

(defn string-contains?
  "Takes in a string and any number of search words. Returns true if
  all of the search words can be found in the input string. "
  [sentence & search-words]
  (let [sent-freq (frequencies (re-seq #"[^\s]+" sentence))
        matches (filter true?
                        (map #(contains? sent-freq %1) search-words))]
    (= (count matches) (count search-words))))

(defn first-word
  "Takes an input string and returns the first word in it"
  [x]
  (clojure.core/get (s/split x #"\s") 0))

(defn collectify
  "Takes in one argument, and returns it if its already a vector,
  otherwise it makes it into one."
  [x]
  (if (sequential? x) x [x]))

(defn insert
  "Takes in a vector, a position, and a value. Returns a new vector
  with the value inserted at the given position."
  [v pos val]
  (apply conj (subvec v 0 pos) val (subvec v pos)))
