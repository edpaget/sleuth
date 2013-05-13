(ns sleuth.main
  (:use [jayq.core :only [$ on ready inner]])
  (:use-macros [secretary.macros :only [defroute]])
  (:require [sleuth.auth :as auth]
            [sleuth.sites :as sites]
            [secretary.core :as secretary]
            [sleuth.analytics :as analytics]))

(defn clean-and-dispatch
  [& [key a old-val new-val]]
  (do (secretary/dispatch! (.-hash js/location))
      (inner ($ :#main.container) "")))

(defn initialize
  [] 
  (def user (atom {}))
  (def sites (atom {}))
  (auth/initialize :li.auth user)
  (add-watch user :clean-and-dispatch clean-and-dispatch)

  (defroute "#/sites" [] (sites/initialize user sites))
  (defroute "#/sites/:id" {:keys [id]} (sites/initialize user sites id))
  (defroute "#/analytics" [] (analytics/list-all user sites))
  (defroute "#/analytics/:id" {:keys [id]} (analytics/show user sites id))
  (defroute "#/analytics/:id/event/:ev-id" {:keys [id ev-id]} (analytics/show user sites id ev-id))
  (on ($ js/window) "hashchange" clean-and-dispatch))

(initialize) 
