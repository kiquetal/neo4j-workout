package com.neo4j.workout;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jConnection implements AutoCloseable {

    private static final String DEFAULT_URI = "bolt://localhost:7687";
    private static final String DEFAULT_USER = "neo4j";
    private static final String DEFAULT_PASSWORD = "workout123";

    private final Driver driver;

    public Neo4jConnection(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public Neo4jConnection() {
        this(DEFAULT_URI, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    public Driver driver() {
        return driver;
    }

    @Override
    public void close() {
        driver.close();
    }

    public static void main(String[] args) {
        try (var conn = new Neo4jConnection()) {
            conn.driver().verifyConnectivity();
            var result = conn.driver().executableQuery("RETURN 'Neo4j is ready!' AS message")
                    .execute().records();
            System.out.println(result.get(0).get("message").asString());
        }
    }
}
