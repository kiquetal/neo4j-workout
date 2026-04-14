# Session 3: Intermediate Querying

## Core Concepts
*   **Filtering and Aggregation**: Advanced `WITH` and `WHERE` clauses.
*   **Pattern Comprehension**: Extracting lists of patterns directly in the `RETURN` clause.
*   **Subqueries**: Using `CALL {}` for isolated query parts and post-union processing.
*   **List Comprehension**: Transforming arrays and lists (`[x IN list WHERE condition | extraction]`).

## Advanced Filtering

Cypher provides several powerful string operators for filtering in the `WHERE` clause. These are crucial for handling real-world data where casing and formatting might not be perfect.

### String Operators: STARTS WITH, ENDS WITH, CONTAINS
These operators allow you to perform partial string matching. They are **case-sensitive**.

```cypher
// Find movies starting with 'The'
MATCH (m:Movie)
WHERE m.title STARTS WITH 'The '
RETURN m.title

// Find movies containing 'Matrix' anywhere in the title
MATCH (m:Movie)
WHERE m.title CONTAINS 'Matrix'
RETURN m.title
```

### Case-Insensitive Matching (toLower)
To perform a case-insensitive search, you must convert the property to lowercase *before* comparing it to your lowercase search string using the `toLower()` function.

```cypher
// Find movies that start with 'the', regardless of how it is capitalized in the DB
MATCH (m:Movie)
WHERE toLower(m.title) STARTS WITH 'the '
RETURN m.title
```

### Handling Lists of Strings
If a property (like `m.countries`) is a **List of Strings**, you cannot use `toLower()` directly on the entire list. You must either use list comprehension or the `ANY()` predicate to check each element individually:

```cypher
// Check if any country in the list, when converted to lowercase, starts with 'jam'
MATCH (m:Movie)
WHERE m.countries IS NOT NULL
  AND ANY(country IN m.countries WHERE toLower(country) STARTS WITH 'jam')
RETURN m.title, m.countries
```

## Working with Lists and Paths

## Subqueries

## Performance Tuning (EXPLAIN / PROFILE)

## Schema Discovery

When exploring a new database or verifying an import, it is crucial to understand the existing schema. Neo4j provides built-in procedures for this:

### `CALL db.schema.nodeTypeProperties()`
**Use Case:** This procedure returns a list of all node labels in the database, along with the properties associated with each label and the data types of those properties. It is extremely useful when you inherit a graph and need to know exactly what properties exist on a `:Person` or `:Movie` node without having to query and inspect individual nodes.

### `CALL db.schema.relTypeProperties()`
**Use Case:** Similar to the node procedure, this returns all relationship types in the database, the properties that exist on those relationships, and their data types. It helps you understand what metadata is stored on the edges (e.g., finding out that an `:ACTED_IN` relationship has a `roles` property of type List of Strings).

