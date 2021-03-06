(ns harja.views.ilmoituskuittaukset
  "Harjan ilmoituskuittausten listaus & uuden kuittauksen kirjaus lomake."
  (:require [clojure.string :refer [capitalize]]
            [reagent.core :refer [atom]]
            [harja.atom :refer [paivita-periodisesti] :refer-macros [reaction<!]]
            [harja.tiedot.ilmoituskuittaukset :as tiedot]
            [harja.domain.ilmoitukset :as apurit]
            [harja.ui.grid :refer [grid]]
            [harja.ui.yleiset :refer [ajax-loader] :as yleiset]
            [harja.loki :refer [log tarkkaile!]]
            [harja.ui.napit :refer [palvelinkutsu-nappi] :as napit]
            [harja.ui.valinnat :refer [urakan-hoitokausi-ja-aikavali]]
            [harja.ui.lomake :as lomake]
            [harja.ui.bootstrap :as bs]
            [harja.pvm :as pvm]
            [harja.ui.ikonit :as ikonit]
            [harja.ui.komponentti :as komp]
            [harja.tiedot.ilmoitukset :as ilmoitukset]
            [harja.ui.viesti :as viesti]
            [harja.asiakas.tapahtumat :as tapahtumat]
            [cljs.core.async :refer [<!]]
            [harja.tiedot.ilmoitukset.viestit :as v]
            [harja.ui.protokollat :as protokollat])
  (:require-macros [cljs.core.async.macros :refer [go]]))


(def fraasihaku
  (reify protokollat/Haku
    (hae [_ teksti]
      (let [teksti (.toLowerCase teksti)]
        (go (into []
                  (filter #(not= -1 (.indexOf (.toLowerCase %) teksti)))
                  apurit/+kuittauksen-vakiofraasit+))))))

(defn esta-lahetys? [kuittaus]
  (or (:tallennus-kaynnissa? kuittaus)
      (nil? (:tyyppi kuittaus))))

(defn uusi-kuittaus [e! kuittaus]
  [:div
   {:class "uusi-kuittaus"}
   [lomake/lomake
    {:muokkaa! #(e! (v/->AsetaKuittausTiedot %))
     :luokka   :horizontal
     :footer   [:div
                [napit/tallenna
                 "Lähetä"
                 #(e! (v/->Kuittaa))
                 {:tallennus-kaynnissa? (:tallennus-kaynnissa? kuittaus)
                  :ikoni        (ikonit/tallenna)
                  :disabled     (esta-lahetys? kuittaus)
                  :virheviesti  "Kuittauksen tallennuksessa tai lähetyksessä T-LOIK:n tapahtui virhe."
                  :luokka       "nappi-ensisijainen"}]
                [napit/peruuta
                 "Peruuta"
                 #(e! (v/->SuljeUusiKuittaus))
                 {:luokka "pull-right"}]]}
    [(lomake/ryhma {:otsikko    "Kuittaus"}
                   {:nimi          :tyyppi
                    :otsikko       "Tyyppi"
                    :pakollinen?   true
                    :tyyppi        :valinta
                    :valinnat      apurit/kuittaustyypit
                    :valinta-nayta #(if %
                                     (apurit/kuittaustyypin-selite %)
                                     "- Valitse kuittaustyyppi -")}
                   {:nimi :vakiofraasi
                    :otsikko "Vakiofraasi"
                    :tyyppi :haku
                    :lahde fraasihaku
                    :hae-kun-yli-n-merkkia 0}

                   {:nimi        :vapaateksti
                    :otsikko     "Vapaateksti"
                    :tyyppi      :text
                    ;; pituus on XSD-skeeman maksimi 1024 - pisimmän vakiofraasin mitta (48)
                    :pituus-max 976})

     (lomake/ryhma {:otsikko    "Käsittelijä"
                    :leveys-col 3}
                   {:nimi       :kasittelija-etunimi
                    :otsikko    "Etunimi"
                    :leveys-col 3
                    :tyyppi     :string
                    :pituus-max 32}
                   {:nimi       :kasittelija-sukunimi
                    :otsikko    "Sukunimi"
                    :leveys-col 3
                    :tyyppi     :string
                    :pituus-max 32}
                   {:nimi       :kasittelija-matkapuhelin
                    :otsikko    "Matkapuhelin"
                    :leveys-col 3
                    :tyyppi     :puhelin
                    :pituus-max 32}
                   {:nimi       :kasittelija-tyopuhelin
                    :otsikko    "Työpuhelin"
                    :leveys-col 3
                    :tyyppi     :puhelin
                    :pituus-max 32}
                   {:nimi       :kasittelija-sahkoposti
                    :otsikko    "Sähköposti"
                    :leveys-col 3
                    :tyyppi     :email
                    :pituus-max 64}
                   {:nimi       :kasittelija-organisaatio
                    :otsikko    "Organisaation nimi"
                    :leveys-col 3
                    :tyyppi     :string
                    :pituus-max 128}
                   {:nimi       :kasittelija-ytunnus
                    :otsikko    "Organisaation y-tunnus"
                    :leveys-col 3
                    :tyyppi     :string
                    :pituus-max 9})]
    kuittaus]])

(defn kanavan-ikoni [kuittaus]
  (case (:kanava kuittaus)
    :sms (ikonit/phone)
    :sahkoposti (ikonit/envelope)
    :ulkoinen_jarjestelma (ikonit/livicon-download)
    :harja (ikonit/pencil)
    nil))

(defn kuittauksen-tiedot [kuittaus]
  (let [valitys? (apurit/valitysviesti? kuittaus)]
    ^{:key (str "kuittaus-paneeli-" (:id kuittaus))}
    [bs/panel
     {:class (if valitys? "valitys-viesti" "kuittaus-viesti")}
     [:span (str (apurit/kuittaustyypin-otsikko (:kuittaustyyppi kuittaus)) " ") (kanavan-ikoni kuittaus)]
     [:span
      ^{:key "kuitattu"}
      [yleiset/tietoja {}
       (if valitys? "Lähetetty: " "Kuitattu: ") (pvm/pvm-aika-sek (:kuitattu kuittaus))
       "Vakiofraasi: " (:vakiofraasi kuittaus)

       ;; Välitysviestien tapauksessa vapaatekstissä on viestin määrämittainen raakadata, eli sähköpostin tapauksessa
       ;; HTML:ää. Ei näytetä sitä turhaan, tärkeää on joka tapauksessa tieto, kenelle välitysviesti on lähtenyt
       "Vapaateksti: " (when-not valitys? (:vapaateksti kuittaus))
       "Kanava: " (apurit/kanavan-otsikko (:kanava kuittaus))]
      [:br]
      ^{:key "kuittaaja"}
      [yleiset/tietoja {}
       (if valitys? "Vastaanottaja: " "Kuittaaja: ") (apurit/nayta-henkilo (:kuittaaja kuittaus))
       "Puhelinnumero: " (apurit/parsi-puhelinnumero (:kuittaaja kuittaus))
       "Sähköposti: " (get-in kuittaus [:kuittaaja :sahkoposti])]
      [:br]
      (when (:kasittelija kuittaus)
        ^{:key "kasittelija"}
        [yleiset/tietoja {}
         "Käsittelijä: " (apurit/nayta-henkilo (:kasittelija kuittaus))
         "Puhelinnumero: " (apurit/parsi-puhelinnumero (:kasittelija kuittaus))
         "Sähköposti: " (get-in kuittaus [:kasittelija :sahkoposti])])]]))

(defn kuittaa-monta-lomake [e! {:keys [ilmoitukset tyyppi vapaateksti tallennus-kaynnissa?]
                                :as data}]
  (let [valittuna (count ilmoitukset)]
    [:div.ilmoitukset-kuittaa-monta
     [lomake/lomake
      {:muokkaa! #(e! (v/->AsetaKuittausTiedot %))
       :palstoja 3
       :otsikko "Kuittaa monta ilmoitusta"}
      [
       (lomake/rivi
         {:otsikko "Kuittaustyyppi"
          :pakollinen? true
          :tyyppi :valinta
          :valinnat apurit/kuittaustyypit
          :valinta-nayta #(or (apurit/kuittaustyypin-selite %) "- Valitse kuittaustyyppi -")
          :nimi :tyyppi}

         {:otsikko "Vakiofraasi"
          :tyyppi :haku
          :hae-kun-yli-n-merkkia 0
          :lahde fraasihaku
          :nimi :vakiofraasi}

         {:otsikko "Vapaateksti"
          :tyyppi :text
          :koko [80 :auto]
          :pituus-max 976
          :nimi :vapaateksti})]

      data]
     [napit/tallenna
      (if (> valittuna 1)
        (str "Kuittaa " valittuna " ilmoitusta")
        "Kuittaa ilmoitus")
      #(e! (v/->Kuittaa))

      {:ikoni (ikonit/tallenna)
       :tallennus-kaynnissa? tallennus-kaynnissa?
       :luokka   (str (when tallennus-kaynnissa? "disabled ") "nappi-ensisijainen kuittaa-monta-tallennus")
       :disabled (or (:tallennus-kaynnissa? data)
                     (not (lomake/voi-tallentaa-ja-muokattu? data))
                     (zero? valittuna))}]
     [napit/peruuta "Peruuta" #(e! (v/->PeruMonenKuittaus))]
     [yleiset/vihje "Valitse kuitattavat ilmoitukset listalta."]]))
