(ns harja.palvelin.integraatiot.api.varustetoteuma
  "Varustetoteuman kirjaaminen urakalle"
  (:require [com.stuartsierra.component :as component]
            [compojure.core :refer [POST GET]]
            [taoensso.timbre :as log]
            [clojure.java.jdbc :as jdbc]
            [harja.palvelin.komponentit.http-palvelin :refer [julkaise-reitti poista-palvelut]]
            [harja.palvelin.integraatiot.api.tyokalut.kutsukasittely :refer [kasittele-kutsu-async tee-kirjausvastauksen-body]]
            [harja.palvelin.integraatiot.api.tyokalut.json-skeemat :as json-skeemat]
            [harja.palvelin.integraatiot.api.tyokalut.validointi :as validointi]
            [harja.kyselyt.toteumat :as toteumat-q]
            [harja.palvelin.integraatiot.api.varusteet :as varusteet]
            [harja.palvelin.integraatiot.api.toteuma :as api-toteuma]
            [harja.palvelin.integraatiot.api.tyokalut.liitteet :refer [dekoodaa-base64]]
            [harja.palvelin.integraatiot.api.tyokalut.json :refer [aika-string->java-sql-date]]
            [harja.palvelin.integraatiot.tierekisteri.tierekisteri-komponentti :as tierekisteri]
            [harja.palvelin.integraatiot.api.sanomat.tierekisteri-sanomat :as tierekisteri-sanomat]
            [harja.kyselyt.livitunnisteet :as livitunnisteet]
            [harja.palvelin.integraatiot.api.validointi.toteumat :as toteuman-validointi]
            [harja.domain.tierekisterin-tietolajin-kuvauksen-kasittely :as tr-tietolaji]
            [clj-time.core :as t]
            [harja.palvelin.integraatiot.api.tyokalut.virheet :as virheet]
            [clojure.string :as str]
            [harja.geo :as geo])
  (:use [slingshot.slingshot :only [throw+]])
  (:import (org.postgis GeometryCollection Geometry PGgeometry)))

(def toimenpide-tyyppi->toimenpide
  {:varusteen-lisays "lisatty"
   :varusteen-paivitys "paivitetty"
   :varusteen-poisto "poistettu"
   :varusteen-tarkastus "tarkastus"})

(defn- tee-onnistunut-vastaus [vastaukset]
  (tee-kirjausvastauksen-body
    {:ilmoitukset "Varustetoteuma kirjattu onnistuneesti."
     :uudet-idt (mapv :uusi-id vastaukset)}))

(defn- lisaa-varuste-tierekisteriin [tierekisteri otsikko toimenpide livitunniste arvot-string]
  (log/debug "Lisätään varuste livitunnisteella " livitunniste " tierekisteriin")
  (let [valitettava-data (tierekisteri-sanomat/luo-tietueen-lisayssanoma
                           otsikko
                           livitunniste
                           toimenpide
                           arvot-string)]
    (let [vastaus (tierekisteri/lisaa-tietue tierekisteri valitettava-data)]
      (log/debug "Tierekisterin vastaus: " (pr-str vastaus))
      (assoc vastaus :uusi-id livitunniste))))

(defn- paivita-varuste-tierekisteriin [tierekisteri otsikko toimenpide arvot-string]
  (log/debug "Päivitetään varuste tierekisteriin")
  (let [valitettava-data (tierekisteri-sanomat/luo-tietueen-paivityssanoma
                           otsikko
                           toimenpide
                           arvot-string)]
    (let [vastaus (tierekisteri/paivita-tietue tierekisteri valitettava-data)]
      (log/debug "Tierekisterin vastaus: " (pr-str vastaus))
      vastaus)))

(defn- poista-varuste-tierekisterista [tierekisteri otsikko toimenpide]
  (log/debug "Poistetaan varuste tierekisteristä")
  (let [valitettava-data (tierekisteri-sanomat/luo-tietueen-poistosanoma
                           otsikko
                           toimenpide)]
    (let [vastaus (tierekisteri/poista-tietue tierekisteri valitettava-data)]
      (log/debug "Tierekisterin vastaus: " (pr-str vastaus))
      vastaus)))

(defn- toimenpide-lahetetty-tierekisteriin? [db toimenpide]
  (true? (:lahetetty_tierekisteriin
           (first (toteumat-q/hae-varustetoteuman-lahetystiedot
                    db
                    {:id (:varustetoteuma-id toimenpide)})))))

(defn- laheta-varustetoteuman-toimenpiteet-tierekisteriin
  "Päivittää varustetoteumassa tehdyt toimenpiteet Tierekisteriin.
  On mahdollista, että muutoksen välittäminen Tierekisteriin epäonnistuu.
  Tässä tapauksessa halutaan, että muutos jää kuitenkin Harjaan ja Harjan integraatiolokeihin, jotta
  nähdään, että toteumaa on yritetty kirjata."
  [tierekisteri db otsikko varustetoteuma]
  (when tierekisteri
    (doseq [toimenpide (get-in varustetoteuma [:varustetoteuma :toimenpiteet])]
      (let [toimenpide-tyyppi (first (keys toimenpide))
            toimenpiteen-tiedot (toimenpide-tyyppi toimenpide)
            tunniste (if (= toimenpide-tyyppi :varusteen-poisto)
                       (:tunniste toimenpiteen-tiedot)
                       (get-in toimenpiteen-tiedot [:varuste :tunniste]))
            tietolajin-arvot-string (:arvot-string toimenpide)]

        (log/debug "Valmistellaan toimenpiteen lähetys tierekisteriin, tyyppi: " (pr-str toimenpide-tyyppi))
        ;; On mahdollista, että sama toteuma ja toimenpide lähetetään Harjaan useaan kertaan. Tässä tilanteessa
        ;; tarkistetaan, onko toimenpide jo lähetetty tierekisteriin. Jos on, sitä ei lähetetä uudelleen."
        (if (toimenpide-lahetetty-tierekisteriin? db toimenpide)
          (log/debug "Toimenpide on jo lähetetty, ohitetaan.")

          (let [vastaus
                (case toimenpide-tyyppi
                  :varusteen-lisays
                  (lisaa-varuste-tierekisteriin tierekisteri otsikko toimenpiteen-tiedot
                                                tunniste tietolajin-arvot-string)

                  :varusteen-poisto
                  (poista-varuste-tierekisterista tierekisteri otsikko toimenpiteen-tiedot)

                  :varusteen-paivitys
                  (paivita-varuste-tierekisteriin tierekisteri otsikko toimenpiteen-tiedot
                                                  tietolajin-arvot-string)

                  :varusteen-tarkastus
                  (paivita-varuste-tierekisteriin tierekisteri otsikko toimenpiteen-tiedot
                                                  tietolajin-arvot-string))]

            ;; FIXME On mahdollista, joskin epätodennäköistä, että kirjaus lähtee tierekisteriin,
            ;; mutta kuittausta ei koskaan saada. Tällöin varuste saatetaan kirjata kahdesti jos
            ;; sama payload lähetetään Harjaan uudelleen.
            ;; --> Pitää tutkia mitä tierekisteri palauttaa samalle kutsulle
            (when (:onnistunut vastaus)
              (log/debug "Merkitään toimenpide id:llä " (:varustetoteuma-id toimenpide) " lähetetyksi.")
              (toteumat-q/merkitse-varustetoteuma-lahetetyksi<! db (:varustetoteuma-id toimenpide)))))))))

(defn- luo-uusi-varustetoteuma [db kirjaaja toteuma-id varustetoteuma toimenpiteen-tiedot tietolaji
                                tunniste tehty-toimenpide tie toimenpiteen-arvot-tekstina]
  (log/debug "Luodaan uusi varustetoteuma tunnisteella tyyppiä " tehty-toimenpide " tuunnisteella " tunniste)
  (:id (toteumat-q/luo-varustetoteuma<!
         db
         {:tunniste tunniste
          :toteuma toteuma-id
          :toimenpide tehty-toimenpide
          :tietolaji tietolaji
          :arvot toimenpiteen-arvot-tekstina
          :karttapvm (get-in toimenpiteen-tiedot [:varuste :tietue :karttapvm])
          :alkupvm (aika-string->java-sql-date (get-in varustetoteuma [:varustetoteuma :toteuma :alkanut]))
          :loppupvm (aika-string->java-sql-date (get-in varustetoteuma [:varustetoteuma :toteuma :paattynyt]))
          :piiri (get-in toimenpiteen-tiedot [:varuste :tietue :piiri])
          :kuntoluokka (get-in toimenpiteen-tiedot [:varuste :tietue :kuntoluokitus])
          :tierekisteriurakkakoodi (get-in toimenpiteen-tiedot [:varuste :tietue :tierekisteriurakkakoodi])
          :luoja (:id kirjaaja)
          :tr_numero (:numero tie)
          :tr_alkuosa (:aosa tie)
          :tr_alkuetaisyys (:aet tie)
          :tr_loppuosa (:losa tie)
          :tr_loppuetaisyys (:let tie)
          :tr_puoli (:puoli tie)
          :tr_ajorata (:ajr tie)})))

(defn- etsi-varustetoteuma
  "Etsii toimenpiteen varustetoteuman id:n kannasta annettujen tietojen perusteella"
  [db toteuma-id tunniste tietolaji toimenpiteen-tiedot tehty-toimenpide]
  (let [varustetoteuma (first (toteumat-q/hae-varustetoteuma
                                db
                                {:toteumaid toteuma-id
                                 :tunniste tunniste
                                 :tietolaji tietolaji
                                 :toimenpide tehty-toimenpide
                                 :tr_numero (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie :numero])
                                 :tr_aosa (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie :aosa])
                                 :tr_aet (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie :aet])
                                 :tr_losa (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie :losa])
                                 :tr_let (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie :let])
                                 :tr_ajorata (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie :ajr])
                                 :tr_puoli (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie :puoli])}))]
    (log/debug "Löytyi varustetoteuma: " (pr-str varustetoteuma))
    varustetoteuma))

(defn- tallenna-varustetoteuman-toimenpiteet
  "Luo jokaisesta varustetoteuman toimenpiteestä varustetoteuman.
   Palauttaa päivitetyt varustetoteumat:
   - Kaikille toimenpiteille liitetään niiden varustetoteuman id kannassa.
   - Lisäystoimenpiteelle on lisäksi lisätty Harjan luoma tunniste"
  [db tierekisteri toteuma-id kirjaaja varustetoteuma]
  (log/debug "Tallennetaan toteuman toimenpiteet.")
  (mapv
    (fn [toimenpide]
      (let [toimenpide-tyyppi (first (keys toimenpide))
            toimenpiteen-tiedot (toimenpide-tyyppi toimenpide)
            tietolaji (if (not= toimenpide-tyyppi :varusteen-poisto)
                        (get-in toimenpiteen-tiedot [:varuste :tietue :tietolaji :tunniste])
                        (:tietolajitunniste toimenpiteen-tiedot))
            tietolajin-arvot (get-in toimenpiteen-tiedot [:varuste :tietue :tietolaji :arvot])
            tunniste (if (not= toimenpide-tyyppi :varusteen-poisto)
                       (get-in toimenpiteen-tiedot [:varuste :tunniste])
                       (:tunniste toimenpiteen-tiedot))
            tie (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie])
            tietolajin-arvot-string (when tietolajin-arvot
                                      (varusteet/validoi-ja-muunna-arvot-merkkijonoksi
                                        tierekisteri
                                        tietolajin-arvot
                                        tietolaji))
            tallenna-toimenpide (partial luo-uusi-varustetoteuma db kirjaaja toteuma-id varustetoteuma
                                         toimenpiteen-tiedot tietolaji)]

        ;; On mahdollista, että sama toteuma lähetetään useaan kertaan. Tässä tilanteessa
        ;; tarkistetaan, onko toimenpide jo tallennettu. Jos on, sitä ei tallenneta uudelleen."
        (let [varustetoteuma-kannassa (etsi-varustetoteuma
                                        db
                                        toteuma-id
                                        tunniste
                                        tietolaji
                                        toimenpiteen-tiedot
                                        (toimenpide-tyyppi->toimenpide toimenpide-tyyppi))
              varustetoteuma-id (:id varustetoteuma-kannassa)
              tunniste (:tunniste varustetoteuma-kannassa)]
          (if varustetoteuma-id
            (do (log/debug (str "Toimenpide " toimenpide-tyyppi " on jo tallennettu, ohitetaan."))
                (cond-> (assoc toimenpide :varustetoteuma-id varustetoteuma-id)
                        (not= toimenpide-tyyppi :varusteen-poisto)
                        (assoc :arvot-string tietolajin-arvot-string)
                        (= toimenpide-tyyppi :varusteen-lisays)
                        (assoc-in [:varusteen-lisays :varuste :tunniste] tunniste)))
            (do
              (log/debug (str "Tallennetaan toimenpide " toimenpide-tyyppi "."))
              (case toimenpide-tyyppi
                :varusteen-lisays
                (let [uusi-livitunniste (livitunnisteet/hae-seuraava-livitunniste db)
                      varustetoteuma-id (tallenna-toimenpide uusi-livitunniste "lisatty" tie tietolajin-arvot-string)]
                  (-> (assoc toimenpide :varustetoteuma-id varustetoteuma-id)
                      (assoc :arvot-string tietolajin-arvot-string)
                      (assoc-in [:varusteen-lisays :varuste :tunniste] uusi-livitunniste)))

                :varusteen-paivitys
                (let [varustetoteuma-id (tallenna-toimenpide tunniste "paivitetty" tie tietolajin-arvot-string)]
                  (-> (assoc toimenpide :varustetoteuma-id varustetoteuma-id)
                      (assoc :arvot-string tietolajin-arvot-string)))

                :varusteen-poisto
                (let [varustetoteuma-id (tallenna-toimenpide tunniste "poistettu" nil nil)]
                  (assoc toimenpide :varustetoteuma-id varustetoteuma-id))

                :varusteen-tarkastus
                (let [varustetoteuma-id (tallenna-toimenpide tunniste "tarkastus" tie tietolajin-arvot-string)]
                  (-> (assoc toimenpide :varustetoteuma-id varustetoteuma-id)
                      (assoc :arvot-string tietolajin-arvot-string)))))))))
    (get-in varustetoteuma [:varustetoteuma :toimenpiteet])))

(defn- hae-toimenpiteen-geometria [db toimenpide]
  (let [toimenpide-tyyppi (first (keys toimenpide))
        toimenpiteen-tiedot (toimenpide-tyyppi toimenpide)
        ;; Huomaa, että poistotoimenpiteellä ei ole sijaintia
        tr-osoite (get-in toimenpiteen-tiedot [:varuste :tietue :sijainti :tie])
        viiva? (and (:losa tr-osoite)
                    (:let tr-osoite))
        geometria (:sijainti (first (toteumat-q/varustetoteuman-toimenpiteelle-sijainti
                                      db {:tie (:numero tr-osoite)
                                          :aosa (:aosa tr-osoite)
                                          :aet (:aet tr-osoite)
                                          :losa (if viiva?
                                                  (:losa tr-osoite)
                                                  (:aosa tr-osoite))
                                          :let (if viiva?
                                                 (:let tr-osoite)
                                                 (:aet tr-osoite))})))]
    geometria))

(defn- tallenna-varustetoteuman-geometria
  "Muuntaa varustetoteuman jokaisen toimenpiteen piste-geometriaksi.
  Toteuman geometriaksi muodostuu point geometry collection."
  [db varustetoteuma toteuma-id]
  (log/debug "Tallennetaan toteuman geometria")
  (let [geometriat
        (keep (fn [toimenpide]
                (hae-toimenpiteen-geometria db toimenpide))
              (get-in varustetoteuma [:varustetoteuma :toimenpiteet]))
        geometry-collection (GeometryCollection.
                              (into-array Geometry
                                          (map #(.getGeometry %) geometriat)))
        pg-geometry (PGgeometry. geometry-collection)]
    (toteumat-q/paivita-toteuman-reitti<! db {:reitti pg-geometry
                                              :id toteuma-id})))

(defn- tallenna-toteumat
  "Tallentaa varustetoteumat kantaan. Palauttaa päivitetyt varustetoteumat, joihin on liitetty
   toteuman ja toimenpiteiden kanta-id."
  [db tierekisteri urakka-id kirjaaja varustetoteumat]
  (jdbc/with-db-transaction [db db]
    (mapv (fn [varustetoteuma]
            (log/debug "Tallennetaan toteuman perustiedot")
            (let [toteuma (assoc
                            (get-in varustetoteuma [:varustetoteuma :toteuma])
                            :reitti nil)
                  toteuma-id (api-toteuma/paivita-tai-luo-uusi-toteuma db urakka-id kirjaaja toteuma)]
              (log/debug "Toteuman perustiedot tallennettu, toteuma-id: " (pr-str toteuma-id))
              (api-toteuma/tallenna-tehtavat db kirjaaja toteuma toteuma-id)
              (tallenna-varustetoteuman-geometria db varustetoteuma toteuma-id)
              (let [paivitetyt-toimenpiteet (tallenna-varustetoteuman-toimenpiteet
                                              db
                                              tierekisteri
                                              toteuma-id
                                              kirjaaja
                                              varustetoteuma)]
                (-> varustetoteuma
                    (assoc-in [:varustetoteuma :toimenpiteet] paivitetyt-toimenpiteet)
                    (assoc :toteuma-id toteuma-id)))))
          varustetoteumat)))

(defn- laheta-kirjaus-tierekisteriin
  "Lähettää varustetoteumat tierekisteriin yksi kerrallaan.
   Palauttaa vectorissa tierekisterikomponentin antamat vastaukset."
  [db tierekisteri otsikko varustetoteumat]
  (mapv (fn [varustetoteuma]
          (let [vastaus (laheta-varustetoteuman-toimenpiteet-tierekisteriin
                          tierekisteri
                          db
                          otsikko
                          varustetoteuma)]
            vastaus))
        varustetoteumat))

(defn- validoi-tehtavat [db varustetoteumat]
  (doseq [varustetoteuma varustetoteumat]
    (toteuman-validointi/tarkista-tehtavat db (get-in varustetoteuma [:varustetoteuma :toteuma :tehtavat]))))

(defn kirjaa-toteuma
  "Varustetoteuman kirjauksessa kirjataan yksi tai useampi toteuma.
   Jokainen toteuma voi sisältää useita toimenpiteitä (varusteen lisäys, poisto, päivitys, tarkastus)"
  [tierekisteri db {id :id} {:keys [otsikko varustetoteumat] :as payload} kirjaaja]
  (let [urakka-id (Integer/parseInt id)]
    (log/debug "Kirjataan uusi varustetoteuma urakalle id:" urakka-id
               " kayttäjän:" (:kayttajanimi kirjaaja)
               " (id:" (:id kirjaaja) " tekemänä.")
    (validointi/tarkista-urakka-ja-kayttaja db urakka-id kirjaaja)
    (validoi-tehtavat db varustetoteumat)
    (let [varustetoteumat (tallenna-toteumat db tierekisteri urakka-id kirjaaja varustetoteumat)
          tierekisterin-vastaukset (laheta-kirjaus-tierekisteriin db tierekisteri otsikko varustetoteumat)]
      (tee-onnistunut-vastaus tierekisterin-vastaukset))))

(defrecord Varustetoteuma []
  component/Lifecycle
  (start [{http :http-palvelin db :db integraatioloki :integraatioloki tierekisteri :tierekisteri :as this}]
    (julkaise-reitti
      http :lisaa-varustetoteuma
      (POST "/api/urakat/:id/toteumat/varuste" request
        (kasittele-kutsu-async
          db
          integraatioloki
          :lisaa-varustetoteuma
          request
          json-skeemat/varustetoteuman-kirjaus
          json-skeemat/kirjausvastaus
          (fn [parametit data kayttaja db]
            (kirjaa-toteuma tierekisteri db parametit data kayttaja)))))
    this)
  (stop [{http :http-palvelin :as this}]
    (poista-palvelut http :lisaa-varustetoteuma)
    this))
