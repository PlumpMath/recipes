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

(def attributes
  [:name :description
   :difficulty :region :category :season
   :ingredients])

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

   (def-entity ::ingredients :ref :many)
   (def-entity ::ingredient-name :string :one
     :db/unique :db.unique/value)
   (def-entity ::ingredient-quantity :string :one)])

(defn prepare-schema [schema]
  (map (fn [entity]
         (into entity {:db/id (d/tempid :db.part/db)
                       :db.install/_attribute :db.part/db}))
       schema))

(defn mk-ingredient [name quantity]
  {:db/id (d/tempid :db.part/user)
   ::ingredient-name name
   ::ingredient-quantity quantity})

(defn mk-recipe [recipe]
  (let [ingredients (mapv (partial apply mk-ingredient)
                         (or (::ingredients recipe) []))
        ingredient-ids (map :db/id ingredients)
        attributes (remove (fn [[k v]] (= k ::ingredients)) recipe)]
    (conj ingredients
          (into {:db/id (d/tempid :db.part/user)
                 ::ingredients ingredient-ids}
                attributes))))

(defn transact-test-data! [conn]
  (d/transact conn (prepare-schema schema))
  (d/transact conn [{:db/id (d/tempid :db.part/user)
                     ::name "Hello World"
                     ::description "Just do it!"
                     ::category "scary"}
                    {:db/id (d/tempid :db.part/user)
                     ::name "Helo Somebody"
                     ::description "I want to snuggle with you"}
                    {:db/id (d/tempid :db.part/user)
                     ::name "Apple pie"
                     ::description "Key talent. Might be inedible"}])
  (d/transact conn (mk-recipe {::name "real pie"
                               ::description "try it, it's real!"
                               ::category "experimental"
                               ::ingredients [["clojure" "a bit"]
                                              ["datomic" "more"]
                                              ["duct tape" "lots"]
                                              ["fun" "quite some"]]})))
