(ns recipes.schema)

(defn resolve [kw ns]
  (if (namespace kw)
    kw
    (keyword (name ns) (name kw))))

(defn def-entity [id type cardinality & opts]
  (let [type (resolve type :db.type)
        cardinality (resolve cardinality :db.cardinality)]
    (into {:db.ident id
           :db.valueType type
           :db.cardinality cardinality}
          (apply hash-map opts))))

(def schema
  [(def-entity ::name :string :one
     :db.unique :db.unique/value
     :db.fulltext true)
   (def-entity ::description :string :one
     :db.fulltext true)
   (def-entity ::difficulty :keyword :one)
   (def-entity ::region :string :one)
   (def-entity ::category :string :one)
   (def-entity ::saison :string :one)

   (def-entity ::ingredient :ref :one)
   (def-entity ::ingredient-name :string :one)
   (def-entity ::ingredient-quanity :string :one)

   (def-entity ::uses :ref :many)])
