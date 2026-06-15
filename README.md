# MenuForge

MenuForge is an embeddable Spring Boot framework for digital restaurant and bar
menus.

It is built for agencies that create many client websites and want the menu
logic to be reusable: data model, JSON storage, Java services, public API,
optional protected HTTP admin API, import/export, badges, allergens, tags and
availability.

Current release: `v0.1.0`.

## What Is Stable In v0.1.0

- Maven artifact version: `0.1.0`.
- JSON document contract: `schemaVersion: "2.0"`.
- Spring Boot 3 / Java 21 support.
- Document-based JSON storage.
- Internal Java service API for custom website admin areas.
- Public read-only HTTP API.
- Optional API-key protected admin HTTP API for bots and external tools.
- Standalone admin demo app under `examples/menu-admin-demo`.

## Repository Structure

- `menuforge-core`: framework package.
- `test-app`: minimal Spring Boot app that consumes the framework.
- `examples`: standalone and copy-friendly examples.
- `docs`: release, JSON format and integration guidance.

## Start Here

1. Read [menuforge-core/README.md](menuforge-core/README.md).
2. Read [docs/REAL_SITE_INTEGRATION.md](docs/REAL_SITE_INTEGRATION.md).
3. Check the JSON contract in [docs/JSON_FORMAT.md](docs/JSON_FORMAT.md).
4. Convert menu data with [docs/MENU_DATA_TO_JSON.md](docs/MENU_DATA_TO_JSON.md).
5. Try the standalone demo in [examples/menu-admin-demo](examples/menu-admin-demo).

## Internal Backend vs External API

Inside a client website backend, call MenuForge Java services directly:
`CategoryService`, `MenuItemService`, `MenuDocumentService`, `BadgeService`,
`AllergenService` and `MenuQueryService`.

Do not call the admin HTTP API from the website backend just to manage the menu.
The admin HTTP API exists for external integrations: bots, agency tools,
automation scripts or separate dashboards that are outside the website backend.

## Local Demo

```powershell
cd menuforge-core
..\maven\apache-maven-3.9.6\bin\mvn.cmd install

cd ..\test-app
..\maven\apache-maven-3.9.6\bin\mvn.cmd package -DskipTests
java -jar target\test-app-0.1.0.jar --server.port=18081
```

Then serve/open `examples/menu-admin-demo/index.html` and connect it to:

- Public API: `http://localhost:18081/api/menu`
- Admin API: `http://localhost:18081/api/menu/admin`
- API key: `local-dev-key`

## Production Note

MenuForge v0.1.0 is ready as an internal MVP framework. Before using it on a
public client site, configure API keys from environment variables, restrict CORS
to known domains, and keep backups enabled.
