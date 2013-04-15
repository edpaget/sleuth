(ns sleuth.test.sites
  (:use clojure.test
        ring.mock.request
        sleuth.sites)
  (:require [monger.core :as mc]))

(mc/connect!)
(mc/set-db! (mc/get-db "monger-test"))
 
