(ns harja.views.tilannekuva.tilannekuva
  (:require [reagent.core :refer [atom]]
            [harja.ui.komponentti :as komp]
            [harja.tiedot.tilannekuva.tilannekuva :as tiedot]
            [harja.tiedot.tilannekuva.tilannekuva-kartalla :as tilannekuva-kartalla]
            [harja.views.kartta :as kartta]
            [harja.ui.valinnat :as ui-valinnat]
            [harja.loki :refer [log tarkkaile!]]
            [harja.views.kartta.popupit :as popupit]
            [harja.views.murupolku :as murupolku]
            [harja.ui.kentat :as kentat]
            [harja.ui.yleiset :as yleiset]
            [harja.ui.dom :as dom]
            [reagent.core :as r]
            [goog.events.EventType :as EventType]
            [harja.ui.ikonit :as ikonit]
            [harja.tiedot.istunto :as istunto]
            [harja.ui.checkbox :as checkbox]
            [harja.ui.on-off-valinta :as on-off]
            [harja.domain.tilannekuva :as tk]
            [harja.ui.modal :as modal]
            [harja.tiedot.navigaatio :as nav])
  (:require-macros [reagent.ratom :refer [reaction]]
                   [harja.atom :refer [reaction-writable]]))

(def hallintapaneeli-max-korkeus (atom nil))

(defn aseta-hallintapaneelin-max-korkeus [paneelin-sisalto]
  (let [r (.getBoundingClientRect paneelin-sisalto)
        etaisyys-alareunaan (- @dom/korkeus (.-top r))]
    (reset! hallintapaneeli-max-korkeus (max
                                          200
                                          (- etaisyys-alareunaan 30)))))

(defn tilan-vaihtaja []
  (let [on-off-tila (atom (not= :nykytilanne @tiedot/valittu-tila))]
    (fn []
      [:div#tk-tilan-vaihto
       [:div.tk-tilan-vaihto-nykytilanne "Nykytilanne"]
       [:div.tk-tilan-vaihto-historia "Historia"]
       [on-off/on-off-valinta on-off-tila {:luokka "on-off-tilannekuva"
                                           :on-change (fn []
                                                        ;; Päivitä valittu tila
                                                        (reset! tiedot/valittu-tila
                                                                (if (false? @on-off-tila)
                                                                  :nykytilanne
                                                                  :historiakuva))
                                                        (reset! tiedot/suodattimet
                                                                (if (= :nykytilanne @tiedot/valittu-tila)
                                                                  (assoc-in @tiedot/suodattimet
                                                                            [:ilmoitukset :tilat]
                                                                            tiedot/ilmoitusten-tilat-nykytilanteessa)
                                                                  (assoc-in @tiedot/suodattimet
                                                                            [:ilmoitukset :tilat]
                                                                            tiedot/ilmoitusten-tilat-historiakuvassa))))}]])))

(defn nykytilanteen-aikavalinnat []
  [:div#tk-nykytilanteen-aikavalit
   [kentat/tee-kentta {:tyyppi :radio
                       :valinta-nayta first
                       :valinta-arvo second
                       :valinnat tiedot/nykytilanteen-aikasuodatin-tunteina}
    tiedot/nykytilanteen-aikasuodattimen-arvo]])

(defn historiankuvan-aikavalinnat []
  [:div#tk-historiakuvan-aikavalit
   [ui-valinnat/aikavali tiedot/historiakuvan-aikavali {:nayta-otsikko? false
                                                        :aikavalin-rajoitus [12 :kuukausi]
                                                        :aloitusaika-pakota-suunta :alas-oikea
                                                        :paattymisaika-pakota-suunta :alas-vasen}]])

(defn yksittainen-suodatincheckbox
  "suodatin-polku on polku, josta tämän checkboxin nimi ja tila löytyy suodattimet-atomissa"
  [nimi suodattimet-atom suodatin-polku]
  [checkbox/checkbox
   (r/wrap (checkbox/boolean->checkbox-tila-keyword
             (get-in @suodattimet-atom suodatin-polku))
           (fn [uusi-tila]
             (swap! suodattimet-atom
                    assoc-in
                    suodatin-polku
                    (checkbox/checkbox-tila-keyword->boolean uusi-tila))))
   nimi])

(def auki-oleva-checkbox-ryhma (atom nil))

(defn checkbox-suodatinryhma
  "ryhma-polku on polku, josta tämän checkbox-ryhmän jäsenten nimet ja tilat löytyvät suodattimet-atomissa.
   kokoelma-atomin antaminen tarkoittaa, että checkbox-ryhmä on osa usean checkbox-ryhmän kokoelmaa, joista
   vain atomin ilmoittama ryhmä voi olla kerrallaan auki. Jos kokoelmaa ei anneta, tämä checkbox-ryhmä ylläpitää
   itse omaa auki/kiinni-tilaansa."
  [otsikko suodattimet-atom ryhma-polku kokoelma-atom]
  (let [oma-auki-tila (atom false)
        ryhmanjohtaja-tila-atom (reaction-writable
                                  (if (every? true? (vals (get-in @suodattimet-atom ryhma-polku)))
                                    :valittu
                                    (if (every? false? (vals (get-in @suodattimet-atom ryhma-polku)))
                                      :ei-valittu
                                      :osittain-valittu)))]
    (fn [otsikko suodattimet-atom ryhma-polku kokoelma-atom]
      (let [ryhman-elementtien-avaimet (or (get-in tk/tehtavien-jarjestys ryhma-polku)
                                           (sort-by :otsikko (keys (get-in @suodattimet-atom ryhma-polku))))
            auki? (fn [] (or @oma-auki-tila
                             (and kokoelma-atom
                                  (= otsikko @kokoelma-atom))))]
        (when-not (empty? ryhman-elementtien-avaimet)
          [:div.tk-checkbox-ryhma
           [:div.tk-checkbox-ryhma-otsikko.klikattava
            {:on-click (fn [_]
                         (if kokoelma-atom
                           ;; Osa kokoelmaa, vain yksi kokoelman jäsen voi olla kerrallaan auki
                           (if (= otsikko @kokoelma-atom)
                             (reset! kokoelma-atom nil)
                             (reset! kokoelma-atom otsikko))
                           ;; Ylläpitää itse omaa auki/kiinni-tilaansa
                           (swap! oma-auki-tila not))
                         (aseta-hallintapaneelin-max-korkeus (dom/elementti-idlla "tk-suodattimet")))}
            [:span {:class (str
                             "tk-checkbox-ryhma-tila chevron-rotate "
                             (when-not (auki?) "chevron-rotate-down"))}
             (if (auki?)
               (ikonit/livicon-chevron-down) (ikonit/livicon-chevron-right))]
            [:div.tk-checkbox-ryhma-checkbox {:on-click #(.stopPropagation %)}
             [checkbox/checkbox ryhmanjohtaja-tila-atom otsikko
              {:width "230px" ;; 100% ei toimi tässä kontekstissa, joten määritetään leveydeksi paneelin leveys
               :on-change (fn [uusi-tila]
                            ;; Aseta kaikkien tämän ryhmän suodattimien tilaksi tämän elementin uusi tila.
                            (when (not= :osittain-valittu uusi-tila)
                              (reset! suodattimet-atom
                                      (reduce (fn [edellinen-map tehtava-avain]
                                                (assoc-in edellinen-map
                                                          (conj ryhma-polku tehtava-avain)
                                                          (checkbox/checkbox-tila-keyword->boolean uusi-tila)))
                                              @suodattimet-atom
                                              (keys (get-in @suodattimet-atom ryhma-polku))))))}]]]

           (when (auki?)
             [:div.tk-checkbox-ryhma-sisalto
              (doall (for [elementti (seq ryhman-elementtien-avaimet)]
                       ^{:key (str "pudotusvalikon-asia-" (:id elementti))}
                       [yksittainen-suodatincheckbox
                        (:otsikko elementti)
                        suodattimet-atom
                        (conj ryhma-polku elementti)]))])])))))

(defn aluesuodattimet []
  (let [uusimaa "Uusimaa"
        varsinais-suomi "Varsinais-Suomi"
        kaakkois-suomi "Kaakkois-Suomi"
        pirkanmaa "Pirkanmaa"
        pohjois-savo "Pohjois-Savo"
        keski-suomi "Keski-Suomi"
        etela-pohjanmaa "Etelä-Pohjanmaa"
        pohjois-pohjanmaa "Pohjois-Pohjanmaa ja Kainuu"
        lappi "Lappi"
        onko-alueita? (reaction-writable
                        (some
                          (fn [[_ suodattimet]]
                            (not (empty? suodattimet)))
                          (:alueet @tiedot/suodattimet)))
        ensimmainen-haku-kaynnissa? (reaction-writable
                                      (and (empty? (:alueet @tiedot/suodattimet))
                                           (nil? @tiedot/uudet-aluesuodattimet)))]
    (komp/luo
      (fn []
        [:div#tk-aluevalikko
         [:span#tk-alueotsikko (cond
                                 @ensimmainen-haku-kaynnissa? "Haetaan alueita"
                                 @onko-alueita? "Näytä alueilta"
                                 :else "Ei näytettäviä alueita")]
         [yleiset/livi-pudotusvalikko {:valinta @nav/urakkatyyppi
                                       :format-fn #(if % (:nimi %) "Kaikki")
                                       :valitse-fn nav/vaihda-urakkatyyppi!
                                       :class (str "alasveto-urakkatyyppi" (when (boolean @nav/valittu-urakka) " disabled"))
                                       :disabled (boolean @nav/valittu-urakka)
                                       :pakota-suunta :ylos}
          nav/+urakkatyypit+]

         (if @ensimmainen-haku-kaynnissa?
           [yleiset/ajax-loader]

           [:div#tk-aluevaihtoehdot
            [checkbox-suodatinryhma uusimaa tiedot/suodattimet [:alueet uusimaa] nil]
            [checkbox-suodatinryhma varsinais-suomi tiedot/suodattimet [:alueet varsinais-suomi] nil]
            [checkbox-suodatinryhma kaakkois-suomi tiedot/suodattimet [:alueet kaakkois-suomi] nil]
            [checkbox-suodatinryhma pirkanmaa tiedot/suodattimet [:alueet pirkanmaa] nil]
            [checkbox-suodatinryhma pohjois-savo tiedot/suodattimet [:alueet pohjois-savo] nil]
            [checkbox-suodatinryhma keski-suomi tiedot/suodattimet [:alueet keski-suomi] nil]
            [checkbox-suodatinryhma etela-pohjanmaa tiedot/suodattimet [:alueet etela-pohjanmaa] nil]
            [checkbox-suodatinryhma pohjois-pohjanmaa tiedot/suodattimet [:alueet pohjois-pohjanmaa] nil]
            [checkbox-suodatinryhma lappi tiedot/suodattimet [:alueet lappi] nil]])]))))

(defn aikasuodattimet []
  [:div#tk-paavalikko
   [:span "Näytä aikavälillä" (when-not (= :nykytilanne @tiedot/valittu-tila)
                                " (max. yksi vuosi):")]
   (when (= :nykytilanne @tiedot/valittu-tila)
     [nykytilanteen-aikavalinnat])
   (when (= :historiakuva @tiedot/valittu-tila)
     [historiankuvan-aikavalinnat])
   (when (= :nykytilanne @tiedot/valittu-tila)
     [checkbox-suodatinryhma "Ilmoitukset" tiedot/suodattimet [:ilmoitukset :tyypit] auki-oleva-checkbox-ryhma])
   (when (= :nykytilanne @tiedot/valittu-tila)
     [checkbox-suodatinryhma "Ylläpito" tiedot/suodattimet [:yllapito] auki-oleva-checkbox-ryhma])
   [:div.tk-suodatinryhmat
    (when (= :historiakuva @tiedot/valittu-tila)
      [:div.tk-suodatinryhmat
       ^{:key "ilmoitukset"} ; Avainta ei ehkä tarvittaisi tässä, mutta jostain syystä Reagent luulee näitä muuten samoiksi
       [checkbox-suodatinryhma "Ilmoitukset" tiedot/suodattimet [:ilmoitukset :tyypit] auki-oleva-checkbox-ryhma]
       ^{:key "yllapito"}
       [checkbox-suodatinryhma "Ylläpito" tiedot/suodattimet [:yllapito] auki-oleva-checkbox-ryhma]])
    [:div.tk-suodatinryhmat
     [checkbox-suodatinryhma "Talvihoitotyöt" tiedot/suodattimet [:talvi] auki-oleva-checkbox-ryhma]
     [checkbox-suodatinryhma "Kesähoitotyöt" tiedot/suodattimet [:kesa] auki-oleva-checkbox-ryhma]
     [checkbox-suodatinryhma "Laatupoikkeamat" tiedot/suodattimet [:laatupoikkeamat] auki-oleva-checkbox-ryhma]
     [checkbox-suodatinryhma "Tarkastukset" tiedot/suodattimet [:tarkastukset] auki-oleva-checkbox-ryhma]]]
   [:div.tk-yksittaiset-suodattimet
    [yksittainen-suodatincheckbox "Turvallisuuspoikkeamat"
     tiedot/suodattimet [:turvallisuus tk/turvallisuuspoikkeamat]
     auki-oleva-checkbox-ryhma]]])

(defn suodattimet []
  (let [resize-kuuntelija (fn [this _]
                            (aseta-hallintapaneelin-max-korkeus (r/dom-node this)))]
    (komp/luo
      (komp/dom-kuuntelija js/window
                           EventType/RESIZE resize-kuuntelija)
      (fn []
        [:div#tk-suodattimet {:style {:max-height @hallintapaneeli-max-korkeus :overflow "auto"}}
         [tilan-vaihtaja]
         [aikasuodattimet]
         [aluesuodattimet]]))))

(def hallintapaneeli (atom {1 {:auki true :otsikko "Hallintapaneeli" :sisalto [suodattimet]}}))

(defn tilannekuva []
  (komp/luo
    (komp/lippu tiedot/nakymassa? tilannekuva-kartalla/karttataso-tilannekuva istunto/ajastin-taukotilassa?)
    (komp/sisaan-ulos #(do (reset! kartta/pida-geometriat-nakyvilla? false)
                           (kartta/aseta-paivitetaan-karttaa-tila! true)
                           (reset! tiedot/valittu-urakka-tilannekuvaan-tullessa @nav/valittu-urakka)
                           (reset! tiedot/valittu-hallintayksikko-tilannekuvaan-tullessa @nav/valittu-hallintayksikko)
                           (tiedot/seuraa-alueita!))
                      #(do (reset! kartta/pida-geometriat-nakyvilla? true)
                           (kartta/aseta-paivitetaan-karttaa-tila! false)
                           (reset! tiedot/valittu-urakka-tilannekuvaan-tullessa nil)
                           (reset! tiedot/valittu-hallintayksikko-tilannekuvaan-tullessa nil)
                           (tiedot/lopeta-alueiden-seuraus!)))
    (komp/kuuntelija [:toteuma-klikattu :ilmoitus-klikattu
                      :laatupoikkeama-klikattu :tarkastus-klikattu :turvallisuuspoikkeama-klikattu
                      :paallystys-klikattu :paikkaus-klikattu :tyokone-klikattu
                      :uusi-tyokonedata :suljettu-tieosuus-klikattu]
                     (fn [_ tapahtuma] (popupit/nayta-popup tapahtuma))
                     :popup-suljettu
                     #(reset! popupit/klikattu-tyokone nil)
                     :ilmoituksen-kuittaustiedot-päivitetty
                     (fn [_ ilmoitus]
                       (modal/piilota!)
                       (tiedot/paivita-ilmoituksen-tiedot (:id ilmoitus))))
    {:component-will-mount (fn [_]
                             (kartta/aseta-yleiset-kontrollit!
                               [yleiset/haitari hallintapaneeli {:piiloita-kun-kiinni? false
                                                                 :luokka "haitari-tilannekuva"}]))
     :component-will-unmount (fn [_]
                               (kartta/tyhjenna-yleiset-kontrollit!)
                               (kartta/poista-popup!))}
    (fn []
      [:span.tilannekuva
       [kartta/kartan-paikka]])))
