-- Nimi: Varustetoteumalle oma aikaleima
-- Kuvaus: Koska jos toteumaan linkitetään useampi varustetoteuma, ei ole omia aikaleimoja.

ALTER TABLE varustetoteuma ADD COLUMN luoja integer REFERENCES kayttaja (id);
ALTER TABLE varustetoteuma ADD COLUMN luotu date;
ALTER TABLE varustetoteuma ADD COLUMN toteutunut_pvm date;