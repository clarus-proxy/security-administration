package eu.clarussecure.secadm.dao;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;
import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.model.Sorts;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.bson.Document;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CLARUSConfDAO{
	// Singleton implementation
	private static CLARUSConfDAO instance = null;
	private final MongoDatabase db;
	private final MongoClient mongoClient;
	private int instancesNumber;

    private String confFile = "/etc/clarus/clarus-mgmt-tools.conf";
    private String mongoDBHostname = "localhost"; // Default server
    private int mongoDBPort = 27017; // Default port
    private String clarusDBName = "CLARUS"; // Default DB name

	private CLARUSConfDAO(){
        // Correctly configure the log level
        Logger mongoLogger = Logger.getLogger( "org.mongodb.driver" );
        mongoLogger.setLevel(Level.SEVERE); 
        // Open the configuraiton file to extract the information from it.
        this.processConfigurationFile();
        // Create a new client connecting to "localhost" on port 
        this.mongoClient = new MongoClient(this.mongoDBHostname, this.mongoDBPort);

		// Get the database (will be created if not present)
		this.db = mongoClient.getDatabase(this.clarusDBName);

		this.instancesNumber++;
	}

	public void deleteInstance(){
		this.instancesNumber--;

		if(this.instancesNumber <= 0){
			this.mongoClient.close();
			CLARUSConfDAO.instance = null;
		}
	}

	public static CLARUSConfDAO getInstance(){
		if (CLARUSConfDAO.instance == null)
			CLARUSConfDAO.instance = new CLARUSConfDAO();
		return CLARUSConfDAO.instance;
	}
    
    public boolean registerProtocol(String protocolName, String protocolSchema){
        MongoCollection<Document> collection = db.getCollection("protocols");
        
        Document doc = new Document("protocolName", protocolName);
        doc.append("protocolSchema", protocolSchema);

		// Insert or update the document
		return collection.replaceOne(eq("key", "userrepo"), doc, new UpdateOptions().upsert(true)).wasAcknowledged();
    }
    
    public boolean deleteProtocol(String protocolName){
		MongoCollection<Document> collection = db.getCollection("protocols");

		// Find the Protocol Name to delete
		long deleted = collection.deleteOne(eq("protocolName", protocolName)).getDeletedCount();

		return deleted > 0;
    }

	public boolean setUserRepo(String proto, String cred, String uri){
		MongoCollection<Document> collection = db.getCollection("config");

		// FIXME - This implementation is provisory
		Document doc = new Document("key", "userrepo");
		doc.append("protocol", proto);
		doc.append("credentials", cred);
		doc.append("uri", uri);

		// Insert or update the document
		long modified = collection.replaceOne(eq("key", "userrepo"), doc, new UpdateOptions().upsert(true)).getModifiedCount();

		return modified > 0;
	}

	public int registerCSP(String name, String cred, String endpoint){
		MongoCollection<Document> collection = db.getCollection("config");

		// Find the highest CSP ID
		int cspID = 0;
		MongoCursor<Document> cursor = collection.find(eq("key", "csp")).sort(Sorts.descending("cspID")).limit(1).iterator();


		while(cursor.hasNext()){
			Document d = cursor.next();
			if (d == null)
				break;

			cspID = d.getInteger("cspID");
		}

		cspID++;

		// FIXME - This implementation is provisory
		Document doc = new Document("key", "csp");
		doc.append("cspID", cspID);
		doc.append("name", name);
		doc.append("credentials", cred);
		doc.append("endpoint", endpoint);
		doc.append("enabled", false);

		// Insert the document
		collection.insertOne(doc);

		return cspID;
	}

	public Set<String> listCSP(){
		Set<String> res = new HashSet<>();
		MongoCollection<Document> collection = db.getCollection("config");

		// Find all the CSPs
		MongoCursor<Document> cursor = collection.find(eq("key", "csp")).sort(Sorts.descending("cspID")).iterator();

		// Iterate the results, converting them to JSON
		while(cursor.hasNext()){
			Document d = cursor.next();
            String format= "CSP: ID = %d\n\tname = %s\n\tcredentials = %s\n\tendpoint = %s\n\tenabled = %s";
			res.add(String.format(format, d.getInteger("cspID"), d.getString("name"), d.getString("credentials"), d.getString("endpoint"), d.getBoolean("enabled").toString()));
		}

		return res;
	}

	public boolean deleteCSP(int cspID){
		MongoCollection<Document> collection = db.getCollection("config");

		// Find the CSP to delete
		long deleted = collection.deleteOne(and(eq("key", "csp"), eq("cspID", cspID))).getDeletedCount();

		return deleted > 0;
	}

	public boolean enableCSP(int cspID){
		MongoCollection<Document> collection = db.getCollection("config");

		// Update the document on the DB
		long updated = collection.updateOne(and(eq("key", "csp"), eq("cspID", cspID)), set("enabled", true)).getModifiedCount();

		return updated > 0;
	}

	public boolean disableCSP(int cspID){
		MongoCollection<Document> collection = db.getCollection("config");

		// Update the document on the DB
		long updated = collection.updateOne(and(eq("key", "csp"), eq("cspID", cspID)), set("enabled", false)).getModifiedCount();

		return updated > 0;
	}

	public boolean setFailoverMode(boolean failover, String masterNode){
		MongoCollection<Document> collection = db.getCollection("config");

		Document doc = new Document("key", "failover");
		doc.append("enabled", failover);
		doc.append("masternode", masterNode);

		// Insert or update the document
		long modified = collection.replaceOne(eq("key", "failover"), doc, new UpdateOptions().upsert(true)).getModifiedCount();

		return modified > 0;
	}

	public int registerModule(String modulePath, String verStr, String moduleName){
		MongoCollection<Document> collection = db.getCollection("config");

		// Find the highest Module ID
		int moduleID = 0;
		MongoCursor<Document> cursor = collection.find(eq("key", "clarus-module")).sort(Sorts.descending("moduleID")).limit(1).iterator();


		while(cursor.hasNext()){
			Document d = cursor.next();
			if (d == null)
				break;

			moduleID = d.getInteger("moduleID");
		}

		moduleID++;

		// Eliminate dots from the version and parse the integer. This is the comparison is easier
		int version = Integer.parseInt(verStr.replace(".", ""));

		// FIXME - This implementation is provisory
		Document doc = new Document("key", "clarus-module");
		doc.append("moduleID", moduleID);
		doc.append("file", modulePath); // Replace this with the BLOB????
		doc.append("version", version);
        doc.append("name", moduleName);
		doc.append("enabled", false);

		// Insert the document
		collection.insertOne(doc);

		return moduleID;
	}

	public Set<String> listModules(){
		Set<String> res = new HashSet<>();
		MongoCollection<Document> collection = db.getCollection("config");

		// Find all the Modules
		MongoCursor<Document> cursor = collection.find(eq("key", "clarus-module")).sort(Sorts.descending("moduleID")).iterator();

		// Iterate the results, converting them to JSON
		while(cursor.hasNext()){
			Document d = cursor.next();
            String format = "Module: ID = %d\n\tName = %s\n\tFile = %s\n\tVersion = %d\n\tEnabled = %s";
			res.add(String.format(format, d.getInteger("moduleID"), d.getString("name"), d.getString("file"), d.getInteger("version"), d.getBoolean("enabled").toString()));
		}

		return res;
	}

	public boolean deleteModule(int moduleID){
		// ToDO
		MongoCollection<Document> collection = db.getCollection("config");

		// Find the CSP to delete
		long deleted = collection.deleteOne(and(eq("key", "clarus-module"), eq("moduleID", moduleID))).getDeletedCount();

		return deleted > 0;
	}

	public int updateModule(int moduleID, String modulePath, String newVerStr){
		MongoCollection<Document> collection = db.getCollection("config");

		// TODO - Get the version of the module and update it ONLY IF the new version is greater
		int newVersion = Integer.parseInt(newVerStr.replace(".", ""));

		// Get the version from the DB
		MongoCursor<Document> cursor = collection.find(and(eq("key", "clarus-module"), eq("moduleID", moduleID))).iterator();
		int oldVersion;
		Document module = null;

		if (cursor.hasNext()){
			module = cursor.next();
		}

		if(module == null) {
			// The module was nout found, inform the user
			return -1;
		}

		oldVersion = module.getInteger("version");

		if (newVersion > oldVersion){
			// Update the document on the DB
			long updated = collection.updateOne(and(eq("key", "clarus-module"), eq("moduleID", moduleID)), combine(set("file", modulePath), set("version", newVersion))).getModifiedCount();
			return 0;
		}

		return 1;
	}
    
    public int getModuleVersion(int moduleID){
		MongoCollection<Document> collection = db.getCollection("config");

		// Find all the Modules
		MongoCursor<Document> cursor = collection.find(and(eq("key", "clarus-module"), eq("moduleID", moduleID))).iterator();
        
        Document module = null;
        
        if(cursor.hasNext()){
            module = cursor.next();
        }
        
        if(module == null){
            return -1;
        }
        
        return module.getInteger("version");
    }
	
	public boolean userAuthModule(String pathname){
		MongoCollection<Document> collection = db.getCollection("config");
		
		Document doc = new Document("key", "userAuthModuleConfig");
		doc.append("path", pathname);

		// Insert or update the document
		long modified = collection.replaceOne(eq("key", "userAuthModuleConfig"), doc, new UpdateOptions().upsert(true)).getModifiedCount();

		return modified > 0;
	}

    private void processConfigurationFile() throws RuntimeException {
        // Open the file in read-only mode. This will avoid any permission problem
        try {
            // Read all the lines and join them in a single string
            List<String> lines = Files.readAllLines(Paths.get(this.confFile));
            String content = lines.stream().reduce("", (a, b) -> a + b);

            // Use the bson document parser to extract the info
            Document doc = Document.parse(content);
            this.mongoDBHostname = doc.getString("CLARUS_metadata_db_hostname");
            this.mongoDBPort = doc.getInteger("CLARUS_metadata_db_port");
            this.clarusDBName = doc.getString("CLARUS_metadata_db_name");
        } catch (IOException e) {
            throw new RuntimeException("CLARUS configuration file could not be processed", e);
        }
    }


	/*
	// Get the collection of BSON documents that contain the configurations
	MongoCollection<Document> collection = db.getCollection("config");
	*/
}
