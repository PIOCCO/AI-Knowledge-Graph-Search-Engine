package org.example.repository;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;



public class Neo4jConnection {
    private static Neo4jConnection instance;
    private final Driver driver;

    // TODO: Replace with your Neo4j credentials
    private static final String URI = "bolt://localhost:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "11111111";

    private Neo4jConnection() {
        driver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
        System.out.println("✅ Connected to Neo4j!");
    }
    
    public static Neo4jConnection getInstance() {
        if (instance == null) {
            instance = new Neo4jConnection();
        }
        return instance;
    }

    public Session getSession() {
        return driver.session();
    }

    public Driver getDriver() {
        return driver;
    }

    public void close() {
        if (driver != null) {
            driver.close();
            System.out.println("❌ Neo4j connection closed");
        }
    }

    public boolean testConnection() {
        try (Session session = getSession()) {
            session.run("RETURN 1").consume();
            return true;
        } catch (Exception e) {
            System.err.println("❌ Neo4j connection failed: " + e.getMessage());
            return false;
        }
    }
}