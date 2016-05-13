-- Päällystyskohteet

INSERT INTO yllapitokohde
(urakka, sopimus, kohdenumero, nimi, sopimuksen_mukaiset_tyot, arvonvahennykset, bitumi_indeksi, kaasuindeksi,
 aikataulu_paallystys_alku, aikataulu_paallystys_loppu, aikataulu_tiemerkinta_alku, aikataulu_tiemerkinta_loppu,
 aikataulu_kohde_valmis, aikataulu_muokkaaja, aikataulu_muokattu, valmis_tiemerkintaan)
VALUES
  ((SELECT id FROM urakka WHERE  nimi = 'Muhoksen päällystysurakka'),
   (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Muhoksen päällystysurakka') AND paasopimus IS null),
   'L03', 'Leppäjärven ramppi', 400, 100, 4543.95, 0,
   '2016-05-19 06:00:00+02', '2016-05-21 16:00:00+02', null, null,
   null, (SELECT id FROM kayttaja where kayttajanimi = 'jvh'), NOW(), '2016-05-21 16:00:00+02'),
  ((SELECT id FROM urakka WHERE  nimi = 'Muhoksen päällystysurakka'),
   (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Muhoksen päällystysurakka') AND paasopimus IS null),
   308, 'Mt 2855 Viisari - Renko', 9000, 200, 565, 100,
   '2016-05-21 06:00:00+02', null, null, null,
   null, (SELECT id FROM kayttaja where kayttajanimi = 'jvh'), NOW(), null),
  ((SELECT id FROM urakka WHERE  nimi = 'Muhoksen päällystysurakka'),
   (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Muhoksen päällystysurakka') AND paasopimus IS null),
   'L010', 'Tie 357', 500, 3457, 5, 6,
   '2016-05-26 06:00:00+02', null, null, null,
   null, (SELECT id FROM kayttaja where kayttajanimi = 'jvh'), NOW(), null),
  ((SELECT id FROM urakka WHERE  nimi = 'Muhoksen päällystysurakka'),
   (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Muhoksen päällystysurakka') AND paasopimus IS null),
   310, 'Oulaisten ohitusramppi', 500, 3457, 5, 6,
   '2016-06-02 06:00:00+02', null, null, null,
   null, (SELECT id FROM kayttaja where kayttajanimi = 'jvh'), NOW(), null);

INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Oulaisten ohitusramppi'), 'Laivaniemi 1', 19521, 10, 5, 10, 15, 'PAB-B 16/80 MPKJ', ST_GeomFromText('MULTILINESTRING((426888 7212758,427081 7212739),(434777 7215499,436899 7217174,438212 7219910,438676 7220554,440102 7221432,441584 7222729,442255 7223162,443128 7223398,443750 7223713,448682 7225293,451886 7226708,456379 7228018,459945 7229222,461039 7229509))'));
INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Oulaisten ohitusramppi'), 'Laivaniemi 2', 849, 2, 3334, 4, 3042, 'PAB-B 16/80 MPKJ', ST_GeomFromText('LINESTRING(443798.31784756 7229301.60995499,443815.8652 7229436.1158,443833.9475 7229570.7144,443848.4732 7229677.7047,443853.788 7229716.0813,443866.5645 7229808.3416,443885.5991 7229951.5002,443927.1304 7230263.7455,443941.7585 7230371.0705,443948.0643 7230406.7379,443956.0579 7230442.0361,443966.7857 7230475.0032,443982.0731 7230510.7802,443990.5562 7230529.2878,444006.5416 7230557.6144,444025.6995 7230588.9264,444037.7745 7230606.8807,444045.5012 7230618.2083,444082.9136 7230673.0389,444104.773 7230704.9174,444156.4008 7230780.2234,444288.7505 7230974.0735,444443.9311 7231201.8097,444487.1597 7231264.983,444525.1617 7231320.7237,444527.8345 7231324.7098,444557.055 7231368.3946,444562.0464 7231375.9254,444565.6966 7231381.4254,444578.7703 7231402.3343,444591.0411 7231426.0613,444604.4496 7231458.1202,444611.9679 7231480.3107,444614.9772 7231490.5698,444615.1535 7231491.2017,444617.2755 7231500.1506,444623.237 7231525.566,444630.3182 7231568.8256,444637.5525 7231635.3477,444644.8779 7231715.0995,444646.5299 7231731.6626,444653.573 7231802.244,444658.6585 7231855.4637,444660.2111 7231872.1602,444665.4306 7231927.1154,444670.9091 7231972.8716,444671.4255 7231987.8677,444677.5544 7232053.2338,444682.5219 7232107.3344,444685.0495 7232146.7734,444684.8243 7232179.5554,444680.8365 7232215.8463,444676.287 7232245.196,444676.231 7232245.5522,444669.771 7232281.8151,444665.2114 7232304.3255,444660.2701 7232328.6558,444656.9284 7232345.4059,444651.6636 7232371.7046,444648.7234 7232386.4292,444637.8413 7232436.5662,444627.5376 7232489.4769,444624.7897 7232503.5624,444610.613 7232574.5607,444599.4749 7232629.3557,444587.7304 7232687.3577,444574.2726 7232755.9881,444547.5296 7232888.5546,444545.0825 7232900.6725,444540.8564 7232921.6445,444531.6211 7232966.9111,444518.4878 7233032.2278,444509.1352 7233083.3451,444502.5888 7233126.0199,444499.2638 7233165.6626,444496.7416 7233214.1852,444494.9335 7233260.2653,444492.6876 7233311.3803,444490.9599 7233370.0541,444488.5848 7233443.4847,444486.2425 7233506.5431,444484.3808 7233550.1201,444482.2963 7233598.9708,444480.7628 7233647.5123,444479.1405 7233688.8048,444477.199 7233754.3972,444475.666 7233808.3584,444473.7417 7233863.1861,444471.724 7233918.8571,444469.7944 7233977.5345,444463.6315 7234169.9636,444460.7609 7234254.4478,444458.2667 7234342.214,444456.7141 7234385.5237,444455.3628 7234423.2422,444451.818 7234532.2383,444448.2971 7234648.2984,444448.2661 7234649.1393,444445.252 7234728.8429,444442.7519 7234819.5429,444439.3756 7234927.0036,444436.4753 7235007.2837,444433.6088 7235103.4855,444431.4809 7235180.219,444431.4136 7235182.4816,444429.3857 7235248.7112,444423.7827 7235413.3981,444420.774 7235509.8636,444418.7068 7235595.9963,444415.137 7235693.033,444413.7345 7235727.5194,444409.4834 7235774.2815,444403.1895 7235819.9042,444390.5095 7235887.082,444369.6875 7236004.422,444352.4336 7236101.5117,444338.7471 7236180.4077,444323.9975 7236258.5384,444307.2945 7236354.7592,444304.1017 7236373.1703,444286.2766 7236472.1033,444274.587 7236536.6845,444268.8869 7236566.2248,444262.0457 7236606.7007,444261.9736 7236607.0575,444248.9327 7236676.7098,444248.8416 7236677.2911,444244.6411 7236706.1864,444243.911 7236710.2368,444241.6395 7236720.9878,444209.8915 7236894.7855,444197.9386 7236956.8308,444197.0453 7236970.8818,444186.433 7237023.0342,444185.3223 7237028.4937,444160.3356 7237167.7228,444135.4794 7237301.7169,444132.1562 7237320.15,444130.8513 7237327.3509,444129.6322 7237334.1016,444117.9337 7237399.0014,444096.7407 7237514.9495,444066.6364 7237680.6821,444038.2837 7237838.2716,444005.0056 7238029.0326,443995.0783 7238080.5561,443972.0969 7238207.6775,443960.5847 7238269.2481,443951.0171 7238322.7084,443949.9206 7238333.363,443945.8012 7238368.2752,443944.5856 7238396.9466,443944.2229 7238408.3342,443943.3028 7238437.7286,443941.5411 7238487.883,443941.5167 7238488.6679,443936.0358 7238674.0421,443927.9803 7238911.6044,443924.9185 7239003.2138,443921.9961 7239056.8843,443921.319 7239078.4441,443916.5378 7239230.5414,443912.3606 7239353.8852,443906.2888 7239521.1067,443900.4762 7239717.887,443895.5599 7239848.7574,443893.6749 7239908.3138,443891.0926 7239990.3836,443887.6151 7240099.3809,443887.5859 7240100.2969,443886.3174 7240145.3771,443884.3765 7240198.6932,443881.5779 7240290.3163,443879.457895224 7240351.79959421)'));

INSERT INTO yllapitokohde (urakka, sopimus, kohdenumero, nimi) VALUES ((SELECT id FROM urakka WHERE  nimi = 'Tienpäällystysurakka KAS ELY 1 2015'), (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Tienpäällystysurakka KAS ELY 1 2015') AND paasopimus IS null), '1501', 'Vt13 Hartikkala - Pelkola');
INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Vt13 Hartikkala - Pelkola'), 'Vt13 Hartikkala - Pelkola', 13, 239, 0, 239, 222, 'PAB-B 16/80 MPKJ', 'MULTILINESTRING((569679.576280243 6770940.38019019,569685.927911525 6770936.54598464,569694.917866912 6770931.11985125,569702.825096966 6770926.21423598,569717.059540404 6770917.1194623,569768.068529403 6770886.26298144,569772.905655448 6770883.31437137,569800.582449535 6770866.39038579,569825.147450518 6770851.51214363,569826.659573521 6770850.53304547,569830.600979599 6770848.01502418,569849.985812847 6770836.16460137,569852.934422925 6770834.4297395,569853.208975388 6770834.26715203,569869.210775049 6770824.97723093),(570700.430827977 6770091.60690774,570705.758098659 6770086.85732879,570844.195073798 6769973.54994409,570872.894391503 6769951.86928975))');
INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Vt13 Hartikkala - Pelkola'), 'Vt13 Hartikkala - Pelkola', 13, 239, 222, 239, 820, 'PAB-B 16/80 MPKJ', 'MULTILINESTRING((569869.210775049 6770824.97723093,569872.124508548 6770823.28564906,569918.85619648 6770796.53077951,569962.043715846 6770770.06058692,569985.755877074 6770755.92441063,570007.509246432 6770743.31881966,570092.85754233 6770692.64036563,570152.581934252 6770655.3316041,570259.021934027 6770569.17811313,570337.970358406 6770487.07441987,570348.766508366 6770474.12501744),(570872.894391503 6769951.86928975,570913.557984301 6769921.15032602,571038.051744102 6769821.61642248,571042.671491299 6769817.89894639,571157.696466689 6769725.89230659,571340.874536167 6769579.60903354))');
INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Vt13 Hartikkala - Pelkola'), 'Vt13 Hartikkala - Pelkola', 13, 239, 820, 239, 870, 'PAB-B 16/80 MPKJ', 'MULTILINESTRING((570348.766508366 6770474.12501744,570380.784445928 6770435.72121979),(571340.874536167 6769579.60903354,571371.85691699 6769554.86696651,571373.690046018 6769553.31672776,571379.770800303 6769548.19579434))');
INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Vt13 Hartikkala - Pelkola'), 'Vt13 Hartikkala - Pelkola', 13, 239, 870, 239, 1275, 'PAB-B 16/80 MPKJ', 'MULTILINESTRING((570380.784445928 6770435.72121979,570380.996481422 6770435.46689459,570381.23768261 6770435.1726887,570498.097573703 6770293.76209003,570611.87068515 6770173.67844084,570652.178907897 6770135.64614894),(571379.770800303 6769548.19579434,571411.39783173 6769521.56095359,571569.835447595 6769389.46608079,571570.855639286 6769388.70257481,571697.443978293 6769304.28692351,571698.415334188 6769303.70923177,571700.883529032 6769302.3581588))');
INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Vt13 Hartikkala - Pelkola'), 'Vt13 Hartikkala - Pelkola', 13, 239, 870, 239, 1275, 'PAB-B 16/80 MPKJ', 'MULTILINESTRING((570380.784445928 6770435.72121979,570380.996481422 6770435.46689459,570381.23768261 6770435.1726887,570498.097573703 6770293.76209003,570611.87068515 6770173.67844084,570652.178907897 6770135.64614894),(571379.770800303 6769548.19579434,571411.39783173 6769521.56095359,571569.835447595 6769389.46608079,571570.855639286 6769388.70257481,571697.443978293 6769304.28692351,571698.415334188 6769303.70923177,571700.883529032 6769302.3581588))');

INSERT INTO yllapitokohde (urakka, sopimus, kohdenumero, nimi) VALUES ((SELECT id FROM urakka WHERE  nimi = 'Tienpäällystysurakka KAS ELY 1 2015'), (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Tienpäällystysurakka KAS ELY 1 2015') AND paasopimus IS null), '1502', 'Vt 13 Kähärilä - Liikka');
INSERT INTO yllapitokohdeosa (yllapitokohde, nimi, tr_numero, tr_alkuosa, tr_alkuetaisyys, tr_loppuosa, tr_loppuetaisyys, toimenpide, sijainti) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Vt 13 Kähärilä - Liikka'), 'Vt 13 Kähärilä - Liikka', 13, 241, 0, 241, 30, 'PAB-B 16/80 MPKJ', 'MULTILINESTRING((578249.322868685 6763497.87157121,578262.945673555 6763491.3180456,578275.84176723 6763483.88913412),(581383.200687944 6760054.36044461,581382.427437082 6760078.6053179,581386.126450856 6760082.99815731,581391.183933542 6760078.6196113,581393.197140761 6760055.39623812),(581397.589195136 6759849.00348945,581396.515373133 6759862.00903338,581395.753058267 6759871.11750491,581399.465769886 6759877.98846319,581404.941334631 6759871.56834268,581407.346106159 6759850.14085703))');

INSERT INTO yllapitokohde (urakka, sopimus, kohdenumero, nimi) VALUES ((SELECT id FROM urakka WHERE  nimi = 'Tienpäällystysurakka KAS ELY 1 2015'), (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Tienpäällystysurakka KAS ELY 1 2015') AND paasopimus IS null), '1503', 'Mt 387 Mattila - Hanhi-Kemppi');

INSERT INTO yllapitokohde (urakka, sopimus, kohdenumero, nimi) VALUES ((SELECT id FROM urakka WHERE  nimi = 'Tienpäällystysurakka KAS ELY 1 2015'), (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Tienpäällystysurakka KAS ELY 1 2015') AND paasopimus IS null), '1504', 'Mt 408 Pallo - Kivisalmi');

INSERT INTO yllapitokohde (urakka, sopimus, kohdenumero, nimi) VALUES ((SELECT id FROM urakka WHERE  nimi = 'Tienpäällystysurakka KAS ELY 1 2015'), (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Tienpäällystysurakka KAS ELY 1 2015') AND paasopimus IS null), '1505', 'Kt 62 Sotkulampi - Rajapatsas');

INSERT INTO yllapitokohde (urakka, sopimus, kohdenumero, nimi) VALUES ((SELECT id FROM urakka WHERE  nimi = 'Tienpäällystysurakka KAS ELY 1 2015'), (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Tienpäällystysurakka KAS ELY 1 2015') AND paasopimus IS null), '1506', 'Kt 62 Haloniemi - Syyspohja');

INSERT INTO yllapitokohde (urakka, sopimus, kohdenumero, nimi) VALUES ((SELECT id FROM urakka WHERE  nimi = 'Tienpäällystysurakka KAS ELY 1 2015'), (SELECT id FROM sopimus WHERE urakka = (SELECT id FROM urakka WHERE nimi='Tienpäällystysurakka KAS ELY 1 2015') AND paasopimus IS null), '1507', 'Mt 387 Raippo - Koskenkylä');

-- Päällystysilmoitukset

INSERT INTO paallystysilmoitus (paallystyskohde, tila, aloituspvm, takuupvm, muutoshinta, ilmoitustiedot) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Leppäjärven ramppi'), 'aloitettu'::paallystystila, '2005-11-14 00:00:00+02', '2005-12-20 00:00:00+02', 2000, '{"osoitteet":[{"tie":2846,"aosa":5,"aet":22,"losa":5,"let":9377,"ajorata":0,"suunta":0,"kaista":1,"paallystetyyppi":21,"raekoko":16,"massa":100,"rc%":0,"tyomenetelma":12,"leveys":6.5,"massamaara":1781,"edellinen-paallystetyyppi":12,"pinta-ala":15},{"tie":2846,"aosa":5,"aet":22,"losa":5,"let":9377,"ajorata":1,"suunta":0,"kaista":1,"paallystetyyppi":21,"raekoko":10,"massa":512,"rc%":0,"tyomenetelma":12,"leveys":4,"massamaara":1345,"edellinen-paallystetyyppi":11,"pinta-ala":9}],"kiviaines":[{"esiintyma":"KAMLeppäsenoja","km-arvo":"An14","muotoarvo":"Fi20","sideainetyyppi":"B650/900","pitoisuus":4.3,"lisaaineet":"Tartuke"}],"alustatoimet":[{"aosa":22,"aet":3,"losa":5,"let":4785,"kasittelymenetelma":13,"paksuus":30,"verkkotyyppi":1, "verkon-tarkoitus": 1, "verkon-sijainti": 1, "tekninen-toimenpide":2}],"tyot":[{"tyyppi":"ajoradan-paallyste","tyo":"AB 16/100 LTA","tilattu-maara":10000,"toteutunut-maara":10100,"yksikkohinta":20, "yksikko": "km"}]}');
INSERT INTO paallystysilmoitus (paallystyskohde, tila, aloituspvm, valmispvm_kohde, valmispvm_paallystys, takuupvm, muutoshinta, ilmoitustiedot) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Tie 357'), 'valmis'::paallystystila, '2005-11-14 00:00:00+02', '2005-12-19 00:00:00+02', '2005-12-19 00:00:00+02', '2005-12-20 00:00:00+02', 2000, '{"osoitteet":[{"tie":2846,"aosa":5,"aet":22,"losa":5,"let":9377,"ajorata":0,"suunta":0,"kaista":1,"paallystetyyppi":21,"raekoko":16,"massa":100,"rc%":0,"tyomenetelma":12,"leveys":6.5,"massamaara":1781,"edellinen-paallystetyyppi":12,"pinta-ala":15},{"tie":2846,"aosa":5,"aet":22,"losa":5,"let":9377,"ajorata":1,"suunta":0,"kaista":1,"paallystetyyppi":21,"raekoko":10,"massa":512,"rc%":0,"tyomenetelma":12,"leveys":4,"massamaara":1345,"edellinen-paallystetyyppi":11,"pinta-ala":9}],"kiviaines":[{"esiintyma":"KAMLeppäsenoja","km-arvo":"An14","muotoarvo":"Fi20","sideainetyyppi":"B650/900","pitoisuus":4.3,"lisaaineet":"Tartuke"}],"alustatoimet":[{"aosa":22,"aet":3,"losa":5,"let":4785,"kasittelymenetelma":13,"paksuus":30,"verkkotyyppi":1,"verkon-tarkoitus": 1, "verkon-sijainti": 1,"tekninen-toimenpide":2}],"tyot":[{"tyyppi":"ajoradan-paallyste","tyo":"AB 16/100 LTA","tilattu-maara":10000,"toteutunut-maara":10100,"yksikkohinta":20, "yksikko": "km"}]}');
INSERT INTO paallystysilmoitus (paallystyskohde, tila, aloituspvm, valmispvm_kohde, valmispvm_paallystys, takuupvm, muutoshinta, ilmoitustiedot, paatos_tekninen_osa, paatos_taloudellinen_osa, perustelu_tekninen_osa, perustelu_taloudellinen_osa, kasittelyaika_tekninen_osa, kasittelyaika_taloudellinen_osa) VALUES ((SELECT id FROM yllapitokohde WHERE nimi ='Oulaisten ohitusramppi'), 'valmis'::paallystystila, '2005-11-14 00:00:00+02', '2005-12-19 00:00:00+02', '2005-12-19 00:00:00+02', '2005-12-20 00:00:00+02', 2000, '{"osoitteet":[{"tie":2846,"aosa":5,"aet":22,"losa":5,"let":9377,"ajorata":0,"suunta":0,"kaista":1,"paallystetyyppi":21,"raekoko":16,"massa":100,"rc%":0,"tyomenetelma":12,"leveys":6.5,"massamaara":1781,"edellinen-paallystetyyppi":12,"pinta-ala":15},{"tie":2846,"aosa":5,"aet":22,"losa":5,"let":9377,"ajorata":1,"suunta":0,"kaista":1,"paallystetyyppi":21,"raekoko":10,"massa":512,"rc%":0,"tyomenetelma":12,"leveys":4,"massamaara":1345,"edellinen-paallystetyyppi":11,"pinta-ala":9}],"kiviaines":[{"esiintyma":"KAMLeppäsenoja","km-arvo":"An14","muotoarvo":"Fi20","sideainetyyppi":"B650/900","pitoisuus":4.3,"lisaaineet":"Tartuke"}],"alustatoimet":[{"aosa":22,"aet":3,"losa":5,"let":4785,"kasittelymenetelma":13,"paksuus":30,"verkkotyyppi":1,"verkon-tarkoitus": 1, "verkon-sijainti": 1,"tekninen-toimenpide":2}],"tyot":[{"tyyppi":"ajoradan-paallyste","tyo":"AB 16/100 LTA","tilattu-maara":10000,"toteutunut-maara":10100,"yksikkohinta":20, "yksikko": "km"}]}', 'hylatty'::paallystysilmoituksen_paatostyyppi, 'hylatty'::paallystysilmoituksen_paatostyyppi, 'Ei tässä ole mitään järkeä', 'Ei tässä ole mitään järkeä', '2005-12-20 00:00:00+02', '2005-12-20 00:00:00+02');
