(ns multiple-threads-test
  (:require [clojure.test :refer :all]
            [common]
            [core :as sut]
            [taoensso.telemere :as tel]))

(def proc-cnt (Runtime/.availableProcessors (Runtime/getRuntime)))

(use-fixtures :each (fn [f] (sut/with-executor! proc-cnt f)))

(deftest schedule!-info-level-test
  (testing "INFO level does not affect order in log"
    (tel/log! :info (str "\n\n" (testing-contexts-str)))
    (dotimes [n (* 2 proc-cnt)]
      (sut/schedule! #(common/run-on-executor n :info)))))

(deftest schedule!-error-level-test
  (testing "ERROR level affects order in log"
    (tel/log! :info (str "\n\n" (testing-contexts-str)))
    (dotimes [n (* 2 proc-cnt)]
      (sut/schedule! #(common/run-on-executor n :error)))))
