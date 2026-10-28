-- V12: commodity source aliases for legacy DMI spellings (owner module: commodity; ADR-0051, RD-050).
--
-- Until 2026-05-06 the data.gov.in historical resource reported 20 commodity spellings that are not names of the
-- Agmarknet 2.0 commodity master (for example 'Arhar(Tur/Red Gram)(Whole)'). Each carries, in the same resource, the
-- DMI Commodity_Code of exactly one master name (code 49 = 'Red gram/Arhar/Tur(whole)'), so the mapping is one-to-one
-- and evidenced; 13 differ from the master name only in spacing, 7 are renames. Ingestion resolves a source name by
-- exact match with commodities.canonical_name first and through this table second (ADR-0051 item 2); nothing is
-- merged, renamed or fuzzy-matched. Evidence: docs/decision-review/TASK_12_OWNER_DECISION_PACKET.md D1.
--
-- price_observations.source_commodity_name keeps the source spelling of a record resolved through an alias, so
-- every stored row shows which name the source used (NULL when the source used the canonical name). Adding a
-- nullable column without a default changes no existing row.

CREATE TABLE commodity_source_aliases (
    id                    uuid         NOT NULL DEFAULT gen_random_uuid(),
    commodity_id          uuid         NOT NULL,
    source_commodity_name varchar(200) NOT NULL,
    evidence_note         varchar(500) NOT NULL,
    created_at            timestamptz  NOT NULL DEFAULT now(),
    CONSTRAINT pk_commodity_source_aliases PRIMARY KEY (id),
    CONSTRAINT fk_commodity_source_aliases_commodity FOREIGN KEY (commodity_id) REFERENCES commodities (id) ON DELETE RESTRICT,
    CONSTRAINT uq_commodity_source_aliases_source UNIQUE (source_commodity_name),
    CONSTRAINT ck_commodity_source_aliases_source_normalized CHECK (source_commodity_name = btrim(regexp_replace(source_commodity_name, '\s+', ' ', 'g')) AND source_commodity_name <> ''),
    CONSTRAINT ck_commodity_source_aliases_evidence CHECK (evidence_note <> '')
);

CREATE INDEX ix_commodity_source_aliases_commodity_id ON commodity_source_aliases (commodity_id);

ALTER TABLE price_observations ADD COLUMN source_commodity_name varchar(200);
ALTER TABLE price_observations ADD CONSTRAINT ck_price_observations_source_commodity_name
    CHECK (source_commodity_name IS NULL OR (source_commodity_name = btrim(regexp_replace(source_commodity_name, '\s+', ' ', 'g')) AND source_commodity_name <> ''));

-- An alias never shadows a canonical name: the 20 spellings below are not master names (checked when seeding).
INSERT INTO commodity_source_aliases (commodity_id, source_commodity_name, evidence_note)
SELECT c.id, v.source_commodity_name, v.evidence_note
FROM commodities c JOIN (VALUES
    ('Bengal Gram Dal(Chana Dal)', 'Bengal Gram Dal (Chana Dal)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 263, the code every record of the Agmarknet 2.0 master name ''Bengal Gram Dal(Chana Dal)'' carries in that resource; seen 2025-10-27 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Black Gram Dal(Urd Dal)', 'Black Gram Dal (Urd Dal)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 264, the code every record of the Agmarknet 2.0 master name ''Black Gram Dal(Urd Dal)'' carries in that resource; seen 2025-10-27 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Black Gram(Urd Beans)(Whole)', 'Black Gram (Urd Beans)(Whole)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 8, the code every record of the Agmarknet 2.0 master name ''Black Gram(Urd Beans)(Whole)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Cardamom', 'Cardamoms', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 40, the code every record of the Agmarknet 2.0 master name ''Cardamom'' carries in that resource; seen 2025-10-27 to 2025-12-29 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Cowpea(Lobia/Karamani)', 'Cowpea (Lobia/Karamani)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 92, the code every record of the Agmarknet 2.0 master name ''Cowpea(Lobia/Karamani)'' carries in that resource; seen 2025-10-27 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Elephant Yam(Suran)/Amorphophallus', 'Elephant Yam (Suran)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 296, the code every record of the Agmarknet 2.0 master name ''Elephant Yam(Suran)/Amorphophallus'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('French Beans(Frasbean)', 'French Beans (Frasbean)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 298, the code every record of the Agmarknet 2.0 master name ''French Beans(Frasbean)'' carries in that resource; seen 2025-10-27 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Green Gram Dal(Moong Dal)', 'Green Gram Dal (Moong Dal)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 265, the code every record of the Agmarknet 2.0 master name ''Green Gram Dal(Moong Dal)'' carries in that resource; seen 2025-10-27 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Green Gram(Moong)(Whole)', 'Green Gram (Moong)(Whole)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 9, the code every record of the Agmarknet 2.0 master name ''Green Gram(Moong)(Whole)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Jack Fruit(Ripe)', 'Jack Fruit', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 182, the code every record of the Agmarknet 2.0 master name ''Jack Fruit(Ripe)'' carries in that resource; seen 2025-10-27 to 2026-02-23 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Lentil(Masur)(Whole)', 'Lentil (Masur)(Whole)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 63, the code every record of the Agmarknet 2.0 master name ''Lentil(Masur)(Whole)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Little gourd(Kundru)', 'Little gourd (Kundru)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 302, the code every record of the Agmarknet 2.0 master name ''Little gourd(Kundru)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Mango(Raw-Ripe)', 'Mango (Raw-Ripe)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 172, the code every record of the Agmarknet 2.0 master name ''Mango(Raw-Ripe)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Paddy(Common)', 'Paddy(Dhan)(Common)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 2, the code every record of the Agmarknet 2.0 master name ''Paddy(Common)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Pointed gourd(Parval)', 'Pointed gourd (Parval)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 303, the code every record of the Agmarknet 2.0 master name ''Pointed gourd(Parval)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Ragi(Finger Millet)', 'Ragi (Finger Millet)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 30, the code every record of the Agmarknet 2.0 master name ''Ragi(Finger Millet)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Red gram split/Arhar dal/Tur dal', 'Arhar Dal(Tur Dal)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 260, the code every record of the Agmarknet 2.0 master name ''Red gram split/Arhar dal/Tur dal'' carries in that resource; seen 2025-10-27 to 2026-05-04 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Red gram/Arhar/Tur(whole)', 'Arhar (Tur/Red Gram)(Whole)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 49, the code every record of the Agmarknet 2.0 master name ''Red gram/Arhar/Tur(whole)'' carries in that resource; seen 2025-10-20 to 2025-11-03 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Red gram/Arhar/Tur(whole)', 'Arhar(Tur/Red Gram)(Whole)', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 49, the code every record of the Agmarknet 2.0 master name ''Red gram/Arhar/Tur(whole)'' carries in that resource; seen 2025-11-03 to 2026-05-04 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.'),
    ('Sunflower/Sunflower Seed', 'Sunflower', 'data.gov.in historical resource (DMI) spelling with Commodity_Code 14, the code every record of the Agmarknet 2.0 master name ''Sunflower/Sunflower Seed'' carries in that resource; seen 2025-11-03 to 2026-04-13 in the Task 12 harvest (156 Maharashtra report dates, retrieved 2026-10-14/15). ADR-0051.')
) AS v (canonical_name, source_commodity_name, evidence_note) ON c.canonical_name = v.canonical_name
WHERE NOT EXISTS (SELECT 1 FROM commodities x WHERE x.canonical_name = v.source_commodity_name)
ON CONFLICT (source_commodity_name) DO NOTHING;
