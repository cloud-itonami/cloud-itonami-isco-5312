(ns teachers-aide.actor-test
  (:require [clojure.test :refer [deftest is testing]]
            [teachers-aide.actor :as actor]
            [teachers-aide.store :as store]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-classroom! st {:classroom-id "classroom-1" :name "Room 4 Morning Class"})
    st))

(deftest commits-a-clean-low-risk-request
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:classroom-id "classroom-1" :op :assist :stake :low}
        result (actor/run-request! graph request {} "thread-1")]
    (is (= :done (:status result)))
    (is (some? (get-in result [:state :record])))
    (is (= 1 (count (store/records-of st "classroom-1"))))))

(deftest holds-on-unregistered-classroom-without-committing
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        request {:classroom-id "no-such-classroom" :op :assist :stake :low}
        result (actor/run-request! graph request {} "thread-2")]
    (is (= :done (:status result)))
    (is (nil? (get-in result [:state :record])))
    (is (empty? (store/records-of st "no-such-classroom")))
    (is (= :hold (:disposition (:state result))))))

(deftest interrupts-then-commits-on-human-approval
  (let [st (fresh-store)
        graph (actor/build-graph {:store st})
        ;; one-on-one supervision without lead teacher always escalates (governor invariant)
        request {:classroom-id "classroom-1" :op :one-on-one-supervision-without-lead-teacher :stake :high}
        interrupted (actor/run-request! graph request {} "thread-3")]
    (is (= :interrupted (:status interrupted)))
    (is (empty? (store/records-of st "classroom-1")))
    (let [resumed (actor/approve! graph "thread-3")]
      (is (= :done (:status resumed)))
      (is (some? (get-in resumed [:state :record])))
      (is (= 1 (count (store/records-of st "classroom-1")))))))
