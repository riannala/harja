(ns harja.palvelin.integraatiot.tloik.kasittely.ilmoitus
  (:require [taoensso.timbre :as log]
            [harja.kyselyt.ilmoitukset :as ilmoitukset]
            [harja.palvelin.palvelut.urakat :as urakkapalvelu]
            [slingshot.slingshot :refer [throw+]]
            [harja.palvelin.integraatiot.api.tyokalut.virheet :as virheet]
            [clojure.string :as str]))

(defn urakkatyyppi [urakkatyyppi]
  (case (str/lower-case urakkatyyppi)
    "silta" "siltakorjaus"
    "tekniset laitteet" "tekniset-laitteet"
    urakkatyyppi))

(defn paivita-ilmoittaja [db id ilmoittaja]
  (ilmoitukset/paivita-ilmoittaja-ilmoitukselle!
    db
    (:etunimi ilmoittaja)
    (:sukunimi ilmoittaja)
    (:tyopuhelin ilmoittaja)
    (:matkapuhelin ilmoittaja)
    (:sahkoposti ilmoittaja)
    (:tyyppi ilmoittaja)
    id))

(defn paivita-lahettaja [db id lahettaja]
  (ilmoitukset/paivita-lahettaja-ilmoitukselle!
    db (:etunimi lahettaja)
    (:sukunimi lahettaja)
    (:puhelinnumero lahettaja)
    (:sahkoposti lahettaja)
    id))

(defn paivita-ilmoitus [db id urakka-id {:keys [ilmoitettu ilmoitus-id ilmoitustyyppi valitetty otsikko paikankuvaus lisatieto yhteydenottopyynto ilmoittaja lahettaja selitteet sijainti vastaanottaja]}]
  (ilmoitukset/paivita-ilmoitus!
    db
    urakka-id
    ilmoitus-id
    ilmoitettu
    valitetty
    yhteydenottopyynto
    otsikko
    paikankuvaus
    lisatieto
    ilmoitustyyppi
    (str "{" (clojure.string/join "," (map name selitteet)) "}")
    id)
  (paivita-ilmoittaja db id ilmoittaja)
  (paivita-lahettaja db id lahettaja)
  (ilmoitukset/aseta-ilmoituksen-sijainti! db (:tienumero sijainti) (:x sijainti) (:y sijainti) id)
  id)

(defn luo-ilmoitus [db urakka-id urakkatyyppi {:keys [ilmoitettu ilmoitus-id ilmoitustyyppi valitetty otsikko paikankuvaus lisatieto yhteydenottopyynto ilmoittaja lahettaja selitteet sijainti vastaanottaja]}]
  (let [id (:id (ilmoitukset/luo-ilmoitus<!
                  db
                  urakka-id
                  ilmoitus-id
                  ilmoitettu
                  valitetty
                  yhteydenottopyynto
                  otsikko
                  paikankuvaus
                  lisatieto
                  ilmoitustyyppi
                  (str "{" (clojure.string/join "," (map name selitteet)) "}")
                  urakkatyyppi))]
    (paivita-ilmoittaja db id ilmoittaja)
    (paivita-lahettaja db id lahettaja)
    (ilmoitukset/aseta-ilmoituksen-sijainti! db (:tienumero sijainti) (:x sijainti) (:y sijainti) id)
    id))

(defn tallenna-ilmoitus [db ilmoitus]
  (log/debug "Käsitellään ilmoitusta T-LOIK:sta id:llä: " (:ilmoitus-id ilmoitus) ", joka välitettiin viestillä id: " (:viesti-id ilmoitus))
  (let [ilmoitus-id (:ilmoitus-id ilmoitus)
        nykyinen-id (:id (first (ilmoitukset/hae-id-ilmoitus-idlla db ilmoitus-id)))
        urakkatyyppi (urakkatyyppi (:urakkatyyppi ilmoitus))
        urakka-id (first (urakkapalvelu/hae-urakka-idt-sijainnilla db urakkatyyppi (:sijainti ilmoitus)))
        uusi-id (if nykyinen-id
                  (paivita-ilmoitus db nykyinen-id urakka-id ilmoitus)
                  (luo-ilmoitus db urakka-id urakkatyyppi ilmoitus))]
    (log/debug (format "Ilmoitus (id: %s) käsitelty onnistuneesti" (:ilmoitus-id ilmoitus)))
    (when-not urakka-id
      (throw+ {:type virheet/+urakkaa-ei-loydy+}))
    uusi-id))

(defn hae-ilmoituksen-tieosoite [db ilmoitus-id]
  (first (ilmoitukset/hae-ilmoituksen-tieosoite db ilmoitus-id)))