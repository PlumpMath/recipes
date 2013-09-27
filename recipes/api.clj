(ns recipes.api
  (:require [datomic.api :as d])
  (:require [recipes.schema :as r]))

(defn in-mem-conn []
  (let [db-name "datomic:mem://recipes"]
    (d/create-database db-name)
    (d/connect db-name)))

(defn in-mem-db []
  (let [conn (in-mem-conn)]
    (r/transact-test-data! conn)
    (d/db conn)))

(defn as-entities [res db]
  (map (comp #(d/entity db %) first) res))

(defn find-all [db]
  (as-entities
       (d/q '[:find ?id
              :where [?id ::r/name _]] db)
       db))

(defn find-by [attr val db]
  (d/q `[:find ?id
         :where [?id ~attr ~val]] db))

(defn find-first-by [attr val db]
  (d/entity db (ffirst (find-by attr val db))))

(defn nested-coll? [obj]
  (and (coll? obj) (coll? (first obj))))

(defn flat-map [f coll]
  (reduce (fn [cur el]
            (let [el (f el)]
              (if (nested-coll? el)
                (concat cur el)
                (concat cur (list el))))) '() coll))

(defn find-by-many [m db]
  (let [clauses (flat-map (fn [[k v]]
                       (if (vector? v)
                         (map #(vector '?id k %) v)
                         ['?id k v]))
                     m)]
    (d/q `[:find ~'?id
           :where ~@clauses] db)))

(defn summarize [recipe]
  (select-keys recipe [::r/name ::r/description]))

(defn full [recipe]
  (select-keys recipe (map #(r/resolve % :recipes.schema) r/attributes)))

(def mantras
  ["Fake chocolate is better than no chocolate."
   "If in doubt, an apple cake is always a great present."
   "Don't eat late, it will make you baaad."])

(defn mantra []
  (rand-nth mantras))
