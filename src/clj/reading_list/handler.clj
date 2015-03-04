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
            [prone.middleware :refer [wrap-exceptions]]))



(def items
  [{:id 1 :name "Widget 1"} {:id 2 :name "Widget 2" :type 1} {:id 3 :name "Widget 3" :type 2}  {:id 4 :name "Widget 15" :type 1}])

(defn refilter [items filter-type]
  (filter #(= filter-type (str (:type %))) items))

(def customRoute
  (GET  "/widgets/:filter-type" [filter-type] (response (refilter items filter-type))))



(defroutes routes
  (GET "/" [] (render-file "templates/index.html" {:dev (env :dev?)}))
  customRoute
  (route/resources "/")
  (route/not-found "Page not Found"))

(def app
  (-> (handler/api routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))
;(def app
;  (let [handler (wrap-defaults routes site-defaults)]
;    (if (env :dev?) (wrap-exceptions handler) handler)))


