# Questions for Session 2: Graph Data Modeling

## Topic: Intermediate Nodes (Reification)

Answer the following questions to test your understanding of graph data modeling and relationship reification.

**Question 1:**
What is "reification" in the context of Neo4j graph data modeling, and what does it achieve?

**Question 2:**
You are modeling a job board. Currently, your model is `(:Person)-[:APPLIED_FOR {date: '2023-10-27'}]->(:Job)`. 
The business now wants to allow recruiters to add multiple status updates (e.g., "Screening", "Interview", "Offer") and interview notes specific to each person's application. 
Explain why the current model is insufficient and how you would redesign it using an intermediate node.

**Question 3:**
In what scenario would keeping a simple relationship with properties (like `(:User)-[:RATED {score: 5}]->(:Movie)`) be preferable to creating an intermediate node (like `Review`)?

**Question 4:**
Draw (using text/ascii) the updated graph schema for a scenario where a `Student` takes a `Course`. Initially the model was a simple relationship: `(:Student)-[:ENROLLED_IN {date: '2023-01-01', final_grade: 'A'}]->(:Course)`. 
Now, the university needs to track the history of assignments, midterms, and the specific `Teacher` who graded each individual assignment for that student throughout the semester. Explain how an intermediate node solves this complexity and draw the new schema.
