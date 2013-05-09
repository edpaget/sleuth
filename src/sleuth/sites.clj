(ns sleuth.sites
  (:use compojure.core
        sleuth.util)
  (:require [monger.collection :as mc]
            [sleuth.users :as user]
            [clojure.string :as s]
            [sleuth.site-events :as se])
  (:import [org.bson.types ObjectId]))

(defn gen-key
  [user url]
  (sha256 (:email user) url))

(defn by-id
  [id]
  (mc/find-map-by-id "sites" (ObjectId. id)))

(defn for-user
  [user]
  (mc/ensure-index "sites" (array-map :user-id 1))
  (mc/find-maps "sites" {:user-id (:_id user)}))

(defn create-or-update!
  [{:keys [user id name url event-count created-at]}]
  (mc/ensure-index "sites" (array-map :url 1) {:unique true})
  (let [id (if (nil? id) (ObjectId.) (ObjectId. id))
        site (merge {:_id id
                     :name (if name (s/trim name) "")
                     :url (if url (s/trim url) "")
                     :event-count (or event-count 0)
                     :site-key (gen-key user url)
                     :user-id (:_id user)
                     :site-events []}
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
  (or (nil? id) 
      (= (:_id user) (:user-id (by-id id)))))

(defn allow-owner
  [handler]
  (fn [req]
    (if-let [user (get-in req [:params :user])]
      (if (owner? (:params req))
        (handler req)
        (forbidden))
      (not-authorized))))

(defroutes unwrapped
  (GET "/" [user] (respond-with-edn (for-user user)))
  (POST "/" {params :params} (respond-with-edn (create-or-update! params) 201))
  (GET "/:id" {params :params}
       (if-let [site (by-id (:id params))] 
         (respond-with-edn site)
         (respond-with-edn nil 404)))
  (PUT "/:id" {params :params} 
       (do (create-or-update! params) 
           (respond-with-edn (by-id (:id params)))))
  (DELETE "/:id" {params :params}
          (do (delete! params)
              (respond-with-edn nil 204)))
  (context "/:id/site-events" [id] se/site-event-routes))

(defn wrap-user-info
  [handler]
  (wrap-auth handler user/auth :user))

(def site-routes (-> unwrapped
                     allow-owner
                     (require-auth has-user?)
                     wrap-user-info))
