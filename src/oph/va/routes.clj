(ns oph.va.routes
  (:use [clojure.tools.trace :only [trace]])
  (:require [compojure.route :as route]
            [ring.util.http-response :refer :all]
            [ring.util.response :as resp]
            [compojure.core :refer [GET]]
            [compojure.api.sweet :refer :all]
            [schema.core :as s]
            [oph.va.db :as db]
            [oph.va.validation :as validation]))

(s/defschema LocalizedString {:fi s/Str
                              :sv s/Str})

(s/defschema Option {:value s/Str
                     (s/optional-key :label) LocalizedString})

(s/defschema InfoElement {:type (s/eq "infoElement")
                          :id s/Str
                          :displayAs (s/enum :h1
                                             :bulletList
                                             :dateRange
                                             :endOfDateRange)
                          (s/optional-key :params) s/Any
                          (s/optional-key :label) LocalizedString})

(s/defschema FormField {:type (s/eq "formField")
                        :id s/Str
                        :required s/Bool
                        :label LocalizedString
                        (s/optional-key :params) s/Any
                        (s/optional-key :options) [Option]
                        :displayAs (s/enum :textField
                                           :textArea
                                           :dropdown
                                           :radioButton)})

(s/defschema BasicElement (s/either FormField
                                    InfoElement))

(s/defschema WrapperElement {:type (s/eq "wrapperElement")
                             :id s/Str
                             :displayAs (s/enum :theme)
                             :children  [(s/either BasicElement
                                                   (s/recursive #'WrapperElement))]
                             (s/optional-key :params) s/Any
                             (s/optional-key :label) LocalizedString})

(s/defschema Content [(s/either BasicElement
                                WrapperElement)])

(s/defschema Form {:id Long,
                   :content Content,
                   :start s/Inst})

(s/defschema AvustusHaku {:id Long
                          :content s/Any
                          :form Long
                          :submittime s/Inst})

(s/defschema Submission
  "Submission consists of a flat field id to value mapping"
  {s/Keyword s/Str})

(s/defschema SubmissionValidationErrors
  "Submission validation errors contain a mapping from field id to list of validation errors"
  {s/Keyword [s/Str]})

(s/defschema SubmissionId
  "Submission id contains id of the newly created submission"
  {:id Long})

(s/defschema HakemusId
  "Hakemus id contains id of the newly created hakemus"
  {:id s/Str})

(defn- create-form-submission [form-id answers]
  (let [submission (db/create-submission! form-id answers)]
    (if submission
      (ok {:id submission})
      (internal-server-error!))))

(defn- get-form-submission [form-id values-id]
  (let [submission (db/get-form-submission form-id values-id)]
    (if submission
      (ok (:answers submission))
      (not-found))))

(defn- update-form-submission [form-id values-id answers]
  (if (not (db/submission-exists? form-id values-id))
    (not-found)
      (let [submission (db/update-submission! form-id values-id answers)]
        (if submission
          (ok submission)
          (internal-server-error!)))))

(defroutes* api-routes
  "API implementation"

  (GET* "/form" []
        :return [Form]
        (ok (db/list-forms)))

  (GET* "/avustushaku/:id" [id]
        :path-params [id :- Long]
        :return AvustusHaku
        (let [avustushaku (db/get-avustushaku id)]
          (if avustushaku
            (ok avustushaku)
            (not-found))))

  (GET* "/form/:id" [id]
        :path-params [id :- Long]
        :return Form
        (let [form (db/get-form id)]
          (if form
            (ok form)
            (not-found))))

  (GET* "/form/:form-id/values/:values-id" [form-id values-id]
        :path-params [form-id :- Long, values-id :- Long]
        :return  Submission
        :summary "Get current answers"
        (get-form-submission form-id values-id))

  (PUT* "/form/:form-id/values" [form-id :as request]
        :path-params [form-id :- Long]
        :body    [answers (describe Submission "New answers")]
        :return  (s/either SubmissionId
                           SubmissionValidationErrors)
        :summary "Create initial form answers"
        (let [validation (validation/validate-form (db/get-form form-id) answers)]
          (if (every? empty? (vals validation))
            (create-form-submission form-id answers)
            (bad-request validation))))

  (POST* "/form/:form-id/values/:values-id" [form-id values-id :as request]
         :path-params [form-id :- Long, values-id :- Long]
         :body    [answers (describe Submission "New answers")]
         :return  (s/either Submission
                            SubmissionValidationErrors)
         :summary "Update form values"
         (let [validation (validation/validate-form (db/get-form form-id) answers)]
           (if (every? empty? (vals validation))
             (update-form-submission form-id values-id  answers)
             (bad-request validation))))

  (PUT* "/avustushaku/:haku-id/hakemus" [haku-id :as request]
      :path-params [haku-id :- Long]
      :body    [answers (describe Submission "New answers")]
      :return  HakemusId
      :summary "Create initial hakemus"
      (let [form-id (:form (db/get-avustushaku haku-id))]
        (let [hakemus-id (db/create-hakemus! form-id answers)]
          (if hakemus-id
            (ok hakemus-id)
            (internal-server-error!)))))

  (GET* "/avustushaku/:haku-id/hakemus/:hakemus-id" [haku-id hakemus-id]
        :path-params [haku-id :- Long, hakemus-id :- s/Str]
        :return  Submission
        :summary "Get current answers"
        (let [form-id (:form (db/get-avustushaku haku-id))]
          (let [hakemus (db/get-hakemus hakemus-id)]
            (get-form-submission form-id (:form_submission hakemus)))))

  (POST* "/avustushaku/:haku-id/hakemus/:hakemus-id" [haku-id hakemus-id :as request]
       :path-params [haku-id :- Long, hakemus-id :- s/Str]
       :body    [answers (describe Submission "New answers")]
       :return  Submission
       :summary "Update hakemus values"
         (let [form-id (:form (db/get-avustushaku haku-id))]
           (let [hakemus (db/get-hakemus hakemus-id)]
             (update-form-submission form-id (:form_submission hakemus) answers))))

  (POST* "/avustushaku/:haku-id/hakemus/:hakemus-id/submit" [haku-id hakemus-id :as request]
       :path-params [haku-id :- Long, hakemus-id :- s/Str]
       :body    [answers (describe Submission "New answers")]
       :return  Submission
       :summary "Update hakemus values"
         (let [form-id (:form (db/get-avustushaku haku-id))]
           (let [validation (validation/validate-form (db/get-form form-id) answers)]
             (if (every? empty? (vals validation))
               (let [hakemus (db/get-hakemus hakemus-id)]
                 (let [saved-answers (update-form-submission form-id (:form_submission hakemus) answers)]
                   (db/submit-hakemus hakemus-id)
                   saved-answers))
               (bad-request validation))))))

(defroutes* doc-routes
  "API documentation browser"
  (swagger-ui))

(defapi all-routes
  {:formats [:json-kw]}

  ;; swagger.json generation
  (swagger-docs {:info {:title "Valtionavustus API"}})

  ;; Route all requests with API prefix to API routes
  (context "/api" [] api-routes)

  ;; Documentation
  (context "/doc" [] doc-routes)

  (GET "/" [](charset (content-type (resp/resource-response "index.html" {:root "public"}) "text/html") "utf-8"))
  (route/resources "/" {:mime-types {"html" "text/html; charset=utf-8"}})
  (route/not-found "<p>Page not found.</p>"))
