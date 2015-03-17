(ns reading-list.routes
    (:require [reagent.session :as session]
              [secretary.core :as secretary :include-macros true]
              [reading-list.common.routes :as server-routes]
              [domkm.silk :as silk]))



;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

(secretary/defroute home-path "/" {:as params}
  (session/put! :params params)
  (session/put! :current-page :home-page)
)

(secretary/defroute about-path "/about/:name" {:as params}
  (session/put! :params params)
  (session/put! :current-page :about-page)
  )
