(ns oph.va.server-spec
  (:use [clojure.tools.trace])
  (:require [speclj.core :refer :all]
            [org.httpkit.client :as http]
            [cheshire.core :refer :all]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [oph.va.db :as va-db]
            [oph.form.validation :as validation]
            [oph.va.spec-plumbing :refer :all]))

(def base-url "http://localhost:9000")
(defn path->url [path] (str base-url path))
(defn get! [path] @(http/get (path->url path)))
(defn put! [path body] @(http/put (path->url path) {:body (generate-string body true)
                                                    :headers {"Content-Type" "application/json"}}))
(defn post! [path body] @(http/post (path->url path) {:body (generate-string body true)
                                                      :headers {"Content-Type" "application/json"}}))
(defn json->map [body] (parse-string body true))

(defn find-by-id [json id] (first (filter (fn [e] (.equals ( :id e) id)) (-> (validation/flatten-elements (json :content))))))

(def valid-answers
  {:value [
           {:key "organization" :value "Testi Organisaatio"}
           {:key "primary-email" :value "test@example.com"}
           {:key "signature" :value "Teemu Testaaja, CEO"}
           {:key "signature-email" :value "teemu@example.com"}
           {:key "language" :value "fi"}
           {:key "combined-effort" :value "no"}
           {:key   "other-organizations"
            :value [{:key   "other-organizations-1"
                     :value [{:key "other-organizations.other-organizations-1.name" :value "E.T. Extra Terrestrial"}
                             {:key "other-organizations.other-organizations-1.email" :value "et@example"}
                             ]}]}
           {:key "project-goals" :value "Maaleja"}
           {:key "project-description.project-description-1.goal" :value "Paremmat oppimistulokset"}
           {:key "project-description.project-description-1.activity" :value "Pidämme työpajoja"}
           {:key "project-description.project-description-1.result" :value "Jotkut lähtevät jatko-opiskelemaan"}
           {:key "bank-bic" :value "5000"}
           {:key "bank-iban" :value "FI 32 5000 4699350600"}
           {:key "project-target" :value "Maali"}
           {:key "project-measure" :value "Mittaus"}
           {:key "project-announce" :value "Julkaisut"}
           {:key "project-effectiveness" :value "Tehokkuus"}
           {:key "project-spreading-plan" :value "Jakelusuunnitelma"}
           {:key "coordination-costs-row.amount" :value "10"}
           {:key "personnel-costs-row.amount" :value "10"}
           {:key "service-purchase-costs-row.amount" :value "10"}
           {:key "material-costs-row.amount" :value "10"}
           {:key "rent-costs-row.amount" :value "10"}
           {:key "equipment-costs-row.amount" :value "10"}
           {:key "steamship-costs-row.amount" :value "10"}
           {:key "other-costs-row.amount" :value "10"}
           {:key "project-incomes-row.amount" :value "10"}
           {:key "eu-programs-income-row.amount" :value "10"}
           {:key "other-public-financing-income-row.amount" :value "10"}
           {:key "private-financing-income-row.amount" :value "10"}
           ]})

(def most-required-missing
  {:other-organizations.other-organizations-1.email      [{:error "required"}]
   :project-begin                                        []
   :other-organizations.other-organizations-1.name       [{:error "required"}]
   :primary-email                                        []
   :signature                                            [{:error "required"}]
   :project-description.project-description-1.goal     [{:error "required"}]
   :project-description.project-description-1.activity [{:error "required"}]
   :project-description.project-description-1.result   [{:error "required"}]
   :continuation-project                                 []
   :bank-bic                                             [{:error "required"}]
   :organization                                         []
   :project-effectiveness                                [{:error "required"}]
   :project-spreading-plan                               [{:error "required"}]
   :project-measure                                      [{:error "required"}]
   :combined-effort                                      [{:error "required"}]
   :language                                             [{:error "required"}]
   :project-www                                          []
   :signature-email                                      [{:error "required"}]
   :bank-iban                                            [{:error "required"}]
   :project-goals                                        [{:error "required"}]
   :project-target                                       [{:error "required"}]
   :other-funding                                        []
   :other-partners                                       []
   :project-announce                                     [{:error "required"}]
   :project-end                                          []
   :vat-included                                         []
   :coordination-costs-row.description                   []
   :coordination-costs-row.amount                        [{:error "required"}]
   :personnel-costs-row.description                      []
   :personnel-costs-row.amount                           [{:error "required"}]
   :service-purchase-costs-row.description               []
   :service-purchase-costs-row.amount                    [{:error "required"}]
   :material-costs-row.description                       []
   :material-costs-row.amount                            [{:error "required"}]
   :rent-costs-row.description                           []
   :rent-costs-row.amount                                [{:error "required"}]
   :equipment-costs-row.description                      []
   :equipment-costs-row.amount                           [{:error "required"}]
   :steamship-costs-row.description                      []
   :steamship-costs-row.amount                           [{:error "required"}]
   :other-costs-row.description                          []
   :other-costs-row.amount                               [{:error "required"}]
   :project-incomes-row.description                      []
   :project-incomes-row.amount                           [{:error "required"}]
   :eu-programs-income-row.description []
   :eu-programs-income-row.amount [{:error "required"}]
   :other-public-financing-income-row.description []
   :other-public-financing-income-row.amount [{:error "required"}]
   :private-financing-income-row.description []
   :private-financing-income-row.amount [{:error "required"}]})

(describe "HTTP server"

  (tags :server)

  ;; Start HTTP server for running tests
  (around-all [_] (with-test-server! (_)))

  (it "GET should return valid form JSON from route /api/form/1"
      (let [{:keys [status headers body error] :as resp} (get! "/api/form/1")
            json (json->map body)]
        (should= 200 status)
        (should= 1 (-> json :id))
        (should= {:type "infoElement"
                  :id "name"
                  :displayAs "h1"} (-> json :content first))
        (should= {:type "infoElement"
                  :id "duration"
                  :displayAs "endOfDateRange"
                  :label {:fi "Hakuaika päättyy"
                          :sv "Sista ansöknings"}} (-> json :content second))
        (should= {:type "formField"
                  :id "organization"
                  :required true
                  :displayAs "textField"
                  :params {:size "large"
                           :maxlength 80}
                  :label {:fi "Hakijaorganisaatio"
                          :sv "Organisation"}} (find-by-id json "organization"))
        (should= {:type "infoElement"
                  :id "selection-criteria"
                  :displayAs "bulletList"
                  :params {:initiallyOpen true}} (find-by-id json "selection-criteria"))
        ))

  (it "GET should return not-found from route /api/form/1/values/1"
      (let [{:keys [status headers body error] :as resp} (get! "/api/form/1/values/1")]
        (should= 404 status)))

  (it "PUT should validate required values when done to route /api/form/1/values"
      (let [{:keys [status headers body error] :as resp} (put! "/api/form/1/values" {:value [ {:key "organization" :value "Testi Organisaatio" }
                                                                                              {:key "primary-email" :value "test@example.com" }
                                                                                              {:key "signature " :value "" }
                                                                                             ] } )
            json (json->map body)]
        (should= 400 status)
        (should= most-required-missing json)))

  (it "PUT should validate text field lengths when done to route /api/form/1/values"
      (let [{:keys [status headers body error] :as resp} (put! "/api/form/1/values" (update-in valid-answers [ :value ] concat [ {:key "project-end" :value "10.10.10000"} ]))
            json (json->map body)]
        (should= 400 status)
        (should= (json :project-end) [{:error "maxlength", :max 10}])))

  (it "PUT should validate text field lengths and options when done to route /api/avustushaku/1/hakemus"
      (let [{:keys [status headers body error] :as resp} (put! "/api/avustushaku/1/hakemus" {:value [{:key "language" :value "ru"}
                                                                                                     {:key "project-end" :value "10.10.10000"}  ]})
            json (json->map body)]
        (should= 400 status)
        (should= (json :language) [{:error "invalid-option"}])
        (should= (json :project-end) [{:error "maxlength", :max 10}])))

  (it "PUT should create a new form submission and return id when done to route /api/form/1/values"
      (let [{:keys [status headers body error] :as resp} (put! "/api/form/1/values" valid-answers)
            json (json->map body)]
        (should= 200 status)
        (should= 1 (:id json))))

  (it "POST should validate required values when done to route /api/form/1/values/1"
      (let [{:keys [status headers body error] :as resp} (post! "/api/form/1/values/1" {:value [{:key "organization" :value "Testi Organisaatio"}
                                                                                                {:key "primary-email" :value "test@example.com"}
                                                                                                ]})
          json (json->map body)]
      (should= 400 status)
      (should= most-required-missing json)))

  (it "POST should fail if done to non-existing node /api/form/1/values/2"
      (let [{:keys [status headers body error] :as resp} (post! "/api/form/1/values/2" valid-answers)]
        (should= 404 status)))

  (it "POST should create a new form submission version when done to route /api/form/1/values/1"
      (let [{:keys [status headers body error] :as resp} (post! "/api/form/1/values/1" valid-answers)
            json (json->map body)]
        (should= 200 status)
        (should= 1 (:version json))
        (let [{:keys [status headers body error] :as resp} (post! "/api/form/1/values/1" valid-answers)
              json (json->map body)]
          (should= 200 status)
          (should= 2 (:version json)))))

  (it "GET should always return latest form submission version for /api/form/1/values/1"
      (let [{:keys [status headers body error] :as resp} (get! "/api/form/1/values/1")
            json (json->map body)]
        (should= 200 status)
        (should= 2 (:version json))))

  (it "PUT /api/avustushaku/1/hakemus/ should return hakemus status"
      (let [{:keys [status headers body error] :as resp} (put! "/api/avustushaku/1/hakemus" {:value [{:key "language" :value "sv"}
                                                                                                     {:key "primary-email" :value "testi@test.te"}  ]})
            json (json->map body)]
        (should= 200 status)
        (should= "draft" (:status json))))

  (it "POST to /api/avustushaku/1/hakemus/<id>/verify/<key> should faild to verify wrong key"
      (let [hakemus-from-db (va-db/get-hakemus-internal 1)
            url (str "/api/avustushaku/1/hakemus/" (:user_key hakemus-from-db) "/verify/x")
            {:keys [status headers body error] :as resp} (post! url {})]
        (should= nil (:verified_at hakemus-from-db))
        (should= 400 status)
        (should= "x" body)))

  (it "POST to /api/avustushaku/1/hakemus/<id>/verify/<key> should verify right key"
    (let [hakemus-from-db (va-db/get-hakemus-internal 1)
          url (str "/api/avustushaku/1/hakemus/" (:user_key hakemus-from-db) "/verify/" (:verify_key hakemus-from-db))
          {:keys [status headers body error] :as resp} (post! url {})
          json (json->map body)
          hakemus-from-db-after (va-db/get-hakemus-internal 1)
          unparse-instant (fn [inst] (->> inst
                                          l/to-local-date-time
                                          (f/unparse (f/formatters :date-time-no-ms))))]
      (should= 200 status)
      (should= nil (:verified_at hakemus-from-db))
      (should= (unparse-instant (:verified_at hakemus-from-db-after)) (:verified_at json))))

  (it "POST to /api/avustushaku/1/hakemus/<id>/verify/<key> with gith key should return hakemus even if verified before"
    (let [hakemus-from-db (va-db/get-hakemus-internal 1)
          url (str "/api/avustushaku/1/hakemus/" (:user_key hakemus-from-db) "/verify/" (:verify_key hakemus-from-db))
          {:keys [status headers body error] :as resp} (post! url {})
          hakemus-from-db-after (va-db/get-hakemus-internal 1)]
      (should= 200 status)
      (should= (:verified_at hakemus-from-db) (:verified_at hakemus-from-db-after))))

  (it "POST to /api/avustushaku/1/hakemus/<id>/verify/<key> with wrong key should fail hakemus even when already verified"
    (let [hakemus-from-db (va-db/get-hakemus-internal 1)
          url (str "/api/avustushaku/1/hakemus/" (:user_key hakemus-from-db) "/verify/x")
          {:keys [status headers body error] :as resp} (post! url {})
          hakemus-from-db-after (va-db/get-hakemus-internal 1)]
      (should= 400 status)
      (should= "x" body)
      (should= (:verified_at hakemus-from-db) (:verified_at hakemus-from-db-after)))))
(run-specs)
