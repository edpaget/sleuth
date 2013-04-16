(ns sleuth.sites
  (:use compojure.core
        sleuth.util)
  (:require [monger.collection :as mc]
            [sleuth.users :as user])
  (:import [org.bson.types ObjectId]))

(defn by-id
  [id]
  (mc/find-map-by-id "sites" id))

(defn create-or-update!
  [{:keys [user site]}]
  (let [id (ObjectId.)]
    (mc/save "sites" (merge {:_id id} site))
    (user/update-sites user id)))

(defn delete!
  [id]
  (mc/remove-by-id "sites" id))

(defn owner? 
  [{:keys [user id]}]
  (->> user
       :site-ids
       (some #{id})))

(defroutes site-routes
  (GET "/:id" [id] (respond-with-edn (by-id id)))
  (POST "/" {params :params} (respond-with-edn (create-or-update! params) 201))
  (PUT "/:id" {params :params} 
       (if (owner? params) 
         (respond-with-edn (create-or-update! params))
         (forbidden)))
  (DELETE "/:id" {params :params}
          (if (owner? params) 
            (do (delete! (:id params))
                (respond-with-edn "" 204))
            (forbidden))))
