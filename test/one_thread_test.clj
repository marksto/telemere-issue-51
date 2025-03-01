(ns one-thread-test
  (:require [clojure.test :refer :all]
            [common]
            [core :as sut]))

(use-fixtures :each (fn [f] (sut/with-executor! 1 f)))

(deftest schedule!-info-level-test
  (common/testing "INFO level does not affect order in log"
    (dotimes [n 10]
      (sut/schedule! #(common/run-on-executor n :info)))))

(deftest schedule!-error-level-test
  (common/testing "ERROR level affects order in log"
    (dotimes [n 10]
      (sut/schedule! #(common/run-on-executor n :error)))))

(comment
  (run-tests)
  .)
