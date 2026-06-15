# MenuForge Examples

These examples are copy-friendly starting points. They are not part of the
compiled framework.

## `internal-admin-controller`

Shows how a client website can build its own authenticated admin endpoints and
call MenuForge Java services directly.

Use this when the restaurant owner is already logged into the website admin
area. No `X-MenuForge-Key` is needed in this flow.

## `public-frontend`

Shows a small browser script that reads `GET /api/menu` and renders categories
and items.

Your real sites should replace the HTML/CSS with their own design.

## `external-http-client`

Shows how a bot or external agency automation can call the optional admin HTTP
API using `X-MenuForge-Key`.

Use environment variables for keys. Do not hardcode production secrets.

## `menu-documents`

Contains a realistic `MenuDocument` JSON that can be imported with:

```http
PUT /api/menu/admin/import
X-MenuForge-Key: your-secret-key
Content-Type: application/json
```

## Root-level `examples/menu-admin-demo`

The repository root also contains `examples/menu-admin-demo`, a standalone
static admin demo. It is intentionally outside `menuforge-core` and outside
`test-app`, so it can be used as a non-technical playground against any
MenuForge backend URL.

## Guided Real Site Integration

For a full recommended flow in a client website, read:

```text
docs/REAL_SITE_INTEGRATION.md
```
