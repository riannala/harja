(ns harja.palvelin.palvelut.muokkauslukko
  (:require [com.stuartsierra.component :as component]
            [harja.palvelin.komponentit.http-palvelin :refer [julkaise-palvelu poista-palvelut]]
            [taoensso.timbre :as log]
            [clj-time.core :as t]
            [clj-time.coerce :as coerce]
            [harja.domain.skeema :refer [Toteuma validoi]]
            [clojure.java.jdbc :as jdbc]
            [harja.kyselyt.muokkauslukko :as q]))

(def suurin-sallittu-lukon-ika-minuuteissa 5)
(def suurin-sallittu-lukon-ika-sekunneissa (* 60 suurin-sallittu-lukon-ika-minuuteissa))

(defn lukko-vanhentunut?
  "Kertoo onko lukon suurin salittu ikä ylittynyt. true tai false"
  [lukko]
  (log/debug "Tarkistetaan lukon ikä")
  (let [ika (:ika lukko)
        lukko-vanhentunut (> ika
                             suurin-sallittu-lukon-ika-sekunneissa)]
    (if lukko-vanhentunut
      (do (log/debug "Lukko on vanhentunut")
          true)
      (do (log/debug "Lukko ei ole vanhentunut")
          false))))

(defn virkista-lukko [db user {:keys [id]}]
  (log/debug "Virkistetään lukko")
  (q/virkista-lukko<! db id (:id user)))

(defn vapauta-lukko [db lukko-id]
  (log/debug "Vapautetaan lukko " lukko-id)
  (q/vapauta-lukko! db lukko-id)
  (log/debug "Lukko vapautettu"))

(defn vapauta-kayttajan-lukko [db user lukko-id]
  (log/debug "Vapautetaan käyttäjän " (:kayttajanimi user) " lukko " lukko-id)
  (q/vapauta-kayttajan-lukko! db lukko-id (:id user))
  (log/debug "Lukko vapautettu"))

(defn hae-lukko-idlla
  "Hakee lukon id:llä.
  Jos lukko löytyy, palauttaa sen.
  Jos lukko löytyy, mutta se on vanhentunut, poistaa sen ja palauttaa :ei-lukittu
  Jos lukkoa ei löydy, palauttaa :ei-lukittu."
  [db {:keys [id]}]
  (jdbc/with-db-transaction [c db]
    (log/debug "Haetaan lukko id:llä " id)
    (let [lukko (first (q/hae-lukko-idlla c id))]
      (log/debug "Lukko saatu: " (pr-str lukko))
      (if lukko
        (if (lukko-vanhentunut? lukko)
          (do
            (vapauta-lukko db (:id lukko))
            :ei-lukittu)
          lukko)
        :ei-lukittu))))

(defn lukitse
  "Yrittää luoda uuden lukon annetulla id:llä.
  Jos onnistuu, palauttaa lukon tiedot
  Jos epäonnistuu, palauttaa :ei-lukittu"
  [db user {:keys [id]}]
  (jdbc/with-db-transaction [db db]
    (log/debug "Yritetään lukita " id)
    (let [lukko (first (q/hae-lukko-idlla db id))]
      (log/debug "Tarkistettiin vanha lukko. Tulos: " (pr-str lukko))
      (if (nil? lukko)
        (do
          (log/debug "Ei vanhaa lukkoa. Lukitaan " id)
          (q/luo-lukko<! db id (:id user)))
        (do
          (log/debug "Vanha lukko löytyi. Tarkistetaan sen ikä.")
          (if (lukko-vanhentunut? lukko)
            (do
              (log/debug "Edellinen lukko on vanhentunut. Poistetaan se ja luodaan uusi..")
              (vapauta-lukko db (:id lukko))
              (q/luo-lukko<! db id (:id user)))
            (do (log/debug "Ei voida lukita " id " koska on jo lukittu!")
                :ei-lukittu)))))))

(defrecord Muokkauslukko []
  component/Lifecycle
  (start [this]
    (let [http (:http-palvelin this)
          db (:db this)]
      (julkaise-palvelu http :hae-lukko-idlla
                        (fn [user tiedot]
                          (hae-lukko-idlla db tiedot)))
      (julkaise-palvelu http :lukitse
                        (fn [user tiedot]
                          (lukitse db user tiedot)))
      (julkaise-palvelu http :vapauta-lukko
                        (fn [user tiedot]
                          (vapauta-kayttajan-lukko db user (:id tiedot))))
      (julkaise-palvelu http :virkista-lukko
                        (fn [user tiedot]
                          (virkista-lukko db user tiedot)))
      this))

  (stop [this]
    (poista-palvelut
      (:http-palvelin this)
      :hae-lukko-idlla
      :lukitse
      :vapauta-lukko
      :virkista-lukko)
    this))
