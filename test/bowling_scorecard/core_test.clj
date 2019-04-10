(ns bowling-scorecard.core-test
  (:require [clojure.test :refer :all]
            [bowling-scorecard.core :refer :all]))

(def sample-scorecard
  [[3 6] [1 9] [2 1] [10] [1 3] [2 7] [3 2] [3 7] [10] [2 4]])

(def sample-scorecard-two
  [[10] [10] [10] [10] [10] [10] [10] [10] [10] [10 10 10]])

(def sample-scorecard-three
  [[3 6] [1 9] [2 1] [6 4] [7 3] [2 7] [1 9] [3 2] [10] [3 7 2]])

(deftest test-score-for-spare
  (testing "Testing spare 1"
    (is (= (get-score-for-spare sample-scorecard 1) 12)))
  (testing "Testing spare 2"
    (is (= (get-score-for-spare sample-scorecard 7) 20)))
  (testing "Testing spare 3"
    (is (= (get-score-for-spare sample-scorecard-three 4) 12)))
  (testing "Testing spare 4"
    (is (= (get-score-for-spare sample-scorecard-three 6) 13))))

(deftest test-score-for-strike
  (testing "Testing strike 1"
    (is (= (get-score-for-strike sample-scorecard 3) 14)))
  (testing "Testing strike 2"
    (is (= (get-score-for-strike sample-scorecard 8) 16)))
  (testing "Testing strike 3"
    (is (= (get-score-for-strike sample-scorecard-two 2) 30)))
  (testing "Testing strike 4"
    (is (= (get-score-for-strike sample-scorecard-three 8) 20))))

(deftest test-score-for-last-frame
  (testing "Testing last frame 1"
    (is (= (get-score-for-last-frame sample-scorecard) 6)))
  (testing "Testing last frame 2"
    (is (= (get-score-for-last-frame sample-scorecard-two) 30)))
  (testing "Testing last frame 3"
    (is (= (get-score-for-last-frame sample-scorecard-three) 12))))

(deftest test-score-for-frame
  (testing "Testing frame 1"
    (is (= (get-score-for-frame sample-scorecard 3) 14)))
  (testing "Testing frame 2"
    (is (= (get-score-for-frame sample-scorecard-two 5) 30)))
  (testing "Testing frame 3"
    (is (= (get-score-for-frame sample-scorecard-three 1) 12)))
  (testing "Testing frame 4"
    (is (= (get-score-for-frame sample-scorecard 0) 9)))
  (testing "Testing frame 5"
    (is (= (get-score-for-frame sample-scorecard-two 7) 30)))
  (testing "Testing frame 6"
    (is (= (get-score-for-frame sample-scorecard-three 7) 5))))

(deftest test-score-for-scorecard
  (testing "Testing scorecard 1"
    (is (= (get-score-for-scorecard sample-scorecard) 98)))
  (testing "Testing scorecard 2"
    (is (= (get-score-for-scorecard sample-scorecard-two) 300)))
  (testing "Testing scorecard 3"
    (is (= (get-score-for-scorecard sample-scorecard-three) 112))))
