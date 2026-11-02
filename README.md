# Six Letter Words API

Finds every combination of words from a list that together form a six-letter word that is also in the list.

```
foobar
fo        →   fo+obar=foobar
obar
```

Combinations can have any number of parts (`f+oo+bar=foobar`). The target length and the maximum number of parts are configurable per request. Results are returned as plain text and stored in an H2 database.

## Quick start

Requires Docker with Docker Compose.

```bash
git clone https://github.com/mestresanna/sixletterwords.git
cd sixletterwords
docker compose -f infrastructure/docker-compose.yaml up --build
```

The first build runs the full test suite and takes a minute or two. The App is running on port 8081.

From the project root, in a second terminal:

```bash
curl --request POST 'http://localhost:8081/api/file' \
  --data-binary '@input.txt' \
  --header "Content-Type: text/plain" > output.txt
```

> Use `--data-binary`, not `--data`: `--data` strips the newlines from the file, so the words arrive glued together.

Ready-made requests are also in [`src/test/combination.http`](src/test/combination.http) (IntelliJ HTTP client).

## API

`POST /api/file`: plain-text body, one word per line. Returns one combination per line.

| Parameter | Default | Meaning |
|---|---|---|
| `targetLength` | `6` | Length of the words to build |
| `maxParts` | same as `targetLength` | Maximum number of words per combination |

```bash
# Two-word combinations only
curl -X POST 'http://localhost:8081/api/file?maxParts=2' \
  --data-binary '@input.txt' -H "Content-Type: text/plain"
```

For the sample `input.txt` this gives **915** combinations with `maxParts=2` and **4,055** without a limit (the file contains every single letter, so many words can be spelled from smaller pieces).

Returns `400` for an empty body or invalid rules (e.g. `maxParts=1`), and `415` if the body is not `text/plain`.

## Database

Open **http://localhost:8081/h2-console** and log in with:

| JDBC URL | User | Password |
|---|---|---|
| `jdbc:h2:file:/data/sixletterwords` | `sa` | `sa` |

```sql
SELECT * FROM COMBINATION WHERE RESULT = 'abroad';
```

The data is kept in a Docker volume; `docker compose -f infrastructure/docker-compose.yaml down -v` resets it.

## Running without Docker

Requires Java 21.

```bash
./gradlew bootRun   # http://localhost:8080, H2 URL jdbc:h2:file:./data/sixletterwords
./gradlew test
```

## Architecture

Hexagonal (ports and adapters): the domain is plain Java and knows nothing about HTTP or the database.

```
Controller ──► FindCombinationUseCase ──► FindCombinationService ──► SaveCombinationsPort ──► Persistence adapter ──► H2
 (adapter)        (port in)                  │  (core)                 (port out)               (adapter)
                                             ▼
                                   CombinationFinder (domain)
```

| Package | Contents |
|---|---|
| `domain` | `Combination`, `CombinationRules`, `CombinationFinder` and `BacktrackingCombinationFinder` |
| `port.in` / `port.out` | `FindCombinationUseCase`, `SaveCombinationsPort` |
| `core` | `FindCombinationService`, which connects the domain to the ports |
| `adapter.in.web` | Controller, input parser, output formatter, error handling |
| `adapter.out.persistence` | JPA entity, Spring Data repository, persistence adapter |
| `config` | Wires the framework-free domain into Spring |

Changing the maximum combination length is a request parameter, not a code change. A different input source is a new inbound adapter calling the same use case.

The algorithm uses backtracking: for each word of the target length, it tries every way to cut it into pieces that exist in the input. A six-letter word has only \(2^5 = 32\) possible ways to partition its characters, so exhaustive backtracking is sufficient without introducing a more complex data structure. 
## Design decisions

- A word may be reused in one combination (`foo+foo=foofoo`); duplicate input words are ignored.
- Output is sorted by resulting word, then by parts, so it is deterministic.
- Input is split on any whitespace and lowercased, so Windows line endings and stray spaces don't matter.
- Every request stores its results, so posting the same input twice stores it twice.
- The schema is created by Hibernate (`ddl-auto=update`); a real deployment would use Flyway.
- The H2 console allows remote connections so it works through Docker; that is for this demo only.

## Tests

`./gradlew test` runs unit tests for the domain, service, parser and formatter, a `@WebMvcTest` for the controller, a `@DataJpaTest` for persistence, and a `@SpringBootTest` end-to-end test that posts the sample `input.txt` and compares the output with a known result. The Docker build runs all of them.

## Time spent

About **3 hours**.