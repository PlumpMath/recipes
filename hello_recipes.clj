(ns hello-recipes
  "Say hello to recipes. (also, a playground.)"
  (:use recipes.db)
  (:use korma.core)
  (:require [clojure.repl :as repl]))

(defdb dev (sqlite3 {:db "recipes.sqlite3"}))

(select recipes
        (with ingredients
          (fields :name :ingredientsinrecipes.quantity)))

(find-by :name [= "Oregano"] ingredients)
(find-by :name [like "%Tomate%"] ingredients)

(select recipes
        (with ingredients
          (fields :name :ingredientsinrecipes.quantity)
          (where (and {:name [like "%Tomate%"]}
          					  {:name [= "Spaghetti"]})))
        (order :id :DESC))

; select recipes containing a list of ingredients
;; select rid, iid, iname
;; from (select r.id as rid, i.id as iid, i.name as iname
;;       from recipes r, ingredientsinrecipes iir, ingredients i
;;       where r.id = iir.recipes_id and i.id = iir.ingredients_id)
;; where 2 in (select ingredients_id
;;             from ingredientsinrecipes
;;             where recipes_id = rid);
(def recipe-ingredients
  (subselect ingredients
             (fields :id)
             (where {:ingredientsinrecipes.ingredients_id [= :id]})))

(println (sql-only (select recipes
        (fields :title)
        (join ingredientsinrecipes (= :ingredientsinrecipes.recipes_id :id))
        (where (and {1 [in recipe-ingredients]}
                   {2 [in recipe-ingredients]})))))


(map :iname (exec-raw ["select iname
            from (select r.id as rid, i.id as iid, i.name as iname
                  from recipes r, ingredientsinrecipes iir, ingredients i
                  where r.id = iir.recipes_id and i.id = iir.ingredients_id),
                 recipes rc
            where rc.id = rid
               and (2 in (select ingredients_id
                          from ingredientsinrecipes
                          where recipes_id = rid)
                    and 1 in (select ingredients_id
                             from ingredientsinrecipes
                             where recipes_id = rid))"] :results))
(repl/doc exec-raw)

(remove-all-data)

(last-inserted recipes)

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
                :ingredients [{:name "Paprika", :quantity ""}
                              {:name "Zwiebeln", :quantity ""}
                              {:name "Räuchertofu", :quantity ""}
                              {:name "optional: Zuchini, Chili, Kidneybohnen", :quantity ""}]})
