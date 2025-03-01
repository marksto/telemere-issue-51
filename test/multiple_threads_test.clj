(ns multiple-threads-test
  (:require [clojure.test :refer :all]
            [common]
            [core :as sut]))

(def proc-cnt (Runtime/.availableProcessors (Runtime/getRuntime)))

(use-fixtures :each (fn [f] (sut/with-executor! proc-cnt f)))

(deftest schedule!-info-level-test
  (common/testing "INFO level does not affect order in log"
    (dotimes [n (* 2 proc-cnt)]
      (sut/schedule! #(common/run-on-executor n :info)))))

(deftest schedule!-error-level-test
  (common/testing "ERROR level affects order in log"
    (dotimes [n (* 2 proc-cnt)]
      (sut/schedule! #(common/run-on-executor n :error)))))
