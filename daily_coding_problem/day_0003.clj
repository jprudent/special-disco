;; # Day 3

(ns day-0003
  (:require [clojure.tools.reader.edn :as edn]))

;; This problem was asked by Google.
;;
;; Given the root to a binary tree,
;; implement `serialize(root)`, which serializes the tree into a string,
;; and `deserialize(s)`, which deserializes the string back into the tree.

;; For example, given the following `Node` class (Python code)

;; ```python
;; class Node:
;;   def __init__(self, val, left=None, right=None):
;;     self.val = val
;;     self.left = left
;;     self.right = right
;; ```

;; The following test should pass:
;; ```python
;; node = Node('root', Node('left', Node('left.left')), Node('right'))
;; assert deserialize(serialize(node)).left.left.val == 'left.left'
;; ```

;; ## What's the point ?

;; I don't know what the interviewer are trying to test here,
;; there is a lot of place for talking:
;; - should it be stored somewhere ?
;; - should it be plain text ?
;; - should it be optimized for space ?
;; ...

;; With Clojure, I would just argue data is code:
(def serialize pr-str)
;; Just aliasing `pr-str` which is a function that returns a string ready to
;; be read again.

;; And I would argue that code is data:
(def deserialize edn/read-string)
;; Here I alias `edn/read-string` which takes a string and returns the corresponding
;; datastructure. Clojure core has a `read-string` function but I stick with the EDN version
;; of it that limits some code injection capabilities.

(def serdes (comp deserialize serialize))


;; ### The map version
(let [node {:val "root"
            :left {:val "left"
                   :left {:val "left.left"}}
            :right {:val "right"}}]
  (= "left.left"
     (-> (serdes node) :left :left :val)
     (get-in (serdes node) [:left :left :val])))

;; ### The vector version
(let [[VAL LEFT RIGHT] [0 1 2]
      node ["root"
            ["left" ["left.left"]]
            ["right"]]]
  (= "left.left" (get-in (serdes node) [LEFT LEFT VAL])))

;; ## Oh ... wait...

;; The point was tree walking, not serialization per se.

;;; ### Out of the box

;; Clojure got walks function

(defn serialize
  [node]
  (clojure.walk/postwalk (fn [x]
                           (if (map? x)
                             ((juxt :val :left :right) x)
                             x))
                         node))

(defn deserialize
  [node]
  (clojure.walk/postwalk (fn [x]
                          (prn x)
                          (if (vector? x)
                            (zipmap [:val :left :right] x)
                            x))
                        node))

(def node {:val "root"
           :left {:val "left"
                  :left {:val "left.left" :left nil :right nil}
                  :right nil}
           :right {:val "right" :left nil :right nil}})

(def ser-node (serialize node))

(def des-ser-node (deserialize ser-node))

(= des-ser-node node)


;; ## Oh ... wait...

;; The point was tree walking, not serialization per se.

;;; ### Out of the box solution

;; Clojure got generic walks function to browse datastructure recursively

(def node {:val "root"
           :left {:val "left"
                  :left {:val "left.left" :left nil :right nil}
                  :right nil}
           :right {:val "right" :left nil :right nil}})

;; Here is a function that serializes the tree as vector,
;; but we could choose any other serialization format, like a string,
;; but like I said, that's not the point.
(def serialize-node (juxt :val :left :right))
(defn serialize
  [node]
  (clojure.walk/postwalk #(if (map? %) (serialize-node %) %) node))

(def ser-node (serialize node))

;; And this is the deserialize function that takes the vector version
;; and serialize back to the map version.
(def deserialize-node (partial zipmap [:val :left :right]))
(defn deserialize
  [node]
  (clojure.walk/postwalk #(if (vector? %) (deserialize-node %) %) node))

(def des-ser-node (deserialize ser-node))

;; let's check that's correct
(= des-ser-node node)

;; ### How postwalk is implemented ?

;; Let's peek source code for post-walk (I kept only the parts I'm interested in)

(defn walk
  [inner outer form]
  (cond
    (instance? clojure.lang.IMapEntry form)
    (outer (clojure.lang.MapEntry/create (inner (key form)) (inner (val form))))
    (coll? form) (outer (into (empty form) (map inner form)))
    :else (outer form)))

(defn postwalk
  [f form]
  (walk (partial postwalk f) f form))

;; Except for `walk` that is a bit complex because it returns structure of
;; the same type, the implementation is really straightforward. It recursively
;; applies postwalk to each element of the data structure.

;; The main drawback of this implementation is that it's not tail recursive.
;; That means there is a hard limit on the depth of the tree.

;;; ### Hitting the limits

;; Lets find something very deep, like a node that would contain
;; 2000 times itself on the right branch

(defn ->node [val] {:val val :left nil :right nil})
(def deep-tree
  (reduce (fn [acc i]
            (let [root (->node (str "root" i))]
              (assoc root :right acc)))
          (->node "init")
          (range 2000)))

;; Now trying to `walk` that tree

(def deep-ser (try (serialize deep-tree)
                   (catch StackOverflowError _ :stack-overflow!)))

;; Calls to `postwalk` keep stacking until hard limit of JVM is hit
;; I'll provide a better implementation of tree walking in another adventure.