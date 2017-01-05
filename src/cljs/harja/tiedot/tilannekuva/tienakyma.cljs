(ns harja.tiedot.tilannekuva.tienakyma
  "Tien 'supernäkymän' tiedot."
  (:require [reagent.core :refer [atom] :as r]
            [harja.asiakas.kommunikaatio :as k]
            [harja.ui.kartta.esitettavat-asiat :as esitettavat-asiat]
            [harja.loki :refer [log]]
            [cljs.core.async :refer [<!] :as async]
            [tuck.core :as tuck]
            [harja.ui.kartta.infopaneelin-sisalto :as infopaneelin-sisalto]
            [harja.tyokalut.functor :refer [fmap]])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [reagent.ratom :refer [reaction]]))

;; Feature fläg, jolla tienäkymä on pois käytöstä.
;; Testailua varten, sen voi kytkeä JS konsolissa päälle.
(defonce tienakyma-kaytossa? (atom false))
(defn ^:export tienakyma-paalle []
  (reset! tienakyma-kaytossa? true))


(defonce tienakyma (atom {:valinnat {}
                          :sijainti nil
                          :haku-kaynnissa? nil
                          :tulokset nil
                          :nakymassa? false}))

(defrecord PaivitaSijainti [sijainti])
(defrecord PaivitaValinnat [valinnat])
(defrecord Hae [])
(defrecord HakuValmis [tulokset])
(defrecord Nakymassa [nakymassa?])
(defrecord SuljeInfopaneeli [])
(defrecord AvaaTaiSuljeTulos [idx])

(defn- kartalle
  "Muodosta tuloksista karttataso.
  Kaikki, jotka ovat infopaneelissa avattuina, renderöidään valittuina."
  [{:keys [avatut-tulokset kaikki-tulokset valinnat] :as tienakyma}]
  (assoc tienakyma
         :tulokset-kartalla (esitettavat-asiat/kartalla-esitettavaan-muotoon
                             (conj kaikki-tulokset (assoc (:sijainti valinnat)
                                                          :tyyppi-kartalla :tr-osoite-indikaattori))
                             (comp avatut-tulokset :idx))))

(extend-protocol tuck/Event
  Nakymassa
  (process-event [{nakymassa? :nakymassa?} tienakyma]
    (assoc tienakyma :nakymassa? nakymassa?))

  PaivitaSijainti
  (process-event [{s :sijainti} tienakyma]
    (assoc-in tienakyma [:valinnat :sijainti] s))

  PaivitaValinnat
  (process-event [{uusi :valinnat} tienakyma]
    (let [vanha (:valinnat tienakyma)
          alku-muuttunut? (not= (:alku vanha) (:alku uusi))
          valinnat (as-> uusi v
                     ;; Jos alku muuttunut ja vanhassa alku ja loppu olivat samat,
                     ;; päivitä myös loppukenttä
                     (if (and alku-muuttunut?
                              (= (:alku vanha) (:loppu vanha)))
                       (assoc v :loppu (:alku uusi))
                       v))]
      (assoc tienakyma :valinnat valinnat)))

  Hae
  (process-event [_ tienakyma]
    (let [valinnat (:valinnat tienakyma)
          tulos! (tuck/send-async! ->HakuValmis)]
      (go
        (tulos! (<! (k/post! :hae-tienakymaan valinnat))))
      (assoc tienakyma
             :tulokset nil
             :tulokset-kartalla nil
             :haku-kaynnissa? true)))

  HakuValmis
  (process-event [{tulokset :tulokset} tienakyma]
    (let [kaikki-tulokset (into []
                                (comp (mapcat val)
                                      (map-indexed (fn [i tulos]
                                                     (assoc tulos :idx i))))
                                tulokset)]
      (log "Tienäkymän haku löysi: " (pr-str (fmap count tulokset)))
      (kartalle
       (assoc tienakyma
              :kaikki-tulokset kaikki-tulokset
              :tulokset (infopaneelin-sisalto/skeemamuodossa kaikki-tulokset)
              :avatut-tulokset #{}
              :haku-kaynnissa? false))))

  AvaaTaiSuljeTulos
  (process-event [{idx :idx} tienakyma]
    (kartalle
     (update tienakyma :avatut-tulokset
             #(if (% idx)
                (disj % idx)
                (conj % idx)))))

  SuljeInfopaneeli
  (process-event [_ tienakyma]
    (assoc tienakyma
           :tulokset nil
           :tulokset-kartalla nil)))

(defonce tulokset-kartalla
  (r/cursor tienakyma [:tulokset-kartalla]))

(defonce karttataso-tienakyma
  (r/cursor tienakyma [:nakymassa?]))
