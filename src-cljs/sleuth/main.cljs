(ns sleuth.main
  (:use [jayq.core :only [$ on ready]])
  (:use-macros [secretary.macros :only [defroute]])
  (:require [sleuth.auth :as auth]
            [sleuth.sites :as sites]
            [secretary.core :as secretary]))

(defn initialize
  [] 
  (def user (atom {}))
  (auth/initialize :li.auth user)

  (add-watch user :dispatch (fn [key a old-val new-val]
                              (if-not (empty? new-val)
                                (secretary/dispatch! (.-hash js/location)))))

  (defroute "#/sites" [] (sites/initialize user))
  (defroute "#/sites/:id" {:keys [id]} (sites/initialize user id))
  (defroute "#/sites/:id/events/:event-id" {:keys [id event-id] (sites/initialize user id event-id)})
  (on ($ js/window) "hashchange" #(secretary/dispatch! (.-hash js/location)))) 

(.ready ($ :document) initialize) 
