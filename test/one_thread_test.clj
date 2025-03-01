(ns one-thread-test
  (:require [clojure.test :refer :all]
            [common]
            [core :as sut]
            [taoensso.telemere :as tel]))

(use-fixtures :each (fn [f] (sut/with-executor! 1 f)))

(deftest schedule!-info-level-test
  (testing "INFO level does not affect order in log"
    (tel/log! :info (str "\n\n" (testing-contexts-str)))
    (dotimes [n 10]
      (sut/schedule! #(common/run-on-executor n :info)))))

(deftest schedule!-error-level-test
  (testing "ERROR level affects order in log"
    (tel/log! :info (str "\n\n" (testing-contexts-str)))
    (dotimes [n 10]
      (sut/schedule! #(common/run-on-executor n :error)))))
