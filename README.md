# Neo4j Workout

Exam prep repo for the **Neo4j Certified Professional** certification.

## Exam At a Glance

- 80 multiple-choice questions, 60 minutes
- Passing score: **80%**
- Retake allowed after 24 hours
- Hands-on Cypher challenges using the **recommendations** dataset

## Exam Topics

| # | Topic | Key Areas |
|---|-------|-----------|
| 1 | Neo4j Graph Platform | Architecture, Browser, Bloom, AuraDB |
| 2 | Graph Database Concepts | Nodes, relationships, properties, labels, ACID |
| 3 | Cypher Query Language | MATCH, MERGE, aggregations, subqueries, procedures |
| 4 | Graph Data Modeling | Design, refactoring, nodes vs relationships vs properties |
| 5 | Importing Data | LOAD CSV, neo4j-admin import, APOC |
| 6 | Indexes & Constraints | Uniqueness, existence, full-text, composite, performance |
| 7 | Drivers & App Development | Sessions, transactions, connection URIs, error handling |

## Stack

- Java 21+, Maven
- `org.neo4j.driver:neo4j-java-driver:5.27.0`
- JUnit 5.11.4 (test scope)

## Project Structure

```
src/main/java/com/neo4j/workout/   # Java driver examples per topic
docs/
  session-tracker.md               # Hyperfocus session scores
  gap-log.md                       # Weak spots for review
```

## Practice Database

```
URI:      bolt://demo.neo4jlabs.com:7687
User:     recommendations
Password: recommendations
```

## Hyperfocus Session Format (90 min)

Each session targets one topic:

1. **Warm-up (5 min)** — 5 recall questions from previous sessions
2. **Concept drill (20 min)** — deep explanation + Cypher examples
3. **Code-along (25 min)** — Java driver code for the topic
4. **Quiz blast (20 min)** — 20 exam-style multiple-choice questions
5. **Review & gap log (15 min)** — review wrong answers, update `docs/gap-log.md`
6. **Cheat sheet (5 min)** — key takeaways saved to `docs/`

Each session produces a doc at `docs/sessions/session-XX-<topic>.md` with notes, code snippets, quiz results, and gaps identified.

Sessions are tracked in `docs/session-tracker.md`. Topics scoring < 80% get prioritized next. After all 7 topics, full mock exams begin.

## Recommended GraphAcademy Courses

1. [Neo4j Fundamentals](https://graphacademy.neo4j.com) (~1 hr)
2. [Cypher Fundamentals](https://graphacademy.neo4j.com) (~1 hr)
3. [Graph Data Modeling Fundamentals](https://graphacademy.neo4j.com) (~2 hrs)
4. [Importing Data Fundamentals](https://graphacademy.neo4j.com) (~2 hrs)
5. [Intermediate Cypher Queries](https://graphacademy.neo4j.com) (~4 hrs)
6. [Building Neo4j Applications with Java](https://graphacademy.neo4j.com)

## Local Neo4j (Docker)

```bash
docker compose up -d
```

- Browser: http://localhost:7474
- Bolt: `bolt://localhost:7687`
- Auth: `neo4j` / `workout123`

## Build & Run

```bash
mvn compile
```
