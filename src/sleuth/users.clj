(ns sleuth.users
  (:use compojure.core
        monger.operators
        sleuth.util) 
  (:require [monger.collections :as mc]
            [validateur.validation :as v])
  (:import [ord.bson.types ObjectId]))

(def get-user
  [id]
  (mc/find-map-by-id "users" id))

(defroutes user-routes
  (GET '/' [] ))
