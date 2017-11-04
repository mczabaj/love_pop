(ns love-pop.utils)

;; Creating Orders

(def sample-order {:uuid {:process-state nil
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
(def QUANTITIES [10 20 30])

(defn gen-uuid []
  (random-uuid))

(defn gen-quantity []
  (rand-nth QUANTITIES))

(defn gen-card-id []
  (rand-nth CARD_IDS))

(defn gen-priority []
  (rand-int 10))

(defn gen-row []
  {:card_id       (gen-card-id)
   :quantity      (gen-quantity)
   :priority      (gen-priority)})

(defn gen-rows []
  ;; dont want empty orders! add 1 to the random number of rows (rand-int includes 0)
  (let [cnt  (+ 1 (rand-int 3))]
    (for [x (range 0 cnt)
          :let [row (gen-row)]]
      row)))

(defn gen-order []
  (let [rows (gen-rows)
        id   (keyword (str (gen-uuid)))]
    (-> id {}
        (assoc-in [id :process-state] nil)
        (assoc-in [id :rows] (vec rows)))))

;; Workstation Stuff

(defn gen-time-to-complete []
  ;; rand-int includes 0, so, min time to complte is going to 2, max 5
  (+ 2 (rand-int 4)))
(defn gen-num-workstations []
  ;; same here, minimum workstations is 5, max 10
  (+ 5 (rand-int 4)))
(defn gen-workstation []
  {:count (gen-num-workstations)
   :complete-time (gen-time-to-complete)
   :waiting 0})

(defn set-workstations []
   {:select-paper       (gen-workstation)
    :lazer-cut          (gen-workstation)
    :assemble-sculpture (gen-workstation)
    :assemble-card      (gen-workstation)
    :pack-mail-order    (gen-workstation)})
