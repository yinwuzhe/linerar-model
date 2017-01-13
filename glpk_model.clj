;;这里只是摘取部分代码 描述了glpk的整个的建模编程过程；需要在project.clj或者boot.dev加入依赖包[org.gnu.glpk/glpk-java "1.7.0" ]
(ns erp.resplan.intraconnect
    
    (:use korma.db)
    (:use [korma.core :exclude [update]])
    (:use loco.core loco.constraints)
    (:use clojure.pprint)
    (:require [com.stuartsierra.component :as component]
      [clojure.tools.logging :as log]
      [clojure.set :as set]
      [lonocloud.synthread :as ->]
      [schema.core :as schema :refer [optional-key Any Int]]
      [cheshire.core :as j]
      [cheshire.generate :as jg]
      [clojure.walk :as w]
      [clojure.math.numeric-tower :as math]
      )
    (:import java.text.SimpleDateFormat)
    (:import java.util.Date)
    (:import java.util.Objects)
    (:import [org.gnu.glpk GLPK GLPKConstants SWIGTYPE_p_double SWIGTYPE_p_int glp_prob glp_iocp]))
    
(defn glpk-model
      [device-type a-A-group a-B-group ca cb costs-array]
      (let [lp (doto (GLPK/glp_create_prob)
                     (GLPK/glp_set_prob_name "res-plan")
                     (GLPK/glp_set_obj_name "obj")
                     (GLPK/glp_set_obj_dir GLPKConstants/GLP_MIN)
                     (GLPK/glp_add_rows (+ ca cb))
                     (GLPK/glp_add_cols (* ca cb)))
            ;;add col details
            _ (doseq [i (range ca)]
                     (doseq [j (range cb)]
                            (doto lp
                                  (GLPK/glp_set_col_kind (+ j 1 (* i cb)) GLPKConstants/GLP_IV)
                                  (GLPK/glp_set_col_bnds (+ j 1 (* i cb)) GLPKConstants/GLP_DB 0 (min
                                                                                                   (handle-num-M (:Amount (get a-A-group i)))
                                                                                                   (handle-num-M (:Amount (get a-B-group j)))))
                                  (GLPK/glp_set_obj_coef (+ j 1 (* i cb)) (aget costs-array i j))
                                  )
           ))
            ;;;还需要设置row的约束矩阵
            _ (doseq [i (range ca)]
                     (let [ind (GLPK/new_intArray (+ 1 cb))
                           val (GLPK/new_doubleArray (+ 1 cb))]
                          (GLPK/glp_set_row_bnds lp (+ i 1) GLPKConstants/GLP_FX (handle-num-M (:Amount (get a-A-group i))) 0)

                          (doseq [j (range cb)]
                                 (GLPK/intArray_setitem ind (+ 1 j) (+ (* cb i) (+ 1 j)))
                                 (GLPK/doubleArray_setitem val (+ 1 j) 1.0)
                                 )
                          (GLPK/glp_set_mat_row lp (+ 1 i) cb ind val)
                          #_(log/info "row consts1:" (GLPK/glp_get_mat_row lp (+ 1 i) nil nil))
                          ))
            _ (doseq [j (range cb)]
                     (let [ind (GLPK/new_intArray (+ 1 ca))
                           val (GLPK/new_doubleArray (+ 1 ca))]
                          (GLPK/glp_set_row_bnds lp (+ ca j 1) GLPKConstants/GLP_UP 0 (handle-num-M (:Amount (get a-B-group j))))
                          (doseq [i (range ca)]
                                 (GLPK/intArray_setitem ind (+ 1 i) (+ (* cb i) (+ 1 j)))
                                 (GLPK/doubleArray_setitem val (+ 1 i) 1.0)
                                 )
                          (GLPK/glp_set_mat_row lp (+ ca j 1) ca ind val)

                          ))
            ;;solve
            iocp (glp_iocp.)

            _ (doto iocp
                    (GLPK/glp_init_iocp)
                    (.setPresolve GLPKConstants/GLP_ON))
            ret (time (GLPK/glp_intopt lp iocp))

            ;;需要将输出的数据存储到数据库之中，便于对结果进行处理，目前看来这结果就是最优的(还需要判断是最优的吗)
            _ (dao/with
                db2
                (doseq [i (range ca)]
                       (doseq [j (range cb)]
                              (let [val (GLPK/glp_mip_col_val lp (+ j 1 (* cb i)))
                                    ;_ (log/info "alloc i,j[" i "," j "]:" val)
                                    ]
                                   (-> (update* :bas_strategy_model_resource_plan)
                                       (set-fields {:X2YAlloc val})
                                       (where {:DeviceType device-type
                                               :Xaxis i
                                               :Yaxis j})
                                       exec)
                                   ))))
            val (time (GLPK/glp_mip_obj_val lp))
            _ (log/info "the result:" val)

            _ (GLPK/glp_delete_prob lp)
            ]))
