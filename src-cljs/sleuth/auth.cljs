(ns sleuth.auth
  (:use [jayq.core :only [$ inner on]]
        [dommy.core :only [replace-contents!]])
  (:use-macros [dommy.macros :only [sel1 deftemplate]])
  (:require [sleuth.xhr :as xhr]))

(deftemplate auth-template [user]
  (if (empty? user) 
    [:a.persona-button.sign-in {:href "#"} "Sign In"]
    [:a.persona-button.sign-out {:href "#"} "Sign Out"]))

(defn render-template
  [container user]
  (replace-contents! (sel1 container) (auth-template user)))

(defn login
  [user]
  (fn [assertion]
    (xhr/post "/auth/login" {:assertion assertion} 
              nil #(swap! user merge %))))

(defn logout
  [user]
  (fn []
    (xhr/post "/auth/logout" "" nil #(swap! user {}))))

(defn initialize
  [container user]
  (add-watch user :render (fn [key a old-val new-val]
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
