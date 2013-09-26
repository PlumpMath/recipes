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

(defn find-all [db]
  (map #(d/entity db (first %))
       (d/q '[:find ?id
              :where [?id ::r/name _]] db)))

(defn find-by [attr val db]
  (d/q `[:find ?id
         :where [?id ~attr ~val]] db))

(defn find-first-by [attr val db]
  (d/entity db (ffirst (find-by attr val db))))

(defn with-fields [fields entity]
  (doseq [field fields]
    (get entity field))
  entity)

(defn summarize [recipe]
  (with-fields [::r/name ::r/description] recipe))

(defn full [recipe]
  (with-fields [::r/name ::r/description ::r/difficulty ::r/region
                ::r/category ::r/saison ::r/uses]
    recipe))
