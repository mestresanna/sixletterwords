# Six Letter Words API

Finds every combination of words from a list that together form a six-letter word that is also in the list.

```
foobar
fo        →   fo+obar=foobar
obar
```

Combinations can have **any number of parts** (`f+oo+bar=foobar`), and both the target word length and the maximum number of parts are configurable per request. Results are returned as plain text and stored in an H2 database.

---

## Quick start

**Requirements:** Docker with Docker Compose. Nothing else (no Java or Gradle needed).

```bash
git clone https://github.com/mestresanna/sixletterwords.git
cd sixletterwords
docker compose -f infrastructure/docker-compose.yaml up --build
```

The first build downloads dependencies and runs the full test suite, so it takes a minute or two. The application is ready when the log shows `Started SixLetterWordsApplication`. It listens on **http://localhost:8081**.

Then, from the project root (where `input.txt` is):

```bash
curl --request POST 'http://localhost:8081/api/file' \
  --data-binary '@input.txt' \
  --header "Content-Type: text/plain" > output.txt
```

`output.txt` now contains all combinations, one per line.

> **Why `--data-binary` instead of `--data`?**
> `curl --data @file` strips all newlines from the file before sending it, so the server would receive one long line of glued-together words that can no longer be split. `--data-binary` sends the file exactly as it is.

---

## The API

### `POST /api/file`

| | |
|---|---|
| Request body | Plain text, one word per line (`Content-Type: text/plain`) |
| Response body | Plain text, one combination per line: `fo+obar=foobar` |
| Query parameters | Optional, see below |

| Parameter | Default | Meaning |
|---|---|---|
| `targetLength` | `6` | Length of the words to build |
| `maxParts` | `6` (no limit for six-letter words) | Maximum number of words per combination |

Examples:

```bash
# Only two-word combinations (915 results for the sample input)
curl -X POST 'http://localhost:8081/api/file?maxParts=2' \
  --data-binary '@input.txt' -H "Content-Type: text/plain"

# Any number of words (4,055 results for the sample input)
curl -X POST 'http://localhost:8081/api/file' \
  --data-binary '@input.txt' -H "Content-Type: text/plain"

# Four-letter words from at most three parts
curl -X POST 'http://localhost:8081/api/file?targetLength=4&maxParts=3' \
  --data-binary '@input.txt' -H "Content-Type: text/plain"
```

### Responses

| Status | When |
|---|---|
| `200 OK` | Combinations found; the body is empty if there are none |
| `400 Bad Request` | Empty body, or invalid rules such as `maxParts=1` (the message explains why) |
| `415 Unsupported Media Type` | The request is not `text/plain` |

### About the sample input

`input.txt` contains 4,202 lines but only 1,421 distinct words (the whole list appears twice), including every single letter from `a` to `z`. That is why the number of results depends so much on `maxParts`:

| Parts | Combinations |
|---|---|
| 2 | 915 |
| 3 | 1,125 |
| 4 | 1,124 |
| 5 | 708 |
| 6 | 183 (each word spelled letter by letter) |
| **Total** | **4,055** |

---

## The database

Every combination found is stored in an H2 database (in a Docker volume, so it survives restarts).

Open the H2 console at **http://localhost:8081/h2-console** and log in with:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:/data/sixletterwords` |
| User | `sa` |
| Password | `sa` |

```sql
SELECT COUNT(*) FROM COMBINATION;
SELECT * FROM COMBINATION WHERE RESULT = 'abroad';
```

The JDBC URL must match exactly: H2 silently creates a new, empty database for any other URL.

To start again with an empty database: `docker compose -f infrastructure/docker-compose.yml down -v`.

> The H2 console is enabled with `web-allow-others=true` so it can be reached through Docker. That is fine for this demo, but it would never be enabled in a real deployment.

---

## Running without Docker

Requires Java 21. The Gradle wrapper is included.

```bash
./gradlew bootRun      # starts on http://localhost:8080, database in ./data
./gradlew test         # runs all tests
```

When running locally, use port **8080** and JDBC URL `jdbc:h2:file:./data/sixletterwords` in the H2 console.

---

## Architecture

The project uses a **hexagonal (ports and adapters) architecture**: the business logic sits in the centre and knows nothing about HTTP, files or databases. Everything external talks to it through interfaces (ports).

```
            ┌───────────────────────── core ─────────────────────────┐
 HTTP ──►   │                                                         │
 Controller │──► FindCombinationUseCase ──► FindCombinationService ───┼──► CombinationRepository ──► Persistence adapter ──► H2
 (adapter)  │       (port in)                    │                    │      (port out)              (adapter)
            │                                    ▼                    │
            │                         CombinationFinder (domain)      │
            └─────────────────────────────────────────────────────────┘
```

| Package | Contents |
|---|---|
| `domain` | `Combination`, `CombinationRules`, `CombinationFinder` and its `BacktrackingCombinationFinder` implementation. The algorithm and its rules. |
| `port.in` | `FindCombinationUseCase`: what the application can do. |
| `port.out` | `CombinationRepository`: what the application needs from outside (storage). |
| `core` | `FindCombinationService`: runs the use case by connecting the domain to the ports. |
| `adapter.in.web` | REST controller, input parser, output formatter and error handling. |
| `adapter.out.persistence` | JPA entity, Spring Data repository and the adapter implementing the storage port. |

### How it handles changing requirements

The brief mentions two likely changes. Both are isolated to one place:

- **A different maximum combination length** is a value, not code: it is a query parameter (`maxParts`, `targetLength`) and a field in `CombinationRules`. The algorithm works for any length.
- **A different source of input data** means adding a new inbound adapter (for example, one that reads a file at startup or listens to a queue) that calls the same `FindCombinationUseCase`. The domain and the service do not change.

Likewise, a different output format means a new formatter in the web adapter, and a different database means a new implementation of `CombinationRepository`.

### SOLID in practice

- **Single responsibility:** parsing, finding, formatting, storing and HTTP handling each live in their own class.
- **Open/closed:** `CombinationFinder` is an interface, so a different algorithm (for example a trie for very large inputs) can be added without changing its callers.
- **Liskov substitution:** any `CombinationFinder` or `CombinationRepository` implementation can replace another; the tests use an in-memory fake repository.
- **Interface segregation:** each port has one small, focused interface.
- **Dependency inversion:** the core depends on interfaces (ports); the adapters implement or call them.

---

## The algorithm

For each word with the target length, a backtracking search tries every way to cut it into pieces and keeps the cuts where every piece is a word from the input:

1. Put all distinct words in a `HashSet` for constant-time lookups.
2. For each candidate word, try every prefix that exists in the set and recurse on the remainder, stopping as soon as the number of parts exceeds `maxParts`.
3. Keep only results with at least two parts (a word is not a combination of itself).

A word of length *n* has at most 2<sup>n−1</sup> ways to be cut (32 for six letters), so this simple search is fast; the full sample input is processed in well under a second. A trie or dynamic programming would only pay off for much longer target words.

---

## Design decisions

- **Words may be reused** within one combination: `foo+foo=foofoo` is valid.
- **Duplicate input words are ignored**; every combination appears once.
- **Output is sorted** by resulting word, then by parts, so the same input always gives the same output.
- **Input is split on any whitespace** (`\n`, `\r\n`, spaces, tabs) and **lowercased**, so Windows line endings and stray spaces do not matter.
- **The output ends with a newline**, so appending with `>> output.txt` keeps lines separate.
- **Results are stored on every request**, so posting the same input twice stores the rows twice.
- **The database schema is created by Hibernate** (`ddl-auto=update`). A real deployment would use versioned migrations (Flyway) instead.
- **The domain has no framework dependencies** where possible, so it can be tested with plain JUnit.

---

## Tests

```bash
./gradlew test
```

| Level | What is tested |
|---|---|
| Unit | Domain records and the finding algorithm, the service (with an in-memory fake repository), the input parser and the output formatter |
| Web | The controller with `@WebMvcTest`: output format, query parameters, status codes |
| Persistence | The persistence adapter with `@DataJpaTest` against an in-memory H2 database |
| End-to-end | The whole application with `@SpringBootTest` over real HTTP, using the sample `input.txt` and comparing with a known expected output |

The end-to-end test runs on its own in-memory database (`application-test.properties`), so running the tests never touches local data. The Docker build runs all tests, so the image only builds if they pass.

---

## What I would improve with more time

- A dedicated domain exception (for example `InvalidRulesException`) instead of mapping every `IllegalArgumentException` to `400`, so unexpected bugs are not reported as bad input.
- Flyway migrations instead of `ddl-auto=update`.
- A `GET` endpoint to read stored results, and storing each request as a batch with a timestamp.
- A streaming parser and response for very large inputs.
- A command-line adapter that reads `input.txt` and writes `output.txt` directly, as a second inbound adapter on the same use case.
- An ArchUnit test that enforces the layer rules automatically.

---

## Time spent

About **3 hours** in total, including setup, implementation, tests and this README.