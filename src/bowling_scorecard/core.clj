(ns bowling-scorecard.core
      (:use compojure.core)
      (:use cheshire.core)
      (:use ring.util.response)
      (:require [compojure.handler :as handler]
                [ring.middleware.json :as middleware]
                [compojure.route :as route]))

;; Auxiliary function for generating UUIDs as a string
(defn uuid [] (str (java.util.UUID/randomUUID)))

;; An atom that will store all scoreboards for the duration
;; of the lifespan of the server
(def scorecards (atom {}))

(defn create-new-scorecard
  "Creates a new sorecard and returns the uuid"
  [req]
  (let [id (uuid)]
    (swap! scorecards assoc id [])
    (response id))

(defn get-all-scorecards
  "Returns a list of all known scorecard ids"
  [req]
  (response
    (keys @scorecards)))

(defn get-scorecard
  "Returns an unformatted scorecard by id"
  [req]
  (response
    (get @scorecards (-> req :params :scorecard-id))))

;; Posible functions for individial frames
(defroutes frame-routes
  (GET "/" [] "get frame")
  (POST "/" [] "add-frame")
  ;; not specified, we'll add as nice-to-have
  #_(DELETE "/" [] "delete-frame")
  (route/not-found "frame operation not found"))

;; Possible operations on a single scorecard
(defroutes single-scorecard-routes
  ;; grants access to single frame (probs not needed)
  (context "/frame" [] frame-routes)
  ;; retreives the scorecard itself
  (GET "/" [] get-scorecard)
  ;; Adds the next frame to the scorecard
  (POST "/" [] "adding a frame to scorecard")
  #_(DELETE "/" [] "Deleting a frame form scorecard")
  (route/not-found "No such scorecard operation found"))

;; Possible routes for a scorecard
(defroutes scorecard-routes
  (context "/:scorecard-id" [] single-scorecard-routes)
  ;; gets a list of all scorecards
  (GET "/" [] get-all-scorecards)
  ;; creates a new scorecard
  (POST "/" [] create-new-scorecard)
  ;; not specified, we'll add as nice-to-have
  #_(DELETE "/" [] "delete scorecard")
  (route/not-found "Scorecard operation not found"))

(defroutes v1-routes
  (context "/scorecard" [] scorecard-routes)
  (route/not-found "Not Found"))

(defroutes app-routes
  (context "/v1" [] v1-routes)
  (GET  "/" [] "we're live")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))