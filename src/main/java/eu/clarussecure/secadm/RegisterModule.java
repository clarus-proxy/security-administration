package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class RegisterModule extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected String moduleFilename;
	protected String moduleVersion;

	
	public RegisterModule(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
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
        
		// FIXME - Change the implementation to meet the refined requirements
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		int module = dao.registerModule(this.moduleFilename, this.moduleVersion);
		dao.deleteInstance();

		// This command SHOULD return the Module id
		// In general, the entry in the database should contain all the data gathered and a "enabled" flag

		CommandReturn cr = new CommandReturn(0, "The Module was created with ID " + module);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("register_module"))
			throw new CommandParserException("Why a non-'register_module' command ended up in the 'register_module' part of the parser?");

		// Second, parse the filename of the module
		try{
			this.moduleFilename = args[1];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'moduleFile' was not given and it is required.");
		}

		// TODO - Extract the module version from the file!
		this.moduleVersion = "1.0";

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
