(ns sleuth.factories
  (:require [monger.core :as m]
            [monger.collection :as mc])
  (:import [org.bson.types ObjectId]))

(m/connect!)
(m/set-db! (m/get-db "sleuth-test"))

(defn create-user!
  [& [{:keys [email api-key]}]]
  (let [user {:email (or email "example@example.com")
              :api-key (or api-key "not-an-api-key")
              :_id (ObjectId.)}]
    (mc/insert-and-return "users" user)))
