(ns recipes.server
  (:use compojure.core)
  (:require [compojure.route :as route])
  (:require [compojure.handler :as handler])
  (:use [ring.adapter.jetty]
        [ring.util.response]
        [ring.middleware.params :only (wrap-params)])
  (:require [cheshire.core :as json])

  (:require [recipes.schema :as r])
  (:require [recipes.api :as q]))

(def db (q/in-mem-db))

(defn render-map [m & ks]
  (json/generate-string (select-keys m (or ks (keys m)))
                        {:key-fn name}))

(defn params-to-schema [q]
  (into {} (map (fn [[k v]]
                  [(r/resolve k :recipes.schema) v])
                (select-keys q r/attributes))))

(def docs
  {"/mantra" "Wisdom"
   "/mantras" "More wisdom"
   "/docs" "Practical wisdom (e.g. this page)"
   "/" "All recipes"
   "/?key=val" {:doc "Searching for recipes"
                :keys r/attributes
                :returns "A collection of recipes that satisfy all criteria"}
   "/:name" {:doc "Retrieve a recipe by name"
             :comment "Same as `/?name=:name` but returns it directly"
             :returns "One recipe, or an error if not found"}})

(defroutes app-routes
  (GET "/mantra" []
       (-> (response (q/mantra))
           (content-type "text/plain")))
  (GET "/mantras" []
       (-> (response (json/generate-string q/mantras))
           (content-type "application/json")))

  (GET "/docs" []
       (-> (response (json/generate-string (into (sorted-map) docs)
                                           {:pretty true}))
           (content-type "application/json")))

  (GET "/" [& q]
       (let [query (params-to-schema q)
             recipes (if (empty? query)
                       (q/find-all db)
                       (-> (q/find-by-many query db)
                            (q/as-entities db)))]
         (-> (response (json/generate-string (map q/summarize recipes) {:key-fn name}))
             (content-type "application/json"))))
  (GET "/:name" [name]
       (let [recipe (q/find-first-by ::r/name name db)
             json (if (nil? recipe)
               (render-map {:error "not found"})
               (render-map (q/full recipe)))]
         (-> (response json)
             (content-type "application/json"))))
  (route/not-found "oops"))

(def app
  (-> app-routes
      handler/api))

(defn main []
  (run-jetty app {:port 8080}))
