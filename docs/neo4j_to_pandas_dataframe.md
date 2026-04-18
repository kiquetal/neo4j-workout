# Converting Neo4j Query Results to Pandas DataFrames

When working with Neo4j and Python, you often need to analyze your graph data using familiar tools like Pandas DataFrames. Neo4j's Python drivers return query results as iterable objects containing `Record` instances, which are not directly compatible with Pandas DataFrames. This document explains how to bridge that gap.

## Prerequisites

Before you start, ensure you have the following installed:

*   **Python 3.x**
*   **`neo4j` driver**: The official Neo4j Python driver.
    ```bash
    pip install neo4j
    ```
*   **`pandas` library**: For creating and manipulating DataFrames.
    ```bash
    pip install pandas
    ```

## Understanding Neo4j Query Results

When you execute a Cypher query using the `neo4j` driver, the `session.run()` method returns a `neo4j.work.result.Result` object. You can iterate over this object, and each item returned will typically be a `neo4j.graph.Record` object.

A `Record` object behaves like a dictionary or a tuple, allowing you to access elements by key (the names defined in your Cypher `RETURN` clause) or by index.

## Example Scenario

Let's assume we have a simple graph with `Person` nodes and `KNOWS` relationships:

```cypher
// Create some sample data if you don't have it
CREATE (a:Person {name: 'Alice', age: 30})
CREATE (b:Person {name: 'Bob', age: 25})
CREATE (c:Person {name: 'Charlie', age: 35})
CREATE (a)-[:KNOWS {since: 2018}]->(b)
CREATE (b)-[:KNOWS {since: 2020}]->(c)
```

We want to retrieve all `Person` nodes and convert their properties into a Pandas DataFrame.

## Converting Node Properties to DataFrame

Here's a Python example that connects to Neo4j, runs a query to fetch `Person` nodes, and converts the results into a Pandas DataFrame.

```python
import pandas as pd
from neo4j import GraphDatabase

# --- Neo4j Connection Details ---
URI = "bolt://localhost:7687"
USERNAME = "neo4j"
PASSWORD = "your_neo4j_password" # <<< IMPORTANT: Change this to your Neo4j password

def get_person_data(uri, username, password):
    driver = None
    try:
        driver = GraphDatabase.driver(uri, auth=(username, password))
        driver.verify_connectivity()
        print("Neo4j connection established.")

        with driver.session() as session:
            query = """
            MATCH (p:Person)
            RETURN p.name AS name, p.age AS age
            ORDER BY p.name
            """
            result = session.run(query)

            # Option 1: Convert to list of dictionaries directly
            # This is often the most straightforward approach
            data = [record.data() for record in result]
            
            # Option 2: Manually extract properties (useful for complex results)
            # data = []
            # for record in result:
            #     data.append({
            #         "name": record["name"],
            #         "age": record["age"]
            #     })

            return pd.DataFrame(data)

    except Exception as e:
        print(f"Error connecting to Neo4j or executing query: {e}")
        return pd.DataFrame() # Return empty DataFrame on error
    finally:
        if driver:
            driver.close()
            print("Neo4j connection closed.")

if __name__ == "__main__":
    df_persons = get_person_data(URI, USERNAME, PASSWORD)

    if not df_persons.empty:
        print("\n--- Persons DataFrame ---")
        print(df_persons)
    else:
        print("\nNo data retrieved or an error occurred.")

```

### Explanation:

1.  **Import Libraries**: `pandas` for DataFrames and `GraphDatabase` from `neo4j` for connecting.
2.  **Connection Details**: Set your Neo4j URI, username, and password.
3.  **`get_person_data` Function**:
    *   Establishes a connection to the Neo4j database using `GraphDatabase.driver()`.
    *   Verifies connectivity.
    *   Opens a `session` to interact with the database.
    *   Defines a Cypher `query` to fetch `Person` nodes and return their `name` and `age` properties. It's crucial to explicitly `RETURN` the properties you want, often aliasing them (`AS name`, `AS age`) for clearer column names in the DataFrame.
    *   **Processing Results**:
        *   `session.run(query)` executes the query and returns a `Result` object.
        *   The most idiomatic way to convert `Record` objects to dictionaries that Pandas can understand is `record.data()`. This method converts the entire record into a Python dictionary.
        *   A list comprehension `[record.data() for record in result]` efficiently gathers all these dictionaries into a list.
        *   Finally, `pd.DataFrame(data)` creates the DataFrame from the list of dictionaries. Each dictionary becomes a row, and dictionary keys become column headers.
    *   Includes error handling and ensures the driver connection is properly closed.
4.  **`if __name__ == "__main__":` block**: Calls the function and prints the resulting DataFrame.

## Converting Relationship Properties to DataFrame

You can apply a similar pattern for relationships. Let's get the `KNOWS` relationships:

```python
import pandas as pd
from neo4j import GraphDatabase

# ... (URI, USERNAME, PASSWORD as above) ...

def get_knows_relationships(uri, username, password):
    driver = None
    try:
        driver = GraphDatabase.driver(uri, auth=(username, password))
        driver.verify_connectivity()
        print("Neo4j connection established for relationships.")

        with driver.session() as session:
            query = """
            MATCH (p1:Person)-[r:KNOWS]->(p2:Person)
            RETURN p1.name AS source_person, 
                   type(r) AS relationship_type,
                   r.since AS knows_since,
                   p2.name AS target_person
            ORDER BY source_person, target_person
            """
            result = session.run(query)
            data = [record.data() for record in result]
            return pd.DataFrame(data)

    except Exception as e:
        print(f"Error connecting to Neo4j or executing query: {e}")
        return pd.DataFrame()
    finally:
        if driver:
            driver.close()
            print("Neo4j connection closed for relationships.")

if __name__ == "__main__":
    # ... (existing person DataFrame code) ...

    df_knows = get_knows_relationships(URI, USERNAME, PASSWORD)

    if not df_knows.empty:
        print("\n--- KNOWS Relationships DataFrame ---")
        print(df_knows)
    else:
        print("\nNo relationship data retrieved or an error occurred.")

```

### Key Differences for Relationships:

*   The Cypher query now matches `(p1)-[r:KNOWS]->(p2)`.
*   We return properties from both the source node (`p1.name`), the relationship itself (`type(r)` for the type and `r.since` for its properties), and the target node (`p2.name`). This gives you a "tabular" view of your relationships.

## Converting Paths to DataFrame

Converting paths can be a bit more complex as paths are structured objects containing multiple nodes and relationships. You'll need to decide how you want to represent a path in a flat DataFrame.

One common approach is to represent each segment of the path (node-relationship-node) as a row, or to flatten the path's nodes and relationships into separate columns.

```python
import pandas as pd
from neo4j import GraphDatabase

# ... (URI, USERNAME, PASSWORD as above) ...

def get_paths_data(uri, username, password):
    driver = None
    try:
        driver = GraphDatabase.driver(uri, auth=(username, password))
        driver.verify_connectivity()
        print("Neo4j connection established for paths.")

        with driver.session() as session:
            query = """
            MATCH path = (p1:Person)-[r:KNOWS*1..2]->(p2:Person)
            RETURN nodes(path) AS path_nodes, 
                   relationships(path) AS path_relationships, 
                   length(path) AS path_length
            LIMIT 5
            """
            result = session.run(query)
            
            records_for_df = []
            for record in result:
                path_nodes = record["path_nodes"]
                path_rels = record["path_relationships"]
                path_length = record["path_length"]

                # Extract details for each segment of the path
                for i in range(path_length):
                    source_node = path_nodes[i]
                    relationship = path_rels[i]
                    target_node = path_nodes[i+1]

                    records_for_df.append({
                        "path_id": record.id, # Using record.id if available for unique path identifier
                        "segment_index": i,
                        "source_name": source_node.get("name"),
                        "source_age": source_node.get("age"),
                        "relationship_type": relationship.type,
                        "relationship_since": relationship.get("since"),
                        "target_name": target_node.get("name"),
                        "target_age": target_node.get("age"),
                        "path_length": path_length
                    })
            
            return pd.DataFrame(records_for_df)

    except Exception as e:
        print(f"Error connecting to Neo4j or executing query: {e}")
        return pd.DataFrame()
    finally:
        if driver:
            driver.close()
            print("Neo4j connection closed for paths.")

if __name__ == "__main__":
    # ... (existing person and knows DataFrame code) ...

    df_paths = get_paths_data(URI, USERNAME, PASSWORD)

    if not df_paths.empty:
        print("\n--- Paths DataFrame (Flattened) ---")
        print(df_paths)
    else:
        print("\nNo path data retrieved or an error occurred.")
```

### Key Considerations for Paths:

*   The Cypher query now returns the `path` itself, and we use `nodes(path)` and `relationships(path)` to get lists of the nodes and relationships forming that path.
*   The Python code iterates through each `record` (which represents one path).
*   Inside the loop, it further iterates through the segments of the path, extracting properties from the source node, the relationship, and the target node for each segment.
*   This approach "flattens" each path into multiple rows in the DataFrame, where each row represents a single hop (node-relationship-node). You might need to adjust this depending on how you want to represent complex paths in a DataFrame.

## Conclusion

Converting Neo4j query results to Pandas DataFrames is a straightforward process once you understand the structure of the `neo4j` driver's `Record` objects. By explicitly returning the data you need in your Cypher query and then using `record.data()` or manually extracting properties in Python, you can efficiently transform your graph data into a tabular format for further analysis. Remember to handle your Neo4j connection responsibly by closing the driver.
