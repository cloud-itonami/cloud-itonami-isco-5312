(ns teachers-aide.store
  "SSoT for the ISCO-08 5312 independent teacher's-aide sole-
  proprietor actor. Store is a protocol injected into the
  `teachers-aide.actor` StateGraph — `MemStore` is the default,
  deterministic, zero-dep backend; a Datomic/kotoba-server-backed
  implementation can be swapped in without touching the actor or
  governor (itonami actor pattern, per ADR-2607011000 / CLAUDE.md
  Actors section).

  Domain:

    classroom — a registered classroom (:classroom-id, :name)
    record    — a committed operating record under a classroom
                (assist step, monitor entry, one-on-one supervision
                without a lead teacher, behavioral-incident response)
                — written ONLY via commit-record!, never mutated in
                place
    ledger    — an append-only audit trail of every proposal/verdict/
                disposition, regardless of outcome (commit or hold)")

(defprotocol Store
  (classroom [s classroom-id])
  (records-of [s classroom-id])
  (ledger [s])
  (register-classroom! [s classroom])
  (commit-record! [s record])
  (append-ledger! [s fact]))

(defrecord MemStore [a]
  Store
  (classroom [_ classroom-id] (get-in @a [:classrooms classroom-id]))
  (records-of [_ classroom-id] (filter #(= classroom-id (:classroom-id %)) (:records @a)))
  (ledger [_] (:ledger @a))
  (register-classroom! [s classroom]
    (swap! a assoc-in [:classrooms (:classroom-id classroom)] classroom) s)
  (commit-record! [s record]
    (swap! a update :records (fnil conj []) record) s)
  (append-ledger! [s fact]
    (swap! a update :ledger (fnil conj []) fact) s))

(defn mem-store
  ([] (mem-store {}))
  ([seed] (->MemStore (atom (merge {:classrooms {} :records [] :ledger []} seed)))))
