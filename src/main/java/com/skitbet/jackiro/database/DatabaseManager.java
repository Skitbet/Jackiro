package com.skitbet.jackiro.database;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class DatabaseManager {

    private MongoClient client;
    private MongoDatabase mongoDatabase;


    private static DatabaseManager instance;

    public static synchronized DatabaseManager get() {
        return instance == null ? instance = new DatabaseManager() : instance;
    }

    public void setup(String uri) {
        client = new MongoClient(new MongoClientURI(uri));
        mongoDatabase = client.getDatabase("JackiroCore");
    }

    public void shutdown() {
        client.close();
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getMongoDatabase() {
        return mongoDatabase;
    }
}
