-- Ensimmäinen ilmoitus: Oulun alueella, kysely

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, otsikko, paikankuvaus, lisatieto, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2005-2012'), 12345, '2005-10-01 10:00:00', '2005-10-01 10:05:13', TRUE,
        'Soittakaa Sepolle', 'Voisko joku soittaa?', 'Seppo Savela on pulassa ja kaipaa, että joku soittaa hänelle',
        ST_MakePoint(452935, 7186873) :: GEOMETRY, 6, 6, 6, 6, 6, 'kysely' :: ilmoitustyyppi,
        ARRAY ['saveaTiella', 'vettaTiella'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2005-2012'),
        'Seppo', 'Savela', '0441231234', '0441231234', 'seppo.savela@eiole.fi', 'asukas' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@isoveli.com');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, (SELECT valitetty FROM ilmoitus WHERE ilmoitusid=12345), 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
                                                          'Välittävä Urakoitsija', 'Y1234',  'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, (SELECT valitetty FROM ilmoitus WHERE ilmoitusid=12345), 'valitys'::kuittaustyyppi,
                                                          'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
                                                          'Välittävä Urakoitsija', 'Y1234',  'ulos'::viestisuunta, 'sahkoposti'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12345), 12345, '2005-10-01 10:07:03', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-01 10:07:13', 'valitys'::kuittaustyyppi,
                                                          'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
                                                          'Välittävä Urakoitsija', 'Y1234',  'ulos'::viestisuunta, 'sahkoposti'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-01 10:07:13', 'valitys'::kuittaustyyppi,
                                                          'Mari', 'Marttala', '085674567', 'mmarttala@isoveli.com',
                                                          'ulos'::viestisuunta, 'sahkoposti'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12345), 12345, '2005-10-01 10:34:50', 'Soitan kunhan kerkeän', 'vastaus' :: kuittaustyyppi,
        'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234',
        'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-01 10:35:50', 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
                                                          'Välittävä Urakoitsija', 'Y1234',  'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-01 10:35:50', 'valitys'::kuittaustyyppi,
                                                          'Mari', 'Marttala', '085674567', 'mmarttala@isoveli.com',
                                                          'ulos'::viestisuunta, 'sahkoposti'::viestikanava,
                                                          'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12345), 12345, '2005-10-02 11:28:50', 'Soitan lounaan jälkeen!',
        'aloitus' :: kuittaustyyppi,
        'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234',
        'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-02 11:28:50', 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
                                                          'Välittävä Urakoitsija', 'Y1234',  'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-02 11:28:50', 'valitys'::kuittaustyyppi,
                                                          'Mari', 'Marttala', '085674567', 'mmarttala@isoveli.com',
                                                          'ulos'::viestisuunta, 'sahkoposti'::viestikanava,
                                                          'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12345), 12345, '2005-10-02 12:08:02',
        'Homma on hoidettu. Ei siellä oikeastaan mitään tähdellistä asiaa ollutkaan..', 'lopetus' :: kuittaustyyppi,
        'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234',
        'Usko', 'Untamo', '04428121283', '0509288383', 'usko.untamo@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-02 12:08:52', 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
                                                          'Välittävä Urakoitsija', 'Y1234',  'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12345), 12345, '2005-10-02 12:08:52', 'valitys'::kuittaustyyppi,
                                                          'Mari', 'Marttala', '085674567', 'mmarttala@isoveli.com',
                                                          'ulos'::viestisuunta, 'sahkoposti'::viestikanava,
                                                          'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

-- Toinen ilmoitus: Oulun alueella, toimenpidepyynto

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, otsikko, paikankuvaus, lisatieto, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2005-2012'), 12346, '2005-10-10 06:05:32', '2005-10-11 06:06:37', TRUE,
        'Tiellä 6 on taas vikaa', 'Taas täällä joku mättää!', 'Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Sed posuere interdum sem. Quisque ligula eros ullamcorper quis, lacinia quis facilisis sed sapien. Mauris varius diam vitae arcu. Sed arcu lectus auctor vitae, consectetuer et venenatis eget velit. Sed augue orci, lacinia eu tincidunt et eleifend nec lacus. Donec ultricies nisl ut felis, suspendisse potenti. Lorem ipsum ligula ut hendrerit mollis, ipsum erat vehicula risus, eu suscipit sem libero nec erat. Aliquam erat volutpat. Sed congue augue vitae neque. Nulla consectetuer porttitor pede. Fusce purus morbi tortor magna condimentum vel, placerat id blandit sit amet tortor.

Mauris sed libero. Suspendisse facilisis nulla in lacinia laoreet, lorem velit accumsan velit vel mattis libero nisl et sem. Proin interdum maecenas massa turpis sagittis in, interdum non lobortis vitae massa. Quisque purus lectus, posuere eget imperdiet nec sodales id arcu. Vestibulum elit pede dictum eu, viverra non tincidunt eu ligula.

Nam molestie nec tortor. Donec placerat leo sit amet velit. Vestibulum id justo ut vitae massa. Proin in dolor mauris consequat aliquam. Donec ipsum, vestibulum ullamcorper venenatis augue. Aliquam tempus nisi in auctor vulputate, erat felis pellentesque augue nec, pellentesque lectus justo nec erat. Aliquam et nisl. Quisque sit amet dolor in justo pretium condimentum.

Vivamus placerat lacus vel vehicula scelerisque, dui enim adipiscing lacus sit amet sagittis, libero enim vitae mi. In neque magna posuere, euismod ac tincidunt tempor est. Ut suscipit nisi eu purus. Proin ut pede mauris eget ipsum. Integer vel quam nunc commodo consequat. Integer ac eros eu tellus dignissim viverra. Maecenas erat aliquam erat volutpat. Ut venenatis ipsum quis turpis. Integer cursus scelerisque lorem. Sed nec mauris id quam blandit consequat. Cras nibh mi hendrerit vitae, dapibus et aliquam et magna. Nulla vitae elit. Mauris consectetuer odio vitae augue.',
        ST_MakePoint(435847, 7216217) :: GEOMETRY, 6, 6, 6, 6, 6, 'toimenpidepyynto' :: ilmoitustyyppi,
        ARRAY ['kaivonKansiRikki', 'vettaTiella'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2005-2012'),
        'Yrjö', 'Mestari', '0441271234', '0441233424', 'tyonvalvonta@isoveli.com', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@isoveli.com');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12346), 12346, '2005-10-11 06:10:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12346), 12346, '2005-10-11 14:02:57', 'Siirretty aliurakoitsijalle',
        'muutos' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234',
        'Veljekset Ukkola Huoltoyritys', 'Y8172', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12346), 12346, '2005-10-11 19:20:57', 'Ukkolat korjasi tilanteen',
        'lopetus' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'mikael.poyta@valittavaurakoitsija.fi',
        'Välittävä Urakoitsija', 'Y1234',
        'Veljekset Ukkola Huoltoyritys', 'Y8172', 'sisaan'::viestisuunta, 'sms'::viestikanava);


-- Kolmas ilmoitus: Pudasjärvi, toimenpidepyynto, avoin

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, otsikko, paikankuvaus, lisatieto, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Pudasjärven alueurakka 2007-2012'), 12347, '2007-12-01 20:01:20', '2007-12-07 08:07:50',
        FALSE, 'Kuoppa kutostiellä', 'Kauhea kuoppa tiessä', 'Jukolan talo, eteläisessä Hämeessä, seisoo erään mäen pohjoisella rinteellä, liki Toukolan kylää. Sen läheisin ym­päristö on kivinen tanner, mutta alempana alkaa pellot, joissa, ennenkuin talo oli häviöön mennyt, aaltoili teräinen vilja. Peltojen alla on niittu, apilaäyräinen, halkileikkaama monipolvisen ojan; ja runsaasti antoi se heiniä, ennenkuin joutui laitumeksi kylän karjalle. Muutoin on talolla avaria metsiä, soita ja erämaita, jotka, tämän tilustan ensimmäisen perustajan oivallisen toiminnan kautta, olivat langenneet sille osaksi jo ison jaon käydessä entisinä aikoina. Silloinpa Jukolan isäntä, pitäen enemmän huolta jälkeentulevainsa edusta kuin omasta parhaastansa, otti vastaan osaksensa kulon polttaman metsän ja sai sillä keinolla seitsemän vertaa enemmän kuin toiset naapurinsa. Mutta kaikki kulovalkean jäljet olivat jo kadonneet hänen piiristänsä ja tuuhea metsä kasvanut sijaan. - Ja tämä on niiden seitsemän veljen koto, joiden elämänvaiheita tässä nyt käyn kertoilemaan.

Veljesten nimet vanhimmasta nuorimpaan ovat: Juhani, Tuomas, Aapo, Simeoni, Timo, Lauri ja Eero. Ovat heistä Tuomas ja Aapo kaksoispari ja samoin Timo ja Lauri. Juhanin, vanhimman veljen, ikä on kaksikymmentä ja viisi vuotta, mutta Eero, nuorin heistä, on tuskin nähnyt kahdeksantoista auringon kierrosta. Ruumiin vartalo heillä on tukeva ja harteva, pituus kohtalainen, paitsi Eeron, joka vielä on kovin lyhyt. Pisin heistä kaikista on Aapo, ehkä ei suinkaan hartevin. Tämä jälkimmäinen etu ja kunnia on Tuomaan, joka oikein on kuuluisa hartioittensa levyyden tähden. Omituisuus, joka heitä kaikkia yhteisesti merkitsee, on heidän ruskea ihonsa ja kankea, hampunkarvainen tukkansa, jonka karheus etenkin Juhanilla on silmään pistävä.

Heidän isäänsä, joka oli ankaran innokas metsämies, kohtasi hänen parhaassa iässään äkisti surma, kun hän tappeli äkeän karhun kanssa. Molemmat silloin, niin metsän kontio kuin mies, löyttiin kuolleina, toinen toisensa rinnalla maaten verisellä tanterella. Pahoin oli mies haavoitettu, mutta pedonkin sekä kurkku että kylki nähtiin puukon viiltämänä ja hänen rintansa kiväärin tuiman luodin lävistämänä. Niin lopetti päivänsä roteva mies, joka oli kaatanut enemmän kuin viisikymmentä karhua. ­ Mutta näiden metsäretkiensä kautta löi hän laimin työn ja toimen talossansa, joka vähitellen, ilman esimiehen johtoa, joutui rappiolle. Eivät kyenneet hänen poikansakaan kyntöön ja kylvöön; sillä olivatpa he perineet isältänsä saman voimallisen innon metsäotusten pyyntöön. He rakentelivat satimia, loukkuja, ansaita ja teerentarhoja surmaksi linnuille ja jäniksille. Niin viettivät he poikuutensa ajat, kunnes rupesivat käsittelemään tuliluikkua ja rohkenivat lähestyä otsoa korvessa.',
        ST_MakePoint(499687, 7248153) :: GEOMETRY, 6, 6, 6, 6, 6, 'toimenpidepyynto' :: ilmoitustyyppi,
        ARRAY ['kuoppiaTiessa', 'vettaTiella'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Pudasjärven alueurakka 2007-2012'),
        'Paavo', 'Poliisimies', '086727461', '0448261234', 'paavo.poliisimies@poliisi.fi',
        'viranomainen' ,
        'Mika', 'Vaihdemies', '085612567', 'vaihde@valituspalvelu.fi');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12347), 12347, '2007-12-07 08:47:50', 'vastaanotto' :: kuittaustyyppi,
        'Merituuli', 'Salmela', '04020671222', '081234512', 'merituuli.salmela@vainamoinen.fi',
        'Väinämöinen', 'Y72787', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12347), 12347, '2007-12-07 08:48:05', 'Anteeksi kauheasti olin kahvilla!',
        'vastaus' :: kuittaustyyppi, 'Merituuli', 'Salmela', '04020671222', '081234512',
        'merituuli.salmela@vainamoinen.fi',
        'Väinämöinen', 'Y72787', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12347), 12347, '2007-12-07 11:27:07', 'Aliurakoitsija käy katsomassa',
        'muutos' :: kuittaustyyppi,
        'Merituuli', 'Salmela', '04020671222', '081234512', 'merituuli.salmela@vainamoinen.fi',
        'Väinämöinen', 'Y72787',
        'Veljekset Ukkola Huoltoyritys', 'Y8172', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12347), 12347, '2007-12-07 15:07:30', 'Ukkolat aloitti työt',
        'aloitus' :: kuittaustyyppi,
        'Merituuli', 'Salmela', '04020671222', '081234512', 'merituuli.salmela@vainamoinen.fi',
        'Väinämöinen', 'Y72787',
        'Veljekset Ukkola Huoltoyritys', 'Y8172', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12347), 12347, '2007-12-17 09:17:30', 'Työt ei edisty, hoidetaan itse.',
        'muutos' :: kuittaustyyppi,
        'Merituuli', 'Salmela', '04020671222', '081234512', 'merituuli.salmela@vainamoinen.fi',
        'Väinämöinen', 'Y72787',
        'Väinämöinen', 'Y72787', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, vapaateksti, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12347), 12347, '2007-12-18 19:17:30', 'Normaalia kiperämpi kuoppa.',
        'vastaus' :: kuittaustyyppi,
        'Merituuli', 'Salmela', '04020671222', '081234512', 'merituuli.salmela@vainamoinen.fi',
        'Väinämöinen', 'Y72787',
        'Väinämöinen', 'Y72787', 'sisaan'::viestisuunta, 'sms'::viestikanava);


-- Neljäs ilmoitus: Turun alueella, tiedoitus. Ei kuittauksia!

INSERT INTO ilmoitus
(ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES (12348, '2005-02-13 00:00:00', '2005-02-13 00:01:00', FALSE, 'Täällä joku pommi räjähti!!',
        ST_MakePoint(249863, 6723867) :: GEOMETRY, 6, 6, 6, 6, 6, 'tiedoitus' :: ilmoitustyyppi,
        ARRAY ['virkaApupyynto'],
        'George', 'Doe', '05079163872', '05079163872', '', 'tienkayttaja' ,
        'Mika', 'Vaihdemies', '085612567', 'vaihde@valityspalvelu.fi');


-- Ilmoituksia Oulun alueurakka 2014-2019

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'), 12372, '2015-11-26 06:05:32', '2015-11-26 06:06:32', TRUE,
        'Voimakas lumipyry nelostiellä Ristisuon kohdalla ja tiet auraamatta.',
        ST_MakePoint(430716, 7200111) :: GEOMETRY, 6, 6, 6, 6, 6, 'toimenpidepyynto' :: ilmoitustyyppi,
        ARRAY ['auraustarve'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'),
        'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12372), 12372, '2015-11-26 06:06:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12372), 12372, '2015-11-26 06:10:07', 'aloitus' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'), 14372, '2015-12-26 06:05:32', '2015-12-26 06:06:32', TRUE,
        'Tiedoksi että este on tiellä..',
        ST_MakePoint(430716, 7200111) :: GEOMETRY, 6, 6, 6, 6, 6, 'tiedoitus' :: ilmoitustyyppi,
        ARRAY ['tiellaOnEste'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'),
        'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 14372), 14372, '2015-12-26 06:10:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'), 12373, '2015-10-26 06:05:32', '2015-10-26 06:06:32', TRUE,
        'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
        ST_MakePoint(430100, 7197493) :: GEOMETRY, NULL, NULL, NULL, NULL, NULL, 'toimenpidepyynto' :: ilmoitustyyppi,
        ARRAY ['auraustarve'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'),
        'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12373), 12373, '2015-10-26 06:10:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);


INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'), 12374, '2014-11-26 06:05:32', '2014-11-26 06:06:32', TRUE,
        'Pasku lumipeite nelostiellä Ristisuon kohdalla ja tiet auraamatta.',
        ST_MakePoint(430716, 7200111) :: GEOMETRY, NULL, NULL, NULL, NULL, NULL, 'tiedoitus' :: ilmoitustyyppi,
        ARRAY ['tiellaOnEste'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'),
        'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12374), 12374, '2014-11-26 06:10:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'), 12375, '2014-10-26 06:05:32', '2014-10-26 06:06:32', TRUE,
        'Täysin jäätävä keli, muttei ole suolattu yhtään.',
        ST_MakePoint(427898, 7188532) :: GEOMETRY, NULL, NULL, NULL, NULL, NULL, 'toimenpidepyynto' :: ilmoitustyyppi,
        ARRAY ['auraustarve'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Oulun alueurakka 2014-2019'),
        'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12375), 12375, '2014-10-26 06:10:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);


--Oulu au 2014-2019 syyskuu 2015 ja aiemmin *** ***
INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12600, '2015-09-26 06:05:32', '2015-09-26 06:06:32', true, 'Voimakas lumipyry nelostiellä Ristisuon kohdalla ja tiet auraamatta.',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, 6, 6, 6, 6, 6, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                              ARRAY['auraustarve'],
                                                                                                                              (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                              'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12600), 12600, '2015-09-26 08:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12601, '2015-9-26 06:05:32', '2015-9-26 06:06:32', true, 'Tiedoksi että este on tiellä..',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, 6, 6, 6, 6, 6, 'tiedoitus'::ilmoitustyyppi,
                                                                                                                              ARRAY['tiellaOnEste'],
                                                                                                                              (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                              'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12601), 12601, '2015-9-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12602, '2015-8-26 06:05:32', '2015-8-26 06:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(430100, 7197493)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12602), 12602, '2015-8-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);


INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12603, '2015-6-26 06:05:32', '2015-6-26 06:06:32', true, 'Pasku lumipeite nelostiellä Ristisuon kohdalla ja tiet auraamatta.',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, null, null, null, null, null, 'tiedoitus'::ilmoitustyyppi,
                                                                                                                                          ARRAY['tiellaOnEste'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12603), 12603, '2015-6-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12604, '2015-6-26 12:05:32', '2015-6-26 12:06:32', true, 'Onhan varmasti tarpeeksi kalustoa vesakonraivaukseen?',
                                                                         ST_MakePoint(427898, 7188532)::GEOMETRY, null, null, null, null, null, 'kysely'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12604), 12604, '2015-6-26 12:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12605, '2015-6-26 06:05:32', '2015-6-26 06:06:32', true, 'Onhan varmasti kalustoa poppeleiden hoitoon?',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, 6, 6, 6, 6, 6, 'kysely'::ilmoitustyyppi,
                                                                                                                              ARRAY['auraustarve'],
                                                                                                                              (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                              'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12605), 12605, '2015-6-26 06:10:07', 'aloitus'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12606, '2015-4-26 06:05:32', '2015-4-26 06:06:32', true, 'Tiedoksi että este on tiellä..',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, 6, 6, 6, 6, 6, 'tiedoitus'::ilmoitustyyppi,
                                                                                                                              ARRAY['tiellaOnEste'],
                                                                                                                              (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                              'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12606), 12606, '2015-4-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12607, '2015-3-26 06:05:32', '2015-3-26 06:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(430100, 7197493)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12607), 12607, '2015-3-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);


INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12608, '2015-2-26 06:05:32', '2015-2-26 06:06:32', true, 'Pasku lumipeite nelostiellä Ristisuon kohdalla ja tiet auraamatta.',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, null, null, null, null, null, 'tiedoitus'::ilmoitustyyppi,
                                                                                                                                          ARRAY['tiellaOnEste'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12608), 12608, '2015-2-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12609, '2015-2-26 06:05:32', '2015-2-26 06:06:32', true, 'Täysin jäätävä keli, muttei ole suolattu yhtään.',
                                                                         ST_MakePoint(427898, 7188532)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12609), 12609, '2015-2-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);
INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12610, '2015-1-26 06:05:32', '2015-1-26 06:06:32', true, 'Voimakas lumipyry nelostiellä Ristisuon kohdalla ja tiet auraamatta.',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, 6, 6, 6, 6, 6, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                              ARRAY['auraustarve'],
                                                                                                                              (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                              'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12610), 12610, '2015-1-26 06:10:07', 'aloitus'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12611, '2015-01-26 06:05:32', '2015-01-26 06:06:32', true, 'Tiedoksi että este on tiellä..',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, 6, 6, 6, 6, 6, 'tiedoitus'::ilmoitustyyppi,
                                                                                                                              ARRAY['tiellaOnEste'],
                                                                                                                              (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                              'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12611), 12611, '2015-01-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12612, '2014-12-26 06:05:32', '2014-12-26 06:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(430100, 7197493)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12612), 12612, '2014-12-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);


INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12613, '2014-11-26 06:05:32', '2014-11-26 06:06:32', true, 'Pasku lumipeite nelostiellä Ristisuon kohdalla ja tiet auraamatta.',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, null, null, null, null, null, 'tiedoitus'::ilmoitustyyppi,
                                                                                                                                          ARRAY['tiellaOnEste'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12613), 12613, (SELECT valitetty FROM ilmoitus WHERE ilmoitusid=12613), 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12613), 12613, '2014-11-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12614, '2014-10-26 06:05:32', '2014-10-26 06:06:32', true, 'Täysin jäätävä keli, muttei ole suolattu yhtään.',
                                                                         ST_MakePoint(427898, 7188532)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12614), 12614, (SELECT valitetty FROM ilmoitus WHERE ilmoitusid=12614), 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12614), 12614, '2014-10-26 06:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12615, '2015-1-26 16:05:32', '2015-1-26 16:06:32', true, 'Onhan varmasti kalustoa poppeleiden hoitoon?',
                                                                         ST_MakePoint(430716, 7200111)::GEOMETRY, 6, 6, 6, 6, 6, 'kysely'::ilmoitustyyppi,
                                                                                                                              ARRAY['auraustarve'],
                                                                                                                              (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                              'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12615), 12615, (SELECT valitetty FROM ilmoitus WHERE ilmoitusid=12615), 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12615), 12615, '2015-1-26 16:10:07', 'aloitus'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);


INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12616, '2015-3-22 16:05:32', '2015-3-22 16:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(430100, 7197493)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12616), 12616, (SELECT valitetty FROM ilmoitus WHERE ilmoitusid=12616), 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12616), 12616, '2015-3-22 16:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'), 12617, '2015-3-16 11:05:32', '2015-3-16 11:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(430100, 7197493)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Oulun alueurakka 2014-2019'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kasittelija_henkilo_etunimi, kasittelija_henkilo_sukunimi, kasittelija_henkilo_matkapuhelin, kasittelija_henkilo_tyopuhelin, kasittelija_henkilo_sahkoposti,
 kasittelija_organisaatio_nimi, kasittelija_organisaatio_ytunnus, suunta, kanava, vapaateksti)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12617), 12617, (SELECT valitetty FROM ilmoitus WHERE ilmoitusid=12617), 'valitys'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'ulos'::viestisuunta, 'sms'::viestikanava,
        'Tähän tulisi viestin raakadata, mutta tää nyt on kirjoitettu käsin');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12617), 12617, '2015-3-16 11:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);
-- ***** Oulun au 2014-2019

-- Kajaanin alueurakka 2014-2019
INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Kajaanin alueurakka 2014-2019'), 22372, '2015-11-26 06:05:32', '2015-11-26 06:06:32', TRUE,
                                                     'Voimakas lumipyry nelostiellä lähellä prikaatia kohdalla ja tiet auraamatta.',
                                                     ST_MakePoint(533601, 7119368) :: GEOMETRY, 6, 6, 6, 6, 6, 'toimenpidepyynto' :: ilmoitustyyppi,
                                                                                                            ARRAY ['auraustarve'],
                                                                                                            (SELECT tyyppi
                                                                                                             FROM urakka
                                                                                                             WHERE nimi = 'Kajaanin alueurakka 2014-2019'),
                                                                                                            'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 22372), 22372, '2015-11-26 06:06:07', 'vastaanotto' :: kuittaustyyppi,
                                    'Mikael', 'Pöytä', '04428671283', '0509288383', 'kajaanin-mikael.poyta@example.org',
                                    'Välittävä Urakoitsija', 'Y1242', 'sisaan'::viestisuunta, 'sms'::viestikanava);


-- Pudasjärvi
INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Pudasjärven alueurakka 2007-2012'), 12618, '2012-8-16 11:05:32', '2012-8-16 11:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(496694, 7243835)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Pudasjärven alueurakka 2007-2012'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12618), 12618, '2012-8-16 11:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Pudasjärven alueurakka 2007-2012'), 12619, '2012-7-16 11:05:32', '2012-7-16 11:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(496694, 7243835)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Pudasjärven alueurakka 2007-2012'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12619), 12619, '2012-7-16 11:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id FROM urakka WHERE nimi='Pudasjärven alueurakka 2007-2012'), 12620, '2012-3-16 11:05:32', '2012-3-16 11:06:32', true, 'Voimakas lumipyry nelostiellä tiet taas auraamatta.',
                                                                         ST_MakePoint(496694, 7243835)::GEOMETRY, null, null, null, null, null, 'toimenpidepyynto'::ilmoitustyyppi,
                                                                                                                                          ARRAY['auraustarve'],
                                                                                                                                          (SELECT tyyppi FROM urakka WHERE nimi='Pudasjärven alueurakka 2007-2012'),
                                                                                                                                          'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu',
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id FROM ilmoitus WHERE ilmoitusid=12620), 12620, '2012-3-16 11:10:07', 'vastaanotto'::kuittaustyyppi,
                                                          'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
                                                          'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);
-- Pudasjärvi

--Ilmoituksia etelämmäksi: Porin alueurakka 2007-2012

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Porin alueurakka 2007-2012'), 12376, '2010-10-26 06:05:32', '2010-10-26 06:06:32', TRUE,
        'Täysin jäätävä keli, muttei ole suolattu yhtään.',
        ST_MakePoint(227110, 6820660) :: GEOMETRY, NULL, NULL, NULL, NULL, NULL, 'toimenpidepyynto' :: ilmoitustyyppi,
        ARRAY ['auraustarve'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Porin alueurakka 2007-2012'),
        'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12376), 12376, '2010-10-26 06:10:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);

INSERT INTO ilmoitus
(urakka, ilmoitusid, ilmoitettu, valitetty, yhteydenottopyynto, paikankuvaus, sijainti,
 tr_numero, tr_alkuosa, tr_loppuosa, tr_alkuetaisyys, tr_loppuetaisyys, ilmoitustyyppi, selitteet, urakkatyyppi,
 ilmoittaja_etunimi, ilmoittaja_sukunimi, ilmoittaja_tyopuhelin, ilmoittaja_matkapuhelin, ilmoittaja_sahkoposti, ilmoittaja_tyyppi,
 lahettaja_etunimi, lahettaja_sukunimi, lahettaja_puhelinnumero, lahettaja_sahkoposti)
VALUES ((SELECT id
         FROM urakka
         WHERE nimi = 'Porin alueurakka 2007-2012'), 12377, '2011-10-26 06:05:32', '2011-10-26 06:06:32', TRUE,
        'Tiedotus: Kuorma-auton rengas keskellä tietä, en tohtinut poimia kyytiin.',
        ST_MakePoint(224238, 6834028) :: GEOMETRY, NULL, NULL, NULL, NULL, NULL, 'tiedoitus' :: ilmoitustyyppi,
        ARRAY ['tiellaOnEste'],
        (SELECT tyyppi
         FROM urakka
         WHERE nimi = 'Porin alueurakka 2007-2012'),
        'Pekka', 'Porinmatti', '0501234567', '0502234567', 'tyonvalvonta@example.org', 'muu' ,
        'Mari', 'Marttala', '085674567', 'mmarttala@example.org');

INSERT INTO ilmoitustoimenpide
(ilmoitus, ilmoitusid, kuitattu, kuittaustyyppi,
 kuittaaja_henkilo_etunimi, kuittaaja_henkilo_sukunimi, kuittaaja_henkilo_matkapuhelin, kuittaaja_henkilo_tyopuhelin, kuittaaja_henkilo_sahkoposti,
 kuittaaja_organisaatio_nimi, kuittaaja_organisaatio_ytunnus, suunta, kanava)
VALUES ((SELECT id
         FROM ilmoitus
         WHERE ilmoitusid = 12377), 12377, '2011-10-26 06:10:07', 'vastaanotto' :: kuittaustyyppi,
        'Mikael', 'Pöytä', '04428671283', '0509288383', 'oulun-mikael.poyta@example.org',
        'Välittävä Urakoitsija', 'Y1242334', 'sisaan'::viestisuunta, 'sms'::viestikanava);
