(ns sleuth.main
  (:require [sleuth.auth :as auth]))

(def user (atom {}))

(auth/initialize :li.auth user)
