(ns oph.va.server-spec
  (:use [clojure.tools.trace])
  (:require [speclj.core :refer :all]
            [oph.va.server :refer :all]
            [org.httpkit.client :as http]
            [cheshire.core :refer :all]))

(def base-url "http://localhost:9000")
(defn path->url [path] (str base-url path))
(defn get! [path] @(http/get (path->url path)))
(defn json->map [body] (parse-string body true))

(describe "HTTP server route /"

  ;; Start HTTP server for running tests
  (around-all [_]
              (let [stop-server (start-server "localhost" 9000 false)]
                (try (_) (finally (stop-server)))))

  (it "GET should return valid JSON"
      (let [{:keys [status headers body error] :as resp} (get! "/api")]
        (should= 200 status)
        (should= {:id 1, :name "lolbal"} (json->map body)))))
(run-specs)