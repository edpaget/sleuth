(ns sleuth.events
  (:use compojure.core)
  (:require [monger.collection :as mc])
  (:import [org.bson.types ObjectId]))

(defn create!
  [event]
  (mc/insert "Events" event))

(defroutes raw-routes
  (POST "/" {params :params} 
        (do (future (create! (map #(merge % {:site (:site params)})
                                  (:logs params))))
            {:status 201
             :headers {"Content-Type" "text/plain"}
             :body "5000"})))
