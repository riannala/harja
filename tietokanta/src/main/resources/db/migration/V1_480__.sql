-- Irrota päällystyslomakkeen määrämuutokset omaan tauluun

CREATE TYPE maaramuutos_tyon_tyyppi AS ENUM ('ajoradan_paallyste', 'pienaluetyot', 'tasaukset', 'jyrsinnat',
                                              'muut');

CREATE TABLE yllapito_maaramuutokset {
  id serial PRIMARY KEY,
  yllapitokohde integer REFERENCES yllapitokohde (id) NOT NULL,
  tyon_tyyppi maaramuutos_tyon_tyyppi NOT NULL,
  tyo VARCHAR(256) NOT NULL,
  yksikko VARCHAR(32) NOT NULL,
  tilattu_maara NUMERIC NOT NULL,
  toteutunut_maara NUMERIC NOT NULL,
  yksikkohinta NUMERIC NOT NULL
}

ALTER TABLE Paallystysilmoitus DROP COLUMN muutoshinta; -- Lasketaan jatkossa yllä olevasta taulusta
ALTER TABLE paatos_taloudellinen_osa DROP COLUMN muutoshinta; -- Hinnanmuutosten hyväksyminen jää pois (HAR-4090)
ALTER TABLE perustelu_taloudellinen_osa DROP COLUMN muutoshinta; -- Hinnanmuutosten hyväksyminen jää pois (HAR-4090)
ALTER TABLE kasittelyaika_taloudellinen_osa DROP COLUMN muutoshinta; -- Hinnanmuutosten hyväksyminen jää pois (HAR-4090)

-- FIXME TÄSSÄ VAIHEESSA NYKYISTEN POTTIEN ilmoitustiedot-SARAKKEESEEN JÄÄ VANHANMALLINEN JSON, JOSSA
-- TALOUSOSA MUKANA. MITEN MIGRATOIDAAN?