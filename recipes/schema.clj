(ns recipes.schema
  (:require [datomic.api :as d]))

(defn resolve [kw ns]
  (if (namespace kw)
    kw
    (keyword (name ns) (name kw))))

(defn def-entity [id type cardinality & opts]
  (let [type (resolve type :db.type)
        cardinality (resolve cardinality :db.cardinality)]
    (into {:db/ident id
           :db/valueType type
           :db/cardinality cardinality}
          (apply hash-map opts))))

(defn def-enum [enum-name vals]
  (let [ns (str (namespace enum-name) "." (name enum-name))]
    (apply conj [(def-entity enum-name :ref :one
                   :db/id (d/tempid :db.part/db))]
           (map (fn [val]
                  {:db/id (d/tempid :db.part/user)
                   :db/ident (keyword ns (name val))})
                vals))))

(def schema
  [(def-entity ::name :string :one
     :db/unique :db.unique/value
     :db/fulltext true)
   (def-entity ::description :string :one
     :db/fulltext true)
   (def-entity ::difficulty :keyword :one)
   (def-entity ::region :string :one)
   (def-entity ::category :string :one)
   (def-entity ::saison :string :one)

   (def-entity ::ingredient :ref :one)
   (def-entity ::ingredient-name :string :one)
   (def-entity ::ingredient-quanity :string :one)

   (def-entity ::uses :ref :many)])

(defn prepare-schema [schema]
  (map (fn [entity]
         (into entity {:db/id (d/tempid :db.part/db)
                       :db.install/_attribute :db.part/db}))
       schema))
