(ns harja.views.main
  "Harjan päänäkymä"
  (:require [harja.ui.bootstrap :as bs]
            [reagent.core :refer [atom]]
            [harja.tiedot.istunto :as istunto]
            [harja.ui.komponentti :as komp]
            [harja.ui.listings :refer [suodatettu-lista]]
            [harja.ui.yleiset :refer [linkki ajax-loader livi-pudotusvalikko]]
            [harja.ui.dom :as dom]
            [harja.ui.modal :as modal]
            [harja.ui.viesti :refer [viesti-container]]
            [harja.tiedot.navigaatio :as nav]
            [harja.loki :refer [log logt]]
            [harja.views.murupolku :as murupolku]
            [harja.views.haku :as haku]
            [cljs.core.async :refer [put! close! chan timeout]]

            [harja.views.urakat :as urakat]
            [harja.views.raportit :as raportit]
            [harja.views.tilannekuva.tilannekuva :as tilannekuva]
            [harja.views.ilmoitukset :as ilmoitukset]
            [harja.views.kartta :as kartta]
            [harja.views.hallinta :as hallinta]
            [harja.views.about :as about]
            [harja.asiakas.kommunikaatio :as k])
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]))

(defn kayttajatiedot [kayttaja]
  (let [{:keys [etunimi sukunimi]} @kayttaja
        kayttajainfo [:a {:href "#" :on-click #(do
                                  (.preventDefault %)
                                  (haku/nayta-kayttaja @kayttaja))}
       etunimi " " sukunimi]]
    (if-not (istunto/testikaytto-mahdollista?)
      kayttajainfo
      
      (let [testikayttaja @istunto/testikayttaja]
        [:span
         (if testikayttaja
           [:span.alert-warning "TESTIKÄYTTÖ"]
           kayttajainfo)
         [livi-pudotusvalikko {:valinta testikayttaja
                                       :class      "testikaytto-alasveto"
                                       :title "Järjestelmän vastuuhenkilönä voit testata Harjaa myös muissa rooleissa."
                                       :format-fn #(if %
                                                     (:kuvaus %)
                                                     (str "- Ei testikäyttäjänä -"))
                                       :valitse-fn istunto/aseta-testikayttaja!}
          (concat [nil] @istunto/testikayttajat)]]))))


(defn header [s]
  [bs/navbar {}
   [:img#harja-brand-icon {:alt      "HARJA"
                           :src      "images/harja_logo_soft.svg"
                           :on-click #(.reload js/window.location)}]
   [haku/haku]

   [:ul#sivut.nav.nav-pills

    [:li {:role "presentation" :class (when (= s :urakat) "active")}
     [linkki "Urakat" #(nav/vaihda-sivu! :urakat)]]

    [:li {:role "presentation" :class (when (= s :raportit) "active")}
     [linkki "Raportit" #(nav/vaihda-sivu! :raportit)]]

    [:li {:role "presentation" :class (when (= s :tilannekuva) "active")}
     [linkki "Tilannekuva" #(nav/vaihda-sivu! :tilannekuva)]]

    [:li {:role "presentation" :class (when (= s :ilmoitukset) "active")}
     [linkki "Ilmoitukset" #(nav/vaihda-sivu! :ilmoitukset)]]

    [:li {:role "presentation" :class (when (= s :hallinta) "active")}
     [linkki "Hallinta" #(nav/vaihda-sivu! :hallinta)]]]
   :right
   [kayttajatiedot istunto/kayttaja]])

(defn ladataan []
  [:div {:style {:position "absolute" :top "50%" :left "50%"}}
   [:div {:style {:position "relative" :left "-50px" :top "-20px"}}
    [ajax-loader "Ladataan..." {:luokka "ladataan-harjaa"}]]])

(defn yleinen-varoituspalkki
  "Näyttää yleisluontoisen varoituspalkin selaimen ylänurkassa.
  Ottaa varoitustekstin ja mahdollisia optioita:

  nayta-pisteanimaatio?     Näytetäänkö kolmen pisteen animaatio varoitustekstin perässä? Oletuksena false.
  linkki                    Varoitustekstin perässä olevan linkin teksti
  linkki-fn                 Linkin suoritusfunktio"
  ([varoitusteksti] (yleinen-varoituspalkki varoitusteksti {}))
  ([varoitusteksti opts]
   (assert varoitusteksti "Varoitusteksti on pakollinen!")
   (let [pisteanimaation-pisteet (atom "")
         nayta-pisteanimaatio? (:nayta-pisteanimaatio? opts)
         linkki-fn (:linkki-fn opts)
         linkki (:linkki opts)]
     (komp/luo
       (komp/ulos (let [pisteanimaatio-kaynnissa (atom true)]
                    (go-loop [[teksti & tekstit] (cycle ["" "." ".." "..."])]
                             (when @pisteanimaatio-kaynnissa
                               (<! (timeout 1000))
                               (reset! pisteanimaation-pisteet teksti)
                               (recur tekstit)))
                    #(reset! pisteanimaatio-kaynnissa false)))
       (fn []
         [:div.yhteysilmoitin.yhteys-katkennut-varoitus
          [:div.yhteysilmoitin-viesti varoitusteksti
           (when nayta-pisteanimaatio?
             [:div.yhteysilmoitin-pisteet @pisteanimaation-pisteet])
           (when linkki
             [:span " "
              [:a.klikattava {:on-click linkki-fn} linkki]])]])))))

(defn yhteys-palautunut-ilmoitus []
  [:div.yhteysilmoitin.yhteys-palautunut-ilmoitus "Yhteys palautui!"])

(defn paasisalto [sivu korkeus]
  [:div
   (cond
     @k/istunto-vanhentunut? [yleinen-varoituspalkki
                              "Istunto on vanhentunut."
                              {:linkki "Lataa sivu uudelleen"
                               :linkki-fn #(.reload js/location)}]
     @k/yhteys-katkennut? [yleinen-varoituspalkki
                           "Yhteys Harjaan on katkennut! Yritetään yhdistää uudelleen"
                           {:nayta-pisteanimaatio? true}]
     (and (not @k/yhteys-katkennut?) @k/yhteys-palautui-hetki-sitten) [yhteys-palautunut-ilmoitus])
   [:div.container
    [header sivu]]

   (when @nav/murupolku-nakyvissa?
     [:div.container
      [murupolku/murupolku]])

   ^{:key "harjan-paasisalto"}
   [:div.container.sisalto {:style {:min-height (max 200 (- @dom/korkeus 220))}} ; contentin minimikorkeus pakottaa footeria alemmas
    [:div.row.row-sisalto
     [:div {:class (when-not (= sivu :tilannekuva) "col-sm-12")}
      (case sivu
        :urakat [urakat/urakat]
        :raportit [raportit/raportit]
        :ilmoitukset [ilmoitukset/ilmoitukset]
        :hallinta [hallinta/hallinta]
        :tilannekuva [tilannekuva/tilannekuva]
        :about [about/about])]]]
   [modal/modal-container]
   [viesti-container]

   ;; kartta luodaan ja liitetään DOM:iin tässä. Se asemoidaan muualla #kartan-paikka divin avulla
   ;; asetetaan alkutyyli siten, että kartta on poissa näkyvistä, jos näkymässä on kartta,
   ;; se asemoidaan mountin jälkeen
   ^{:key "kartta-container"}
   [:div#kartta-container {:style {:position "absolute" :top (- korkeus)}}
    [kartta/kartta]]])

(defn varoita-jos-vanha-ie []
  (if dom/ei-tuettu-ie?
    (modal/nayta! {:otsikko "Käytössä vanha Internet Explorer"
                   :footer  [:span
                             [:button.nappi-toissijainen {:type     "button"
                                                          :on-click #(do (.preventDefault %)
                                                                         (modal/piilota!))}
                              "OK"]]}
                  [:div
                   [:p "Käytössäsi on vanhentunut Internet Explorer -selaimen versio. Emme voi taata, että kaikki Harjan ominaisuudet toimivat täysin oikein."]])))

(defn main
  "Harjan UI:n pääkomponentti"
  []
  (varoita-jos-vanha-ie)
  (komp/luo
    (fn []
      (if @nav/render-lupa?
        (let [sivu @nav/valittu-sivu
              aikakatkaistu? @istunto/istunto-aikakatkaistu?
              korkeus @dom/korkeus
              kayttaja @istunto/kayttaja]
          (if aikakatkaistu?
            [:div "Harjan käyttö aikakatkaistu kahden tunnin käyttämättömyyden takia. Lataa sivu uudelleen."]
            (if (nil? kayttaja)
              [ladataan]
              (if (or (:poistettu kayttaja)
                      (empty? (:roolit kayttaja)))
                [:div.ei-kayttooikeutta "Ei Harja käyttöoikeutta. Ota yhteys pääkäyttäjään."]
                [paasisalto sivu korkeus]))))
        [ladataan]))))

