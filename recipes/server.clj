(ns recipes.server
  (:use compojure.core)
  (:require [compojure.route :as route])
  (:use ring.adapter.jetty)
  (:use ring.util.response)
  (:require [cheshire.core :as json])

  (:require [recipes.schema :as r])
  (:require [recipes.api :as q]))

(def db (q/in-mem-db))

(defn render-map [m & ks]
  (json/generate-string (select-keys m (or ks (keys m)))
                        {:key-fn name}))

(defn render-recipe [entity]
  (render-map entity ::r/name ::r/description))

(defroutes app
  (GET "/mantra" []
       (-> (response (q/mantra))
           (content-type "text/plain")))
  (GET "/mantras" []
       (-> (response (json/generate-string q/mantras))
           (content-type "application/json")))

  (GET "/" []
       (let [recipes (q/find-all db)
             simplified (map #(select-keys % [::r/name ::r/description]) recipes)]
         (-> (response (json/generate-string simplified {:key-fn name}))
             (content-type "application/json"))))
  (GET "/:name" [name]
       (let [recipe (q/find-first-by ::r/name name db)
             json (if (nil? recipe)
               (render-map {:error "not found"})
               (render-recipe recipe))]
         (-> (response json)
             (content-type "application/json"))))
  (route/not-found "oops"))

(defn main []
  (run-jetty app {:port 8080}))
