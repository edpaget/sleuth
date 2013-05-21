(ns sleuth.site-events
  (:use compojure.core
        sleuth.util
        monger.operators)
  (:require [monger.collection :as mc]
            [monger.conversion :as mcv]
            [sleuth.events :as events])
  (:import [org.bson.types ObjectId]))

(defn- query-selector
  [site-id {:keys [selector type start-date end-date]}]
  (let [site (mc/find-map-by-id "sites" (mcv/to-object-id site-id))]
    (events/query (:url site) type selector start-date end-date)))

(defn all-for-site
  [site-id]
  (:site-events (mc/find-map-by-id "sites" (mcv/to-object-id site-id))))

(defn by-id
  [site-id id]
  (let [event (->> (mc/find-map-by-id "sites" (mcv/to-object-id site-id))
                   :site-events
                   (filter #(= (:_id %) id))
                   first)]
    (merge event {:results (query-selector site-id event)})))

(defn update!
  [site-id id {:keys [selector type start-date end-date]}]
  (let [item {:_id (mcv/to-object-id id) :selector selector :type type}]
    (mc/update "sites" {:_id (mcv/to-object-id site-id) :site-events._id {:_id (mcv/to-object-id id)}} 
               {$set {:site-events.$ item}})
    (by-id site-id id)))

(defn delete
  [site-id id]
  (mc/update "sites" {:_id (mcv/to-object-id site-id)} 
             {$pull {:site-events. {:_id (mcv/to-object-id id)}}}))

(defn create!
  [site-id {:keys [selector type start-date end-date]}]
  (let [item-id (ObjectId.)
        item {:_id item-id :selector selector :type type}]
    (mc/update "sites" {:_id (mcv/to-object-id site-id)} {$push {:site-events item}})
    (by-id site-id item-id)))

(defroutes site-event-routes
  (GET "/" [id] (respond-with-edn (all-for-site id)))
  (POST "/" [id & params] (respond-with-edn (create! id params) 201))
  (GET "/:event-id" [id event-id] (respond-with-edn (by-id id event-id)))
  (PUT "/:event-id" [id event-id & params] (respond-with-edn (update! id params)))
  (DELETE "/:event-id" [id event-id] (do (delete id event-id) 
                                         (respond-with-edn nil 204))))
