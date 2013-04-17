(ns sleuth.editable
  (:use [jayq.core :only [$ on off inner val parent text data]])
  (:require [dommy.template :as dommy]))

(defn- edit-template
  [v]
  [:input.editing {:value v :type "text"}])

(defn- finish-edit
  [callback]
  (fn [e]
  (if (= 13 (.-which e))
    (let [target ($ (.-target e))
          value (val target)
          field (data (parent target) "field")]
      (if (nil? field) (throw (js/Error.)))
      (inner (parent target) value)
      (callback field value)))))

(defn- edit
  [callback]
  (fn [e]
    (let [target ($ (.-target e))] 
      (inner target (dommy/node (edit-template (text target))))
      (on target "keypress" (finish-edit callback)))))

(defn initialize
  [callback]
  (off ($ :#main.container) "dblclick" :.editable)
  (on ($ :#main.container) "dblclick" :.editable (edit callback)))
