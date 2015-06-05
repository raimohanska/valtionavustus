(ns oph.va.db.queries
  (:require [yesql.core :refer [defquery]]))

(defquery list-forms "sql/form/list.sql")
(defquery get-form "sql/form/get.sql")

(defquery submission-exists? "sql/submission/exists.sql")
(defquery create-submission<! "sql/submission/create.sql")
(defquery update-submission<! "sql/submission/update.sql")
(defquery get-form-submission "sql/submission/get.sql")

(defquery get-avustushaku "sql/avustushaku/get.sql")
