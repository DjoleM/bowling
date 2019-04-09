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
    (response id)))

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


;; Helper function to quickly extract and parse integer values
;; While also catching spares ('/') and strikes ('x')
(defn extract-score-from-request
  "Extacts scores in an int or string format form post request by name"
  [req, param]
  (try
    (Integer/parseInt
      (get (req :form-params) param))
    (catch Exception e (get (req :form-params) param))))

;; Helper function that takes in a constructed frame
;; and saves it to the scorecard atom
(defn save-frame
  "Saves a frame to scorecard"
  [id, frame]
  (swap! scorecards assoc-in [id (count (get @scorecards id))] frame))

;; Due to time restriction and the nature of usage
;; we will assume the data provided to be correct and
;; well formatted    
(defn add-frame
  "Adds a new score frame to scorecard"
  [req]
    (let 
      [frame (vec
              (remove nil? 
                [(extract-score-from-request req "first")
                  (extract-score-from-request req "second")
                  (extract-score-from-request req "third")]))]
      (save-frame (-> req :params :scorecard-id) frame)
      (response frame)))

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
  (POST "/" [] add-frame)
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

;; DEBUG ONLY, NUKE LATER
(defn print-req
  [req]
  (println req)
  "success")

(defroutes print-routes
  (GET "/" [] print-req)
  (POST "/" [] print-req))
;; -- END DEBUG --

(defroutes app-routes
  (context "/v1" [] v1-routes)
  (context "/print" [] print-routes)
  (GET  "/" [] "we're live")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))