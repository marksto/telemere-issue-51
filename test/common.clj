(ns common
  (:require [taoensso.telemere :as tel]))

(defn handle-exception [ex error-level]
  (tel/log! {:level error-level
             :let   [error-msg (ex-message ex)
                     error-data (ex-data ex)]
             :msg   (format "#%s: %s" (:n error-data) error-msg)}))

(defn- random-sleep []
  (try
    (Thread/sleep ^long (+ 10 (rand-int 40)))
    (catch InterruptedException _ie
      (Thread/.interrupt (Thread/currentThread)))))

(defn do-smth [n]
  (tel/log! :info (format "#%s: Doing something..." n))
  (random-sleep)
  (throw (ex-info "#%s: Ooops!" {:type :eligible-for-handling, :n n})))

(defn run-on-executor
  [n error-level]
  (try
    (tel/log! :info (format "#%s: Running task..." n))
    (do-smth n)
    (catch Exception ex
      (tel/log! :info (format "#%s: Exception caught, potentially eligible" n))
      (if (= :eligible-for-handling (:type (ex-data ex)))
        (handle-exception ex error-level)
        (throw ex)))))
