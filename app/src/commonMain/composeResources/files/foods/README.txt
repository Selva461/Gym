INDIAN FOOD NUTRITION DATASET — PRODUCTION CANDIDATE v2

Scope: Categories 1–12. 724 rows total. Categories 1–8 retain the previously production-gated data. Categories 9–12 were rebuilt from source-backed INDB records and explicit arithmetic serving-size variants.

QA: exact 35-column schema; 724 rows; 60 rows each for Categories 1–7 and 9–12; 64 rows for Category 8; no exact duplicate rows and no duplicate identities within the same category; no negative numeric nutrient values. A food can appear in multiple category contexts only when the source/category assignment differs (e.g. a regional dish also represented in the rice category).

Source for Categories 9–12: Anuvaad Indian Nutrient Databank (INDB) 2024.11. The Anuvaad documentation states that INDB contains 1,014 recipes and reports values per 100 g and per serving, with ingredient nutrient references including IFCT 2017, Nutritive Value of Indian Foods 2004, CoFID and USDA FoodData Central.
The normalized CSV used for extraction is a public mirror of the INDB dataset. Source URL stored in each rebuilt row.

Important data semantics: the normalized INDB CSV used here exposes energy, carbohydrate, protein, fat, free sugars and fibre plus serving-unit metadata. Free sugar is NOT written into the schema sugar_g field because total sugars are not available in this extract; added_sugar_g is therefore blank. Unknown micronutrients remain blank.

Serving variants for Categories 9–12: small = 0.75x, regular = 1.0x, large = 1.5x of the INDB source serving. Base serving weight is calculated from source per-100g energy and source per-serving energy; all macros are then scaled arithmetically. No ingredient quantities were invented. These rows remain confidence=medium and method=recipe-estimated because INDB values are recipe-composition data rather than laboratory measurements of every restaurant serving.

Categories 1–8 are retained as previously gated production data but are NOT certified as fully authoritative/laboratory-grade. Some rows use third-party transcriptions or INDB-derived recipe data and should be source-revalidated before regulatory or clinical use.

This file is suitable as a production application seed only with the above provenance/limitations understood; it is not a clinical reference or laboratory database.
QA issues found: none under the dataset identity contract (category + name + serving weight + variant); exact full-row duplicate count = 0.
