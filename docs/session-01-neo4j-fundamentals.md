# Session 1: Neo4j Fundamentals

## Graph Basics

- **Nodes**: The entities in a graph
  - Labels point to nodes (e.g., `Person`, `Product`)

- **Edges (Relationships)**: Connect nodes and determine direction
  - Can have properties

## Key Concepts
- Nodes represent objects/concepts
- Relationships connect nodes with semantic meaning
- Both can have properties (key-value pairs)## Query Languages

- **GQL**: Graph Query Language (standard)
- **Cypher**: Neo4j's implementation of GQL

## Most Used Cypher Queries

### Finding Connections (e.g., Friends of Friends)

This query helps discover indirect relationships, useful for social networks, recommendation engines, or fraud detection.

**Scenario**: Find all friends of a person named 'Alice' and their friends (excluding Alice herself).

```cypher
MATCH (p:Person)-[:FRIENDS_WITH]->(f:Person)-[:FRIENDS_WITH]->(fof:Person)
WHERE p.name = 'Alice' AND NOT (p)-[:FRIENDS_WITH]->(fof) AND p <> fof
RETURN fof.name AS FriendOfFriend
```

**Explanation**:
- `MATCH (p:Person)-[:FRIENDS_WITH]->(f:Person)-[:FRIENDS_WITH]->(fof:Person)`: This pattern matches a person `p` who is friends with `f`, and `f` is friends with `fof`. All are labeled as `Person`.
- `WHERE p.name = 'Alice'`: Filters the starting person to 'Alice'.
- `AND NOT (p)-[:FRIENDS_WITH]->(fof)`: Excludes direct friends of Alice, focusing only on friends of friends.
- `AND p <> fof`: Ensures that Alice herself is not returned as a friend of a friend.
- `RETURN fof.name AS FriendOfFriend`: Returns the name of the 'friends of friends'.

### Node Degree (`size((n)--())`)

The "degree" of a node refers to the total number of relationships connected to it (incoming or outgoing, of any type). This is a fundamental metric for understanding a node's importance or connectivity within the graph.

**Scenario**: Find the degree of each node in a simple social network.

**Graph Example (ASCII Art):**
```
(Alice)-[:FRIENDS_WITH]->(Bob)
(Alice)-[:LIKES]->(Movie1)
(Bob)<-[:KNOWS]-(Charlie)
(Bob)-[:LIKES]->(Movie2)
```

**Cypher Query:**
```cypher
MATCH (p)
RETURN p.name, size((p)--()) AS NodeDegree
```

**Expected Results for the example graph:**
| p.name  | NodeDegree |
|---------|------------|
| Alice   | 2          |
| Bob     | 3          |
| Movie1  | 1          |
| Movie2  | 1          |
| Charlie | 1          |
```

## MERGE Clause: Create or Match

The `MERGE` clause ensures that a pattern (node or relationship) exists in the graph. If the pattern is found, it acts like `MATCH`. If it's not found, it acts like `CREATE`. `MERGE` is atomic and idempotent, making it a powerful tool for managing data.

### Explanation

`MERGE` attempts to find the pattern specified.
*   If the pattern exists, `MERGE` will bind the existing graph elements to the pattern variables.
*   If the pattern does *not* exist, `MERGE` will create it.

### Use Cases

1.  **Idempotent Node Creation**: Ensuring a node exists without creating duplicates.
    ```cypher
    MERGE (p:Person {name: 'Alice'})
    RETURN p
    ```
2.  **Idempotent Relationship Creation**: Ensuring a relationship exists between two (possibly merged) nodes.
    ```cypher
    MERGE (p1:Person {name: 'Alice'})
    MERGE (p2:Person {name: 'Bob'})
    MERGE (p1)-[r:FRIENDS_WITH]->(p2)
    RETURN p1, r, p2
    ```
3.  **Complex Pattern Creation**: When parts of a pattern might exist, and others might need to be created.

### Hints and Modern Recommendations

*   **Use with Unique Constraints**: For optimal performance and ensuring data integrity, always define unique constraints on properties used with `MERGE` (e.g., `CREATE CONSTRAINT ON (p:Person) ASSERT p.name IS UNIQUE`). This allows Neo4j to quickly find nodes and guarantees uniqueness.
*   **`ON CREATE` and `ON MATCH`**: These sub-clauses allow you to set properties based on whether the `MERGE` operation created new elements or matched existing ones.
    *   `ON CREATE SET`: Sets properties *only* if the pattern was created.
    *   `ON MATCH SET`: Sets properties *only* if the pattern was matched.
    ```cypher
    MERGE (p:Person {name: 'Charlie'})
    ON CREATE SET p.created = timestamp(), p.status = 'New'
    ON MATCH SET p.lastAccessed = timestamp(), p.accessCount = coalesce(p.accessCount, 0) + 1
    RETURN p
    ```
*   **Full Pattern Matching**: `MERGE` works best when applied to as complete a pattern as possible, including labels and properties that uniquely identify the entities. Be specific.
*   **Avoid Ambiguity**: Be cautious when merging patterns that could lead to multiple matches without sufficient identifying properties, as `MERGE` will pick one arbitrarily if multiple paths fit the pattern without uniqueness constraints.
*   **Scope of `MERGE`**: `MERGE` attempts to merge the *entire* pattern specified. If you want to merge just a node and then create a relationship to an *existing* node, use separate `MERGE` and `CREATE` statements.
    ```cypher
    // Merge a person, then CREATE a relationship to an existing company
    MERGE (p:Person {email: 'dave@example.com'})
    MATCH (c:Company {name: 'Neo4j Inc.'})
    CREATE (p)-[:WORKS_FOR]->(c)
    RETURN p, c
    ```
    (Note: If `WORKS_FOR` should also be merged for idempotency, then `MERGE (p)-[:WORKS_FOR]->(c)` would be used instead of `CREATE` for the relationship).


