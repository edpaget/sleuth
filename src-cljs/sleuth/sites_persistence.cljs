(ns sleuth.sites-persistence
  (:require [sleuth.xhr :as xhr]))

(defn- sites-to-map
  [sites]
  (zipmap (map #(:_id %) sites) sites))

(defn fetch!
  [user sites]
  (xhr/get "/sites/" @user 
           #(swap! sites merge (sites-to-map %))))

(defn save!
  "Saves the active site to the api"
  [sites site user]
  (if-let [id {:_id site}]
    (xhr/put (str "/sites/" id) site user 
             #(swap! sites assoc id %))
    (xhr/post "/sites" site user 
              #(swap! sites conj %))))

(defn delete!
  [user sites]
  (fn [e]
    (let [id (-> e .-target .-dataset .-id)]
      (swap! sites dissoc id)
      (xhr/delete (str "/sites/" id) @user 
                  #(do (set! (.-hash js/location) "#/sites"))))))
