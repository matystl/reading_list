(ns reading-list.common.routes
  #+cljs (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [domkm.silk :as silk]
            #+cljs [cljs-http.client :as http]
            #+cljs [cljs.core.async :refer [<! >! chan]]))

(def nejaky-text "ahoj common15")




(def server-routes
  (silk/routes [[:get-widgets [["widgets" (silk/int :filter-type)]]]
                [:add-widget [["add" "widget" (silk/int :type) :name]]]
                ]))



#+cljs (defn ajax [url-symbol url-params]
  (let [c (chan)
        url (silk/depart server-routes url-symbol url-params)]
    (go (let [{body :body} (<! (http/get url))] (>! c body)))
    c))

;(silk/depart server-routes :get-widgets {:filter-type 42}) ;=> "/pages/about"



;should be somwhere else
(defn refilter [items filter-type]
  (filter #(= filter-type (:type %)) items))
