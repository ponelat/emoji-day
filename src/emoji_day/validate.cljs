(ns emoji-day.validate
  (:require [clojure.string :as s]))

(def days [:sunday :monday :tuesday :wednesday :thursday :friday :saturday])
(def weekend #{:saturday :sunday})
(def weekdays #{:monday :tuesday :wednesday :thursday :friday})
(defn weekday? [day] (contains? weekdays day))
(defn weekend? [day] (contains? weekend day))

;; Enough primes and fibs for now
(def primes-under-1000 #{2 3 5 7 11 13 17 19 23 29 31 37 41 43 47 53 59 61 67 71 73 79 83 89 97 101 103 107 109 113 127 131 137 139 149 151 157 163 167 173 179 181 191 193 197 199 211 223 227 229 233 239 241 251 257 263 269 271 277 281 283 293 307 311 313 317 331 337 347 349 353 359 367 373 379 383 389 397 401 409 419 421 431 433 439 443 449 457 461 463 467 479 487 491 499 503 509 521 523 541 547 557 563 569 571 577 587 593 599 601 607 613 617 619 631 641 643 647 653 659 661 673 677 683 691 701 709 719 727 733 739 743 751 757 761 769 773 787 797 809 811 821 823 827 829 839 853 857 859 863 877 881 883 887 907 911 919 929 937 941 947 953 967 971 977 983 991 997})
(defn prime-under-1000? [n]
  (contains? primes-under-1000 n))
(defn power-of-2? [n]
  (cond
    (> 1 n) false
    (= 1 n) true
    (= 1 (mod n 2)) false
    true (power-of-2? (/ n 2))))

(defn fib?
  ([n] (cond
         (< n 1) false
         (= n 1) true
         true (fib? n [1 1])))
  ([n fibs]
   (let* [x (nth fibs (- (count fibs) 2))
          y (last fibs)
          next-fib (+ x y)]
     (cond
       (= n next-fib) true
       (<= n next-fib) false
       true (fib? n (conj fibs next-fib))))))

(defn to-emoji-str [k]
  "Convert keywork like `:monday` into an emoji string like `\":monday:\"` "
  (str k ":"))

(defn to-day-str [k]
  (case k
      :monday "Monday"
      :tuesday "Tuesday"
      :wednesday "Wednesday"
      :thursday "Thursday"
      :friday "Friday"
      :saturday "Saturday"
      :sunday "Sunday"))

(defn count-strs [match s]
  "Count how many k(string) in s(string)."
  (count (re-seq (re-pattern match) s)))

(defn count-emoji [emoji s]
  "Count how many emoji(keyword) in s(string)."
  (count-strs (to-emoji-str emoji) s))

(defn remove-emoji [emoji s]
  (s/replace s (to-emoji-str emoji) ""))

(defn emoji-valid-count? [emoji n]
  (case emoji
      :monday (= 1 n)
      :tuesday (< 0 n)
      :wednesday (prime-under-1000? n)
      :thursday (fib? n)
      :friday (power-of-2? n)
      false))

(defn emoji-string-clean? [emoji s]
  (->> s
       (remove-emoji emoji)
       (s/trim)
       (count)
       (= 0)))

(defn get-day []
  (nth days (.getDay (new js/Date))))

(defn str-is-most-likely-day [s]
  false)

(def first-day-match (re-pattern (str ".*?(" (s/join "|" days) ":).*")))
(defn day-to-emoji [string]
  (if (string? string)
    (keyword
     (s/replace-first string ":" ""))))

(defn find-first-day-emoji [string]
  (day-to-emoji
   (nth (re-matches first-day-match string) 1)))

(def example-report
  {:match-day :monday
   :actual-day :saturday
   :valid true
   :count 1
   :nth nil
   :has-extra-text false
   :has-extra-emoji false})

(defn validate-report-base [string]
  (let [match-day (find-first-day-emoji string)]
    {:match-day match-day
     :actual-day (get-day)
     :count (count-emoji match-day string)
     :trying-weekend (weekend? match-day)
     :is-empty (empty? string)
     }))

(defn validate-report [string]
  (let* [report (validate-report-base string)
         valid-count (emoji-valid-count?
                      (:match-day report)
                      (:count report))
         is-clean (emoji-string-clean? (:match-day report) string)
         valid (and is-clean valid-count)]
    (assoc report
           :valid-count valid-count
           :is-clean is-clean
           :just-dirty (and (not is-clean) valid-count)
           :valid valid)))
