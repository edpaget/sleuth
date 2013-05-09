(ns sleuth.events
  (:use compojure.core
        sleuth.util
        [clojure.set :only [subset?]]
        [clojure.algo.generic.functor :only [fmap]]
        [clojure.string :only [split]])
  (:require [rotary.client :as db]
            [monger.collection :as mc]
            [clj-time.core :as t]
            [clj-time.format :as f])
  (:import [org.bson.types ObjectId]))

(def cred {:access-key (get (System/getenv) "AMAZON_SECRET_ID")
           :secret-key (get (System/getenv) "AMAZON_SECRET_ACCESS_KEY")})

(defn auth
  [site site-key]
  (mc/find-one-as-map "sites" {:name site :site-key site-key}))

(db/ensure-table cred {:name "events" 
                       :hash-key {:name "site-url" :type :s}
                       :range-key {:name "created_at" :type :s}
                       :throughput {:read 10 :write 5}
                       :indexes [{:name "sessions" 
                                  :range-key {:name "session" :type :s}
                                  :projection :include
                                  :included-attrs ["window_height window_width" "user"]}
                                 {:name "types"
                                  :range-key {:name "type" :type :s}
                                  :projection :include
                                  :included-attrs ["id_name" "tag" "class-names" "value"]}]})

(defn- selector-to-map
  "Splits CSS Selector into map of its components"
  [selector]
  (->> (split selector #"(?=[\.#])")
       (group-by #(case (first %)
                    \. :classes
                    \# :id
                    "default" :tag))
       (fmap (fn [xs] (map #(if (or (= \. (first %)) (= \# (first %)))
                              (apply str (rest %))) xs)))))

(defn- select
  "Predicate to filter based on stripped down css selectors"
  [selector]
  (if-not (nil? selector) 
    (let [{:keys [tag id classes]} (selector-to-map selector)
          [sel-tag] tag
          [id] id
          classes (into #{} classes)]
      (fn [{:strs [tag id_name class-names]}]
        (and (if sel-tag (= sel-tag tag) true)
             (if id (= id id_name) true)
             (if classes (subset? class-names classes) true))))
    (fn [x] true)))

(defn- event-date
  [{:strs [created_at]}]
  (str (t/year created_at) "-" 
       (t/month created_at) "-"
       (t/day created_at)))

(defn- event-value
  [{:strs [value]}]
  value)

(defn- empty-str-to-space
  "Convers all the values of map that are empty string to 
  strings with a single space"
  [m]
  (fmap #(if (= "" %) " " %) m))

(defn- batch-create
  "Formats events for batch writing"
  [events]
  (map #(vector :put "events" (empty-str-to-space %)) events))

(defn- classes-to-set
  "Converts string at class_name to a set of class-names"
  [m]
  (let [classes (->> (split (m "class_name") #"\s")
                     (into #{})
                     empty-str-to-space)]
    (merge (dissoc m "class_name") {"class-names" classes})))

(defn- json-date-to-joda
  [event]
  (merge event {"created_at" (f/parse (event "created_at"))}))

(defn- query-output
  [events]
  [(group-by event-date events) (group-by event-value events)])

(defn create! 
  [{site :site logs "log"}]
  (if-let [events (->> (map #(merge {"site-url" (:url site)} %) logs)
                       (map classes-to-set))]
    (apply (partial db/batch-write-item cred) (batch-create events))))

(defn query
  ([site start-date end-date type selector]
   (if (nil? start-date)
     (query site type selector)
     (when-let [events (->> (db/lazy-query cred "events" {"site-url" site} '("created_at" :<=> start-date end-date))
                            (map json-date-to-joda)
                            (filter #(and (if type (= (% "type") type) true) 
                                          ((select selector) %))))]
       (query-output events))))
  ([site type selector]
   (when-let [events (->> (db/lazy-query cred "events" {"site-url" site} ["type" := type] {:index "types"})
                          (map json-date-to-joda)
                          (filter (select selector)))]
     (query-output events))))

(defn site-match
  [handler]
  (fn [req]
    (if (= (get-in req [:params :site :url])
           (get-in req [:headers "origin"]))
      (handler req)
      (forbidden))))

(defn wrap-site-info
  [handler]
  (wrap-auth handler auth :site))  

(defroutes event-routes
  (OPTIONS "/" {headers :headers} 
           {:status 200
            :headers {"Access-Control-Allow-Origin" "*"
                      "Access-Control-Allow-Headers" "authorization,content-type"
                      "Access-Control-Allow-Methods" "POST"}})

  (-> (POST "/" {params :params} 
            (do (future (create! params))
                {:status 201
                 :headers {"Content-Type" "text/plain;charset=utf-8"
                           "Access-Control-Allow-Origin" (get-in params [:site :url])
                           "Access-Control-Allow-Headers" "authorization,content-type"
                           "Access-Control-Allow-Methods" "POST"}
                 :body "5000"}))
      site-match
      (require-auth has-site?)
      wrap-site-info))
