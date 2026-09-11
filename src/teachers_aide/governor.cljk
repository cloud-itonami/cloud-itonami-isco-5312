(ns teachers-aide.governor
  "TeachersAideGovernor — the independent safety/traceability layer
  for the ISCO-08 5312 independent teacher's-aide actor. Wired as its
  own `:govern` node in `teachers-aide.actor`'s StateGraph,
  downstream of `:advise` — the Advisor has no notion of classroom
  provenance or supervision/incident risk, so this MUST be a separate
  system able to reject a proposal (itonami actor pattern, per
  ADR-2607011000 / CLAUDE.md Actors section).

  `check` is a pure function of (request, context, proposal, store) ->
  verdict; it never mutates the store. The StateGraph's `:decide` node
  routes on the verdict:
    :hard? true                → :hold  (irreversible, no write)
    :escalate? true            → :request-approval (interrupt-before)
    otherwise                  → :commit

  HARD invariants (:hard? true, ALWAYS :hold, never overridable):
    1. classroom provenance — the request's classroom must be
       registered.
    2. no-actuation         — proposal :effect must be :propose.
  ESCALATION invariants (:escalate? true, ALWAYS human sign-off, per
  the README robotics-premise: direct one-on-one supervision of a
  minor without a lead teacher present, or a behavioral-incident
  response, always require human sign-off):
    3. :op :one-on-one-supervision-without-lead-teacher.
    4. :op :behavioral-incident-response.
    5. low confidence (< `confidence-floor`)."
  (:require [teachers-aide.store :as store]))

(def confidence-floor 0.6)
(def ^:private escalating-ops #{:one-on-one-supervision-without-lead-teacher :behavioral-incident-response})

(defn- hard-violations [{:keys [proposal]} classroom-record]
  (cond-> []
    (nil? classroom-record)
    (conj {:rule :no-classroom :detail "未登録 classroom"})

    (not= :propose (:effect proposal))
    (conj {:rule :no-actuation :detail "effect は :propose のみ許可（直接書込禁止）"})))

(defn check
  "Assess a proposal against `request`/`context`/`proposal` and a
  `store` implementing `teachers-aide.store/Store`. Returns
  `{:ok? bool :violations [...] :confidence n :hard? bool :escalate? bool}`."
  [request context proposal store]
  (let [classroom-record (store/classroom store (:classroom-id request))
        hard (hard-violations {:proposal proposal} classroom-record)
        hard? (boolean (seq hard))
        conf (or (:confidence proposal) 0.0)
        low? (< conf confidence-floor)
        risky-op? (contains? escalating-ops (:op proposal))]
    {:ok? (and (not hard?) (not low?) (not risky-op?))
     :violations hard
     :confidence conf
     :hard? hard?
     :escalate? (and (not hard?) (or low? risky-op?))}))
