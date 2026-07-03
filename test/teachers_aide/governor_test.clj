(ns teachers-aide.governor-test
  (:require [clojure.test :refer [deftest is testing]]
            [teachers-aide.store :as store]
            [teachers-aide.governor :as governor]))

(defn- fresh-store []
  (let [st (store/mem-store)]
    (store/register-classroom! st {:classroom-id "classroom-1" :name "Room 4 Morning Class"})
    st))

(deftest ok-on-clean-assist
  (let [st (fresh-store)
        proposal {:op :assist :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:classroom-id "classroom-1"} {} proposal st)]
    (is (:ok? v))
    (is (not (:hard? v)))
    (is (not (:escalate? v)))))

(deftest hard-on-unregistered-classroom
  (let [st (fresh-store)
        proposal {:op :assist :effect :propose :confidence 0.9 :stake :low}
        v (governor/check {:classroom-id "no-such-classroom"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-classroom (:rule %)) (:violations v)))))

(deftest hard-on-no-actuation-violation
  (let [st (fresh-store)
        proposal {:op :assist :effect :direct-write :confidence 0.9 :stake :low}
        v (governor/check {:classroom-id "classroom-1"} {} proposal st)]
    (is (:hard? v))
    (is (some #(= :no-actuation (:rule %)) (:violations v)))))

(deftest escalates-on-one-on-one-supervision-without-lead-teacher
  (let [st (fresh-store)
        proposal {:op :one-on-one-supervision-without-lead-teacher :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:classroom-id "classroom-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-behavioral-incident-response
  (let [st (fresh-store)
        proposal {:op :behavioral-incident-response :effect :propose :confidence 0.9 :stake :high}
        v (governor/check {:classroom-id "classroom-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest escalates-on-low-confidence
  (let [st (fresh-store)
        proposal {:op :assist :effect :propose :confidence 0.2 :stake :low}
        v (governor/check {:classroom-id "classroom-1"} {} proposal st)]
    (is (:escalate? v))
    (is (not (:hard? v)))))

(deftest store-records-and-ledger-append-only
  (let [st (fresh-store)]
    (store/commit-record! st {:classroom-id "classroom-1" :op :monitor})
    (store/append-ledger! st {:disposition :commit})
    (is (= 1 (count (store/records-of st "classroom-1"))))
    (is (= 1 (count (store/ledger st))))))
