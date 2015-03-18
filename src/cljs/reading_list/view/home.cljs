(ns reading-list.view.home
  (:require-macros [cljs.core.async.macros :refer [go alt! go-loop]])
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields init-field value-of]]
            [cljs.core.async :refer [put! <! >! chan timeout merge mix admix toggle pub sub unsub unsub-all close!]]
            [reading-list.dispacher :refer [dispach dispacher]]
            [reading-list.routes :as route]
            [reading-list.common.routes :as common]))

(def new-item-init-state {:content "Some text" :type 0})

(defonce new-item (atom new-item-init-state))

(defn row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn input [label type id]
  (row label [:input {:field type :id id}]))

(def add-item-form-template
  [:div
   (input "Text" :text :content)
   (input "Type" :numeric :type)
   ])

(defn add-item-form []
  [:div
   [bind-fields add-item-form-template new-item]
   [:button {:on-click #(dispach :add-item @new-item)} "Save"]
   [:div (str @new-item)]])


(defn add-item [{:keys [content type] :as data}]
  (println content type data)
  (go (let [item (<! (common/ajax :add-widget {:name content :type type}))]
    (<! (timeout 2000))
    (reset! new-item new-item-init-state)
    (dispach :server-items {:data [item]})
  )))

(defn new-item-store []
  (let [ch (chan)]
    (go-loop [val (<! ch)]
             (when val
               (case (:type val)
                 :add-item (add-item (:data val)))
               (recur (<! ch)))
    )
    ch))




(defonce state (atom {:items [] :loading false :query nil :query-shown nil}))

(defonce items_db (atom {}))

(defn show-items! [query]
  (println "show items " query)
  (let [items (vals @items_db)
        filtered-items (common/refilter items query)]
    (swap! state assoc-in [:query-shown] query)
    (swap! state assoc-in [:items] filtered-items)))

(defn load-items [query]
  (println "will load items " query)
  (swap! state assoc-in [:query] query)
  (show-items! query)
  (go (let [items (<! (common/ajax :get-widgets {:filter-type query}))]
    (<! (timeout 2000))
    (dispach :server-items {:query query :data items})
  ))
  (println "last command of load-items"))

(defn apply-items [{query :query data :data}]
  (apply swap! items_db assoc (mapcat #(vector (:id %) %) data))
  (show-items! (:query-shown @state))
  (when (and (= query (:query @state)) query)
      (println "apply items" query data)
      ;(swap! state assoc-in [:items] data)
      (swap! state assoc-in [:query] nil)
      ;(swap! state assoc-in [:query-shown] query)
    )
    ;(swap! state assoc-in [:items] items))
    ;(swap! state assoc-in [:loading] false))
  )


(defn store []
  (println "Store will be created")
  (let [ch (chan)]
    (go-loop [val (<! ch)]
             (println "store readed from channel" val)
             (when val
               (case (:type val)
                 :test-event (println "store get test item")
                 :load-items (load-items (:data val))
                 :server-items (apply-items (:data val)))
               (recur (<! ch)))
             (println "value from channel is empty stop store go block")
      )
    (println "before returning from let store")
    ch
    ))


(defn items-db-show []
  [:div "test db"
   (for [[id item] @items_db]
     ^{:key id} [:div "item:" (str item)])
  ])

(defn home-page-iner []
      [:div
       [:h2 "Welcome to reading list header6"]
       [add-item-form]
       [:div "Conntent of client db:"]
       [items-db-show]
       [:button {:on-click (fn [] (let [cas (.now js/Date)] (println "will dispach click on item" cas ) (dispach :test-event (str "clock on item " cas))))} "send to store"]
       [:button {:on-click #(dispach :load-items 1)} "Load items 1.1"]
       [:button {:on-click #(dispach :load-items 2)} "Load items 2"]
       (if (:query @state) [:div "Loading... " (:query @state)] [:div "-"])
       [:div "Current selection"(:query-shown @state)]
       [:div
        (for [item (:items @state)]
         ^{:key item} [:div "item:" (str item)])]
       [:div [:a {:href (route/about-path {:name "Maty"})} "go to about page"]]
      ])


(defn page []
  (let [test (println "let of home-page-2 called")
        store-ch (store)
        new-item-store-ch (new-item-store)]
    (reagent/create-class {
      :component-will-mount
        (fn [_]
          (println "home page - :component-will-mount")
          (sub dispacher :test-event store-ch)
          (sub dispacher :load-items store-ch)
          (sub dispacher :server-items store-ch)
          (sub dispacher :add-item new-item-store-ch)
          (dispach :load-items 1))
      :component-did-mount #(println "did mount home")
      :component-did-update #(println "did updatre home")
      :component-will-unmount (fn [_]
                                (println "will unmount home")
                                (unsub-all dispacher)
                                (close! store-ch)
                                (close! new-item-store-ch))
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
