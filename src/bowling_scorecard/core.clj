(ns bowling-scorecard.core
      (:use compojure.core)
      (:use cheshire.core)
      (:use ring.util.response)
      (:require [compojure.handler :as handler]
                [ring.middleware.json :as middleware]
                [compojure.route :as route]))


;; Posible functions for individial frames
(defroutes frame-routes
  (GET "/" [] "get frame")
  (POST "/" [] "add-frame")
  ;; not specified, we'll add as nice-to-have
  #_(DELETE "/" [] "delete-frame")
  (route/not-found "frame operation not found"))

;; Possible routes for a board
(defroutes board-routes
  (context "/frame" [] frame-routes)
  (GET "/" [] "get board")
  (POST "/" [] "add board")
  ;; not specified, we'll add as nice-to-have
  #_(DELETE "/" [] "delete board")
  (route/not-found "Board operation not found"))

(defroutes app-routes
  (context "/board" [] board-routes)
  (GET  "/" [] "we're live")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))