(ns oph.form.validation
  (:use [clojure.tools.trace :only [trace]])
  (:require [clojure.string :as string])
  )

(defn validate-required [field answer]
  (if (and (= (field :required) true) (string/blank? answer))
    [{:error "required"}]
    []))

(defn validate-options [field answer]
  (if (and (not (string/blank? answer)) (> (count (field :options)) 0) (not-any? #{answer} (map (fn [option] (option :value)) (field :options))))
    [{:error "invalid-option"}]
    []))

(defn validate-textarea-maxlength [field answer]
  (let [maxlength ((get field :params {}) :maxlength)]
    (if (and (= (field :displayAs) "textArea") (> (count answer) maxlength))
      [{:error "maxlength", :max maxlength}]
      [])))

(defn validate-texfield-maxlength [field answer]
  (let [maxlength ((get field :params {}) :maxlength)]
    (if (and (= (field :displayAs) "textField") (> (count answer) maxlength))
      [{:error "maxlength", :max maxlength}]
      [])))

(defn find-value-for-key [values key]
  (if (some #(= key (:key %)) values)
    (first (filter #(= key (:key %)) values))
    (let [values-that-are-maps (filter map? values)
          nested-map-values (map #(:value %) values-that-are-maps)
          values-that-are-seqs (filter coll? nested-map-values)
          nested-seq-values (mapcat #(:value %) (flatten values-that-are-seqs))
          all-internal-values (concat nested-map-values nested-seq-values)]
      (if (empty? all-internal-values)
        nil
        (find-value-for-key all-internal-values key)))))

(defn find-answer-value [answers key]
  ( let [found-record (find-value-for-key (answers :value) key)]
    (if (nil? found-record)
      nil
      (:value found-record))))

(defn validate-field-security [answers field]
  (let [answer (find-answer-value answers (field :id))]
    {(keyword (field :id)) (concat
       (validate-options field answer)
       (validate-textarea-maxlength field answer)
       (validate-texfield-maxlength field answer))}))

(defn validate-field [answers field]
  (let [answer (find-answer-value answers (field :id))]
    {(keyword (field :id)) (concat
       (validate-required field answer)
       (validate-options field answer)
       (validate-textarea-maxlength field answer)
       (validate-texfield-maxlength field answer))}))

(defn- is-form-field? [field]
  (= (:type field) "formField"))

(defn- is-wrapper-element? [field]
  (= (:type field) "wrapperElement"))

(defn unwrap-answers [answers]
  (let [map-fields (filter map? (vals answers))]
    (if (empty? map-fields)
      answers
      (into answers (map unwrap-answers map-fields)))))

(declare flatten-elements)

(defn unwrap-node [node]
  (if (is-wrapper-element? node)
    (list* node (flatten-elements (:children node)))
    node))

(defn flatten-elements [node-list]
  (->> node-list
     (map unwrap-node)
     flatten))

(defn find-fields [node-list]
  (->> (flatten-elements node-list)
    (filter is-form-field?)))

(defn validate-form-security [form answers]
  (let [validator (partial validate-field-security answers)]
    (->> (find-fields (:content form))
         (map validator)
         (into {}))))

(defn validate-form [form answers]
  (let [validator (partial validate-field answers)]
    (->> (find-fields (:content form))
       (map validator)
       (into {}))))