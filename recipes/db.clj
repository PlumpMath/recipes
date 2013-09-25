(ns recipes.db
  (:use korma.db)
  (:use korma.core))

(defentity ingredientsinrecipes)

(defentity ingredients
  (fields :name))

(defentity recipes
  ; `join-table` must be a keyword! (otherwise (name join-table) says: ClassCastException or NPE
  (many-to-many ingredients :ingredientsinrecipes))

(defn remove-all-data
  "Remove all recipe data (including ingredients)."
  []
  (delete recipes)
  (delete ingredients)
  (delete ingredientsinrecipes))

(defn last-inserted
  "Returns the id of the last inserted row in `table`."
    [table]
  (-> (select table
              (aggregate (max :id) :id))
      first
      :id
      (or 0)))

(def ^{:private true} last-rowid
  (keyword "last_insert_rowid()"))

(defn- ingredient-for-recipe
  "Adds ingredient to the recipe with the given id, creating it if needed."
  [{:keys [name quantity]} recipe-id]
  (let [iid (:id (first (select ingredients
                                (where {:name name}))))
        iid (if (nil? iid)
              (last-rowid (insert ingredients
                                  (values {:id (inc (last-inserted ingredients)),
                                                   :name name})))
              iid)]
    (insert ingredientsinrecipes (values {:recipes_id recipe-id,
                                          :ingredients_id iid,
                                          :quantity quantity}))))

(defn create-recipe
  "Create a recipe with the given ingredients."
  [{req-ingredients :ingredients, :keys [title description]}]
  (let [id (last-rowid (insert recipes
                               (values {:id (inc (last-inserted recipes)),
                                        :title title,
                                        :description description })))]
    (doall
      (for [igrd req-ingredients]
        (ingredient-for-recipe igrd id)))
    id))

(defmacro find-by ; has to be a macro so that condition is not evaluated
  								; otherwise, one would have to quote it every time
  "Find entries in `table` that have an `attr` matching `condition`."
  [attr condition table]
  (select table
          (where {attr condition})))