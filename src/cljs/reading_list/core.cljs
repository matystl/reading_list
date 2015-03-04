(ns reading-list.core
    (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
    (:require [reagent.core :as reagent :refer [atom]]
              [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [goog.events :as events]
              [goog.history.EventType :as EventType]
              [cljs-http.client :as http]
              [cljs.core.async :refer [put! <! >! chan timeout merge mix admix toggle pub sub unsub unsub-all close!]])
    (:import goog.History))

;; -------------------------
;; Views

(enable-console-print!)

(def state (atom {:items [] :loading false :query nil :query-shown nil}))

(def items_db (atom {}))

; publisher is just a normal channel
(def dispacher-ch (chan))

(defn dispach [type data]
  (go (>! dispacher-ch {:type type :data data})))

; publication is a thing we subscribe to
(def dispacher
  (pub dispacher-ch :type))



(defn fetch-widgets
  [url]
  (let [c (chan)]
    (go (let [{widgets :body} (<! (http/get url))]
          (>! c (vec widgets))))
    c))

(defn load-items [query]
  (println "will load items " query)
  (swap! state assoc-in [:query] query)
  (go (let [items (<! (fetch-widgets (str "/widgets/" query)))]
    (<! (timeout 2000))
    (dispach :server-items {:query query :data items})
    ;(println "after load of items" items)
    ;(swap! state assoc-in [:items] items))
    ;(swap! state assoc-in [:loading] false)
  ))
  (println "last command of load-items"))

(defn apply-items [{query :query data :data}]
  (apply swap! items_db assoc (mapcat #(vector (:id %) %) data))
  (if (= query (:query @state))
    (do
      (println "apply items" query data)
      (swap! state assoc-in [:items] data)
      (swap! state assoc-in [:query] nil)
      (swap! state assoc-in [:query-shown] query)))
    ;(swap! state assoc-in [:items] items))
    ;(swap! state assoc-in [:loading] false))
  )


(defn store []
  (println "Store will be created")
  (let [ch (chan)]
    (go-loop []
      (let [val (<! ch)]
        (println "store readed from channel" val)
        (if val
          (do
            (case (:type val)
            :test-event (println "store get test item")
            :load-items (load-items (:data val))
            :server-items (apply-items (:data val)))
            (recur))
          (println "value from channel is empty stop store go block")))
      )
    (println "before returning from let store")
    ch
    ))


(defn testc [a b & c]
  [:div
   "Here i start"
   (str a)
   (str b)
   (str (seq [:div "5"]))
   [:div b]
   [:div c]])

(defn simple-component []
  [:div "Simple component"])

(def simple-with (with-meta simple-component {
   :component-will-mount #(println "will mount simple")
   :component-did-mount #(println "did mount simple")
   :component-did-update #(println "did updatre simple")
   :component-will-unmount #(println "will unmount simple")
  }))


(defn items-db-show []
  [:div "test db"
   (for [[id item] @items_db]
     ^{:key id} [:div "item:" (str item)])
  ])

(defn home-page-iner []
  ;(println "inicilaization of home page")
  ;(let [a 1]
  ;  (fn []
      [:div
       [:h2 "Welcome to reading list header"]
       [items-db-show]
;       [simple-with ]
       [:button {:on-click (fn [] (let [cas (.now js/Date)] (println "will dispach click on item" cas ) (dispach :test-event (str "clock on item " cas))))} "send to store"]
       [:button {:on-click #(dispach :load-items 1)} "Load items 1.1"]
       [:button {:on-click #(dispach :load-items 2)} "Load items 2"]
       (if (:query @state) [:div "Loading... " (:query @state)] [:div "-"])
       [:div "Current selection"(:query-shown @state)]
       [:div
        (for [item (:items @state)]
         ^{:key item} [:div "item:" (str item)])]
    ;   [:div "After list of items"]
    ;   [testc {:a 10} [:div 5 6 7 8]]
    ;   [:div (seq ["a" "b"])]
    ;   [:div a "ahoj daco ine"]
    ;   [:div (seq [:div "test47" "test48"])]
    ;   [:div "test4711" :div "test4811"]
    ;   [simple-with]
       [:div [:a {:href (about-path)} "go to about page"]]
      ])
  ;))


(defn home-page-2 []
  (let [test (println "let of home-page-2 called")
        store-ch (store)]
    (reagent/create-class {
      :component-will-mount
        (fn [_]
          (println "will mount home")
          (sub dispacher :test-event store-ch)
          (sub dispacher :load-items store-ch)
          (sub dispacher :server-items store-ch)
          (dispach :load-items 1))
      :component-did-mount #(println "did mount home")
      :component-did-update #(println "did updatre home")
      :component-will-unmount (fn [_] (println "will unmount home") (unsub-all dispacher) (close! store-ch))
      ;:reagent-render (fn []
      :render home-page-iner
   })))
(comment
(def home-page-1
  (let [store-ch (store)
        test (println "let of home-page-1 called")]
  (with-meta home-page-iner {
   :component-will-mount (fn [_]
     (println "will mount home")
     (sub dispacher :test-event store-ch)
     (sub dispacher :load-items store-ch)
     (dispach :load-items 1)
   )
   :component-did-mount #(println "did mount home")
   :component-did-update #(println "did updatre home")
   :component-will-unmount (fn [_] (println "will unmount home") (unsub-all dispacher) (close! store-ch))
  }))
  )
  )

(defn home-page []
  [home-page-2])



(def a (atom 0))
(def b (atom 0))
(def c (atom 0))

(defn get []
  @a + @b +@c)

(defn showa []
  [:div "Value of a" @a])
(defn showb []
  [:div "Value of b" @b])
(defn showc []
  [:div "Value of c" @c])

(def showabc ^{:component-did-update #(println "did updatre testing component")} (fn []
  [:div
   "testing component"
   @a
   (get)
   " "
   (if (= (mod @a 2) 0) @b @c)
  ]))

(defn common []
  [:div
   [showa]
   [showb]
   [showc]
   [showabc]
   [:input {:type "button" :value "inc A" :on-click #(swap! a inc)}]
   [:input {:type "button" :value "inc B" :on-click #(swap! b inc)}]
   [:input {:type "button" :value "inc C" :on-click #(swap! c inc)}]
  ])


(defn about-page []
  [:div [:h2 "About reading_list"]
   [common]
   [:div [:a {:href (home-path)} "go to the home page"]]])

(defn current-page []
  [:div [(session/get :current-page)]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute home-path "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute about-path "/about" []
  (session/put! :current-page #'about-page))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn init! []
  (hook-browser-navigation!)
  (reagent/render-component [current-page] (.getElementById js/document "app")))
