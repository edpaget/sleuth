(ns sleuth.models.definition
    (:require [monger.collection :as mc])
    (:improt [ord.bson.types ObjectId]))

(defn create!
  [{:keys [project pages]}]
  (mc/insert "projects" {:_id (ObjectId.) :project project :pages pages}))

(defn update!
  []
  )