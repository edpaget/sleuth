(ns sleuth.definitions
  (:use compojure.core)
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
  (merge {:created-at (java.util.Date.)} m {:updated-at (java.util.Date.)}))

(defn create-or-update-page!
  "Creates a single page in the db"
  [{:keys [_id created-at page-regex title events]}]
  (let [record {:_id (or _id (ObjectId.))
                :page-regex page-regex
                :title title
                :events events
                :created-at (or created-at (java.util.Date.))}]
    (if (v/valid? valid-page record)
      (mc/save-and-return "pages" (timestamp record))
      {})))
 
(defn create-or-update-pages!
  "Iterates through Pages creating each one"
  [pages]
  (loop [page pages
         created-pages []]
    (if (nil? page)
      created-pages
      (recur (rest pages)
             (conj created-pages (create-or-update-page! page))))))

(defn create-or-update!
  "Creates a new MongoDB Document for a site's defintions"
  [{:keys [id site project pages user-id]}]
  (let [record (timestamp {:_id (ObjectId.)
                           :site site 
                           :project project 
                           :user-id user-id})]
    (if (v/valid? valid-definition record)
      (mc/save-and-return "defintions" (merge record {:page-ids (->> pages
                                                                     create-or-update-pages!
                                                                     (map :_id)
                                                                     (into []))}))
      {})))

(defn respond-with
  [body & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str body)})

(defroutes definitions
  (GET "/" [] (respond-with []))
  (POST "/" [] (respond-with '() 201))
  (GET "/:id" [id] (respond-with '()))
  (PUT "/:id" [id] (respond-with '())))