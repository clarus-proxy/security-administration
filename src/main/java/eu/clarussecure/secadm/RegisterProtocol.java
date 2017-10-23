package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class RegisterProtocol extends Command{
    
    protected String protocolName; // the identifier of the protocol
    protected String protocolSchema; // the network protocol schema (http, mysql, etc)
    
    public RegisterProtocol(String[] command) throws CommandParserException{
        parseCommandArgs(command);
    }

    @Override
    public CommandReturn execute() throws CommandExecutionException {
        // Authenticate the user
        SimpleMongoUserAccess auth = SimpleMongoUserAccess.getInstance();
        if(!auth.identify(this.loginID)){
            throw new CommandExecutionException("The user '" + this.loginID + "' was not found as a registered user.");
        }
        
        if(!auth.authenticate(this.loginID, this.password)){
            throw new CommandExecutionException("The authentication of the user '" + this.loginID + "' failed.");
        }
        
        // Check is the user is authroized to execute this command
        if(!auth.userProfile(this.loginID).equals("admin")){
            throw new CommandExecutionException("The user '" + this.loginID + "' is not authorized to execute this command.");
        }
        
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		boolean success = dao.registerProtocol(this.protocolName, this.protocolSchema);
		dao.deleteInstance();

		// This command SHOULD return the CSP id
		// In general, the entry in the database should contain all the data gathered and a "enabled" flag
        CommandReturn cr;
        if(success){
            cr = new CommandReturn(0, "The Protocol '" + this.protocolName + "' was created with the schema '" + this.protocolSchema + "'");
        } else {
            cr = new CommandReturn(1, "The Protocol could not be registered");
        }
        
		return cr;
    }

    @Override
    public boolean parseCommandArgs(String[] args) throws CommandParserException {
		// First, sanity check
		if (!args[0].toLowerCase().equals("register_protocol"))
			throw new CommandParserException("Why a non-'register_protocol' command ended up in the 'register_protocol' part of the parser?");

		// Second, parse the name of the CSP
		try{
			this.protocolName = args[1].toLowerCase();
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'protocolName' was not given and it is required.");
		}

		// Third, parse the credentials of the CSP
		try{
			this.protocolSchema = args[2];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'protocolSchema' was not given and it is required.");
		}

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
    }
}
