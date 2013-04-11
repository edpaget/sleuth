(ns sleuth.definitions
  (:use compojure.core
        monger.operators
        sleuth.util)
  (:require [monger.collection :as mc]
            [validateur.validation :as v])
  (:import [org.bson.types ObjectId]))

(def valid-definition (v/validation-set 
                        (v/presence-of :_id) 
                        (v/presence-of :site) 
                        (v/presence-of :project)
                        (v/presence-of :created-at)
                        (v/presence-of :updated-at)
                        (v/presence-of :user-id)))

(def valid-page (v/validation-set
                  v/presence-of :_id
                  v/presence-of :page-regex))

(def valid-event (v/validation-set
                   v/presence-of :type
                   v/presence-of :selector))

(defn timestamp
  "Accepts a map and adds or updates timestamps"
  [m]
  (let [timestamps {:updated-at (java.util.Date.)}
        timestamps (if (:created-at m) 
                     timestamps
                     (merge timestamps {:created-at (java.util.Date.)}))]
    (merge m timestamps)))

(defn get-all-defintions
  []
  (mc/find-amps "definitions"))

(defn get-definition
  [id]
  (mc/find-map-by-id "definitions" id))

(defn get-page 
  [def-id page-id]
  (->> (get-definition def-id)
       :pages
       (filter #(= page-id (:_id %)))
       first))

(defn has-page?
  [def-id page-id]
  (-> (get-page def-id page-id)
      nil?
      not))

(defn create-or-update-page!
  "Creates a single page in the db"
  [{:keys [_id created-at page-regex title events]} def-id]
  (let [record (timestamp {:_id (or _id (ObjectId.))
                           :page-regex page-regex
                           :title title
                           :events events
                           :created-at created-at})]
    (if (v/valid? valid-page record)
      (if (has-page? def-id (:_id record))
        (mc/update "definitions" {:_id def-id "pages._id" (:_id record)} record)
        (mc/update "definitions" {:_id def-id} {$push {:pages record}}))
      {})))

(defn create-or-update-pages!
  "Iterates through Pages creating each one"
  [pages def-id]
  (loop [page pages
         created-pages []]
    (if (nil? page)
      created-pages
      (recur (rest pages)
             (conj created-pages (create-or-update-page! page def-id))))))

(defn create-or-update!
  "Creates a new MongoDB Document for a site's definitions"
  [{:keys [_id site project pages user-id created-at]}]
  (let [record (timestamp {:_id (or _id (ObjectId.))
                           :site site 
                           :project project 
                           :user-id user-id
                           :created-at created-at})]
    (if (v/valid? valid-definition record)
      (do (mc/save-and-return "definitions" record)
          (create-or-update-pages! pages (:_id record))
          (get-definition (:_id record)))
      {})))


(defroutes definitions
  (GET "/" [] (response-with-edn (get-all-defintions)))
  (POST "/" {params :params} (response-with-edn (create-or-update! params) 201))
  (GET "/:id" [id] (response-with-edn (get-definition id)))
  (PUT "/:id" {params :params} (response-with-edn (create-or-update! params))))