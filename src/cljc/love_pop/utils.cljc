(ns love-pop.utils)

(def sample-order {:order {:id "uuid"
                           :rows [{:card_id 110
                                   :quantity 50
                                   :priority 3}
                                  {:card_id 200
                                   :quantity 100
                                   :priority 6}
                                  {:card_id 220
                                   :quantity 10
                                   :priority 1}]}})

(def CARD_IDS [100 110 120 200 210 220])
(def QUANTITIES [10 25 50 75 100 200])

(defn gen-uuid []
  ;; ran into an issue with jave.util.UUID not being found when running in browser
  ;; but when running locall in repl, it worked fine. Stubbing out UUID for now.
  #_(str (java.util.UUID/randomUUID))
  "8b804378-1b3a-446d-8b17-bea4fed709e1")

(defn gen-quantity []
  (rand-nth QUANTITIES))

(defn gen-card-id []
  (rand-nth CARD_IDS))

(defn gen-priority []
  (rand-int 10))

(defn gen-row []
  {:card_id (gen-card-id)
   :quantity (gen-quantity)
   :priority (gen-priority)})

(defn gen-rows []
  ;; dont want empty orders! add 1 to the random number of rows (rand-int includes 0)
  (let [cnt  (+ 1 (rand-int 5))]
    (for [x (range 1 cnt)
          :let [row (gen-row)]]
      row)))

(defn gen-order []
  (let [rows  (gen-rows)]
    (-> {}
      (assoc-in [:order :id] (gen-uuid))
      (assoc-in [:order :rows] (vec rows)))))


(defn gen-time-to-complete []
  ;; rand-int includes 0, so, min time to complte is going to 2, max 10
  (+ 2 (rand-int 8)))
(defn gen-num-workstations []
  ;; same here, minimum workstations is 1, max 5
  (+ 1 (rand-int 4)))

(defn set-workstations []
   {:select-paper           {:count (gen-num-workstations)
                             :complete-time (gen-time-to-complete)}
    :lazer-cut              {:count (gen-num-workstations)
                             :complete-time (gen-time-to-complete)}
    :assemble-sculpture     {:count (gen-num-workstations)
                             :complete-time (gen-time-to-complete)}
    :assemble-card          {:count (gen-num-workstations)
                             :complete-time (gen-time-to-complete)}
    :pack-order             {:count (gen-num-workstations)
                             :complete-time (gen-time-to-complete)}
    :mail-order             {:count (gen-num-workstations)
                             :complete-time (gen-time-to-complete)}})
