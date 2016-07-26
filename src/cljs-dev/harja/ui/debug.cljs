(ns harja.ui.debug
  "UI komponentti datan inspectointiin"
  (:require [harja.ui.yleiset :as yleiset]
            [reagent.core :refer [atom]]))

(defn voi-avata? [item]
  (some #(% item) [map? coll?]))

(defmulti debug-show (fn [item path open-paths toggle!]
                       (cond
                         (map? item) :map
                         (coll? item) :coll
                         :default :pr-str)))


(defn- show-value [value p open-paths toggle!]
  (if (voi-avata? value)
    [:span
     (if (open-paths p)
       (debug-show value p open-paths toggle!)
       (let [printed (pr-str value)]
         (if (> (count printed) 100)
           (str (subs printed 0 100) " ...")
           printed)))]
    [:span (pr-str value)]))

(defn- avaus-solu [value p open-paths toggle!]
  (if (voi-avata? value)
    [:td {:on-click #(toggle! p)}
     (if (open-paths p)
       "\u25bc"
       "\u25b6")]
    [:td " "]))

(defmethod debug-show :coll [data path open-paths toggle!]
  [:table.debug-coll
   [:thead [:th "#"] [:th " "] [:th "Value"]]
   [:tbody
    (doall
     (map-indexed
      (fn [i value]
        [:tr
         [:td i " "]
         (avaus-solu value (conj path i) open-paths toggle!)
         [:td (show-value value (conj path i) open-paths toggle!)]])
      data))]])

(defmethod debug-show :map [data path open-paths toggle!]
  [:table.debug-map
   [:thead
    [:tr [:th "Key"] [:th " "] [:th "Value"]]]
   [:tbody
    (for [[key value] (sort-by first (seq data))
          :let [p (conj path key)]]
      ^{:key key}
      [:tr
       [:td (pr-str key)]
       (avaus-solu value p open-paths toggle!)
       [:td
        (show-value value p open-paths toggle!)]])]])

(defmethod debug-show :pr-str [data _ _ _]
  [:span (pr-str data)])

(defn debug [item]
  (let [open-paths (atom #{})
        toggle! #(swap! open-paths
                        (fn [paths]
                          (if (paths %)
                            (disj paths %)
                            (conj paths %))))]
    (fn [item]
      [:div.debug
       [debug-show item [] @open-paths toggle!]])))
