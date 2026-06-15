# Convert Menu Data To MenuForge JSON

This guide explains how to take a restaurant/bar menu from a photo, PDF,
spreadsheet, document or plain text and convert it into a MenuForge
`MenuDocument`.

The goal is not to invent missing information. The goal is to preserve the menu
faithfully, use required fields correctly, and leave unknown optional fields
empty.

## Output Target

Produce one JSON object shaped like this:

```json
{
  "schemaVersion": "2.0",
  "instanceName": "Nome locale",
  "categories": [],
  "badges": [],
  "allergens": []
}
```

Required top-level fields:

- `schemaVersion`: always `"2.0"`.
- `categories`: array, even when empty.

Recommended top-level fields:

- `instanceName`: restaurant/bar/menu name when known.
- `badges`: shared badges only if used.
- `allergens`: can be omitted; MenuForge can seed standard EU allergens.

Do not write empty optional fields just to fill the shape. If `tag2`, `badges`,
`calories`, `origin`, `imageUrl`, `customAttributes` or another optional field
is not useful, omit it.

## Category Rules

Each visible menu section becomes one category.

Examples:

- Antipasti
- Pizze
- Primi
- Secondi
- Dolci
- Cocktail
- Vini

Required category fields:

- `slug`: stable lowercase identifier, for example `pizze`, `cocktail-classici`.
- `title`: public category name.

Recommended category fields:

- `id`: same value as `slug`.
- `subtitle`: short secondary text when present.
- `note`: warnings or notes that apply to the whole category.
- `visible`: normally `true`.
- `position`: zero-based order. Omit it when the natural order is enough.
- `items`: array of products. Omit it only for empty categories.

Example:

```json
{
  "id": "pizze",
  "slug": "pizze",
  "title": "Pizze",
  "subtitle": "Impasto a lunga lievitazione",
  "note": "Disponibili anche con impasto integrale",
  "items": []
}
```

## Product Rules

Each dish, drink or sellable menu line becomes one item.

Required product fields:

- `id`: stable lowercase identifier, unique in the whole menu.
- `title`: product name.

Recommended product fields:

- `price`: keep the source text, for example `"8,00"`, `"Da 5,00"`, `"S/Q"`.
- `description`: ingredients or description from the source menu.
- `available`: normally `true`.
- `position`: zero-based order inside the category.
- `ingredients`: array when ingredients are clearly listed.
- `allergens`: only when the source provides allergen information or it is safe
  to map them from explicit ingredients.

Optional product fields:

- `imageUrl`
- `highlight`
- `calories`
- `origin`
- `format`
- `specialText1`
- `specialText2`
- `specialText3`
- `tag1`
- `tag2`
- `tag3`
- `badges`
- `customAttributes`

If a field is not present in the source menu, omit it. Do not make up calories,
origin, allergens, tags or badges.

Example:

```json
{
  "id": "margherita",
  "title": "Margherita",
  "price": "6,00",
  "description": "Pomodoro, mozzarella, basilico",
  "ingredients": ["Pomodoro", "Mozzarella", "Basilico"],
  "tag1": ["classico"],
  "allergens": [
    { "id": 1, "code": "GLUTEN", "nameIt": "Cereali contenenti glutine" },
    { "id": 7, "code": "MILK", "nameIt": "Latte e prodotti a base di latte" }
  ]
}
```

## Slug And ID Rules

Use lowercase letters, numbers and hyphens only.

Good:

- `pizze`
- `margherita`
- `spritz-aperol`
- `menu-pranzo`

Bad:

- `Pizze`
- `margherita classica`
- `caffe-espresso!`
- `pizza_1`

Suggested conversion:

- Lowercase text.
- Replace spaces with hyphens.
- Remove accents.
- Remove punctuation.
- Keep IDs stable after the first import.

## Allergen Mapping

MenuForge uses the standard 14 EU allergen IDs.

| ID | Code | Italian label |
|---:|---|---|
| 1 | `GLUTEN` | Cereali contenenti glutine |
| 2 | `CRUSTACEANS` | Crostacei |
| 3 | `EGGS` | Uova |
| 4 | `FISH` | Pesce |
| 5 | `PEANUTS` | Arachidi |
| 6 | `SOYBEANS` | Soia |
| 7 | `MILK` | Latte |
| 8 | `NUTS` | Frutta a guscio |
| 9 | `CELERY` | Sedano |
| 10 | `MUSTARD` | Senape |
| 11 | `SESAME` | Semi di sesamo |
| 12 | `SULPHITES` | Solfiti |
| 13 | `LUPIN` | Lupini |
| 14 | `MOLLUSCS` | Molluschi |

Only add allergens when known. If the source menu does not provide allergens
and the ingredient list is ambiguous, leave `allergens` empty.

## Tags And Badges

Tags and badges are optional UI/integration hints.

Use tags only when the project has a clear convention:

- `tag1`: public/marketing labels, for example `classico`, `promo`, `novita`.
- `tag2`: layout logic, for example `homepage`, `featured`.
- `tag3`: external integrations, for example `delivery`, `telegram-orderable`.

Use badges for visual labels:

```json
{
  "id": "chef",
  "label": "Scelto dallo chef",
  "style": "accent"
}
```

If the original menu does not contain labels such as "Novita", "Consigliato",
"Piccante" or similar, leave badges empty.

## Conversion Checklist

1. Identify the restaurant/menu name.
2. Split the source into categories.
3. Preserve category order with `position`.
4. Create one product per sellable line.
5. Preserve product order with `position`.
6. Copy product names exactly, fixing only clear typing/extraction mistakes.
7. Keep prices as strings.
8. Put ingredient text in `description`; also split into `ingredients` when easy.
9. Add allergens only when known.
10. Generate stable lowercase slugs/IDs.
11. Omit unknown or unused optional fields.
12. Validate the JSON against `menuforge-menu.schema.json`.
13. Import through `MenuDocumentService.replaceMenu(document)` or
    `PUT /api/menu/admin/import`.

## Minimal Category With Products

```json
{
  "schemaVersion": "2.0",
  "instanceName": "Bar Centrale",
  "categories": [
    {
      "id": "caffetteria",
      "slug": "caffetteria",
      "title": "Caffetteria",
      "visible": true,
      "position": 0,
      "items": [
        {
          "id": "espresso",
          "title": "Espresso",
          "price": "1,20"
        }
      ]
    }
  ]
}
```
