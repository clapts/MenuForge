# MenuForge Core

MenuForge is an embeddable Spring Boot framework for digital restaurant and bar
menus. It is made for agencies that build many client sites and want to stop
rewriting the same menu-management logic every time.

Current package release: `0.1.0`.
Current JSON document contract: `schemaVersion: "2.0"`.

MenuForge does not force a prebuilt admin UI. Each website can build its own
admin section, matching its design and authentication system, while reusing the
same Java services, JSON storage, public menu API, and optional external admin
HTTP API.

## What MenuForge Provides

- A standard menu data model for categories, products, ingredients, allergens,
  badges, tags, images, prices, availability, and custom fields.
- A JSON document storage engine using `menuforge-data/menu.json`.
- Java services for the site's backend/admin area.
- Public read-only HTTP endpoints for the frontend.
- Optional protected HTTP admin endpoints for Telegram bots, agency tools, or
  other external integrations.
- Full menu import/export, so menus extracted from photos, OCR or manual entry can be loaded
  in one operation.

## What MenuForge Does Not Provide Yet

- A production-ready admin UI.
- User login, roles, sessions, or password management.
- Image upload/storage.
- A Telegram bot implementation.

Those pieces belong to the host site or to future optional modules. The current
framework gives the stable backend layer those pieces can use.

## Recommended Architecture

```text
Client website
  Frontend menu page
    -> GET /api/menu

  Authenticated admin area
    -> calls Java services directly
       CategoryService, MenuItemService, MenuDocumentService, ...

External integrations
  Telegram bot / agency tool / automation
    -> HTTP admin API
       X-MenuForge-Key required
```

The important rule is simple:

- Inside the website backend: use Java methods. No MenuForge API key is needed.
- Outside the website backend: use HTTP admin endpoints. API key is required.

In other words: if the code is running in the same backend where MenuForge is
installed, call `CategoryService`, `MenuItemService`, `MenuDocumentService`,
`BadgeService`, `AllergenService` or `MenuQueryService` directly. Use the admin
HTTP API only when the caller is outside that backend.

## Installation

Add MenuForge to the Spring Boot site:

```xml
<dependency>
    <groupId>it.menuforge</groupId>
    <artifactId>menuforge-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

Configure it in `application.yml`:

```yaml
menuforge:
  enabled: true
  instance-name: "Ristorante Demo"
  data-dir: "./menuforge-data"
  menu-file: "menu.json"
  backup-on-write: true
  api:
    base-path: /api/menu
    admin-base-path: /api/menu/admin
    cors-origins: "*"
    admin:
      enabled: false
      # api-key: ${MENUFORGE_ADMIN_KEY}
```

On startup, MenuForge creates `menuforge-data/menu.json` if it does not exist.

## Basic Frontend Use

Your public menu page can fetch:

```javascript
async function loadMenu() {
  const response = await fetch('/api/menu');
  const menu = await response.json();
  renderMenu(menu.categories);
}
```

The frontend decides all visual layout. MenuForge only supplies structured data.

## Basic Admin Use Inside A Site

After the restaurant owner logs into your site's admin area, your backend can use
MenuForge services directly:

```java
@RestController
@RequestMapping("/admin/menu")
class SiteMenuAdminController {
    private final CategoryService categoryService;
    private final MenuItemService menuItemService;

    SiteMenuAdminController(CategoryService categoryService, MenuItemService menuItemService) {
        this.categoryService = categoryService;
        this.menuItemService = menuItemService;
    }

    @PostMapping("/categories")
    Category createCategory(@RequestBody CategoryRequest request) {
        return categoryService.createCategory(request);
    }

    @PostMapping("/categories/{slug}/items")
    MenuItem createItem(@PathVariable String slug, @RequestBody MenuItemRequest request) {
        return menuItemService.createItem(slug, request);
    }
}
```

This admin controller is yours. You protect it with your site's login/session/JWT
system. MenuForge does not need an API key in this internal Java-service flow.

## JSON Menu Document

The storage file is a single document:

```text
menuforge-data/menu.json
```

Minimal example:

```json
{
  "schemaVersion": "2.0",
  "instanceName": "Ristorante Demo",
  "categories": [
    {
      "slug": "pizze",
      "title": "Pizze",
      "items": [
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
      ]
    }
  ]
}
```

Only required fields must be written. Empty optional fields such as `tag2`,
`badges`, `customAttributes`, `imageUrl`, `calories` or `position: 0` can be
omitted to keep the JSON clean.

See [REFERENCE.md](REFERENCE.md) and [../docs/JSON_FORMAT.md](../docs/JSON_FORMAT.md)
for the complete field reference.
The machine-readable JSON Schema is bundled at
`classpath:menuforge-menu.schema.json`.

## Full Menu Import

When you convert a paper menu, PDF, spreadsheet or text list into JSON, import
it in Java:

```java
MenuDocument document = ...;
menuDocumentService.replaceMenu(document);
```

Or through the external HTTP admin API:

```http
PUT /api/menu/admin/import
X-MenuForge-Key: your-secret-key
Content-Type: application/json
```

For a practical conversion guide, read
[../docs/MENU_DATA_TO_JSON.md](../docs/MENU_DATA_TO_JSON.md).

## External HTTP Admin API

Enable it only when an external tool needs to manage the menu:

```yaml
menuforge:
  api:
    admin:
      enabled: true
      api-key: ${MENUFORGE_ADMIN_KEY}
```

Every admin HTTP request must include:

```http
X-MenuForge-Key: your-secret-key
```

Use this for Telegram bots, agency dashboards, deployment scripts, or automated
menu imports. Do not use it for the site's own admin panel when that panel is
already backed by the same Spring application: in that case, call Java services
directly after the site has authenticated the owner.

## Demo App

The included `test-app` is only a demonstration and integration test target.
It shows a simple public view and basic HTTP admin calls. It is not meant to be
the final admin UI for client websites.

Run it after installing the core locally:

```powershell
cd menuforge-core
mvn install

cd ..\test-app
mvn package -DskipTests
java -jar target\test-app-0.1.0.jar --server.port=18081
```

Then open:

```text
http://localhost:18081
```

The demo API key is configured in `test-app/src/main/resources/application.yml`.

## Copy-Friendly Examples

See [examples/README.md](examples/README.md) for:

- an internal admin controller that calls Java services;
- a small public frontend fetch/render script;
- an external HTTP client for bots/automation;
- a realistic importable `MenuDocument` JSON.
- a standalone non-technical admin demo in `../examples/menu-admin-demo`.

For a more guided implementation flow, read
[../docs/REAL_SITE_INTEGRATION.md](../docs/REAL_SITE_INTEGRATION.md).

## Next Steps For A Real Site

1. Add the dependency.
2. Configure `menuforge.*`.
3. Create a first `menu.json` or import one with `MenuDocumentService`.
4. Render `GET /api/menu` in your frontend.
5. Build your custom admin UI.
6. In your admin backend, call MenuForge Java services.
7. Enable HTTP admin only if bots or external tools need it.
