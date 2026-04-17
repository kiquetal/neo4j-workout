# Exercises for Session 3: Intermediate Querying

These exercises are designed to test your knowledge on intermediate Cypher querying concepts such as string manipulation, list comprehension, aggregations, and subqueries. 

## Exercise 1: String Filtering & Case Sensitivity
**Scenario:** The marketing team needs a list of all movies that have the word "Love" in their title, but the database is messy. Some titles are "Love Actually", some are "Shakespeare in love", and some are "LOVELACE". 
**Task:** Write a query that finds all `Movie` nodes where the `title` contains the word "love", regardless of how it is capitalized in the database. Return the movie titles.

## Exercise 2: Working with Lists (`IN` and `ANY`)
**Scenario:** You need to find all `Person` nodes who are associated with specific countries. 
**Task:** Write a query that finds any `Person` whose `countries` property (which is a list of strings) contains either "France" or "Germany". Return the person's name and their list of countries.

## Exercise 3: Advanced Aggregation (`collect()`)
**Scenario:** You want to see the complete filmography for a specific actor.
**Task:** Write a query that finds a `Person` named 'Tom Hanks'. Find all `Movie` nodes he has `ACTED_IN`. Instead of returning multiple rows (one for each movie), use the `collect()` function to return a single row with his name and a **List** of all the movie titles he acted in.

## Exercise 4: Pattern Comprehension
**Scenario:** You want to find all Directors, but you also want a quick list of the movies they directed attached to their record, without doing a massive `MATCH` and `GROUP BY` aggregation.
**Task:** Write a query that matches all `Person` nodes who have the `Director` label. Use **Pattern Comprehension** `[ (p)-[:DIRECTED]->(m) | m.title ]` in your `RETURN` clause to return the director's name and a list of their movie titles.

## Exercise 5: Temporal Types
**Scenario:** We need to find older movies in the database.
**Task:** Write a query that finds all `Movie` nodes where the `released` property (which is a Date type) has a year before 1990. Return the movie title and the release year.

## Exercise 6: Unwinding Lists
**Scenario:** You have a hardcoded list of movie titles: `['The Matrix', 'Top Gun', 'Jerry Maguire']`. You want to find these movies in the database and see when they were released without writing multiple `MATCH` clauses or a long `WHERE title IN [...]` clause.
**Task:** Write a query that defines this list as a variable, uses `UNWIND` to turn the list into individual rows, and then matches the `Movie` nodes by their title. Return the title and the release year.

## Exercise 7: Using UNWIND to Deduplicate a List
**Scenario:** An application sent you a list of tags to add to a movie, but the list contains duplicates: `['Action', 'Sci-Fi', 'Action', 'Thriller', 'Sci-Fi']`.
**Task:** Write a query that takes this list, uses `UNWIND` to break it apart, removes the duplicates using `WITH DISTINCT`, and uses `collect()` to return a single, deduplicated list of tags.
