(ns sleuth.auth
  (:use [jayq.core :only [$ inner on]])
  (:require [dommy.template :as dommy]
            [sleuth.xhr :as xhr]))

(defn auth-template
  [user]
  (.log js/console (pr-str user))
  (if (empty? user) 
    [:a.persona-button.sign-in {:href "#"} "Sign In"]
    [:a.persona-button.sign-out {:href "#"} "Sign Out"]))

(defn render-template
  [container user]
  (inner ($ container) (dommy/node (auth-template user))))

(defn login
  [user]
  (fn [assertion]
    (xhr/post "/auth/login" {:assertion assertion} 
              nil #(swap! user conj %))))

(defn logout
  [user]
  (fn []
    (xhr/post "/auth/logout" "" nil #(swap! user {}))))

(defn initialize
  [container user]
  (add-watch user :watch-change (fn [key a old-val new-val]
                                  (render-template container
                                                   new-val)))
  (on ($ container) "click" :a.sign-in 
      #(.request (.-id js/navigator)))
  (on ($ container) "click" :a.sign-out 
      #(.logout (.-id js/navigator)))
  (render-template container @user)
  (.watch (.-id js/navigator) 
          (js-obj "loggedInUser" (:email @user)
                  "onlogin" (login user)
                  "onlogout" (logout user))))
