(ns hello-recipes
  "Say hello to recipes. (also, a playground.)"
  (:use recipes.db)
  (:use korma.core)
  (:require [clojure.repl :as repl]))

(defdb dev (sqlite3 {:db "recipes.sqlite3"}))

(select recipes
        (with ingredients
          (fields :name :ingredientsinrecipes.quantity)))

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
                :ingredients ["Paprika"
                              "Zwiebeln"
                              "Räuchertofu"
                              "optional: Zuchini, Chili, Kidneybohnen"]})
