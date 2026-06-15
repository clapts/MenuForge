# Switch AI / MenuForge

MenuForge e' un framework plug-and-play per gestire menu digitali dentro siti
Spring Boot per bar e ristoranti.

## Visione

L'agenzia crea molti siti per ristorazione e non vuole riscrivere ogni volta la
logica del menu. MenuForge fornisce:

- modello menu pronto;
- servizi Java per il backend del sito;
- API pubblica per il frontend;
- API admin HTTP opzionale per bot e strumenti esterni;
- import/export JSON per caricare menu completi generati da foto, OCR o AI.

## Regola di integrazione

Dentro il sito, dopo che il proprietario ha fatto login nella zona admin, il
backend del sito deve chiamare direttamente i servizi Java di MenuForge.

Esempio:

- `CategoryService.createCategory(...)`
- `MenuItemService.createItem(...)`
- `MenuDocumentService.replaceMenu(...)`

L'API key non serve in questo flusso, perche' la sicurezza e' gia' gestita dal
sito ospite.

## API HTTP esterna

Le API HTTP admin servono per integrazioni fuori dal backend del sito:

- bot Telegram;
- strumenti interni dell'agenzia;
- import/export remoti;
- automazioni.

Queste API sono disabilitate di default e, quando abilitate, richiedono
`X-MenuForge-Key`.

## Storage

La v2 usa storage documentale JSON:

```text
menuforge-data/menu.json
```

Questo rispecchia il flusso reale dell'agenzia: si estrae il menu da una foto,
lo si trasforma in JSON e lo si importa in blocco. Le categorie possono poi
essere aggiornate, rimpiazzate o cancellate velocemente.

SQLite/JPA non sono piu' il cuore del framework. In futuro si potranno aggiungere
adapter alternativi implementando `MenuStorage`, per esempio MongoDB, S3 o Git.
