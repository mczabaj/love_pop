(ns love-pop.routes.home
  (:require [love-pop.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render "home.html"))

(defroutes home-routes
  (GET "/" []
       (home-page))
  (GET "/docs" []
       (-> (response/ok (-> "README.md" slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8")))

  (GET "/sleep" [:as request]
       (let [params (:params request)
             s      (:seconds params)]
         (Thread/sleep (* 1000 (Integer/parseInt s)))
         (response/ok s))))
