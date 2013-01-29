(ns hello-korma
  "Say hello to korma"
  (:use korma.db)
  (:use korma.core)
  (:require [clojure.repl :as repl]))

(defdb dev (sqlite3 {:db "recipes.sqlite3"}))

(defentity ingredientsinrecipes)

(defentity ingredients
  (fields :name))

(defentity recipes
  ; `join-table` must be a keyword! (otherwise (name join-table) says: ClassCastException or NPE
  (many-to-many ingredients :ingredientsinrecipes))

(select ingredients)

(select recipes
        (with ingredients
          (fields :name :ingredientsinrecipes.quantity)))

(defn remove-data
  []
  (delete recipes)
  (delete ingredients)
  (delete ingredientsinrecipes))
(remove-data)

(defn last-inserted
    [table]
  (-> (select table
              (aggregate (max :id) :id))
      first
      :id
      (or 0)))
(last-inserted recipes)

(defn- ingredient-for-recipe
  "Adds ingredient to the recipe with the given id, creating it if needed."
  [{:keys [name quantity]} recipe-id]
  (let [iid (:id (first (select ingredients
                                (where {:name name}))))
        iid (if (nil? iid)
              (last_rowid (insert ingredients
                                  (values {:id (inc (last-inserted ingredients)),
                                                   :name name})))
              iid)]
    (insert ingredientsinrecipes (values {:recipes_id recipe-id,
                                          :ingredients_id iid,
                                          :quantity quantity}))))
(defn create-recipe
  "Create a recipe with the given ingredients."
  [{req-ingredients :ingredients, :keys [title description]}]
  (let [last_rowid (keyword "last_insert_rowid()")
        id (last_rowid (insert recipes
          				 (values {:id (inc (last-inserted recipes)),
                            :title title,
                            :description description })))]
    (doall
      (for [igrd req-ingredients]
        (ingredient-for-recipe igrd id)))
    id))

(create-recipe {:title "Funny thing",
                :description "haha",
                :ingredients [{:name "heya", :quantity "1g"},
                              {:name "whatever", :quantity "1µg"}]})

(create-recipe {:title "Spaghetti a la Lu",
                :description "...",
                :ingredients [{:name "Tomaten (geschält)",
                               :quantity "1 Dose/400g"},
                              {:name "Zwiebeln",
                               :quantity "5 Stück"},
                              {:name "Knoblauch",
                               :quantity "2 Zehen (mindestens :)"},
                              {:name "Oregano",
                               :quantity "2-4EL"},
                              {:name "Sojasauce",
                               :quantity "3EL"},
                              {:name "Spaghetti",
                               :quantity "1 Pkg. (500g)"}]})
(create-recipe {:title "Chili con Tofu",
                :description "...",
                :ingredients ["Paprika"
                              "Zwiebeln"
                              "Räuchertofu"
                              "optional: Zuchini, Chili, Kidneybohnen"]})
