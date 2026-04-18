# Neo4j Python Driver: `run` vs. `execute_query`

A common point of confusion in the Neo4j Python driver is the difference between `session.run()` and the newer `session.execute_query()` methods. This document clarifies their characteristics and use cases.

---

### `session.run(query, parameters=None, **kwparameters)`

This is the **traditional method** for executing Cypher queries and has been part of the driver for a long time.

**Key Characteristics:**

1.  **Return Value**: Returns a `Result` object, which is an **iterator**.
2.  **Evaluation**: **Lazy**. Data is fetched from the database as you iterate over the `Result` object. This is highly memory-efficient for very large result sets.
3.  **Transaction Management**: Often requires explicit, manual transaction management for write operations (e.g., using `session.begin_transaction()`).
4.  **Use Case**: Ideal for streaming or processing very large datasets, needing fine-grained control over the result stream, or working with legacy code.

**Example:**
```python
# 'session' is an active neo4j.Session object
query = "MATCH (p:Person) RETURN p.name AS Name"
result = session.run(query)

# Data is fetched from the server here, as the code loops
for record in result:
    print(record['Name'])
```

---

### `session.execute_query(query, parameters=None, ...)`

This is a **newer, high-level method** introduced in driver version 5.0+ to simplify common query patterns.

**Key Characteristics:**

1.  **Return Value**: Returns a `(records, summary, keys)` tuple.
    *   `records`: A `list` of all result records.
    *   `summary`: A `QuerySummary` object with rich metadata about the query execution (e.g., timings, counters).
    *   `keys`: A `list` of the column headers (aliases) returned by the query.
2.  **Evaluation**: **Eager**. It fetches *all* results into memory at once.
3.  **Transaction Management**: **Automatic**. The method handles transaction boundaries (commit/rollback) for you. The driver also provides `session.execute_read()` and `session.execute_write()` helpers which clearly state intent and add features like automatic retries.
4.  **Use Case**: Recommended for most new development. It is simpler, provides immediate access to query statistics, and is perfect for small-to-medium-sized results where convenience is valued over memory micro-management.

**Example:**
```python
# 'session' is an active neo4j.Session object
query = "MATCH (p:Person) RETURN p.name AS Name"
records, summary, keys = session.execute_query(query)

# All data is already in the 'records' list in memory
for record in records:
    print(record['Name'])

print(f"Query was of type: {summary.query_type}")
```

---

### Comparison Summary

| Feature             | `session.run()`                                | `session.execute_query()` (v5.0+)                                 |
| :------------------ | :--------------------------------------------- | :---------------------------------------------------------------- |
| **Return Value**    | `Result` object (iterator)                     | `(records, summary, keys)` tuple                                  |
| **Evaluation**      | Lazy (streams results)                         | Eager (loads all results into memory)                             |
| **Transaction Mgmt.** | Often manual for writes                       | Automatic (and provides `execute_read/write` helpers)             |
| **Summary Access**  | Via `result.consume()` (after iteration)       | Directly in the `summary` part of the return tuple                |
| **Best For**        | Very large results, streaming, fine-grained control | Most new development, ease of use, getting query stats easily     |
