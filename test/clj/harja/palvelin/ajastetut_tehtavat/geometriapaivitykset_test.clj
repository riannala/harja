(ns harja.palvelin.ajastetut-tehtavat.geometriapaivitykset-test
  (:require [clojure.test :refer [deftest is use-fixtures]]
            [com.stuartsierra.component :as component]
            [clj-time.periodic :refer [periodic-seq]]
            [harja.palvelin.integraatiot.paikkatietojarjestelma.alk :as alk]
            [harja.palvelin.integraatiot.paikkatietojarjestelma.tuonnit.tieverkko :as tieverkon-tuonti]
            [harja.palvelin.ajastetut-tehtavat.geometriapaivitykset :as geometriapaivitykset]
            [harja.palvelin.integraatiot.integraatioloki :as integraatioloki]
            [harja.testi :refer :all]
            [harja.palvelin.komponentit.tietokanta :as tietokanta]
            [harja.palvelin.integraatiot.paikkatietojarjestelma.tuonnit.pohjavesialueet :as pohjavesialueen-tuonti]
            [harja.palvelin.integraatiot.paikkatietojarjestelma.tuonnit.sillat :as siltojen-tuonti]
            [clj-time.coerce :as time-coerce])
  (:use org.httpkit.fake)
  (:import (java.util Date)))

(defn aja-tieverkon-paivitys
  "REPL-testiajofunktio"
  []
  (let [testitietokanta (tietokanta/luo-tietokanta testitietokanta)
        integraatioloki (assoc (integraatioloki/->Integraatioloki nil) :db testitietokanta)]
    (component/start integraatioloki)
    (alk/kaynnista-paivitys
      integraatioloki
      testitietokanta
      "tieverkko"
      "http://185.26.50.104/Tieosoiteverkko.zip"
      "/Users/mikkoro/Desktop/Tieverkko-testi/"
      (fn []
        (tieverkon-tuonti/vie-tieverkko-kantaan
          testitietokanta
          "file:///Users/mikkoro/Desktop/Tieverkko-testi/Tieosoiteverkko.shp")))))

(defn aja-pohjavesialueen-paivitys
  "REPL-testiajofunktio"
  []
  (let [testitietokanta (tietokanta/luo-tietokanta testitietokanta)
        integraatioloki (assoc (integraatioloki/->Integraatioloki nil) :db testitietokanta)]
    (component/start integraatioloki)
    (alk/kaynnista-paivitys
      integraatioloki
      testitietokanta
      "pohjavesialue"
      "http://185.26.50.104/Pohjavesialue.zip"
      "/Users/jarihan/Desktop/Pohjavesialue-testi/"
      (fn []
        (pohjavesialueen-tuonti/vie-pohjavesialueet-kantaan
          testitietokanta
          "file:///Users/jarihan/Desktop/Pohjavesialue-testi/Pohjavesialue.shp")))))

(defn aja-siltojen-paivitys
  "REPL-testiajofunktio"
  []
  (let [testitietokanta (tietokanta/luo-tietokanta testitietokanta)
        integraatioloki (assoc (integraatioloki/->Integraatioloki nil) :db testitietokanta)]
    (component/start integraatioloki)
    (alk/kaynnista-paivitys
      integraatioloki
      testitietokanta
      "sillat"
      "http://185.26.50.104/Sillat.zip"
      "/Users/jarihan/Desktop/Sillat-testi/"
      (fn []
        (siltojen-tuonti/vie-sillat-kantaan
          testitietokanta
          "file:///Users/jarihan/Desktop/Pohjavesialue-testi/Sillat.shp")))))

(defn aja-soratien-hoitoluokkien-paivitys
  "REPL-testiajofunktio"
  []
  (let [testitietokanta (tietokanta/luo-tietokanta testitietokanta)
        integraatioloki (assoc (integraatioloki/->Integraatioloki nil) :db testitietokanta)]
    (component/start integraatioloki)
    (alk/kaynnista-paivitys
      integraatioloki
      testitietokanta
      "tieverkko"
      "http://185.26.50.104/tl132.tgz"
      "/Users/mikkoro/Desktop/Soratiehoitoluokat-testi/"
      (fn []
        (tieverkon-tuonti/vie-tieverkko-kantaan
          testitietokanta
          "file:///Users/mikkoro/Desktop/Soratiehoitoluokat-testi/Sorateiden-hoitoluokat.shp")))))

(def kayttaja "jvh")

(deftest testaa-tiedoston-muokkausajan-selvitys-alk-alustalla
  (let [testitietokanta (tietokanta/luo-tietokanta testitietokanta)
        integraatioloki (assoc (integraatioloki/->Integraatioloki nil) :db testitietokanta)
        fake-tiedosto-url "http://www.example.com/file.zip"
        fake-muokkausaika "Tue, 15 Nov 1994 12:45:26 GMT"
        fake-vastaus {:status 200 :headers {:last-modified fake-muokkausaika} :body "ok"}]
    (component/start integraatioloki)

    (with-fake-http
      [{:url fake-tiedosto-url :method :head} fake-vastaus]
      (let [muokkausaika (alk/hae-tiedoston-muutospaivamaara
                           testitietokanta integraatioloki "tieverkko-muutospaivamaaran-haku" fake-tiedosto-url)]
        (is (= muokkausaika (time-coerce/to-sql-time (Date. fake-muokkausaika))))))))

(deftest testaa-tiedoston-lataus-alk-alustalla
  (let [testitietokanta (tietokanta/luo-tietokanta testitietokanta)
        integraatioloki (assoc (integraatioloki/->Integraatioloki nil) :db testitietokanta)
        lahdetiedosto "test/resurssit/arkistot/test_zip.zip"
        kohdetiedosto "test/resurssit/download_test.zip"]
    (component/start integraatioloki)

    (alk/hae-tiedosto integraatioloki "tieverkko-haku" lahdetiedosto kohdetiedosto)
    (is (true? (.exists (clojure.java.io/file kohdetiedosto))))
    (clojure.java.io/delete-file kohdetiedosto)))
