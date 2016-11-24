(ns harja.palvelin.integraatiot.sampo.vienti
  (:require [taoensso.timbre :as log]
            [hiccup.core :refer [html]]
            [clj-time.core :as t]
            [harja.kyselyt.maksuerat :as qm]
            [harja.kyselyt.kustannussuunnitelmat :as qk]
            [harja.palvelin.integraatiot.sampo.sanomat.kuittaus-samposta-sanoma :as kuittaus-sampoon-sanoma]
            [harja.palvelin.integraatiot.sampo.kasittely.maksuerat :as maksuera]
            [harja.palvelin.integraatiot.sampo.kasittely.kustannussuunnitelmat :as kustannussuunnitelma]
            [harja.palvelin.integraatiot.integraatioloki :as integraatioloki]))

(def +xsd-polku+ "xsd/sampo/outbound/")

(defn kasittele-kuittaus [integraatioloki db viesti jono]
  (log/debug "Vastaanotettiin Sampon kuittausjonosta viesti: " viesti)
  (let [kuittaus-xml (.getText viesti)]
    ;; Validointia ei tehdä, koska jostain syystä Sampon itsensä lähettämät kuittaukset eivät mene läpi validoinnista
    ;; (if (xml/validi-xml? +xsd-polku+ "status.xsd" kuittaus-xml)
    (let [kuittaus (kuittaus-sampoon-sanoma/lue-kuittaus kuittaus-xml)
          onnistunut (not (contains? kuittaus :virhe))]
      (log/debug "Luettiin kuittaus: " kuittaus)
      (if-let [viesti-id (:viesti-id kuittaus)]
        (let [lahetystyyppi (if (= :maksuera (:viesti-tyyppi kuittaus))
                              "maksuera-lähetys"
                              "kustannussuunnitelma-lahetys")]
          (integraatioloki/kirjaa-saapunut-jms-kuittaus
            integraatioloki
            kuittaus-xml
            viesti-id
            lahetystyyppi
            onnistunut jono)
          (if (= :maksuera (:viesti-tyyppi kuittaus))
            (maksuera/kasittele-maksuera-kuittaus db kuittaus viesti-id)
            (kustannussuunnitelma/kasittele-kustannussuunnitelma-kuittaus db kuittaus viesti-id)))
        (log/error "Sampon kuittauksesta ei voitu hakea viesti-id:tä.")))
    #_(log/error "Samposta vastaanotettu kuittaus ei ole validia XML:ää.")))

(defn aja-paivittainen-lahetys [sonja integraatioloki db lahetysjono-ulos]
  (log/debug "Maksuerien päivittäinen lähetys käynnistetty: " (t/now))
  (let [maksuerat (qm/hae-likaiset-maksuerat db)
        lahetettavat-kustannussuunnitelmat (qk/hae-likaiset-kustannussuunnitelmat db)
        urakkaidt (distinct (map :urakkaid maksuerat))
        maksuerien-summat (flatten (map #(qm/hae-urakan-maksuerien-summat db %) urakkaidt))
        lahetettavat-maksuerat (map (fn [m] (assoc m :summat (first (filter #(= (:tpi_id %) (:tpi_id m)) maksuerien-summat)))) maksuerat)]
    (log/debug "Lähetetään " (count lahetettavat-maksuerat) " maksuerää ja " (count lahetettavat-kustannussuunnitelmat) " kustannussuunnitelmaa.")
    (doseq [maksuera lahetettavat-maksuerat]
      (maksuera/laheta-maksuera sonja integraatioloki db lahetysjono-ulos (:numero maksuera) (:summat maksuera)))
    (doseq [kustannussuunnitelma lahetettavat-kustannussuunnitelmat]
      (kustannussuunnitelma/laheta-kustannussuunitelma sonja integraatioloki db lahetysjono-ulos (:maksuera kustannussuunnitelma)))))