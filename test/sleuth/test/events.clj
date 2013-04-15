(ns sleuth.test.events
  (:use clojure.test
        ring.mock.request 
        sleuth.events)
  (:require [monger.core :as mc]))

(mc/connect!)
(mc/set-db! (mc/get-db "monger-test"))
