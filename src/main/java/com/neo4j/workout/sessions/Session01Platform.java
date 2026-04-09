package com.neo4j.workout.sessions;

import com.neo4j.workout.Neo4jConnection;

public class Session01Platform {

    public static void main(String[] args) {
        try (var conn = new Neo4jConnection()) {
            conn.driver().verifyConnectivity();
            System.out.println("=== Session 01: Neo4j Graph Platform ===");
            // session code goes here
        }
    }
}
