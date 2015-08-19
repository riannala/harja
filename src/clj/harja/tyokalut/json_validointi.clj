(ns harja.tyokalut.json_validointi
  (:require [harja.palvelin.integraatiot.api.tyokalut.virheet :as virheet]
            [clojure.java.io :as io]
            [taoensso.timbre :as log]
            [clojure.string :as str])
  (:use [slingshot.slingshot :only [try+ throw+]])
  (:import
    (com.github.fge.jsonschema.main JsonSchemaFactory)
    (com.github.fge.jackson JsonLoader)
    (com.github.fge.jsonschema.core.report ProcessingReport)
    (com.github.fge.jsonschema.core.load.configuration LoadingConfiguration)
    (com.github.fge.jsonschema.core.load.uri URITranslator URITranslatorConfiguration)))

(defn kasittele-validointivirheet
  [^ProcessingReport validointiraportti]
  (let [validointi-virheet (str/join validointiraportti)]
    (log/error "JSON ei ole validia. Validointivirheet: " validointi-virheet)
    (throw+ {:type virheet/+invalidi-json+
             :virheet [{:koodi  virheet/+invalidi-json-koodi+
                        :viesti (str/replace (str/replace validointi-virheet "\n" "") "\"" "'")}]})))

(defn polun-skeemat [polku]
  (->> polku
       io/resource
       io/file
       .listFiles
       (filter #(not (.isDirectory %)))))

(defn preload-schemat []
  (concat
   (for [skeematiedosto (polun-skeemat "api/schemas/")]
     [(str "file:resources/api/schemas/" (.getName skeematiedosto))
      (JsonLoader/fromFile skeematiedosto)])
   (for [skeematiedosto (polun-skeemat "api/schemas/entities/")]
     [(str "file:resources/api/schemas/entities/" (.getName skeematiedosto))
      (JsonLoader/fromFile skeematiedosto)])))

(defn validoi
  "Validoi annetun JSON sisällön vasten annettua JSON-skeemaa. JSON-skeeman tulee olla tiedosto annettussa
  skeema-polussa. JSON on String, joka on sisältö. Jos annettu JSON ei ole validia, heitetään JSONException."
  [skeemaresurssin-polku json]

  (log/debug "Validoidaan JSON dataa käytäen skeemaa:" skeemaresurssin-polku ". Data: " json)

  (try (->
         (let
           [skeema (JsonLoader/fromURL (io/resource skeemaresurssin-polku))
            validaattori-rakentaja (-> (JsonSchemaFactory/newBuilder)
                                       (.setLoadingConfiguration
                                        (let [lcb (LoadingConfiguration/newBuilder)]
                                          (doseq [[uri skeema] (preload-schemat)]
                                            (.preloadSchema lcb uri skeema))
                                          (.freeze lcb)))
                                       .freeze)
            validaattori (.getJsonSchema validaattori-rakentaja skeema)
            json-data (JsonLoader/fromString json)
            validointiraportti (.validate validaattori json-data)]

           (if (.isSuccess validointiraportti)
             (log/debug "JSON data on validia")
             (kasittele-validointivirheet validointiraportti))))
       (catch Exception e
         (log/error "JSON validoinnissa tapahtui poikkeus: " e)
         (throw+ {:type virheet/+invalidi-json+ :virheet
                        [{:koodi  virheet/+invalidi-json-koodi+
                          :viesti "JSON ei ole validia"}]}))))
