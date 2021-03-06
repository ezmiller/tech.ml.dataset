(ns tech.v3.dataset.metamorph
  (:require [tech.v3.dataset :as ds]
            [tech.v3.dataset.modelling :as ds-mod]
             [tech.v3.protocols.dataset :as prot]))

(defn dataset?
  "Is `ds` a `dataset` type?"
  [ds]
  (satisfies? prot/PColumnarDataset ds))

(defmacro build-pipelined-function
  [f m]
  (let [args (map (comp vec rest) (:arglists m))]
    `(defn ~(symbol (name f))
       ~@(for [arg args
               :let [narg (mapv #(if (map? %) 'options %) arg)
                     [a & r] (split-with (partial not= '&) narg)]]
           (list narg `(fn [ds#]
                         (let [ctx# (if (dataset? ds#)
                                      {:metamorph/data ds#} ds#)]
                           (assoc ctx# :metamorph/data (apply ~f (ctx# :metamorph/data) ~@a ~(rest r))))))))))



(def ^:private includes-dataset
  '#{categorical->number categorical->one-hot})



(defmacro ^:private process-all-api-symbols-dataset
  []
  (let [ps (ns-publics 'tech.v3.dataset)]
    `(do ~@(for [[f v] ps
                 :when (includes-dataset f)
                 :let [m (meta v)
                       f (symbol "tech.v3.dataset" (name f))]]
             `(build-pipelined-function ~f ~m)))))


(def ^:private excludes-dataset-modelling
  '#{ds-mod/inference-column?})

(defmacro ^:private process-all-api-symbols-dataset-modelling
  []
  (let [ps (ns-publics 'tech.v3.dataset.modelling)]
    `(do ~@(for [[f v] ps
                 :when (not (excludes-dataset-modelling f))
                 :let [m (meta v)
                       f (symbol "tech.v3.dataset.modelling" (name f))]]
             `(build-pipelined-function ~f ~m)))))



(process-all-api-symbols-dataset)
(process-all-api-symbols-dataset-modelling)

(comment
  (ns-publics 'tech.v3.dataset.metamorph)


  (def data
    (ds/->dataset {:a [1]}))

  (

   (feature-ecount)
   {
    :metamorph/data  data}
   )

  )
