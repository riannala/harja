(ns harja-laadunseuranta.tiedot.sovellus
  (:require [reagent.core :as reagent :refer [atom]]
            [harja-laadunseuranta.tiedot.projektiot :as p]
            [harja-laadunseuranta.utils :as utils])
  (:require-macros [reagent.ratom :refer [reaction run!]]))

(def sovelluksen-alkutila
  {;; Sovelluksen alustustiedot
   :alustus {:alustettu false
             :gps-tuettu false
             :ensimmainen-sijainti nil ; alustusta varten
             :verkkoyhteys (.-onLine js/navigator)
             :selain-tuettu (utils/tuettu-selain?)}

   ;; Tarkastusajon perustiedot
   :valittu-urakka nil ; Urakka valitaan tietyntyyppisiin ajoihin (päällystys), muuten päätellään automaattisesti kun tarkastus päättyy
   :tarkastusajo-id nil
   :tallennus-kaynnissa false
   :palautettava-tarkastusajo nil
   :tarkastusajo-paattymassa nil ; Jos true, näytetään päättämisdialogi

   ;; Käyttäjätiedot
   :kayttaja {:kayttajanimi nil
              :kayttajatunnus nil}

   ;; Ajonaikaiset tiedot
   :lahettamattomia-merkintoja 0
   :sijainti {:nykyinen nil
              :edellinen nil}
   :reittipisteet []
   :kirjauspisteet [] ; Kartalla näytettäviä ikoneita varten
   :tr-tiedot {:tr-osoite {:tie 20 ;; TODO Älä harkoodaa tätä?
                           :aosa 1
                           :aet 1}
               :talvihoitoluokka 2}

   ;; UI
   ;; TODO Mahdollisesti ei kannata sitoa tällaisia pieniä komponenttikohtaisia tiloja
   ;; osaksi koko softan tilaa?
   :tr-tiedot-nakyvissa false

   ;; Havainnot
   ;; TODO Voisiko tämä olla nimeltänsä pistemäinen havainto?
   :pikavalinta nil ; Tähän tallentuu pistemäinen havainto (esim. liikennemerkki vinossa)
   :kirjaamassa-havaintoa false
   :kirjaamassa-yleishavaintoa false
   :vakiohavaintojen-kuvaukset nil ; Serveriltä saadut tiedot vakiohavainnoista

   :tr-alku nil
   :tr-loppu nil

   ;; Jatkuvat havainnot
   ;; TODO Voisiko havainnot olla setti, ja nimeltä jatkuvat-havainnot?
   :havainnot {} ; Tähän tallentuu ainakin välikohtaiset havainnot (esim. liukasta true, lumista true jne.)

   ;; Mittaukset
   :talvihoito {:kitkamittaus nil
                :lumimaara nil
                :tasaisuus nil}
   :soratie {:tasaisuus nil
             :kiinteys nil
             :polyavyys nil}

   ;; Kartta
   :kartta {:keskita-ajoneuvoon false
            :nayta-kiinteistorajat false
            :nayta-ortokuva false}

   ;; Muut
   :ilmoitukset [] ;; Sisältää jonossa olevat ajastetut ilmoitukset
   :idxdb nil
   :palvelinvirhe nil})

(defonce sovellus (atom sovelluksen-alkutila))

(def vakiohavaintojen-kuvaukset (reagent/cursor sovellus [:vakiohavaintojen-kuvaukset]))
(def palautettava-tarkastusajo (reagent/cursor sovellus [:palautettava-tarkastusajo]))

(def talvihoito-kitkamittaus (reagent/cursor sovellus [:talvihoito :kitkamittaus]))
(def talvihoito-lumimaara (reagent/cursor sovellus [:talvihoito :lumimaara]))
(def talvihoito-tasaisuus (reagent/cursor sovellus [:talvihoito :tasaisuus]))
(def soratie-tasaisuus (reagent/cursor sovellus [:soratie :tasaisuus]))
(def soratie-kiinteys (reagent/cursor sovellus [:soratie :kiinteys]))
(def soratie-polyavyys (reagent/cursor sovellus [:soratie :polyavyys]))

(def tr-tiedot-nakyvissa (reagent/cursor sovellus [:tr-tiedot-nakyvissa]))
(def tr-tiedot (reagent/cursor sovellus [:tr-tiedot]))

(def tr-alku (reagent/cursor sovellus [:tr-alku]))
(def tr-loppu (reagent/cursor sovellus [:tr-loppu]))

(def hoitoluokka (reagent/cursor sovellus [:tr-tiedot :talvihoitoluokka]))
(def soratiehoitoluokka (reagent/cursor sovellus [:tr-tiedot :soratiehoitoluokka]))

(def lahettamattomia-merkintoja (reagent/cursor sovellus [:lahettamattomia-merkintoja]))

(def kayttajanimi (reagent/cursor sovellus [:kayttaja :kayttajanimi]))
(def kayttajatunnus (reagent/cursor sovellus [:kayttaja :kayttajatunnus]))

(def kirjaamassa-havaintoa (reagent/cursor sovellus [:kirjaamassa-havaintoa]))
(def kirjaamassa-yleishavaintoa (reagent/cursor sovellus [:kirjaamassa-yleishavaintoa]))

(def tr-osoite (reagent/cursor sovellus [:tr-tiedot :tr-osoite]))

(def pikavalinta (reagent/cursor sovellus [:pikavalinta]))

(def alustus-valmis (reaction (let [sovellus @sovellus]
                                (and (get-in sovellus [:alustus :gps-tuettu])
                                     (get-in sovellus [:alustus :ensimmainen-sijainti])
                                     (get-in sovellus [:alustus :verkkoyhteys])
                                     (get-in sovellus [:alustus :selain-tuettu])
                                     (:idxdb sovellus)
                                     (get-in sovellus [:kayttaja :kayttajanimi])))))

(def sovellus-alustettu (reagent/cursor sovellus [:alustus :alustettu]))
(def verkkoyhteys (reagent/cursor sovellus [:alustus :verkkoyhteys]))
(def selain-tuettu (reagent/cursor sovellus [:alustus :selain-tuettu]))
(def gps-tuettu (reagent/cursor sovellus [:alustus :gps-tuettu]))
(def ensimmainen-sijainti (reagent/cursor sovellus [:alustus :ensimmainen-sijainti]))

(def kirjauspisteet (reagent/cursor sovellus [:kirjauspisteet]))

(def sijainti (reagent/cursor sovellus [:sijainti]))
(def valittu-urakka (reagent/cursor sovellus [:valittu-urakka]))
(def tarkastusajo-id (reagent/cursor sovellus [:tarkastusajo-id]))

(def tyhja-sijainti
  {:lat 0
   :lon 0
   :heading 0
   })

(def ajoneuvon-sijainti (reaction
                          (if (:nykyinen @sijainti)
                            (:nykyinen @sijainti)
                            tyhja-sijainti)))

(def kartan-keskipiste (reaction @ajoneuvon-sijainti))

(def tallennus-kaynnissa (reagent/cursor sovellus [:tallennus-kaynnissa]))
(def ilmoitukset (reagent/cursor sovellus [:ilmoitukset]))

(def nayta-kiinteistorajat (reagent/cursor sovellus [:kartta :nayta-kiinteistorajat]))
(def nayta-ortokuva (reagent/cursor sovellus [:kartta :nayta-ortokuva]))

(def keskita-ajoneuvoon (reagent/cursor sovellus [:kartta :keskita-ajoneuvoon]))

(def karttaoptiot (reaction {:seuraa-sijaintia (or @tallennus-kaynnissa @keskita-ajoneuvoon)
                             :nayta-kiinteistorajat @nayta-kiinteistorajat
                             :nayta-ortokuva @nayta-ortokuva}))

(def havainnot (reagent/cursor sovellus [:havainnot]))

(def reittisegmentti (reaction
                       (let [{:keys [nykyinen edellinen]} @sijainti]
                         (when (and nykyinen edellinen)
                           {:segmentti [(p/latlon-vektoriksi edellinen)
                                        (p/latlon-vektoriksi nykyinen)]
                            :vari (let [s @havainnot]
                                    (cond
                                      (:liukasta s) "blue"
                                      (:lumista s) "blue" ;; TODO Onko tämä oikein?
                                      (:soratie s) "brown"
                                      (:tasauspuute s) "green"
                                      (:yleishavainto s) "red"
                                      :default "black"))}))))

(def reittipisteet (reagent/cursor sovellus [:reittipisteet]))

(def yleishavainto-kaynnissa (reagent/cursor sovellus [:havainnot :yleishavainto]))

(def idxdb (reagent/cursor sovellus [:idxdb]))

(def palvelinvirhe (reagent/cursor sovellus [:palvelinvirhe]))

(def sijainnin-tallennus-mahdollinen (reaction (and @idxdb @tarkastusajo-id)))

(def tarkastusajo-paattymassa (reagent/cursor sovellus [:tarkastusajo-paattymassa]))

(def nayta-paanavigointi? (reaction (boolean (and @tarkastusajo-id
                                                  @tallennus-kaynnissa
                                                  (not @tarkastusajo-paattymassa)))))


(defn valitse-urakka! [urakka]
  (reset! valittu-urakka urakka))