(ns reading-list.view.about
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [cljs-http.client :as http]
            [cljs.core.async :refer [put! <! >! chan timeout merge mix admix toggle pub sub unsub unsub-all close!]]
            [reading-list.dispacher :refer [dispach dispacher]]
            [reading-list.routes :as route]
            [reagent.session :as session]
            [reading-list.common.routes :as common]))




(defonce a (atom 0))
(defonce b (atom 0))
(defonce c (atom 0))

(defn get-my []
  @a + @b +@c)

(defn showa [x1 x2]
  [:div
   "Value of a"
   @a
   "a"])

(defn showb []
  [:div "Value of 4b" @b])
(defn showc []
  [:div "Value of c" @c])

(def showabc ^{:component-did-update #(println "did updatre testing component")} (fn []
  [:div
   "testing component"
   @a
   ;(get-my)
   " "
   (if (= (mod @a 2) 0) @b @c)
  ]))

(defn common []
  [:div
   [:div "commoon text: " common/nejaky-text]
   [showa 1 2]
   [showb]
   [(var showc)]
   [showabc]
   [:input {:type "button" :value "inc A" :on-click #(swap! a inc)}]
   [:input {:type "button" :value "inc B" :on-click #(swap! b inc)}]
   [:input {:type "button" :value "inc C" :on-click #(swap! c inc)}]
  ])


(defn page []
  [:div
   [:h2 "About reading_list 2"]
   [:div "This is name from params: " (:name (session/get :params))]
   [common]
   [:div [:a {:href (route/home-path)} "go to the home page"]]
   ])
