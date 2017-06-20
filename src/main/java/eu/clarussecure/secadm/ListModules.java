package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

import java.util.Set;

public class ListModules extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	
	public ListModules(String[] args) throws CommandParserException{
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
		Set<String> csps = dao.listModules();
		dao.deleteInstance();

		String list = "";

		for(String csp : csps)
			list += csp + "\n";

		CommandReturn cr = new CommandReturn(0, list);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("list_modules"))
			throw new CommandParserException("Why a non-'list_modules' command ended up in the 'list_modules' part of the parser?");

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
