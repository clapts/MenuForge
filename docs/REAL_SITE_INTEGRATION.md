# Real Site Integration Guide

This guide shows the recommended way to use MenuForge inside a client website.

## Recommended Setup

Use MenuForge as a backend dependency of the client site's Spring Boot app.

```xml
<dependency>
    <groupId>it.menuforge</groupId>
    <artifactId>menuforge-core</artifactId>
    <version>0.1.0</version>
</dependency>
```

Configure the menu file:

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
    cors-origins: "https://ristorante.example.com"
    admin:
      enabled: false
```

## Public Website Menu

The public frontend fetches read-only menu data:

```javascript
const response = await fetch("/api/menu");
const menu = await response.json();
```

Render categories and items with the site's own UI.

MenuForge already filters public output:

- hidden categories are excluded;
- unavailable products are excluded.

## Website Admin Area

If the restaurant owner is already logged into the website admin area, do not
call MenuForge admin HTTP endpoints from the browser. Instead, create your own
site admin endpoints and call MenuForge Java services from the backend.

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

Protect `/admin/menu/**` with the site's normal authentication and authorization.
No MenuForge API key is needed in this flow.

## External Tools

Enable the admin HTTP API only for external tools such as:

- Telegram bots;
- agency dashboards;
- import scripts;
- deployment automations.

```yaml
menuforge:
  api:
    cors-origins: "https://agency.example.com"
    admin:
      enabled: true
      api-key: ${MENUFORGE_ADMIN_KEY}
```

Every external admin request must include:

```http
X-MenuForge-Key: your-secret-key
```

## Common Real Site Flow

1. Convert the paper/PDF menu to a `MenuDocument` JSON.
2. Import it with `MenuDocumentService.replaceMenu(document)` or with
   `PUT /api/menu/admin/import`.
3. Build the public menu page using `GET /api/menu`.
4. Build the client-specific admin UI using the site's design system.
5. Call MenuForge Java services from the site's authenticated backend.
6. Enable external HTTP admin only if a bot or external tool needs it.

## Production Checklist

- Store `MENUFORGE_ADMIN_KEY` in environment/secret manager.
- Restrict `menuforge.api.cors-origins` to exact domains.
- Keep `backup-on-write: true`.
- Back up `menuforge-data/menu.json`.
- Do not expose the admin HTTP API unless needed.
- Do not commit production menu files containing private data.
- Keep image uploads outside MenuForge; store only URLs/paths in menu items.

