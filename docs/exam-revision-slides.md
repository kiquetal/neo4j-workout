# Neo4j Certified Professional — Revision Slides

> 80 questions · 60 minutes · 80% to pass
> Navigate slide by slide to refresh each topic before the exam.

---

## TOPIC 1: Neo4j Graph Platform

---

### Slide 1.1 — Core Components

- **Neo4j DBMS**: Graph database engine (Community & Enterprise editions)
- **Cypher**: Declarative query language (GQL-based)
- **Neo4j Browser**: Web UI at port 7474 for running Cypher queries
- **Neo4j Bloom**: Business-friendly visual graph exploration (no Cypher needed)
- **Neo4j AuraDB**: Fully managed cloud database (DBaaS)
- **Neo4j Desktop**: Local development environment, manages multiple DBs

---

### Slide 1.2 — Architecture

- **Bolt Protocol**: Binary protocol for client-driver communication (port 7687)
- **Causal Clustering**: Leader/follower replication for Enterprise HA
  - Leader handles writes, followers handle reads
  - Causal consistency via bookmarks
- **Embedded vs Server mode**: Embedded runs in-process (Java only), Server runs standalone

---

### Slide 1.3 — Tools & Ecosystem

- **APOC**: Standard library of 450+ procedures/functions
- **Graph Data Science (GDS)**: Library for graph algorithms (PageRank, community detection, etc.)
- **neo4j-admin**: CLI tool for backup, restore, import, migration
- **Neo4j Ops Manager (NOM)**: Monitoring & management for Enterprise

---

### Slide 1.4 — Key Ports & Protocols

| Port | Protocol | Purpose |
|------|----------|---------|
| 7474 | HTTP | Browser UI & REST API |
| 7473 | HTTPS | Secure Browser/REST |
| 7687 | Bolt | Driver connections |

---

## TOPIC 2: Graph Database Concepts

---

### Slide 2.1 — Building Blocks

- **Nodes**: Entities (circles). Have zero or more labels.
- **Relationships**: Connections between nodes. Always have a type and direction.
- **Properties**: Key-value pairs on nodes or relationships.
- **Labels**: Tags on nodes for grouping (e.g., `:Person`, `:Movie`).

```
(:Person {name: 'Tom'})-[:ACTED_IN {roles: ['Forrest']}]->(:Movie {title: 'Forrest Gump'})
```

---

### Slide 2.2 — ACID Compliance

Neo4j is fully ACID-compliant:
- **Atomicity**: Transactions are all-or-nothing
- **Consistency**: DB moves from one valid state to another
- **Isolation**: Concurrent transactions don't interfere
- **Durability**: Committed data survives crashes

---

### Slide 2.3 — Traversal & Index-Free Adjacency

- Neo4j uses **index-free adjacency**: each node directly references its neighbors
- Traversal cost is proportional to the subgraph touched, NOT the total graph size
- This is what makes graph DBs fast for connected queries vs. JOINs in RDBMS

---

### Slide 2.4 — Graph vs Relational

| Concept | Relational | Graph |
|---------|-----------|-------|
| Entity | Row in table | Node |
| Relationship | Foreign key + JOIN | First-class relationship |
| Schema | Fixed columns | Flexible properties |
| Traversal | Expensive JOINs | Cheap pointer chasing |

---

## TOPIC 3: Cypher Query Language

---

### Slide 3.1 — Reading Data

```cypher
// Basic pattern match
MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
WHERE p.name = 'Tom Hanks'
RETURN m.title, m.released

// Optional match (returns null if no match)
OPTIONAL MATCH (m)<-[:DIRECTED]-(d:Person)
RETURN m.title, d.name
```

---

### Slide 3.2 — Writing Data

```cypher
// CREATE — always creates new
CREATE (p:Person {name: 'Alice'})

// MERGE — find or create (idempotent)
MERGE (p:Person {name: 'Alice'})
ON CREATE SET p.created = timestamp()
ON MATCH SET p.lastSeen = timestamp()

// SET — update properties
MATCH (p:Person {name: 'Alice'})
SET p.age = 30, p:Employee

// REMOVE — remove properties/labels
MATCH (p:Person {name: 'Alice'})
REMOVE p.age, p:Employee
```

---

### Slide 3.3 — DELETE vs DETACH DELETE

```cypher
// Delete a relationship
MATCH (a)-[r:KNOWS]->(b)
DELETE r

// Delete a node (must have no relationships!)
MATCH (n:Orphan)
DELETE n

// Delete node + all its relationships
MATCH (p:Person {name: 'Alice'})
DETACH DELETE p
```

⚠️ You CANNOT delete a node that still has relationships — use `DETACH DELETE`.

---

### Slide 3.4 — Aggregations

```cypher
// count(*) = rows/paths, count(DISTINCT x) = unique values
MATCH (a:Person)-[:ACTED_IN]->(m:Movie)
RETURN a.name, count(DISTINCT m) AS movies

// collect() — aggregate into a list
RETURN a.name, collect(m.title) AS filmography

// Math aggregations
RETURN avg(m.imdbRating), min(m.released), max(m.revenue)
```

Grouping is implicit: non-aggregated columns become the GROUP BY.

---

### Slide 3.5 — WITH Clause (Piping)

`WITH` passes results between query parts. Variables not listed are dropped.

```cypher
MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
WITH p, count(m) AS movieCount
WHERE movieCount > 5
RETURN p.name, movieCount
ORDER BY movieCount DESC
```

---

### Slide 3.6 — UNWIND

Turns a list into rows (opposite of `collect()`).

```cypher
// Iterate over a list
WITH ['The Matrix', 'Top Gun'] AS titles
UNWIND titles AS title
MATCH (m:Movie {title: title})
RETURN m.title, m.released

// Deduplicate a list
WITH ['A', 'B', 'A'] AS items
UNWIND items AS item
WITH DISTINCT item
RETURN collect(item) AS unique
```

---

### Slide 3.7 — UNION & Subqueries

```cypher
// UNION ALL (keeps duplicates, faster)
MATCH (a)-[:ACTED_IN]->(m) RETURN a.name AS name, 'Actor' AS role
UNION ALL
MATCH (d)-[:DIRECTED]->(m) RETURN d.name AS name, 'Director' AS role

// CALL {} subquery — isolates scope
CALL {
  MATCH (m:Movie) WHERE m.year = $year
  RETURN m
}
RETURN m.title
```

Rules: UNION queries must return same column names. Use `CALL {}` to post-process UNION results.

---

### Slide 3.8 — String Filtering

```cypher
// Case-sensitive
WHERE m.title STARTS WITH 'The'
WHERE m.title CONTAINS 'Matrix'
WHERE m.title ENDS WITH 'Returns'

// Case-insensitive
WHERE toLower(m.title) CONTAINS 'matrix'

// Inside a list property
WHERE ANY(c IN m.countries WHERE toLower(c) = 'germany')
```

---

### Slide 3.9 — CASE Expression

```cypher
RETURN m.title,
  CASE
    WHEN m.imdbRating > 8.5 THEN 'Masterpiece'
    WHEN m.imdbRating > 7.0 THEN 'Good'
    ELSE 'Average'
  END AS tier
```

No `IF` keyword in Cypher — use `CASE` for conditional logic.

---

### Slide 3.10 — Parameters (Best Practice)

```cypher
// Always parameterize — never hardcode values
MATCH (p:Person {name: $actorName})-[:ACTED_IN]->(m)
RETURN m.title
```

Why: execution plan caching, Cypher injection prevention, cleaner code.

In Neo4j Browser: `:param actorName => 'Tom Hanks'`

---

### Slide 3.11 — Map Projections & Pattern Comprehension

```cypher
// Map projection — custom JSON shape
MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
RETURN p { .name, movies: collect(m.title) }

// Pattern comprehension — inline list from pattern
MATCH (p:Person)
RETURN p.name, [(p)-[:DIRECTED]->(m) | m.title] AS directed
```

---

### Slide 3.12 — Dates & Durations

```cypher
RETURN date()                          // today
RETURN date('2023-10-25')              // from string
RETURN date().year                     // extract component
RETURN date() + duration({months: 2})  // date math
RETURN duration.between(d1, d2).years  // difference
```

---

### Slide 3.13 — Useful Functions

| Function | Purpose |
|----------|---------|
| `coalesce(a, b)` | First non-null value |
| `size(list)` | Length of a list |
| `length(path)` | Number of relationships in a path |
| `keys(node)` | List of property keys |
| `properties(n)` | Map of all properties |
| `type(r)` | Relationship type as string |
| `labels(n)` | List of node labels |
| `id(n)` | Internal node ID (avoid relying on this) |
| `toInteger()`, `toFloat()`, `toString()` | Type conversions |

---

## TOPIC 4: Graph Data Modeling

---

### Slide 4.1 — Design Principles

1. Model around your **queries** (use cases first, model second)
2. Nodes for **nouns** (entities with identity)
3. Relationships for **verbs** (actions, connections)
4. Properties for **attributes** (simple values)
5. Labels for **roles/categories**

---

### Slide 4.2 — When to Use What

| Use a Node when... | Use a Relationship when... | Use a Property when... |
|---------------------|---------------------------|----------------------|
| It has its own identity | It connects two entities | It's a simple attribute |
| Other things connect to it | It represents an action/verb | You don't query by it often |
| It needs multiple relationships | Direction matters | It's a primitive value |

---

### Slide 4.3 — Reification (Relationship → Node)

Turn a relationship into an intermediate node when:
- The connection needs its own properties
- Other nodes need to connect to the connection
- You need a history of events

```
BEFORE: (:User)-[:RATED {score: 5}]->(:Movie)

AFTER:  (:User)-[:WROTE]->(:Review {score: 5, text: '...'})-[:REVIEWS]->(:Movie)
                                      ^
                              (:User)-[:LIKED]
```

---

### Slide 4.4 — Common Anti-Patterns

- ❌ Storing connected data as comma-separated strings in properties
- ❌ Using generic relationship types like `:RELATES_TO`
- ❌ Creating "god nodes" connected to millions of other nodes
- ❌ Modeling everything as nodes when a property would suffice
- ✅ Use specific relationship types: `:ACTED_IN`, `:DIRECTED`, `:RATED`

---

### Slide 4.5 — Refactoring Patterns

- **Extract node from property**: Turn `m.genre = 'Sci-Fi'` into `(m)-[:IN_GENRE]->(g:Genre {name: 'Sci-Fi'})`
- **Qualify relationships**: Split `:ACTED_IN` into `:ACTED_IN_2023`, `:ACTED_IN_2024` for time-based filtering
- **Merge duplicate nodes**: Use `MERGE` + unique constraints to deduplicate

---

## TOPIC 5: Importing Data

---

### Slide 5.1 — Import Methods Overview

| Method | Best For | Speed | Notes |
|--------|----------|-------|-------|
| `LOAD CSV` | Small-medium CSV files | Medium | Runs as Cypher, transactional |
| `neo4j-admin import` | Initial bulk load | Fastest | DB must be empty/offline |
| APOC procedures | JSON, JDBC, XML, APIs | Varies | Flexible, many formats |

---

### Slide 5.2 — LOAD CSV

```cypher
// With headers
LOAD CSV WITH HEADERS FROM 'file:///movies.csv' AS row
MERGE (m:Movie {id: toInteger(row.movieId)})
SET m.title = row.title, m.year = toInteger(row.year)

// Without headers (access by index)
LOAD CSV FROM 'file:///data.csv' AS row
RETURN row[0], row[1]
```

Key points:
- All values come in as **strings** — cast with `toInteger()`, `toFloat()`, etc.
- Use `MERGE` (not `CREATE`) to avoid duplicates
- Create constraints BEFORE importing for performance
- Use `USING PERIODIC COMMIT 1000` for large files (pre-5.x) or `CALL {} IN TRANSACTIONS` (5.x)

---

### Slide 5.3 — neo4j-admin import

```bash
neo4j-admin database import full \
  --nodes=Person=persons.csv \
  --nodes=Movie=movies.csv \
  --relationships=ACTED_IN=acted_in.csv \
  neo4j
```

- Requires specific CSV header format (`:ID`, `:LABEL`, `:TYPE`, `:START_ID`, `:END_ID`)
- Database must be **stopped** or **empty**
- Fastest method — bypasses transaction layer
- No Cypher — purely structural

---

### Slide 5.4 — APOC Import

```cypher
// JSON import
CALL apoc.load.json('https://api.example.com/data') YIELD value
MERGE (p:Person {id: value.id})
SET p.name = value.name

// JDBC (relational DB)
CALL apoc.load.jdbc('jdbc:mysql://...', 'SELECT * FROM users') YIELD row
MERGE (u:User {id: row.id})
```

---

### Slide 5.5 — Import Best Practices

1. **Create uniqueness constraints first** — speeds up MERGE lookups
2. **Import nodes before relationships**
3. **Batch large imports** with `CALL {} IN TRANSACTIONS OF 1000 ROWS`
4. **Cast data types** — CSV values are always strings
5. **Handle nulls** — use `CASE` or `coalesce()` for missing values

---

## TOPIC 6: Indexes & Constraints

---

### Slide 6.1 — Constraint Types

```cypher
// Uniqueness constraint (also creates an index)
CREATE CONSTRAINT person_name_unique
FOR (p:Person) REQUIRE p.name IS UNIQUE

// Node property existence (Enterprise only)
CREATE CONSTRAINT person_name_exists
FOR (p:Person) REQUIRE p.name IS NOT NULL

// Relationship property existence (Enterprise only)
CREATE CONSTRAINT acted_in_roles_exists
FOR ()-[r:ACTED_IN]-() REQUIRE r.roles IS NOT NULL

// Node key (unique + exists, Enterprise only)
CREATE CONSTRAINT person_key
FOR (p:Person) REQUIRE (p.name, p.born) IS NODE KEY
```

---

### Slide 6.2 — Index Types

```cypher
// Range index (default, general purpose)
CREATE INDEX person_name_idx FOR (p:Person) ON (p.name)

// Composite index (multiple properties)
CREATE INDEX movie_composite FOR (m:Movie) ON (m.title, m.year)

// Text index (optimized for string CONTAINS/ENDS WITH)
CREATE TEXT INDEX person_name_text FOR (p:Person) ON (p.name)

// Full-text index (Lucene-based, supports fuzzy search)
CREATE FULLTEXT INDEX movie_search
FOR (m:Movie) ON EACH [m.title, m.plot]

// Point index (spatial queries)
CREATE POINT INDEX location_idx FOR (p:Place) ON (p.location)
```

---

### Slide 6.3 — When Indexes Are Used

| Query Pattern | Index Type Used |
|--------------|----------------|
| `WHERE n.prop = value` | Range index |
| `WHERE n.prop > value` | Range index |
| `WHERE n.prop STARTS WITH 'x'` | Range index |
| `WHERE n.prop CONTAINS 'x'` | Text index |
| `WHERE n.prop ENDS WITH 'x'` | Text index |
| Full-text search | Full-text index |
| `MERGE (n:Label {prop: val})` | Uniqueness constraint's index |

---

### Slide 6.4 — Managing Indexes & Constraints

```cypher
// List all
SHOW INDEXES
SHOW CONSTRAINTS

// Drop
DROP INDEX index_name
DROP CONSTRAINT constraint_name
```

⚠️ A uniqueness constraint automatically creates a range index — no need to create both.

---

### Slide 6.5 — EXPLAIN & PROFILE

```cypher
// EXPLAIN — shows query plan WITHOUT executing
EXPLAIN MATCH (p:Person {name: 'Tom'}) RETURN p

// PROFILE — executes and shows actual rows/db hits
PROFILE MATCH (p:Person {name: 'Tom'}) RETURN p
```

Look for:
- **NodeIndexSeek** ✅ (good — using an index)
- **AllNodesScan** ❌ (bad — scanning everything)
- **db hits** — lower is better

---

## TOPIC 7: Drivers & Application Development

---

### Slide 7.1 — Driver Architecture

```
Application → Driver → Bolt Protocol → Neo4j Server
```

- Drivers are **official** for Java, Python, JavaScript, .NET, Go
- Connection via **Bolt URI**: `bolt://`, `neo4j://` (routing), `bolt+s://` (encrypted)
- `neo4j://` enables **routing** for clusters (recommended for production)

---

### Slide 7.2 — Java Driver Basics

```java
var driver = GraphDatabase.driver(
    "bolt://localhost:7687",
    AuthTokens.basic("neo4j", "password")
);

try (var session = driver.session()) {
    var result = session.run(
        "MATCH (p:Person {name: $name}) RETURN p.name",
        Map.of("name", "Tom Hanks")
    );
    while (result.hasNext()) {
        System.out.println(result.next().get("p.name").asString());
    }
}

driver.close();
```

---

### Slide 7.3 — Sessions & Transactions

| Concept | Description |
|---------|-------------|
| **Driver** | Thread-safe, one per app, manages connection pool |
| **Session** | Lightweight, not thread-safe, borrows connections |
| **Auto-commit tx** | `session.run()` — single query, auto-committed |
| **Managed tx** | `session.executeRead/Write()` — retried on transient errors |
| **Unmanaged tx** | `session.beginTransaction()` — manual commit/rollback |

---

### Slide 7.4 — Managed Transactions (Recommended)

```java
// Read transaction (routed to followers in a cluster)
var name = session.executeRead(tx -> {
    var result = tx.run("MATCH (p:Person {name: $name}) RETURN p",
                        Map.of("name", "Tom Hanks"));
    return result.single().get("p").asNode().get("name").asString();
});

// Write transaction (routed to leader)
session.executeWrite(tx -> {
    tx.run("MERGE (p:Person {name: $name})", Map.of("name", "Alice"));
    return null;
});
```

- Automatically retried on transient errors (deadlocks, leader changes)
- Use `executeRead` for reads, `executeWrite` for writes

---

### Slide 7.5 — Connection URIs

| URI Scheme | Encryption | Routing |
|-----------|-----------|---------|
| `bolt://` | No | No (single server) |
| `bolt+s://` | Yes (TLS) | No |
| `bolt+ssc://` | Yes (self-signed) | No |
| `neo4j://` | No | Yes (cluster) |
| `neo4j+s://` | Yes (TLS) | Yes |
| `neo4j+ssc://` | Yes (self-signed) | Yes |

---

### Slide 7.6 — Error Handling

- **Transient errors** (e.g., `Neo.TransientError.*`): Retry automatically with managed transactions
- **Client errors** (e.g., `Neo.ClientError.Statement.SyntaxError`): Fix the query
- **Database errors**: Check server logs
- Always **close** drivers and sessions (use try-with-resources in Java)

---

### Slide 7.7 — Bookmarks (Causal Consistency)

```java
// Session 1 writes data
Bookmark bookmark;
try (var s1 = driver.session()) {
    s1.executeWrite(tx -> tx.run("CREATE (:Person {name: 'New'})"));
    bookmark = s1.lastBookmark();
}

// Session 2 reads with bookmark — guaranteed to see the write
try (var s2 = driver.session(SessionConfig.builder()
        .withBookmarks(bookmark).build())) {
    s2.executeRead(tx -> tx.run("MATCH (p:Person {name: 'New'}) RETURN p"));
}
```

---

## QUICK REFERENCE — Exam Cheat Sheet

---

### Must-Know Cypher Clauses

| Clause | Purpose |
|--------|---------|
| `MATCH` | Find patterns |
| `WHERE` | Filter results |
| `RETURN` | Output results |
| `CREATE` | Create nodes/relationships |
| `MERGE` | Find or create (idempotent) |
| `SET` | Update properties, add labels |
| `REMOVE` | Remove properties/labels |
| `DELETE` | Delete nodes/relationships |
| `DETACH DELETE` | Delete node + all its relationships |
| `WITH` | Pipe results between query parts |
| `UNWIND` | List → rows |
| `ORDER BY` | Sort results |
| `LIMIT` / `SKIP` | Pagination |
| `CALL {}` | Subquery |
| `UNION` / `UNION ALL` | Combine query results |
| `FOREACH` | Iterate and mutate |
| `LOAD CSV` | Import CSV data |

---

### Must-Know Functions

| Function | Returns |
|----------|---------|
| `count()` | Number of rows |
| `collect()` | Aggregated list |
| `avg()`, `sum()`, `min()`, `max()` | Math aggregations |
| `size()` | List length |
| `length()` | Path length |
| `type(r)` | Relationship type |
| `labels(n)` | Node labels |
| `keys(n)` | Property keys |
| `coalesce(a, b)` | First non-null |
| `toLower()`, `toUpper()`, `trim()` | String functions |
| `toInteger()`, `toFloat()`, `toString()` | Type casting |
| `date()`, `datetime()`, `duration()` | Temporal |
| `exists()` | Property existence check |
| `properties(n)` | All properties as map |

---

### Common Exam Traps ⚠️

1. `DELETE` on a node with relationships → **ERROR**. Use `DETACH DELETE`.
2. `MERGE` on a full pattern creates the ENTIRE pattern if not found — not just missing parts.
3. `LOAD CSV` values are always **strings** — must cast explicitly.
4. `count(*)` counts paths/rows, `count(DISTINCT n)` counts unique nodes.
5. `UNION` requires identical column names in all queries.
6. Uniqueness constraint = automatic index. Don't create a separate one.
7. `neo4j://` = routing (cluster), `bolt://` = single server.
8. `executeRead()` routes to followers, `executeWrite()` routes to leader.
9. `REMOVE` removes properties/labels. `DELETE` removes nodes/relationships.
10. `ON CREATE SET` / `ON MATCH SET` only work with `MERGE`.

---

### Schema Discovery

```cypher
CALL db.schema.visualization()        -- visual schema
CALL db.schema.nodeTypeProperties()    -- node labels + properties
CALL db.schema.relTypeProperties()     -- relationship types + properties
SHOW INDEXES
SHOW CONSTRAINTS
```

---

**Good luck on the exam! 🎯**
