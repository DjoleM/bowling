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

(defn get-score-for-spare
  "Calculates the total value of a spare"
  [scorecard frame-order]
  (+ 10 (get-in scorecard [(inc frame-order) 0])))

(defn get-score-for-strike
  "Calculates the total value of a strike"
  [scorecard frame-order]
  (let [next-frame (get scorecard (inc frame-order))]
    (if (nil? (get next-frame 1))
      ;;Next frame doesn't have a second bowl
      ;;Add strike 10 + first from next 2 frames
      (+ 10 (get next-frame 0) (get-in scorecard [(inc frame-order) 0]))
      (+ 10 (get next-frame 0) (get next-frame 1)))))

(defn get-score-for-last-frame
  "Calculates the sum of the last frame"
  [scorecard]
  (apply + (get scorecard 9)))

(defn get-score-for-frame
  "Calculates the score for a regular frame"
  [scorecard frame-order]
  (let [frame (get scorecard frame-order)]
    (if (= (get frame 0) 10)
      ;; Strike
      (get-score-for-strike scorecard frame-order)
      (if (= (+ (get frame 0) (get frame 1)) 10)
        ;; spare
        (get-score-for-spare scorecard frame-order)
        ;; Bad luck joe :(
        (apply + frame)))))

(defn get-score-from-scorecard
  "Calculates and returns the total score form a scorecard"
  [id]
  (loop [i 0
        scores []]
    (if-not (< i 9)
      (reduce + (conj scores (get-score-for-last-frame (get @scorecards id))))
      (recur (inc i) (conj scores (get-score-for-frame (get @scorecards id) i))))))

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

(defn get-scorecard-by-id
  "Returns a scorecard by id"
  [id]
  (get @scorecards id))

(defn get-scorecard
  "Gets a scorecard by id from a request"
  [req]
  (response
    (get-scorecard-by-id (-> req :params :scorecard-id))))

;; Helper function to quickly extract and parse integer values
;; While also catching spares ('/') and strikes ('x')
(defn extract-score-from-request
  "Extacts scores in an int or string format form post request by name"
  [req param]
  (try
    (Integer/parseInt
      (get (req :form-params) param))
    (catch Exception e nil)))

;; Helper function that takes in a constructed frame
;; and saves it to the scorecard atom
(defn save-frame
  "Saves a frame to scorecard"
  [id frame]
  (swap! scorecards assoc-in [id (count (get-scorecard-by-id id))] frame))

;; Due to time restriction and the nature of usage
;; we will assume the data provided to be correct and
;; well formatted    
(defn add-frame
  "Adds a new score frame to scorecard"
  [req]
    (let [id (-> req :params :scorecard-id)]
      (if (> (count (get-scorecard-by-id id)) 9)
        (response "Error: game already done")
        (let 
          [frame (vec
                  (remove nil? 
                    [(extract-score-from-request req "first")
                     (extract-score-from-request req "second")
                     (extract-score-from-request req "third")]))]
          (save-frame id frame)
          (if (= (count (get-scorecard-by-id id)) 10)
            (response {"Total Score" (get-score-from-scorecard id) "Scorecard" (get-scorecard-by-id id)})
            (response (get @scorecards id)))))))

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

(defroutes app-routes
  (context "/v1" [] v1-routes)
  (GET  "/" [] "we're live")
  (route/not-found "Not Found"))

(def app
  (-> (handler/api app-routes)
      (middleware/wrap-json-body)
      (middleware/wrap-json-response)))