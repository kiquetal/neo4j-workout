# Neo4j Certified Professional — Exam Preparation Steering

## Project Setup

- **Language:** Java 21+
- **Build:** Maven
- **Package:** `com.neo4j.workout`
- **Key dependency:** `org.neo4j.driver:neo4j-java-driver:5.27.0`
- Practice code goes in `src/main/java/com/neo4j/workout/`
- Notes and cheat sheets go in `docs/`

## Exam Overview

- 80 multiple-choice questions in 60 minutes
- Passing score: 80% or higher
- Retake allowed after 24 hours
- Hands-on Cypher challenges using the **recommendations** dataset (People, Movies, Genres, User ratings)
- Practice dataset: demo.neo4jlabs.com (user: recommendations, password: recommendations)

## Exam Topic Areas

1. **Neo4j Graph Platform** — architecture, components, tools (Browser, Bloom, AuraDB)
2. **Graph Database Concepts** — nodes, relationships, properties, labels, traversals, ACID compliance
3. **Cypher Query Language** — MATCH, WHERE, CREATE, MERGE, DELETE, SET, REMOVE, WITH, UNWIND, OPTIONAL MATCH, aggregations, list comprehensions, pattern comprehension, subqueries, CALL procedures
4. **Graph Data Modeling** — designing graph models, refactoring, best practices, when to use nodes vs relationships vs properties
5. **Importing Data** — LOAD CSV, neo4j-admin import, APOC import procedures, data type handling, constraints for import
6. **Indexes and Constraints** — creating/dropping indexes, uniqueness constraints, existence constraints, full-text indexes, composite indexes, query performance
7. **Neo4j Drivers & Application Development** — driver architecture, sessions, transactions, connection URIs, error handling

## Hyperfocus Session Format (90 min)

Each session targets ONE topic area with this flow:

1. **Warm-up (5 min)** — 5 quick recall questions from previous sessions
2. **Concept drill (20 min)** — deep explanation + Cypher examples against recommendations dataset
3. **Code-along (25 min)** — write Java code using the Neo4j driver to exercise the topic
4. **Quiz blast (20 min)** — 20 rapid-fire multiple-choice questions, exam style
5. **Review & gap log (15 min)** — review wrong answers, record weak spots in `docs/gap-log.md`
6. **Cheat sheet (5 min)** — save key takeaways to `docs/`

### Session Docs

After each session, create a session doc at `docs/sessions/session-XX-<topic-slug>.md` containing:
- Session number, date, topic
- Key concepts covered
- Cypher examples used
- Java code snippets written
- Quiz questions and answers (with explanations for wrong answers)
- Score and weak areas identified
- Links to relevant GraphAcademy material

### Session Tracking

- Maintain `docs/session-tracker.md` with: session #, date, topic, score, weak areas
- Prioritize topics where score < 80% in the next session
- After all 7 topics are covered, run full mock exams (20 questions, mixed topics)

## Required GraphAcademy Courses (Latest)

From https://graphacademy.neo4j.com:

1. **Neo4j Fundamentals** (~1 hr)
2. **Cypher Fundamentals** (~1 hr)
3. **Graph Data Modeling Fundamentals** (~2 hrs)
4. **Importing Data Fundamentals** (~2 hrs)
5. **Intermediate Cypher Queries** (~4 hrs)
6. **Building Neo4j Applications with Java** — driver usage, sessions, transactions

## How to Help the User

- Quiz on exam topics with realistic multiple-choice questions
- Explain Cypher syntax with examples against the recommendations dataset
- Walk through graph data modeling scenarios
- Write Java driver code examples in the project structure
- Clarify differences between similar concepts (e.g., CREATE vs MERGE, index types)
- Simulate timed mini-exams (subsets of 10-20 questions)
- Track which topic areas the user struggles with and focus review there
- Always use the latest Neo4j 5.x syntax and features
- Save notes, cheat sheets, and code to the repo as study artifacts

## Response Style

- Be encouraging but honest about knowledge gaps
- Provide the correct answer with a brief explanation after each quiz question
- Use Cypher code blocks for all query examples
- Use Java code blocks for driver examples
- Relate concepts back to the recommendations dataset when possible
