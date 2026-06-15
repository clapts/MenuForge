# MenuForge Admin Demo

Mini app statica, separata dal progetto Spring, pensata per testare MenuForge come farebbe un gestore non tecnico.

Non e' una sezione admin definitiva da copiare nei siti dei clienti. E' un esempio completo per capire quali dati si possono gestire e per provare rapidamente categorie, prodotti, badge, allergeni, import/export e chiamate HTTP.

## Come usarla

1. Avvia un backend che usa MenuForge, per esempio `test-app`.
2. Apri `index.html` nel browser, oppure servila con un server statico.
3. Imposta:
   - Public API base: `http://localhost:8080/api/menu`
   - Admin API base: `http://localhost:8080/api/menu/admin`
   - Admin API key: `local-dev-key`
4. Premi `Connetti`.

## Cosa prova

- Lettura menu pubblico.
- Export JSON completo.
- Import JSON completo.
- Creazione, modifica, eliminazione e visibilita' categorie.
- Creazione, modifica, eliminazione e disponibilita' prodotti.
- Campi prodotto principali:
  - titolo
  - prezzo
  - descrizione
  - immagine
  - evidenza
  - disponibilita'
  - posizione
  - calorie
  - origine
  - formato
  - testi speciali
  - ingredienti
  - tag1, tag2, tag3
  - allergeni
  - badge
- Creazione ed eliminazione badge.

## File inclusi

- `index.html`: shell della mini app.
- `styles.css`: stile completo.
- `app.js`: logica HTTP e stato locale.
- `sample-menu.json`: menu dimostrativo importabile.

