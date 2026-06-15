# MenuForge Reference

This document is the contract for sites that use MenuForge.
The same contract is available as JSON Schema in
`src/main/resources/menuforge-menu.schema.json`.

Current package release: `0.1.0`.
Current JSON document contract: `schemaVersion: "2.0"`.

## Concepts

MenuForge stores the menu as one JSON document. The document contains categories.
Categories contain items. Items contain optional arrays for ingredients, tags,
badges, allergens, and custom attributes.

The framework has two integration surfaces:

- Java services for code running inside the website backend.
- HTTP endpoints for public reads and optional external admin integrations.

## JSON Document Shape

```json
{
  "schemaVersion": "2.0",
  "instanceName": "Ristorante Demo",
  "updatedAt": "2026-06-15T10:00:00Z",
  "categories": [],
  "badges": [],
  "allergens": []
}
```

### `MenuDocument`

| Field | Type | Required | Description |
|---|---|---:|---|
| `schemaVersion` | string | yes | Document format version. Current value: `2.0`. This is independent from the package version `0.1.0`. |
| `instanceName` | string | no | Restaurant/menu name returned by `GET /api/menu`. Defaults to `menuforge.instance-name`. |
| `updatedAt` | string | auto | Last write timestamp. |
| `categories` | array | no | Ordered menu sections. |
| `badges` | array | no | Shared visual badges assignable to items. |
| `allergens` | array | no | EU allergens. Auto-filled when missing. |

## Category Fields

```json
{
  "id": "pizze",
  "slug": "pizze",
  "title": "Pizze",
  "subtitle": "Dal forno a legna",
  "note": "Disponibili anche senza lattosio",
  "categoryImageUrl": "/assets/menu/pizze.jpg",
  "visible": true,
  "position": 0,
  "items": []
}
```

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | string | no | Usually same as `slug`. Auto-filled when possible. |
| `slug` | string | yes | Stable category identifier used in URLs and service calls. Example: `pizze`. |
| `title` | string | yes | Public category name. |
| `subtitle` | string | no | Secondary public text. |
| `note` | string | no | Category note/disclaimer. |
| `categoryImageUrl` | string | no | Image path or URL. MenuForge stores only the string. |
| `visible` | boolean | no | If `false`, category is hidden from public menu. Default: `true`. |
| `position` | number | no | Sort order. Lower appears first. |
| `items` | array | no | Products inside the category. |

## Menu Item Fields

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
  "badges": [],
  "allergens": [],
  "customAttributes": []
}
```

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | string | yes | Stable globally unique item identifier. If missing on create, generated from title. Example: `margherita`. |
| `title` | string | yes | Product name. |
| `price` | string | no | Free text price: `6,00`, `Da 4,00`, `S/Q`. |
| `description` | string | no | Public product description. |
| `imageUrl` | string | no | Image path or URL. The host site serves/uploads files. |
| `highlight` | boolean | no | UI hint for featured item. Default: `false`. |
| `available` | boolean | no | If `false`, hidden from public menu. Default: `true`. |
| `position` | number | no | Sort order inside category. |
| `calories` | string | no | Free text calories. |
| `origin` | string | no | Provenance/origin. |
| `format` | string | no | Size/portion/format. |
| `specialText1` | string | no | Free text slot for site-specific meaning. |
| `specialText2` | string | no | Free text slot for site-specific meaning. |
| `specialText3` | string | no | Free text slot for site-specific meaning. |
| `ingredients` | array of string | no | Ordered ingredient list. |
| `tag1` | array of string | no | Free tag group 1. |
| `tag2` | array of string | no | Free tag group 2. |
| `tag3` | array of string | no | Free tag group 3. |
| `badges` | array of badge | no | Visual badges on this item. |
| `allergens` | array of allergen | no | EU allergen objects. |
| `customAttributes` | array | no | Arbitrary key/value metadata. |

## Suggested Tag Semantics

Tags are intentionally generic because every site may need different logic.
Pick conventions per project and document them in that site's code.
Tag values must use lowercase slug format: letters, digits and hyphens.

Recommended default:

| Tag group | Suggested use | Examples |
|---|---|---|
| `tag1` | Marketing/public labels | `promo`, `classico`, `novita`, `stagionale` |
| `tag2` | UI/layout logic | `homepage`, `featured`, `hide-mobile`, `top-list` |
| `tag3` | External integrations | `telegram-orderable`, `delivery`, `no-asporto` |

Public search supports one tag group at a time:

```http
GET /api/menu/search?tag1=promo
GET /api/menu/search?tag2=homepage
GET /api/menu/search?tag3=telegram-orderable
```

## Badge Fields

```json
{
  "id": "novita",
  "label": "Novita",
  "style": "green"
}
```

| Field | Type | Required | Description |
|---|---|---:|---|
| `id` | string | yes | Stable badge identifier. |
| `label` | string | yes | Text shown by the frontend. |
| `style` | string | no | UI hint. Can be a token, class, or color. |

## Allergen Fields

```json
{
  "id": 7,
  "code": "MILK",
  "nameIt": "Latte e prodotti a base di latte"
}
```

MenuForge seeds the 14 EU allergens when missing.

| ID | Code |
|---:|---|
| 1 | `GLUTEN` |
| 2 | `CRUSTACEANS` |
| 3 | `EGGS` |
| 4 | `FISH` |
| 5 | `PEANUTS` |
| 6 | `SOYBEANS` |
| 7 | `MILK` |
| 8 | `NUTS` |
| 9 | `CELERY` |
| 10 | `MUSTARD` |
| 11 | `SESAME` |
| 12 | `SULPHITES` |
| 13 | `LUPIN` |
| 14 | `MOLLUSCS` |

When creating/updating via Java or HTTP, use `allergenNumbers`, for example:

```json
{
  "title": "Margherita",
  "allergenNumbers": [1, 7]
}
```

## Custom Attribute Fields

```json
{
  "key": "sku",
  "value": "PIZ-001",
  "displayLabel": "Codice prodotto"
}
```

Use custom attributes for project-specific metadata that does not deserve a
standard field yet: SKU, prep time, external product IDs, internal notes, etc.

## Java Service API

Use these services inside the website backend after the site's own authentication.

### `MenuDocumentService`

| Method | Description |
|---|---|
| `exportMenu()` | Returns the full `MenuDocument`. |
| `replaceMenu(MenuDocument document)` | Replaces the entire menu document. Useful for JSON imports. |

### `CategoryService`

| Method | Description |
|---|---|
| `getAllCategories()` | All categories, visible and hidden. |
| `getVisibleCategories()` | Only public visible categories. |
| `getCategoryBySlug(String slug)` | Optional category lookup. |
| `getCategoryBySlugOrThrow(String slug)` | Lookup or throw `CategoryNotFoundException`. |
| `createCategory(CategoryRequest request)` | Creates a category. Generates slug from title when missing. |
| `updateCategory(String slug, CategoryRequest request)` | Updates category fields. |
| `deleteCategory(String slug)` | Deletes category and its items. |
| `toggleVisibility(String slug)` | Shows/hides a category. |
| `reorderCategories(List<String> orderedSlugs)` | Assigns positions from the supplied slug order. |
| `replaceCategory(String slug, Category replacement)` | Replaces one whole category block. |

### `MenuItemService`

| Method | Description |
|---|---|
| `getItemsByCategory(String categorySlug)` | Items in one category. |
| `getItemById(String id)` | Optional item lookup. |
| `getItemByIdOrThrow(String id)` | Lookup or throw `MenuItemNotFoundException`. |
| `createItem(String categorySlug, MenuItemRequest request)` | Creates item in category. Generates ID from title when missing. |
| `updateItem(String id, MenuItemRequest request)` | Updates item fields. Null fields are left unchanged. |
| `deleteItem(String id)` | Deletes an item. |
| `toggleAvailability(String id)` | Shows/hides item from public menu. |
| `reorderItems(String categorySlug, List<String> orderedIds)` | Reorders items and rejects IDs outside that category. |
| `setIngredients(String id, List<String> ingredients)` | Replaces ingredients. |
| `setTag1(String id, List<String> tags)` | Replaces tag group 1. |
| `setTag2(String id, List<String> tags)` | Replaces tag group 2. |
| `setTag3(String id, List<String> tags)` | Replaces tag group 3. |
| `setAllergens(String id, List<Integer> allergenNumbers)` | Replaces allergens by EU numbers. |
| `setBadges(String id, List<String> badgeIds)` | Replaces badges by badge IDs. |
| `setCustomAttribute(String id, String key, String value, String displayLabel)` | Adds or updates one custom attribute. |
| `removeCustomAttribute(String id, String key)` | Removes one custom attribute. |

### `MenuQueryService`

| Method | Description |
|---|---|
| `getFullMenu()` | Public menu response: visible categories and available items. |
| `searchByTag1(String tag)` | Available public items matching `tag1`. |
| `searchByTag2(String tag)` | Available public items matching `tag2`. |
| `searchByTag3(String tag)` | Available public items matching `tag3`. |
| `getAvailableItems()` | All available items in visible categories. |
| `getItemsByAllergen(int allergenNumber)` | Items containing the allergen. |

### `BadgeService`

| Method | Description |
|---|---|
| `getAllBadges()` | Returns shared badges. |
| `getBadgeById(String id)` | Optional badge lookup. |
| `getBadgeByLabel(String label)` | Optional badge lookup by label. |
| `createBadge(BadgeRequest request)` | Creates shared badge. |
| `updateBadge(String id, BadgeRequest request)` | Updates badge label/style. |
| `deleteBadge(String id)` | Deletes badge and removes it from items. |

### `AllergenService`

| Method | Description |
|---|---|
| `getAllAllergens()` | Returns all EU allergens. |
| `getAllergenById(int number)` | Lookup by EU number. |
| `getAllergenByCode(String code)` | Lookup by code. |

## Public HTTP API

Public endpoints are always read-only.

### `GET /api/menu`

Returns public menu: visible categories and available items.

### `GET /api/menu/categories`

Returns visible category metadata without items.

### `GET /api/menu/categories/{slug}`

Returns one category by slug with its available items.

### `GET /api/menu/items/{id}`

Returns one item by ID.

### `GET /api/menu/allergens`

Returns EU allergens.

### `GET /api/menu/search`

Query parameters:

- `tag1`
- `tag2`
- `tag3`

If no tag is supplied, returns all available items.

## External Admin HTTP API

Disabled by default. Enable only for external integrations.

```yaml
menuforge:
  api:
    admin:
      enabled: true
      api-key: ${MENUFORGE_ADMIN_KEY}
```

Required header:

```http
X-MenuForge-Key: your-secret-key
```

### Import / Export

| Method | Path | Body | Description |
|---|---|---|---|
| `GET` | `/api/menu/admin/export` | none | Full `MenuDocument`. |
| `PUT` | `/api/menu/admin/import` | `MenuDocument` | Replaces entire menu. |

### Categories

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/api/menu/admin/categories` | `CategoryRequest` | Create category. |
| `PUT` | `/api/menu/admin/categories/{slug}` | `CategoryRequest` | Update category fields. |
| `PUT` | `/api/menu/admin/categories/{slug}/replace` | `Category` | Replace one category block. |
| `DELETE` | `/api/menu/admin/categories/{slug}` | none | Delete category and items. |
| `PATCH` | `/api/menu/admin/categories/{slug}/toggle` | none | Toggle visibility. |
| `PATCH` | `/api/menu/admin/categories/reorder` | `["slug1","slug2"]` | Reorder categories. |

### Items

| Method | Path | Body | Description |
|---|---|---|---|
| `POST` | `/api/menu/admin/categories/{slug}/items` | `MenuItemRequest` | Create item. |
| `PUT` | `/api/menu/admin/items/{id}` | `MenuItemRequest` | Update item fields. |
| `DELETE` | `/api/menu/admin/items/{id}` | none | Delete item. |
| `PATCH` | `/api/menu/admin/items/{id}/toggle-availability` | none | Toggle availability. |
| `PATCH` | `/api/menu/admin/items/{id}/ingredients` | `["Pomodoro"]` | Replace ingredients. |
| `PATCH` | `/api/menu/admin/items/{id}/tag1` | `["promo"]` | Replace tag1. |
| `PATCH` | `/api/menu/admin/items/{id}/tag2` | `["homepage"]` | Replace tag2. |
| `PATCH` | `/api/menu/admin/items/{id}/tag3` | `["telegram"]` | Replace tag3. |
| `PATCH` | `/api/menu/admin/items/{id}/allergens` | `[1,7]` | Replace allergens. |
| `PATCH` | `/api/menu/admin/items/{id}/badges` | `["novita"]` | Replace badges. |
| `PATCH` | `/api/menu/admin/categories/{slug}/items/reorder` | `["item1","item2"]` | Reorder category items. |

### Badges

| Method | Path | Body | Description |
|---|---|---|---|
| `GET` | `/api/menu/admin/badges` | none | List badges. |
| `POST` | `/api/menu/admin/badges` | `BadgeRequest` | Create badge. |
| `PUT` | `/api/menu/admin/badges/{id}` | `BadgeRequest` | Update badge. |
| `DELETE` | `/api/menu/admin/badges/{id}` | none | Delete badge. |

## Configuration Reference

| Property | Default | Description |
|---|---|---|
| `menuforge.enabled` | `true` | Enable/disable framework. |
| `menuforge.instance-name` | `MenuForge` | Name returned in public menu. |
| `menuforge.data-dir` | `./menuforge-data` | Directory containing JSON file. |
| `menuforge.menu-file` | `menu.json` | Menu document filename. |
| `menuforge.backup-on-write` | `true` | Creates backup JSON before writes. |
| `menuforge.api.base-path` | `/api/menu` | Public API base path. |
| `menuforge.api.admin-base-path` | `/api/menu/admin` | External admin API base path. |
| `menuforge.api.cors-origins` | `*` | CORS origins for MenuForge endpoints. |
| `menuforge.api.admin.enabled` | `false` | Enables external admin HTTP API. |
| `menuforge.api.admin.api-key` | none | Required when admin HTTP is enabled. |

## Versioning Policy

MenuForge uses two versions:

- Package/Git release version, for example `v0.1.0`.
- JSON document `schemaVersion`, currently `"2.0"`.

Patch/minor package releases may keep the same JSON schema. The JSON
`schemaVersion` changes only when saved menu documents need a breaking migration.

For `v0.1.0`, the JSON shape described in this reference and in
`menuforge-menu.schema.json` is the definitive document contract.

## Error Format

REST errors use Spring `ProblemDetail` style where possible:

```json
{
  "type": "https://menuforge.it/errors/category-not-found",
  "title": "Category Not Found",
  "status": 404,
  "detail": "Category not found: 'pizze'"
}
```

Admin API key failures return `401 Unauthorized`.
