(ns day-0001)

;; # Day 1

;; ## The problem
;;Good morning! Here's your coding interview problem for today.
;;This problem was recently asked by Google.
;;Given a list of numbers and a number k, return whether any two numbers from
;; the list add up to k.

;;For example, given `[10, 15, 3, 7]` and `k` of `17`,

(def numbers [10, 15, 3, 7])
(def k 17)

;; return `true` since 10 + 7 is 17.

;; Bonus: Can you do this in one pass?

;; ## The easy solution

;; Doing cartesian product with `for` is easy

(some? (first (for [x numbers
                    y numbers
                    :when (= k (+ x y))]
                [x y])))

;; This solution complexity is O(n²).

;; Also, note that since `for` returns a lazy sequence, computation
;; stops when the first element is computed.


;; A possible improvement is:

(for [i (range (count numbers))
      :let [x (nth numbers i)]
      y (subvec numbers (inc i))
      :when (= k (+ x y))]
  [x y])

;; That cuts in half the problem space.

;; ## Can I do that in one pass ?

;; Doing things in O(n) involves either
;; - an arithmetic trick
;; - some kind of in memory data structure to maintain

;; I store the result of k subtracted to each number in a set.
;; If a number is in this set (that check is performed in O(1)) the computation stops.

(true? (reduce (fn [previous-numbers number]
                 (if (contains? previous-numbers number)
                   (reduced true)
                   (conj previous-numbers (- k number))))
               #{} numbers))

;; Note that `reduced` is used as control flow to stop the processing once
;; a solution is found


