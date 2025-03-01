(ns core
  (:require [qbits.knit :as knit]
            [taoensso.telemere :as tel])
  (:import (java.util.concurrent ExecutorService
                                 RejectedExecutionException
                                 ScheduledFuture
                                 ThreadFactory
                                 TimeUnit)))

(defn drop-hostname-mw [signal]
  (dissoc signal :host))

(tel/set-middleware! drop-hostname-mw)

(Runtime/.addShutdownHook (Runtime/getRuntime) (Thread. ^Runnable tel/stop-handlers!))

;;

(def ^:dynamic *executor* nil)

(defn- log-executor-error [executor-uuid e]
  (tel/error! {:msg   (format "Uncaught exception on executor '%s'" executor-uuid)
               :error e}))

(defn- executor-thread-factory ^ThreadFactory []
  (let [executor-uuid (str (random-uuid))]
    (.factory (doto (Thread/ofPlatform)
                (.name (format "executor-%s" executor-uuid))
                (.priority Thread/NORM_PRIORITY)
                (.uncaughtExceptionHandler #(log-executor-error executor-uuid %2))))))

(def task-lvl :debug #_:info)

(defn schedule!
  ^ScheduledFuture [task-fn]
  (let [task-uuid (str (random-uuid))]
    (tel/log! task-lvl (format "Scheduling a task '%s'" task-uuid))
    (try
      (let [f #(try (task-fn)
                    (finally
                      (tel/log! task-lvl (format "Finished task '%s'" task-uuid))))]
        (knit/schedule :once 0 f {:executor *executor*}))
      (catch RejectedExecutionException _ree
        (tel/error! {:level :warn
                     :msg   (format "Rejected scheduling a task '%s'" task-uuid)})
        nil))))

(defn with-executor!
  [pool-size workload-fn]
  (tel/log! :info "Starting new executor...")
  (let [thread-factory (executor-thread-factory)
        executor (knit/executor :scheduled {:thread-factory thread-factory
                                            :num-threads    pool-size})]
    (tel/log! :info "The executor was started")
    (try
      (binding [*executor* executor]
        (workload-fn))
      (finally
        (tel/log! :info "Stopping the executor...")
        (ExecutorService/.shutdown executor)
        (if (ExecutorService/.awaitTermination executor 5000 TimeUnit/MILLISECONDS)
          (tel/log! :info "All scheduled tasks have completed their execution")
          (tel/log! :warn "The scheduled tasks completion timeout elapsed before termination!"))
        (tel/log! :info "The executor was stopped")))))
