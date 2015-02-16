(ns harja.views.main
  "Harjan päänäkymä"
  (:require [bootstrap :as bs]
            [reagent.core :refer [atom]]
            [harja.tiedot.istunto :as istunto]
            [harja.ui.listings :refer [filtered-listing]]
            [harja.ui.leaflet :refer [leaflet]]
            [harja.ui.yleiset :refer [linkki]]
            
            [harja.tiedot.navigaatio :as nav]
            [harja.views.murupolku :as murupolku]
            
            [harja.views.urakat :as urakat]
            [harja.views.raportit :as raportit]
            [harja.views.tilannekuva :as tilannekuva]
            [harja.views.ilmoitukset :as ilmoitukset]
            [harja.views.kartta :as kartta]
            [harja.views.hallinta :as hallinta]
            [harja.views.about :as about]))



(defn kayttajatiedot [kayttaja]
  [:a {:href "#"} (:nimi @kayttaja)])

(defn header [s]
  [bs/navbar {}
     [:img {
            :id "harja-brand-icon"
            :alt "HARJA"
            :src "images/harja-brand-text.png"
            :on-click #(.reload js/window.location)}]
     [:form.navbar-form.navbar-left {:role "search"}
      [:div.form-group
       [:input.form-control {:type "text" :placeholder "Hae..."}]]
      [:button.btn.btn-default {:type "button"} "Hae"]]

   ;; FIXME: active luokka valitulle sivulle
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

(defn footer []
  [:footer#footer {:role "contentinfo"}
   [:div#footer-wrap
    [:a {:href "http://www.liikennevirasto.fi"}
     "Liikennevirasto, vaihde 0295 34 3000, faksi 0295 34 3700, etunimi.sukunimi(at)liikennevirasto.fi"]
    [:div 
     [linkki "Tietoja" #(nav/vaihda-sivu! :about)]]]])

(defn main
  "Harjan UI:n pääkomponentti"
  []
  (let [sivu @nav/sivu
        kartan-koko @nav/kartan-koko]
    [:span
     [:div.container
      [header sivu]]
     [:div.container
      [murupolku/murupolku]]
  
     (let [[sisallon-luokka kartan-luokka] 
                                              (case (cond
                                                      (= sivu :hallinta) :hidden
                                                      (= sivu :about) :hidden
                                                      (= sivu :tilannekuva) :L
                                                      :default kartan-koko)
                                                  :hidden ["col-sm-12" "hide"]
                                                  :S ["col-sm-10" "col-sm-2"]
                                                  :M ["col-sm-6" "col-sm-6"]
                                                  :L ["hide" "col-sm-12"])]
       ;; Bootstrap grid system: http://getbootstrap.com/css/#grid
       [:div.container
        [:div.row
         [:div#sidebar-left {:class sisallon-luokka}
          (case sivu
            :urakat [urakat/urakat]
            :raportit [raportit/raportit]
            :tilannekuva [tilannekuva/tilannekuva]
            :ilmoitukset [ilmoitukset/ilmoitukset]
            :hallinta [hallinta/hallinta]
            :about [about/about]
            )]
         [:div#kartta-container {:class kartan-luokka}
          [kartta/kartta]]]])
     [footer]
     ]))

