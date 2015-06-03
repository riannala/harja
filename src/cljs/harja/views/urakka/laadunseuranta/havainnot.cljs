(ns harja.views.urakka.laadunseuranta.havainnot
  "Listaa urakan havainnot, jotka voivat olla joko tarkastukseen liittyviä tai irrallisia."
  (:require [reagent.core :refer [atom] :as r]
            [harja.tiedot.urakka.laadunseuranta :as laadunseuranta]
            [harja.ui.grid :as grid]
            [harja.ui.yleiset :as yleiset]
            [harja.ui.ikonit :as ikonit]
            [harja.ui.lomake :as lomake]
            [harja.ui.komponentti :as komp]
            [harja.views.urakka.valinnat :as urakka-valinnat]
            [harja.tiedot.navigaatio :as nav]
            [harja.tiedot.urakka :as tiedot-urakka]
            [harja.pvm :as pvm]
            [harja.loki :refer [log tarkkaile!]]
            )
  (:require-macros [reagent.ratom :refer [reaction]]))

(defonce listaus (atom :kaikki))

(defonce hoitokauden-kuukaudet
  (reaction (some-> @tiedot-urakka/valittu-hoitokausi
                    pvm/hoitokauden-kuukausivalit)))

(defonce aikavali
  (reaction (first @hoitokauden-kuukaudet)))

(defonce valittu-havainto (atom nil))

(defn kuvaile-kasittelytapa [kasittelytapa]
  (case kasittelytapa
    :tyomaakokous "Työmaakokous"
    :puhelin "Puhelimitse"
    :kommentit "Harja-kommenttien perusteella"
    :muu "Muu tapa"
    nil))

(defn kuvaile-paatostyyppi [paatos]
  (case paatos
    :sanktio "Sanktio"
    :ei-sanktiota "Ei sanktiota"
    :hylatty "Hylätty"))
  
(defn kuvaile-paatos [{:keys [pvm paatos kasittelytapa]}]
  (when paatos
    (str
     (pvm/pvm pvm)
     " "
     (kuvaile-paatostyyppi paatos)
     " ("
     (kuvaile-kasittelytapa kasittelytapa) ")")))


  
(defn havaintolistaus
  "Listaa urakan havainnot"
  []

  [:div.havainnot

   [urakka-valinnat/urakan-hoitokausi @nav/valittu-urakka]

   [yleiset/pudotusvalikko
    "Kuukausi"
    {:valinta @aikavali
     :valitse-fn #(reset! aikavali  %)
     :format-fn (fn [[kk _]] (str (pvm/kuukauden-nimi (pvm/kuukausi kk)) " " (pvm/vuosi kk)))}
    @hoitokauden-kuukaudet]
   
   [yleiset/pudotusvalikko
    "Näytä havainnot"
    {:valinta @listaus
     :valitse-fn #(reset! listaus %)
     :format-fn #(case %
                   :kaikki "Kaikki"
                   :kasitellyt "Käsittelyt (päätös tehty)"
                   :selvitys "Odottaa urakoitsijan selvitystä"
                   :omat "Minun kirjaamat / kommentoimat")}

    [:kaikki :selvitys :kasitellyt :omat]]


   
    
     
   [grid/grid
    {:otsikko "Havainnot" :rivi-klikattu #(reset! valittu-havainto %)}
    [{:otsikko "Päivämäärä" :nimi :pvm :fmt pvm/pvm-aika :leveys "10%"}
     {:otsikko "Kohde" :nimi :kohde :leveys "25%"}
     {:otsikko "Tekijä" :nimi :tekija :leveys "25%"}
     {:otsikko "Päätös" :nimi :paatos :fmt kuvaile-paatos :leveys "35%"} ;; Päätös
     ]

    [{:id 1
      :pvm (pvm/->pvm-aika "2.6.2015 08:22")
      :kohde "Tie 8 Sammalniemen kohdalla liukkautta"
      :tekija "Late Laadukas"
      :tekijarooli :konsultti
      :paatos nil
      :kommentit [{:pvm (pvm/->pvm-aika "2.6.2015 08:22")
                   :tekija "Late Laadukas"
                   :kommentti "Tie 8 Sammalniemen kohdalla liukkautta. Kitkamittaus tehty, arvo 0,15 alittaa laatuvaatimukset vaarallisesti."}]
      }

     {:id 2
      :pvm (pvm/->pvm-aika "22.3.2015 11:07")
      :kohde "Vt 20 / Kuusamontien risteys"
      :paatos {:pvm (pvm/->pvm-aika "28.3.2015 15:00")
               :paatos :sanktio
               :kasittelytapa :tyomaakokous
               :selitys "Työmaakokouksessa käsitelty asia, urakoitsija myöntänyt aliurakoitsijan jättäneen alueen hoitamatta."}
      :tekijarooli :tilaaja
      :tekija "Sami Sanktioija"
      :kommentit [{:pvm (pvm/->pvm-aika "22.3.2015 11:07")
                   :tekija "Sami Sanktioija"
                   :kommentti "Vt 20 Kuusamontien risteyksessä on täysin jätetty auraamatta. Nyt 30cm lunta ja polanteet hyvin vaarallisia."}]
      :sanktiot {1 {:perintapvm (pvm/->pvm "8.4.2014")
                    :ryhma :A
                    :summa 3500
                    :indeksi "MAKU 2005"}}}
      

     {:id 3
      :pvm (pvm/luo-pvm 2015 1 25)
      :kohde "Tie 123, aet 100 auraamatta"
      :tekijarooli :urakoitsija
      :tekija "Unto Urakoitsija"
      :kommentit [{:pvm (pvm/luo-pvm 2015 1 25)
                   :rooli :urakoitsija
                   :tekija "Unto Urakoistija"
                   :kommentti "Emme voineet aurata tie 123, alusta, tiellä oli pysähtynyt yhdistelmärekka. Katso kuvat!"}
                  {:pvm (pvm/luo-pvm 2015 1 27)
                   :rooli :tilaaja
                   :tekija "Antti Anteeksiantava"
                   :kommentti "Näyttää tosiaan olevan, ei hätää, sitähän sattuu kaikille..."}]}
     
                   

     ]]])

(defn kommentit [kommentit]
  [:ul.kommentit
   (for [{:keys [pvm tekija kommentti]} kommentit]
     ^{:key (pvm/millisekunteina pvm)}
     [:li [:b (pvm/pvm-aika pvm) " " tekija] ": " kommentti])])


(defn paatos?
  "Onko annetussa havainnossa päätös?"
  [havainto]
  (not (nil? (get-in havainto [:paatos :paatos]))))

  
(defn havainto [havainto]
  (let [havainto (atom havainto)]
    (tarkkaile! "Havainto: " havainto)
    (komp/luo
     {:component-will-receive-props
      (fn [this havainto]
        (log "UUSI havainto: " havainto))}
     
     (fn [alkuperainen]
       [:div.havainto
        [:button.nappi-toissijainen {:on-click #(reset! valittu-havainto nil)}
         (ikonit/chevron-left) " Takaisin havaintoluetteloon"]

        [:h3 "Havainnon tiedot"]
        [lomake/lomake
         {:muokkaa! #(reset! havainto %)
          :luokka :horizontal
          :footer [:button.nappi-ensisijainen {:on-click #(log (pr-str @havainto))}
                   ;; Määritellään "verbi" tilan mukaan, jos päätöstä ei ole: Tallennetaan havainto,
                   ;; jos päätös on tässä muokkauksessa lisätty: Lukitaan havainto
         
                   (cond
                    (and (not (paatos? alkuperainen))
                         (paatos? @havainto))
                    "Lukitse havainnon päätös"
          
                    :default
                    "Tallenna havainto")]}
         [{:otsikko "Tekija" :nimi :tekijarooli
           :tyyppi :valinta
           :valinnat [:tilaaja :urakoitsija :konsultti]
           :valinta-nayta #(case %
                             :tilaaja "Tilaaja"
                             :urakoitsija "Urakoitsija"
                             :konsultti "Konsultti")
           :leveys-col 4}

          {:otsikko "Kohde" :tyyppi :string :nimi :kohde
           :leveys-col 4}

          {:otsikko "Kuvaus ja kommentit" :nimi :kommentit
           :komponentti [kommentit (:kommentit havainto)]}

          (lomake/ryhma
           "Käsittely ja päätös"
           
           {:otsikko "Käsittelyn pvm"
            :nimi :paatos-pvm :hae (comp :pvm :paatos)
            :tyyppi :pvm-aika}
           
           {:otsikko "Käsitelty" :nimi :kasittelytapa
            :hae (comp :kasittelytapa :paatos)
            :aseta #(assoc-in %1 [:paatos :kasittelytapa] %2)
            :tyyppi :valinta
           :valinnat [:tyomaakokous :puhelin :kommentit :muu]
            :valinta-nayta #(if % (kuvaile-kasittelytapa %) "- valitse käsittelytapa -")
            
            :leveys-col 4}
           
           (when (= :muu (:kasittelytapa (:paatos @havainto)))
             {:otsikko "Muu käsittelytapa"
              :nimi :kasittelytapa-selite
              :hae (comp :kasittelytapa-selite :paatos)
             :aseta #(assoc-in %1 [:paatos :kasittelytapa-selite] %2)
              :tyyppi :string
              :leveys-col 4
              :validoi [[:ei-tyhja "Anna lyhyt kuvaus käsittelytavasta."]]})


           {:otsikko "Päätös"
            :nimi :paatos-paatos
            :tyyppi :valinta
            :valinnat [:sanktio :ei-sanktiota :hylatty]
            :hae (comp :paatos :paatos)
            :aseta #(assoc-in %1 [:paatos :paatos] %2)
            :valinta-nayta #(if % (kuvaile-paatostyyppi %) "- valitse päätös -")
            :leveys-col 4}

           (when (:paatos (:paatos @havainto))
             {:otsikko "Päätöksen selitys"
              :nimi :paatoksen-selitys
              :tyyppi :text
              :hae (comp :selitys :paatos)
              :koko [80 4]
              :leveys-col 6
              :aseta #(assoc-in %1 [:paatos :selitys] %2)})


           (when (= :sanktio (:paatos (:paatos @havainto)))
             {:otsikko "Sanktiot"
              :nimi :sanktiot
              :komponentti [:div.sanktiot
                            [grid/muokkaus-grid
                             {:tyhja "Ei kirjattuja sanktioita."
                              :lisaa-rivi " Lisää sanktio"}
                             [{:otsikko "Perintäpvm" :nimi :perintapvm :tyyppi :pvm :leveys "20%"}
                              {:otsikko "Sakkoryhmä" :tyyppi :valinta :leveys "25%"
                               :nimi :ryhma
                               :valinnat [:A :B :C :muistutus]
                               :valinta-nayta #(case %
                                                 :A "Ryhmä A"
                                                 :B "Ryhmä B"
                                                 :C "Ryhmä C"
                                                 :muistutus "Muistutus"
                                                 "- valitse ryhmä -")}
                              {:otsikko "Sakko (€)" :nimi :summa :tyyppi :numero :leveys "15%"}
                              {:otsikko "Sidotaan indeksiin" :nimi :indeksi :leveys "35%"
                               :tyyppi :valinta
                               :valinnat ["MAKU 2005" "MAKU 2010"] ;; FIXME: haetaanko indeksit tiedoista?
                               :valinta-nayta #(or % "Ei sidota indeksiin")}
                              ]

                             (r/wrap (:sanktiot @havainto) #(swap! havainto assoc :sanktiot %))]]})
           )]
         
         @havainto]

        
        
                
        
        ]))))
  

(defn havainnot []
  (if-let [valittu @valittu-havainto]
    [havainto valittu]
    [havaintolistaus]))
  
  
  
