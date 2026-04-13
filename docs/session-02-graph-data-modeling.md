### Graph Data Modeling

Graph data modeling is the process of designing how your domain maps onto nodes, relationships, and properties in a graph database. Unlike relational modeling (tables, rows, foreign keys), graph modeling is driven by the **questions you need to answer** — your use cases shape the model directly.

---

## Core Building Blocks

| Element | What it represents | Example |
|---------|--------------------|---------|
| **Node** | An entity or thing | `(:Person)`, `(:Movie)` |
| **Label** | A category/type for a node, representing the dominant entities in your use cases | `:Actor`, `:Director` (a node can have multiple) |
| **Relationship** | The **verb** connecting two nodes. It is always directed and has a type. | `-[:ACTED_IN]->` |
| **Property** | Key-value pair on a node or relationship | `name: 'Tom Hanks'`, `rating: 4.5` |

---

## Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| **Label** | Starts with uppercase, CamelCase | `Person`, `MovieGenre` |
| **Relationship type** | All uppercase, underscores between words | `ACTED_IN`, `MARRIED_TO` |
| **Property key** | Starts with lowercase, camelCase | `name`, `releaseYear` |

---

## The Golden Rule: Model for Your Queries

In relational databases you normalize first, then write queries. In graph modeling you do the opposite:

1. **List your use cases** — "Which actors appeared in movies released after 2010?"
2. **Whiteboard the pattern** — `(:Person)-[:ACTED_IN]->(:Movie)`
3. **Add properties and labels** to support filtering and traversal

If a query requires many hops or aggregations, the model may need refactoring.

---

## Nodes vs. Relationships vs. Properties — When to Use What

This is one of the most tested areas on the exam.

### Use a Node when:
- The concept has its own identity and can be connected to multiple things
- You need to attach multiple properties or relationships to it
- Example: Promoting a `genre` property on `:Movie` to its own `:Genre` node so multiple movies can share it

### Use a Relationship when:
- You're expressing how two entities are connected
- The connection itself has semantic meaning (verb-like: ACTED_IN, DIRECTED, RATED)
- You need to traverse the connection in queries

### Use a Property when:
- The value is a simple attribute that belongs to one entity
- It helps to uniquely identify a node (e.g., a `productID` or `email` property)
- It's needed to answer specific questions for your use case
- It represents the data you want to return in your query results
- You don't need to traverse through it or connect it to other things
- Example: `born: 1956` on a `:Person` node

### Key exam tip:
> If you find yourself putting an array of values in a property and then searching within that array, it's a signal to **promote that property to a node**.

---

## Relationship Direction

Every relationship in Neo4j has a direction (stored), but Cypher can **ignore direction** at query time:

```cypher
// Follows stored direction
MATCH (a)-[:ACTED_IN]->(m) RETURN a, m

// Ignores direction (matches both ways)
MATCH (a)-[:ACTED_IN]-(m) RETURN a, m
```

Best practice: store direction based on the natural semantic ("Tom ACTED_IN The Matrix"), but query without direction when it doesn't matter.

---

## Labels: Best Practices

- Use labels to **categorize** nodes: `:Person`, `:Movie`, `:Genre`
- A node can have **multiple labels**: `(:Person:Actor:Director)`
- Labels are used by **indexes and constraints** — they're the primary lookup mechanism
- Avoid over-labeling; each label should serve a query or constraint purpose

---

### Labeling Best Practice: Use Orthogonal Concepts

The term **"semantically orthogonal"** means that labels on a single node should represent independent, non-overlapping concepts. Think of them as different facets of an entity's identity (its type, its role, its state), not as data values.

A common anti-pattern is to use labels to store data, such as a location.

#### Anti-Pattern: Using Labels to Store Data

Imagine you have customers and stores and you want to record their region.

```cypher
// Don't do this!
CREATE (:Customer:USA {name: 'Bob'})
CREATE (:Store:EMEA {name: 'Main St Store'})
```

**Why this is a bad model:**
- **Labels are not for data:** The region (`USA`, `EMEA`) is an attribute, a piece of data. Storing it as a label is misusing the feature.
- **Label Proliferation:** If you have 100 regions, you will create 100 labels, which pollutes the database schema and is hard to manage.
- **Inefficient Queries:** A query to find all entities in the USA (`MATCH (n:USA) RETURN n`) is inefficient because it forces the database to look at conceptually different nodes (Customers, Stores, etc.) that have nothing in common besides their location.

#### Best Practice: Use Properties or Nodes for Data

The correct way to model this is to treat the region as a property or, even better, as its own connected node.

**Option 1: Using a Property (Good)**

```cypher
CREATE (:Customer {name: 'Bob', region: 'USA'})
CREATE (:Store {name: 'Main St Store', region: 'EMEA'})
```
This is much cleaner. The region is now a simple attribute, and you can create an index on the `region` property for fast lookups.

**Option 2: Using a Connected Node (Often Best)**

This follows the "Reification" pattern mentioned earlier and is the most powerful graph-native approach.

```cypher
CREATE (:Customer {name: 'Bob'})-[:LOCATED_IN]->(:Region {name: 'USA'})
CREATE (:Store {name: 'Main St Store'})-[:LOCATED_IN]->(:Region {name: 'EMEA'})
```
This approach allows for powerful traversal queries, such as "Find all stores in the same region as customer Bob."

**Rule of Thumb:**
- A **Label** answers: "What *is* this thing?" (e.g., `:Person`, `:Product`).
- A **Property** answers: "What is an attribute *of* this thing?" (e.g., `name: 'Bob'`, `region: 'USA'`).

---

## The Recommendations Dataset Model

The practice exam uses this model. Know it well:

```
(:Person)-[:ACTED_IN]->(:Movie)
(:Person)-[:DIRECTED]->(:Movie)
(:Movie)-[:IN_GENRE]->(:Genre)
(:User)-[:RATED]->(:Movie)   // rating property on the relationship
```

Key observations:
- `Person` can be both actor and director (multiple relationship types, not separate node types)
- `RATED` carries a `rating` property on the relationship itself
- `Genre` is a separate node, not a property on Movie — enables traversal like "find all Sci-Fi movies"

---

## Refactoring a Graph Model

Refactoring means changing the model to better serve queries. Common patterns:

### 1. Property → Node (Reification)

Before: `(:Movie {genres: ['Sci-Fi', 'Action']})`
After: `(:Movie)-[:IN_GENRE]->(:Genre {name: 'Sci-Fi'})`

Why: enables traversal, shared references, and indexing on Genre.

### 2. Node → Relationship

Before: `(:Person)-[:HAS_ROLE]->(:Role)-[:IN_MOVIE]->(:Movie)`
After: `(:Person)-[:ACTED_IN {role: 'Forrest'}]->(:Movie)`

Why: simpler traversal when the intermediate node adds no value.

### 3. Relationship → Intermediate Node

Before: `(:User)-[:RATED {score: 5}]->(:Movie)`
After: `(:User)-[:WROTE]->(:Review {score: 5, text: '...'})-[:REVIEWS]->(:Movie)`

Why: when the relationship grows complex enough to warrant its own identity (e.g., reviews with text, timestamps, votes).

### 4. Adding Labels for Query Performance

Adding a `:Actor` label to Person nodes who have ACTED_IN relationships lets you write:

```cypher
MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)
```

instead of scanning all `:Person` nodes. This narrows the starting set.

---

## Common Modeling Anti-Patterns

| Anti-pattern | Problem | Fix |
|-------------|---------|-----|
| Generic relationship types (`:RELATES_TO`) | Loses semantic meaning, can't filter by type efficiently | Use specific types: `:ACTED_IN`, `:DIRECTED` |
| Too many properties on a relationship | Hard to query, may need its own identity | Promote to intermediate node |
| Using a single "God node" connected to everything | Becomes a supernode, kills traversal performance | Break into typed nodes |
| Storing lists that need to be searched | Can't index array contents efficiently | Promote list items to nodes |
| Duplicating data across nodes | Update anomalies | Extract shared data into its own node |

---

## Supernode Problem

A **supernode** is a node with an extremely high number of relationships (e.g., a `:Genre` node for "Drama" connected to 50,000 movies). Traversing through it is expensive.

Mitigation strategies:
- Add more specific labels or relationship types to narrow traversals
- Use relationship properties to filter early
- Consider fan-out nodes (bucketing)

---

## Modeling Checklist (Exam-Ready)

1. ✅ Start from use cases / access patterns
2. ✅ Entities → Nodes with labels
3. ✅ Connections → Relationships with specific types
4. ✅ Simple attributes → Properties
5. ✅ Shared or searchable values → Promote to nodes
6. ✅ Complex relationships → Consider intermediate nodes
7. ✅ Add indexes/constraints on frequently queried properties
8. ✅ Test the model against your queries — if it's awkward, refactor

---

## Cypher Examples Against Recommendations

```cypher
// Find all actors who also directed a movie they acted in
MATCH (p:Person)-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(p)
RETURN p.name, m.title

// Find genres with the most movies
MATCH (g:Genre)<-[:IN_GENRE]-(m:Movie)
RETURN g.name, count(m) AS movieCount
ORDER BY movieCount DESC

// Find users who rated a movie above 4 and what genre it belongs to
MATCH (u:User)-[r:RATED]->(m:Movie)-[:IN_GENRE]->(g:Genre)
WHERE r.rating > 4
RETURN u.name, m.title, g.name, r.rating
LIMIT 10

// Recommend movies: users who liked what I liked also liked...
MATCH (me:User {name: 'Cynthia Freeman'})-[r:RATED]->(m:Movie)<-[r2:RATED]-(other:User)
WHERE r.rating > 3 AND r2.rating > 3
MATCH (other)-[r3:RATED]->(rec:Movie)
WHERE r3.rating > 3 AND NOT (me)-[:RATED]->(rec)
RETURN rec.title, count(*) AS score
ORDER BY score DESC
LIMIT 10
```

---

## Key Takeaways for the Exam

- Graph modeling is **use-case driven**, not normalization-driven
- Know when to use a node vs. relationship vs. property — this comes up repeatedly
- Understand **reification** (promoting properties/relationships to nodes)
- The recommendations dataset is the exam playground — know its shape
- Direction is stored but can be ignored in queries
- Supernodes are a real performance concern
- Refactoring is normal and expected as requirements evolve

---

## Additional Notes

- **Fundamental Requirement:** For a relationship to exist, it *must* have a start node and an end node. It cannot exist independently.
- **Self-referencing relationships:** A node can have a relationship to itself (i.e., the start and end node can be the same). For example, a `(:Person)-[:MANAGES]->(:Person)` relationship can be used to model an organization chart.
- **Relationship properties:** Relationship properties are often used to add weight or metadata to a connection. For example, in a social network, a `[:FRIENDS_WITH {since: '2023-01-15'}]` relationship can store when the friendship started.
- **Multi-relationalships:** It is possible to have multiple relationships between the same two nodes. For example, a person can be both an `:ACTOR` and a `:DIRECTOR` in the same movie. This would be modeled as `(:Person)-[:ACTED_IN]->(:Movie)` and `(:Person)-[:DIRECTED]->(:Movie)`.
- **Schema design vs. schema-less:** While Neo4j is often described as schema-less, it's more accurate to say it has a flexible schema. You can enforce a schema with constraints and indexes, which is highly recommended for production applications.

---

## Managing Node Labels (`CREATE`, `SET`, `REMOVE`)

Labels are the core mechanism for typing or categorizing your nodes. Here's how to manage them throughout the lifecycle of your data.

### 1. Setting Labels on NEW Nodes with `CREATE`

This is the most common method. The label is assigned at the moment the node is created.

**Syntax**: `CREATE (variable:LabelName {properties})`

**Example:**
Create a new node for a person named "Alice".

```cypher
CREATE (p:Person {name: 'Alice', born: 1990})
```
- `(p:Person)`: We create a node with the label `:Person` and refer to it with the variable `p`.

**Creating a Node with Multiple Labels:**
Simply add the labels one after another, each prefixed with a colon.

```cypher
-- Create a node that is both a User and an Admin
CREATE (ua:User:Admin {username: 'charlie', lastLogin: timestamp()})
```

### 2. Adding Labels to EXISTING Nodes with `SET`

If a node already exists, you can add new labels to it. This is a common pattern for modeling changes in state (e.g., a user becomes a customer).

**Syntax**: `MATCH (variable WHERE condition) SET variable:NewLabel`

**Example:**
Alice, who already exists as a `:Person`, has now become a customer. We need to add the `:Customer` label.

```cypher
MATCH (p:Person {name: 'Alice'})
SET p:Customer
RETURN p
```
The `SET` command is additive; it will not remove the existing `:Person` label. Alice's node will now have both `:Person` and `:Customer` labels.

### 3. Removing Labels from EXISTING Nodes with `REMOVE`

The opposite of `SET` is `REMOVE`. This is used when a node no longer belongs to a certain category.

**Syntax**: `MATCH (variable WHERE condition) REMOVE variable:LabelToRemove`

**Example:**
If Alice is no longer a customer, we can remove that specific label.

```cypher
MATCH (p:Person:Customer {name: 'Alice'})
REMOVE p:Customer
```
This query finds Alice and removes *only* the `:Customer` label, leaving the `:Person` label intact.
