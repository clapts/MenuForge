# MenuForge JSON Format

This is the stable JSON document contract for MenuForge `v0.1.0`.

Important distinction:

- Package/release version: `0.1.0`.
- JSON document schema version: `"2.0"`.

The schema version remains `"2.0"` because this project replaced the older
SQLite-style model with a document model during development. Future package
releases may still use the same JSON schema when the document contract does not
change.

## Contract Rules

- `schemaVersion` must be `"2.0"`.
- `categories` must exist and must be an array.
- Category `slug` values must be lowercase slugs: `pizze`, `menu-pranzo`.
- Item `id` values must be globally unique across the whole menu.
- Badge `id` values must be unique.
- Tag values must be lowercase slugs.
- Allergen IDs must be between `1` and `14`.
- Unknown extra JSON properties are rejected by the JSON Schema.

## Minimal Document

```json
{
  "schemaVersion": "2.0",
  "instanceName": "Ristorante Demo",
  "categories": [],
  "badges": [],
  "allergens": []
}
```

When allergens are missing, MenuForge fills the standard 14 EU allergens.

## Full Product Example

```json
{
  "id": "margherita",
  "title": "Margherita",
  "price": "6,00",
  "description": "Pomodoro, mozzarella, basilico",
  "imageUrl": "/assets/menu/margherita.jpg",
  "highlight": false,
  "available": true,
  "position": 0,
  "calories": "720 kcal",
  "origin": "Campania",
  "format": "Normale / Maxi",
  "specialText1": "Ricetta classica",
  "specialText2": "Disponibile anche senza lattosio",
  "specialText3": "Consigliata dal pizzaiolo",
  "ingredients": ["Pomodoro", "Mozzarella", "Basilico"],
  "tag1": ["classico"],
  "tag2": ["homepage"],
  "tag3": ["telegram-orderable"],
  "badges": [
    { "id": "chef", "label": "Scelto dallo chef", "style": "accent" }
  ],
  "allergens": [
    { "id": 1, "code": "GLUTEN", "nameIt": "Cereali contenenti glutine" },
    { "id": 7, "code": "MILK", "nameIt": "Latte e prodotti a base di latte" }
  ],
  "customAttributes": [
    { "key": "prep-time", "value": "8 min", "displayLabel": "Preparazione" }
  ]
}
```

## Field Ownership

MenuForge owns:

- document validation;
- slug/ID generation when creating through services;
- public filtering by visibility/availability;
- backup-on-write;
- import/export.

The host website owns:

- visual design;
- login and roles;
- image upload/storage;
- product photos;
- currency formatting beyond the raw `price` string;
- interpretation of `tag1`, `tag2`, `tag3`, `style`, and `customAttributes`.

## JSON Schema

The machine-readable schema is included in the package at:

```text
classpath:menuforge-menu.schema.json
```

Source path:

```text
menuforge-core/src/main/resources/menuforge-menu.schema.json
```

