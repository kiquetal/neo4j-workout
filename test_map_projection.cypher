// Creating sample data to demonstrate Map Projections
CREATE (m:Movie {title: 'The Matrix', imdbRating: 8.7})
CREATE (k:Person {name: 'Keanu Reeves', imdbId: 'nm0000206'})
CREATE (c:Person {name: 'Carrie-Anne Moss', imdbId: 'nm0000195'})
CREATE (g1:Genre {name: 'Sci-Fi'})
CREATE (g2:Genre {name: 'Action'})

CREATE (k)-[:ACTED_IN]->(m)
CREATE (c)-[:ACTED_IN]->(m)
CREATE (m)-[:IN_GENRE]->(g1)
CREATE (m)-[:IN_GENRE]->(g2)
