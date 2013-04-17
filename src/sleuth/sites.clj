(ns sleuth.sites
  (:use compojure.core
        sleuth.util)
  (:require [monger.collection :as mc]
            [sleuth.users :as user])
  (:import [org.bson.types ObjectId]))

(defn gen-key
  [user url]
  (sha256 (:email user) url))

(defn by-id
  [id]
  (mc/find-map-by-id "sites" (ObjectId. id)))

(defn for-user
  [user]
  (mc/find-maps "sites" {:user-id (:_id user)}))

(defn create-or-update!
  [{:keys [user id name url event-count created-at]}]
  (let [id (if (nil? id) (ObjectId.) (ObjectId. id))
        site (merge {:_id id
                     :name name
                     :url url
                     :event-count (or event-count 0)
                     :site-key (gen-key user url)
                     :user-id (:_id user)}
                    (if-not (nil? created-at)
                      {:created-at created-at}))]
    (user/update-sites user id)
    (mc/save-and-return "sites" (timestamp site))))

(defn delete!
  [{:keys [id user]}]
  (let [id (ObjectId. id)] 
    (mc/remove-by-id "sites" id)
    (user/delete-site user id)))

(defn owner? 
  [{:keys [user id]}]
  (= (:_id user) (:user-id (by-id id))))

(defroutes site-routes
  (GET "/" [user] (respond-with-edn (for-user user)))
  (GET "/:id" {params :params}
       (if (owner? params) 
         (if-let [site (by-id (:id params))] 
           (respond-with-edn site)
           (respond-with-edn nil 404))
         (forbidden)))
  (POST "/" {params :params} (respond-with-edn (create-or-update! params) 201))
  (PUT "/:id" {params :params} 
       (if (owner? params) 
         (do (create-or-update! params) 
             (respond-with-edn (by-id (:id params))))
         (forbidden)))
  (DELETE "/:id" {params :params}
          (if (owner? params) 
            (do (delete! params)
                (respond-with-edn nil 204))
            (forbidden))))
