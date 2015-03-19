(ns harja.views.hallinta.kayttajat
  "Käyttäjähallinnan näkymä"
  (:require [reagent.core :refer [atom] :as r]
            [cljs.core.async :refer [<! >! chan]]

            [harja.tiedot.kayttajat :as k]
            [harja.tiedot.urakat :as u]
            [harja.tiedot.navigaatio :as nav]
            
            [harja.ui.grid :as grid]
            [harja.ui.ikonit :as ikonit]
            [harja.ui.yleiset :as yleiset]
            [harja.ui.modal :refer [modal] :as modal]
            [harja.ui.viesti :as viesti]
            [bootstrap :as bs]

            [harja.ui.leaflet :refer [leaflet]]
            [harja.ui.protokollat :as protokollat]
            
            [harja.loki :refer [log]]
            [harja.asiakas.tapahtumat :as t]
            [clojure.string :as str]
            [harja.pvm :as pvm])
  (:require-macros [cljs.core.async.macros :refer [go]]
                   [reagent.ratom :refer [reaction run!]]
                   [harja.ui.yleiset :refer [deftk]]))

;; Tietokannan rooli enumin selvempi kuvaus
(def +rooli->kuvaus+
  {"jarjestelmavastuuhenkilo" "Järjestelmävastuuhenkilö"
   "tilaajan kayttaja" " Tilaajan käyttäjä"
   "urakanvalvoja" "Urakanvalvoja"
   "vaylamuodon vastuuhenkilo" "Väylämuodon vastuuhenkilö"
   "liikennepäivystäjä" "Liikennepäivystäjä"
   "tilaajan asiantuntija" "Tilaajan asiantuntija"
   "tilaajan laadunvalvontakonsultti" "Tilaajan laadunvalvontakonsultti"
   "urakoitsijan paakayttaja" "Urakoitsijan pääkäyttäjä"
   "urakoitsijan urakan vastuuhenkilo" "Urakoitsijan urakan vastuuhenkilö"
   "urakoitsijan kayttaja" "Urakoitsijan käyttäjä"
   "urakoitsijan laatuvastaava" "Urakoitsijan laatuvastaava"})


(def valittu-kayttaja
  "Tällä hetkellä muokattava käyttäjä"
  (atom nil))

(defonce haku (atom ""))


(defonce kayttajalista (atom nil))
(defonce kayttajien-haku
  (run! (let [haku @haku]
          (go (let [[lkm data] (<! (k/hae-kayttajat haku 0 500))]
                (reset! kayttajalista data))))))


    

(defn kayttajaluettelo
  "Käyttäjälistauskomponentti"
  []
  [grid/grid
   {:otsikko "Käyttäjät"
    :tyhja "Ei käyttäjiä."
    :rivi-klikattu #(reset! valittu-kayttaja %)
    }
       
   [{:otsikko "Nimi" :hae #(str (:etunimi %) " " (:sukunimi %)) :leveys "30%"}
    {:otsikko "Organisaatio" :nimi :org-nimi
     :hae #(:nimi (:organisaatio %))
     :leveys "30%"}

    {:otsikko "Roolit" :nimi :roolit
     :fmt #(str/join ", " (map +rooli->kuvaus+ %))
     :leveys "40%"}
    ]
       
   @kayttajalista])

(defn valitut-urakat [urakat-map]
  (into #{}
        (comp
         (filter #(not (:poistettu %)))
         (map (comp :id :urakka)))
        (vals urakat-map)))
              
(defn valitse-kartalta [g]
  (let [kuuntelija (atom nil)
        avain (gensym "kayttajat")]
    (r/create-class
     {:component-will-unmount
      (fn [this]
        ;; poista kuuntelija
        (when-let [kuuntelija @kuuntelija]
          (log "poista kuuntelija")
          (kuuntelija))
        ;; poista kartan pakotus
        (swap! nav/tarvitsen-karttaa
               (fn [tk]
                 (disj tk avain))))

      :component-will-update
      (fn [this _]
        (if (not (@nav/tarvitsen-karttaa avain))
          (when-let [kk @kuuntelija]
            (log "en tarvitse karttaa, mutta minulla on kuuntelija... poistetaan!")
            (kk)
            (reset! kuuntelija nil))))
      
      :reagent-render
      (fn [g]
        (let [tk @nav/tarvitsen-karttaa
              kk @kuuntelija]
         
          [:div
           [:button.btn.btn-default
            {:on-click #(do (.preventDefault %)
                            (swap! nav/tarvitsen-karttaa
                                 (fn [tk]
                                   (if (tk avain)
                                     (disj tk avain)
                                     #{avain})))

                            (when-not (nil? @nav/valittu-urakka)
                              ;; Ei voi olla urakan kontekstissa, jos valitaan urakoita
                              (nav/valitse-urakka nil))
                          
                            (swap! kuuntelija
                                   (fn [k]
                                     (if k
                                       (do (k) nil)
                                       (t/kuuntele! :urakka-klikattu
                                                    (fn [urakka]
                                        ;(log "urakka valittu: " (pr-str urakka))
                                                      (let [urakat (valitut-urakat (grid/hae-muokkaustila g))]
                                                        (when-not (urakat (:id urakka))
                                                          (grid/lisaa-rivi! g {:urakka urakka
                                                                               :luotu (pvm/nyt)})))))))))}
          (if (nil? @kuuntelija)
            "Valitse kartalta"
            "Piilota kartta")]]))})))

(defn urakkalista [urakat-atom]
  [:span
   [grid/muokkaus-grid
    {:otsikko "Urakat"
     :tyhja "Ei liitettyjä urakoita."
     :muokkaa-footer valitse-kartalta
     :uusi-rivi #(assoc % :luotu (pvm/nyt))
     :muutos (fn [g]
               (log "gridi muuttui: " g))
     } 
    [{:otsikko "Liitetty urakka" :leveys "50%" :nimi :urakka
      :tyyppi :haku
      :nayta :nimi :fmt :nimi
      :lahde (reify protokollat/Haku
               ;; Tehdään oma haku urakkahaun pohjalta, joka ei näytä jo valittuja urakoita
               (hae [_ teksti]
                 (let [ch (chan)]
                   (go (let [res (<! (protokollat/hae u/urakka-haku teksti))
                             urakat (valitut-urakat @urakat-atom)]
                         (log "JO OLEMASSA OLEVAT " urakat)
                         (>! ch (into []
                                      (filter #(not (urakat (:id %))))
                                      res))))
                   ch)))}
     {:otsikko "Hallintayksikkö" :leveys "30%" :muokattava? (constantly false) :nimi :hal-nimi :hae (comp :nimi :hallintayksikko :urakka) :tyyppi :string}
     {:otsikko "Lisätty" :leveys "20%" :nimi :luotu :tyyppi :string
      :fmt pvm/pvm :muokattava? (constantly false) }]
    
    urakat-atom]])
  

(defn kayttajatiedot [k]
  (let [tyyppi (case (:tyyppi (:organisaatio k))
                   (:hallintayksikko :liikennevirasto) :tilaaja
                   :urakoitsija :urakoitsija
                   nil)
        valittu-tyyppi (atom nil)
        roolit (atom (into #{} (:roolit k)))
        toggle-rooli! (fn [r]
                        (swap! roolit (fn [roolit]
                                        (if (roolit r)
                                          (disj roolit r)
                                          (conj roolit r)))))
        roolivalinta (fn [rooli & sisalto]
                       (let [valittu (@roolit rooli)]
                         [:div.rooli
                          [:div.roolivalinta
                           [:input {:type "checkbox" :checked valittu
                                    :on-change #(toggle-rooli! rooli)
                                    :name rooli}]
                           " "
                           [:label {:for rooli
                                    :on-click #(toggle-rooli! rooli)} (+rooli->kuvaus+ rooli)]]
                          [:div.rooli-lisavalinnat
                           ;; Piilotetaan tämä displayllä, ei poisteta kokonaan, koska halutaan säilyttää
                           ;; tila jos käyttäjä klikkaa roolin pois päältä ja takaisin.
                           {:style {:display (when-not (and valittu (not (empty? sisalto)))
                                               "none")}}
                             sisalto]]))
        tiedot (atom {})

        ;; tekee urakkalistasta {<idx> <urakka>} array-mapin, muokkausgridiä varten
        urakat-muokattava #(into (array-map)
                                 (map-indexed (fn [i urakka]
                                                [i urakka]) %))
        
        urakanvalvoja-urakat (atom (array-map))
        tilaajan-laadunvalvontakonsultti-urakat (atom (array-map))
        urakan-vastuuhenkilo-urakat (atom (array-map))
        urakoitsijan-kayttaja-urakat (atom (array-map))
        urakoitsijan-laatuvastaava-urakat (atom (array-map))
        
        poista-painettu (atom nil)

        ;; tekee muokattavasta urakkalistasta tallenusmuotoisen
        urakat-tallennus (fn [muokattavat rooli]
                           (map (fn [urakkarooli]
                                  ;; poistetaan urakka ja hallintayksikkö ja lähetetään vain id:t
                                  (assoc urakkarooli
                                    :urakka {:id (get-in urakkarooli [:urakka :id])}
                                    ;;:hallintayksikko {:id (get-in urakkarooli [:hallintayksikko :id])}
                                    :rooli rooli))
                                (vals muokattavat)))
        
        tallenna! (fn []
                    (log "TALLENNETAAN KÄYTTÄJÄÄ")
                    (go 
                      (let [uudet-tiedot
                            (<! (k/tallenna-kayttajan-tiedot!
                                              (:id k)
                                              {:roolit @roolit
                                               :urakka-roolit
                                               (into []
                                                     (concat
                                                      (urakat-tallennus @urakanvalvoja-urakat "urakanvalvoja")
                                                      (urakat-tallennus @tilaajan-laadunvalvontakonsultti-urakat "tilaajan laadunvalvontakonsultti")
                                                      (urakat-tallennus @urakan-vastuuhenkilo-urakat "urakoitsijan urakan vastuuhenkilo")
                                                      (urakat-tallennus @urakoitsijan-kayttaja-urakat "urakoitsijan kayttaja")
                                                      (urakat-tallennus @urakoitsijan-laatuvastaava-urakat "urakoitsijan laatuvastaava")))
                                               }))]
                        (reset! valittu-kayttaja nil)
                        (swap! kayttajalista
                               (fn [kl]
                                 (mapv #(if (= (:id %) (:id k))
                                          ;; päivitetään käyttäjän roolit näkymään
                                          (assoc % :roolit (:roolit uudet-tiedot))
                                          %) kl)))
                        (viesti/nayta! "Käyttäjä tallennettu." :success))))

        poista! (fn []
                  (go 
                    (if (<! (k/poista-kayttaja! (:id k)))
                      (do (log "poistettiin")
                          (reset! valittu-kayttaja nil)
                          (swap! kayttajalista
                                 (fn [kl]
                                   (filterv #(not= (:id %) (:id k)) kl)))
                          (viesti/nayta! [:span "Käyttäjän " [:b (:etunimi k) " " (:sukunimi k)] " käyttöoikeus poistettu."]))
                      (viesti/nayta! "Käyttöoikeuden poisto epäonnistui!" :warning))))                  
        ]

    (go (reset! tiedot (<! (k/hae-kayttajan-tiedot (:id k)))))
    (run! (let [tiedot @tiedot]
            
            (let [urakka-roolit (group-by :rooli (:urakka-roolit tiedot))]
              (reset! urakanvalvoja-urakat
                      (urakat-muokattava (or (get urakka-roolit "urakanvalvoja") [])))
              (reset! tilaajan-laadunvalvontakonsultti-urakat
                      (urakat-muokattava (or (get urakka-roolit "tilaajan laadunvalvontakonsultti") [])))
              (reset! urakan-vastuuhenkilo-urakat
                      (urakat-muokattava (or (get urakka-roolit "urakoitsijan urakan vastuuhenkilo") [])))
              (reset! urakoitsijan-kayttaja-urakat
                      (urakat-muokattava (or (get urakka-roolit "urakoitsijan kayttaja") [])))
              (reset! urakoitsijan-laatuvastaava-urakat
                      (urakat-muokattava (or (get urakka-roolit "urakoitsijan laatuvastaava") [])))
              
              )))
    
                                   
    (r/create-class
     {
      
      :reagent-render
      (fn [k]
        [:div.kayttajatiedot
         [:button.btn.btn-default {:on-click #(reset! valittu-kayttaja nil)}
          (ikonit/chevron-left) " Takaisin käyttäjäluetteloon"]
     
         [:h3 "Muokkaa käyttäjää " (:etunimi k) " " (:sukunimi k)]
     
         [bs/panel
          {} "Perustiedot"
          [yleiset/tietoja
           {}
           "Nimi:" [:span.nimi (:etunimi k) " " (:sukunimi k)]
           "Sähköposti:" [:span.sahkoposti (:sahkoposti k)]
           "Puhelinnumero:" [:span.puhelin (:puhelin k)]
           (case (:tyyppi (:organisaatio k))
             :liikennevirasto ""
             :hallintayksikko "Hallintayksikkö:"
             :urakoitsija "Urakoitsija:") (:nimi (:organisaatio k))
           ]]
     
         [:form.form-horizontal

          ;; Valitaan käyttäjän tyyppi
          [:div.form-group
           [:label.col-sm-2.control-label {:for "kayttajatyyppi"}
            "Käyttäjätyyppi"]
           [:div.col-sm-10
            (if tyyppi
              [:span (case tyyppi
                       :tilaaja "Tilaaja"
                       :urakoitsija "Urakoitsija")]
              [:span
               [:input {:name "kayttajatyyppi" :type "radio" :value "tilaaja" :checked (= :tilaaja @valittu-tyyppi)} " Tilaaja"]
               [:input {:name "kayttajatyyppi" :type "radio" :value "urakoitsija" :checked (= :urakoitsija @valittu-tyyppi)} " Urakoitsija"]])]]

          ;; Käyttäjän roolit
          [:div.form-group
           [:label.col-sm-2.control-label
            "Roolit:"]
           [:div.col-sm-10.roolit
            (if (= tyyppi :tilaaja)
              [:span
               [roolivalinta "jarjestelmavastuuhenkilo"]
               [roolivalinta "tilaajan kayttaja"]
               [roolivalinta "urakanvalvoja"
                ^{:key "urakat"}
                [urakkalista urakanvalvoja-urakat]]
               [roolivalinta "vaylamuodon vastuuhenkilo"
                ^{:key "vaylamuoto"}
                [:div
                 "Väylämuoto:"
                 [:div.dropdown
                  [:button.btn.btn-default {:disabled "disabled"}
                   "Tie " [:span.caret]]];;[alasvetovalinta {:valinta "Tie" :format-fn str :class "" :disabled true} ["Tie" "Foo"]]
                 ]]
               [roolivalinta "tilaajan asiantuntija"]
               [roolivalinta "tilaajan laadunvalvontakonsultti"
                ^{:key "urakat"}
                [urakkalista tilaajan-laadunvalvontakonsultti-urakat]]]

              ;; urakoitsijan roolit
              [:span
               [roolivalinta "urakoitsijan paakayttaja"]
               [roolivalinta "urakoitsijan urakan vastuuhenkilo"
                ^{:key "urakat"}
                [urakkalista urakan-vastuuhenkilo-urakat]]
               
               [roolivalinta "urakoitsijan kayttaja"
                ^{:key "urakat"}
                [urakkalista urakoitsijan-kayttaja-urakat]]
               [roolivalinta "urakoitsijan laatuvastaava"
                ^{:key "urakat"}
                [urakkalista urakoitsijan-laatuvastaava-urakat]]]
              )]]

          [:div.form-group
           [:label.col-sm-2.control-label
            "Toiminnot:"]
           [:div.col-sm-10.toiminnot
            [:button.btn.btn-primary {:on-click #(do (.preventDefault %)
                                                     (tallenna!))}
             (ikonit/ok) " Tallenna"]
            [:span.pull-right
             [:button.btn.btn-danger {:disabled (when @poista-painettu "disabled")
                                      :on-click #(do (.preventDefault %)
                                                     (reset! poista-painettu true))}
              (ikonit/ban-circle) " Poista käyttöoikeus"]
             (when @poista-painettu
               [modal {:otsikko "Poistetaanko käyttöoikeus?"
                       :footer [:span
                                [:button.btn.btn-default {:type "button"
                                                          :on-click #(do (.preventDefault %)
                                                                         (modal/piilota!))}
                                 "Peruuta"]
                                [:button.btn.btn-danger {:type "button"
                                                         :on-click #(do (.preventDefault %)
                                                                        (modal/piilota!)
                                                                        (poista!))}
                                 "Poista käyttöoikeus"]
                                ]
                       :sulje #(reset! poista-painettu false)}
                [:div "Haluatko varmasti poistaa käyttäjän "
                 [:b (:etunimi k) " " (:sukunimi k)] " käyttöoikeuden?"]])]
            ]]
          
          ]])})))
              
(defn kayttajat
  "Käyttäjähallinnan pääkomponentti"
  []
  (if-let [vk @valittu-kayttaja]
    [kayttajatiedot vk]
    [kayttajaluettelo]))
