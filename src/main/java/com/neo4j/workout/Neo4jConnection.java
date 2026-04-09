package com.neo4j.workout;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jConnection implements AutoCloseable {

    private final Driver driver;

    public Neo4jConnection(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public Driver driver() {
        return driver;
    }

    @Override
    public void close() {
        driver.close();
    }
}
