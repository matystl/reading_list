(ns reading-list.handler
  (:require [reading-list.dev :refer [browser-repl start-figwheel]]
            [compojure.handler :as handler]
            [compojure.core :refer [GET defroutes]]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response response]]
            ;;[ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.json :as middleware]
            [selmer.parser :refer [render-file]]
            [environ.core :refer [env]]
            [prone.middleware :refer [wrap-exceptions]]
            [prone.debug :refer [debug]]
            [reading-list.common.routes :as common]
            [domkm.silk :as silk]))


(defonce last-items-id (atom 5))

(defonce items (atom
  [{:id 1 :name "Widget 1"} {:id 2 :name "Widget 2" :type 1} {:id 3 :name "Widget 3" :type 2}  {:id 4 :name (str "Widget 16" common/nejaky-text ) :type 1}]))

;here is definition of multhimethod which makes dispaching from route name to actual handler
(defmulti route->handler :domkm.silk/name)

(defmethod route->handler nil
  [_]
  nil)

(defmethod route->handler :get-widgets
  [{:keys [filter-type]}]
  ;(debug)
  (response (common/refilter @items filter-type)))

(defmethod route->handler :add-widget
  [{:keys [type name] :as data}]
  (println data)
  (swap! last-items-id inc)
  (println "test")
  (let [new-item {:id @last-items-id :name name :type type}]
    (swap! items conj new-item)
    (response new-item)))


;generic matching for silk routes
(defn silk-routes [{:keys [uri] :as req}]
  (silk/arrive common/server-routes uri route->handler))



; here we define complojure rotes one part will be silk routes
(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  silk-routes
  (route/resources "/")
  (route/not-found "Page not Found"))

(def app
  (-> (handler/api routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)
      (wrap-exceptions))) ;should be deleted in production
;(def app
;  (let [handler (wrap-defaults routes site-defaults)]
;    (if (env :dev?) (wrap-exceptions handler) handler)))
