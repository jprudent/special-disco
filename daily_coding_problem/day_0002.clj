;; # Day 2
(ns day-0002)

;; ## The problem
; This problem was asked by Uber.
;
; Given an array of integers, return a new array such that each element at index i of the new array is the product of all
; the numbers in the original array except the one at i.
;
;For example, if our input was [1, 2, 3, 4, 5], the expected output would be [120, 60, 40, 30, 24].

(def input1 [1, 2, 3, 4, 5])
(def expected1 [120, 60, 40, 30, 24])

;If our input was [3, 2, 1], the expected output would be [2, 3, 6].

(def input2 [3, 2, 1])
(def expected2 [2, 3, 6])

; Follow-up: what if you can't use division?

;; ## The easy solution that uses division

;; This solution consists in computing the product of each numbers
;; Then for each number, we divide the product by the number at i to "remove" the one at i

(def product1 (reduce * input1))

(def solution1 (map #(/ product1 %) input1))

(= solution1 expected1)

;; This solution is O(n) and we browse the array twice.

;; ## Without using division

;; ### O(n2)

;; First thing that comes up is an algorithm that will compute the product in 0(n)
;; for each element.

;; That's inelegant and inefficient.

;; ### Multiplying right and left product of each element

(def mulright (second (reduce (fn [[last-product result] i]
                                [(* last-product (get input2 i))
                                 (conj result last-product)])
                              [1 []] (range (count input2)))))

(def mulleft (second (reduce (fn [[last-product result] i]
                               [(* last-product (get input2 i))
                                (conj result (* (get mulright i) last-product))])
                             [1 '()] (range (dec (count input2)) -1 -1))))

;; It works, but that's horribly complex because :
;; - The accumulator of the reducing function is a bit complexe.
;;   It holds the accumulation of products **and** the resulting sequence
;; - I rely on indices for accessing the sequences.
;;   Generating the indices in decrementing order is difficult to read.
;; - I use different kinds of sequence which `conj` behave differently.
;;   For vectors (`[]`) `conj` **appends**. For lists (`'()`) it **prepends**.
;;   This is an accepted trick in Clojure world, but it certainly ain't straightforward.

;; Let's try to rewrite this stuff using (omg) mutability

(let [input input2
      n (count input)
      result (transient [])]
  (reduce (fn [last-product i]
            (conj! result last-product)
            (* last-product (get input i)))
          1 (range n))
  (reduce (fn [last-product i]
            (assoc! result i (* (get result i) last-product))
            (* last-product (get input i))
            1)
          1 (range (dec n) -1 -1))
  (persistent! result))

;; Try to uniformize a bit the algorithms for right and left part

(let [input input2
      n (count input)
      result (transient (into [] (repeat n 1)))]
  (reduce (fn [last-product i]
            (assoc! result i (* (get result i) last-product))
            (* last-product (get input i)))
          1 (range n))
  (reduce (fn [last-product i]
            (assoc! result i (* (get result i) last-product))
            (* last-product (get input i))
            1)
          1 (range (dec n) -1 -1))
  (persistent! result))

;; Let's factorize things a bit

(let [input input2
      n (count input)
      result (transient (into [] (repeat n 1)))
      reducing-fn (fn [last-product i]
                    (assoc! result i (* (get result i) last-product))
                    (* last-product (get input i)))]
  (reduce reducing-fn 1 (range n))
  (reduce reducing-fn 1 (range (dec n) -1 -1))
  (persistent! result))

;; Do we still need mutability ?

(let [input input2
      n (count input)
      reducing-fn (fn [[result last-product] i]
                    [(assoc result i (* (get result i) last-product))
                     (* last-product (get input i))])
      [mulright _] (reduce reducing-fn [(vec (repeat n 1)) 1] (range n))
      [mulleft _] (reduce reducing-fn [mulright 1] (range (dec n) -1 -1))]
  mulleft)

;; No we don't ! So the key was to uniformize the algorithms. I had to try
;; the mutable version to achieve this, don't really know why.
;; I still got some complexity I think:
;; - My accumulator is complex
;; - I use indices for accessing data
;; But I don't see possible improvement to get rid of it.

;; Here is a final version
(defn solution [input]
  (let [n (count input)
        reducing-fn (fn [[result last-product] i]
                      [(assoc result i (* (get result i) last-product))
                       (* last-product (get input i))])
        [mulright _] (reduce reducing-fn [(vec (repeat n 1)) 1] (range n))
        [mulleft _] (reduce reducing-fn [mulright 1] (range (dec n) -1 -1))]
    mulleft))

(= expected2 (solution input2))
(= expected1 (solution input1))
