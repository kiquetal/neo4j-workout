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

### Handling Lists of Strings (Case-Insensitive)
If a property (like `m.countries`) is a **List of Strings**, you cannot use `toLower()` directly on the entire list. You must use the `ANY()` predicate to check each element individually:

**1. Exact Match (Case-Insensitive) inside a List:**
If you want to find an exact match for "Germany", but you aren't sure if it's stored as "GERMANY", "Germany", or "germany":

```cypher
MATCH (m:Movie)
WHERE m.countries IS NOT NULL
  // Look at EVERY country in the list, make it lowercase, and see if it equals 'germany'
  AND ANY(country IN m.countries WHERE toLower(country) = 'germany')
RETURN m.title, m.countries
```

**2. Partial Match (Case-Insensitive) inside a List:**
If you want to find any country in the list that *contains* or *starts with* "ger":

```cypher
MATCH (m:Movie)
WHERE m.countries IS NOT NULL
  // Look at EVERY country in the list, make it lowercase, and see if it CONTAINS 'ger'
  AND ANY(country IN m.countries WHERE toLower(country) CONTAINS 'ger')
RETURN m.title, m.countries
```

## Working with Lists and Paths

## Sorting and Limiting Results

To find specific records, you can combine `ORDER BY` with multiple properties and `LIMIT` the results. 

**Scenario**: Find the youngest actor in the highest-rated movie.
```cypher
MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
WHERE m.imdbRating IS NOT NULL
RETURN m.title, m.imdbRating, p.name, p.born
ORDER BY m.imdbRating DESC, p.born DESC
LIMIT 10
```

## Returning Custom JSON (Map Projections)

You can construct custom JSON-like objects directly in Cypher to return structured data to applications (like Node.js). Use dictionary syntax `{ key: value }` to map node properties into custom JSON fields.

```cypher
MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
RETURN {
  actorName: p.name,
  actorAge: p.born,
  movieTitle: m.title,
  rating: m.imdbRating
} AS customResult
LIMIT 10
```

## Conditional Logic (CASE and IF)

Cypher does not have a traditional `IF` keyword. Instead, it uses the `CASE` statement for conditional logic when evaluating or returning values.

### Using CASE
`CASE` allows you to group data or change values on the fly based on conditions:

```cypher
MATCH (m:Movie)
WHERE m.imdbRating IS NOT NULL
RETURN m.title, m.imdbRating,
  CASE
    WHEN m.imdbRating > 8.5 THEN 'Masterpiece'
    WHEN m.imdbRating > 7.0 THEN 'Good'
    ELSE 'Average'
  END AS ratingCategory
LIMIT 5
```

You can also combine `CASE` seamlessly with Map projections (custom JSON):

```cypher
MATCH (p:Person)-[:ACTED_IN]->(m:Movie)
RETURN {
  actor: p.name,
  movie: m.title,
  isClassic: CASE 
               WHEN m.released < 1990 THEN true 
               ELSE false 
             END
} AS filmRecord
LIMIT 3
```

### Alternatives for "IF" (Conditional Execution)
If you need to execute different write queries (like `CREATE` or `SET`) based on a condition, you have a few options:

**1. The FOREACH Hack (IF without ELSE):**
You can iterate over a list that is either empty (false) or has one item (true).
```cypher
MATCH (p:Person {name: 'Tom Hanks'})
// If Tom's age is null, the list has 1 item and runs. If not, it's empty and skips.
FOREACH (ignoreMe IN CASE WHEN p.born IS NULL THEN [1] ELSE [] END |
  SET p.needsReview = true
)
```

**2. APOC Library (For full IF/ELSE branching):**
APOC provides procedures like `apoc.do.when` to conditionally execute full Cypher queries.
```cypher
MATCH (p:Person {name: 'Keanu Reeves'})
CALL apoc.do.when(
  p.born < 1970, 
  'SET p.generation = "Gen X" RETURN p',
  'SET p.generation = "Millennial" RETURN p',
  {p: p}
) YIELD value
RETURN value.p AS Person
```

## Subqueries

## Advanced Aggregations and Grouping

Cypher automatically groups results based on the non-aggregated variables present in your `RETURN` clause. This implicit grouping is powerful but can lead to unexpected results if you aren't careful about what you are counting.

### 1. `count(*)` vs `count(DISTINCT node)`
- `count(*)` counts the number of **paths/rows** found by the MATCH clause.
- `count(DISTINCT n)` counts the number of **unique nodes/values**.

**The Cartesian Explosion Trap:**
If you want to know how many movies an actor has been in, but your query includes directors, `count(*)` will return the wrong number if a movie has multiple directors (it counts the paths, not the movies).

**Wrong way (Counts paths):**
```cypher
MATCH (a:Person {name: 'Keanu Reeves'})-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(d:Person)
RETURN a.name AS Actor, count(*) AS totalMovies
// Will count 'The Matrix' multiple times if it has multiple directors.
```

**Right way (Counts unique movies):**
```cypher
MATCH (a:Person {name: 'Keanu Reeves'})-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(d:Person)
RETURN a.name AS Actor, count(DISTINCT m) AS totalMovies
```

### 2. Grouping by Multiple Variables
When you include multiple non-aggregated variables in `RETURN`, Cypher groups by the unique combinations of those variables.

```cypher
// How many movies has each Actor-Director duo worked on together?
MATCH (a:Person)-[:ACTED_IN]->(m:Movie)<-[:DIRECTED]-(d:Person)
RETURN a.name AS Actor, d.name AS Director, count(*) AS numMovies
```

### 3. Collecting Results into Lists (`collect()`)
Instead of just counting, you often want to see the actual items grouped together. The `collect()` function aggregates values into a List.

```cypher
// Get an actor's name, the total count of their movies, and a list of the actual titles
MATCH (a:Person)-[:ACTED_IN]->(m:Movie)
RETURN a.name AS Actor, 
       count(m) AS totalMovies, 
       collect(m.title) AS movieTitles
```

### 4. Mathematical Aggregations
Cypher supports standard math aggregations like `avg()`, `sum()`, `min()`, and `max()`.

```cypher
// Find the average rating of movies directed by Christopher Nolan
MATCH (d:Person {name: 'Christopher Nolan'})-[:DIRECTED]->(m:Movie)
WHERE m.imdbRating IS NOT NULL
RETURN d.name AS Director, 
       avg(m.imdbRating) AS averageRating,
       max(m.imdbRating) AS highestRating
```

## Working with Date and Time (Temporal Types)

Neo4j provides robust built-in temporal types for handling dates, times, and durations. 

### 1. Creating Dates
You can create dates using the `date()` function, either for the current day, from a string, or from specific components.

```cypher
// Get today's date
RETURN date() AS today

// Create a date from an ISO 8601 formatted string
RETURN date('2023-10-25') AS stringDate

// Create a date from individual components
RETURN date({year: 2023, month: 10, day: 25}) AS componentDate
```

### 2. Accessing Date Components
Once you have a temporal object, you can easily extract its parts using dot notation (e.g., `.year`, `.month`, `.day`).

```cypher
WITH date('2023-10-25') AS myDate
RETURN myDate.year AS year, myDate.month AS month, myDate.day AS day
```

### 3. Comparing Dates
Temporal types can be compared directly using standard operators (`<`, `>`, `=`).

```cypher
// Find movies released after January 1st, 2000
// (Assuming m.releaseDate is stored as a Date type)
MATCH (m:Movie)
WHERE m.releaseDate > date('2000-01-01')
RETURN m.title, m.releaseDate
```

### 4. Working with Durations
You can use the `duration()` function to add or subtract time, or find the difference between two dates using `duration.between()`.

```cypher
// Add 2 months and 5 days to today's date
RETURN date() + duration({months: 2, days: 5}) AS futureDate

// Calculate an actor's current age based on their birth date
MATCH (p:Person {name: 'Tom Hanks'})
// Assuming p.birthDate is stored as a Date
RETURN p.name, duration.between(p.birthDate, date()).years AS age
```

## Performance Tuning (EXPLAIN / PROFILE)

## Schema Discovery

When exploring a new database or verifying an import, it is crucial to understand the existing schema. Neo4j provides built-in procedures for this:

### `CALL db.schema.nodeTypeProperties()`
**Use Case:** This procedure returns a list of all node labels in the database, along with the properties associated with each label and the data types of those properties. It is extremely useful when you inherit a graph and need to know exactly what properties exist on a `:Person` or `:Movie` node without having to query and inspect individual nodes.

### `CALL db.schema.relTypeProperties()`
**Use Case:** Similar to the node procedure, this returns all relationship types in the database, the properties that exist on those relationships, and their data types. It helps you understand what metadata is stored on the edges (e.g., finding out that an `:ACTED_IN` relationship has a `roles` property of type List of Strings).

