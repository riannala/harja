(ns harja.palvelin.palvelut.tierek-haku
  (:require [harja.palvelin.komponentit.http-palvelin :refer [julkaise-palvelut poista-palvelut]]
            [com.stuartsierra.component :as component]
            [harja.kyselyt.tieverkko :as tv]
            [harja.geo :as geo]
            [harja.kyselyt.konversio :as konv]
            [taoensso.timbre :as log]))

(def +treshold+ 250)

(defn muunna-geometria [tros]
  (assoc tros :geometria (geo/pg->clj (:geometria tros))))

(defn hae-tr-pisteilla
  "params on mappi {:x1 .. :y1 .. :x2 .. :y2 ..}"
  [db params]
  (when-let [tros (first (tv/hae-tr-osoite-valille db
                                                   (:x1 params) (:y1 params)
                                                   (:x2 params) (:y2 params)
                                                   +treshold+))]
    (muunna-geometria tros)))

(defn hae-tr-pisteella
  "params on mappi {:x .. :y ..}"
  [db params]
  (let [tros (first (tv/hae-tr-osoite db (:x params) (:y params) +treshold+))]
    (muunna-geometria tros)))

(defn hae-tr-viiva
  "params on mappi {:tie .. :aosa .. :aet .. :losa .. :let"
  [db params]
  (log/debug "Haetaan viiva osoiteelle " (pr-str params))
  (let [korjattu-osoite params
        geom (tv/tierekisteriosoite-viivaksi db
                                             (:numero korjattu-osoite)
                                             (:alkuosa korjattu-osoite)
                                             (:alkuetaisyys korjattu-osoite)
                                             (:loppuosa korjattu-osoite)
                                             (:loppuetaisyys korjattu-osoite))]
    (log/debug "Osoitteelle löydettiin geometria: " (pr-str geom))
    (mapv geo/pg->clj (mapv :tierekisteriosoitteelle_viiva geom))))

(defn hae-tr-piste
  "params on mappi {:tie .. :aosa .. :aet .. :losa .. :let"
  [db params]
  (log/debug "Haetaan piste osoitteelle: " (pr-str params))
  (let [geom (first (tv/tierekisteriosoite-pisteeksi db
                                                    (:numero params)
                                                    (:alkuosa params)
                                                    (:alkuetaisyys params)))]
    (log/debug "Osoitteelle löydettiin geometria: " (pr-str geom))
    (geo/pg->clj (:tierekisteriosoitteelle_piste geom))))

(defn hae-osien-pituudet
  "Hakee tierekisteriosien pituudet annetulle tielle ja osan välille.
  Params mäpissä tulee olla :tie, :aosa ja :losa"
  [db params]
  (into {}
        (map (juxt :osa :pituus))
        (tv/hae-osien-pituudet db params)))

(defrecord TierekisteriHaku []
  component/Lifecycle
  (start [{:keys [http-palvelin db] :as this}]
    (julkaise-palvelut
     http-palvelin
     :hae-tr-pisteilla (fn [_ params]
                         (hae-tr-pisteilla db params))

     :hae-tr-pisteella (fn [_ params]
                         (hae-tr-pisteella db params))

     :hae-tr-viivaksi (fn [_ params]
                        (hae-tr-viiva db params))

     :hae-tr-pisteeksi (fn [_ params]
                         (hae-tr-piste db params))

     :hae-tr-osien-pituudet (fn [_ params]
                              (hae-osien-pituudet db params)))

    this)
  (stop [{http :http-palvelin :as this}]
    (poista-palvelut http
                     :hae-tr-pisteilla
                     :hae-tr-pisteella
                     :hae-tr-viivaksi
                     :hae-tr-pisteeksi
                     :hae-osien-pituudet)
    this))