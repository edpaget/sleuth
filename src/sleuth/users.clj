(ns sleuth.users
  (:use compojure.core
        monger.operators
        sleuth.util) 
  (:require [monger.collection :as mc]
            [validateur.validation :as v])
  (:import [org.bson.types ObjectId]
           [java.security MessageDigest]))

(defn gen-key
  [email]
  (let [md (MessageDigest/getInstance "SHA-256")
        input (str (java.util.Date.) email)]
    (. md update (.getBytes input))
    (let [digest (.digest md)]
      (apply str (map #(format "%02x" (bit-and % 0xff)) digest)))))

(defn by-id 
  [id]
  (mc/find-map-by-id "users" id))

(defn create!
  [user]
  (mc/insert-and-return "users" (merge {:_id (ObjectId.)} user)))

(defn update!
  [id m]
  (mc/update-by-id "users" id m)
  (by-id id))

(defn create-or-update!
  [{:keys [email expires]}]
  (let [user-record (mc/find-one-as-map "users" {:email email})]
    (if (nil? user-record)
      (create! {:email email :expires expires 
                :site-ids [] :api-key (gen-key email)})
      (update! (:_id user-record) 
               (merge user-record {:expires expires})))))

(defn update-sites
  [user id]
  (if (not (some #{id} (:site-ids user)))
    (mc/update-by-id "users" (:_id user) {:site-ids {$push id}})))

(defroutes user-routes
  (GET "/:id" [id] (respond-with-edn (by-id id))))
