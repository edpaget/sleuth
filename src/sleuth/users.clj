(ns sleuth.users
  (:use compojure.core
        monger.operators
        sleuth.util) 
  (:require [monger.collection :as mc]
            [monger.conversion :as conv]
            [validateur.validation :as v])
  (:import [org.bson.types ObjectId]))

(defn gen-key
  "Generates the API-Key for a given user" 
  [email]
  (sha256 (java.util.Date.) email))

(defn auth
  [email api-key]
  (mc/find-one-as-map "users" {:email email :api-key api-key}))

(defn by-id 
  "Retrieves the User Map from MongoDB 
  for the given id"
  [id]
  (mc/find-map-by-id "users" (conv/to-object-id id)))

(defn create!
  "Creates a new MongoDB record for the 
  given user map"
  [user]
  (mc/insert-and-return "users" (merge {:_id (ObjectId.)} user)))

(defn update!
  "Updates the user map in MongoDB given 
  the old map's id" 
  [id m]
  (mc/update-by-id "users" id m)
  (by-id id))

(defn create-or-update!
  "Checks if a user already exists and either creates
  for updates the user from the hash returns by Persona"
  [{:keys [email expires]}]
  (let [user-record (mc/find-one-as-map "users" {:email email})]
    (if (nil? user-record)
      (create! (timestamp {:email email :expires expires 
                           :site-ids [] :api-key (gen-key email)}))
      (update! (:_id user-record) 
               (timestamp (merge user-record {:expires expires}))))))

(defn update-sites
  "Adds another site to a user record"
  [user id]
  (if (not (some #{id} (:site-ids user)))
    (mc/update-by-id "users" (:_id user) {$push {:site-ids id}})))

(defn delete-site
  "Removes site from the user record"
  [user id]
  (if (some #{id} (:site-ids user))
    (mc/update-by-id "users" (:_id user) {$pull {:site-ids id}})))

(defroutes user-routes
  (GET "/:id" [id] (respond-with-edn (by-id id))))
