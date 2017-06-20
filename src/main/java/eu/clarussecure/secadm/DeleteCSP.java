package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class DeleteCSP extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected int cspID;
	
	public DeleteCSP(String[] args) throws CommandParserException{
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
		boolean success = dao.deleteCSP(this.cspID);
		dao.deleteInstance();

		int retValue = 0;
		String retMessage = "The CSP with ID " + this.cspID + " was successfully deleted.";

		if (!success){
			retValue = 3;
			retMessage = "The CSP with ID " + this.cspID + " could not be found.";
		}

		CommandReturn cr = new CommandReturn(retValue, retMessage);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("delete_csp"))
			throw new CommandParserException("Why a non-'delete_csp' command ended up in the 'delete_csp' part of the parser?");

		// Second, parse the CSP ID
		try{
			this.cspID = Integer.parseInt(args[1]);
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'csp_id' was not given and it is required.");
		} catch (NumberFormatException e){
			throw new CommandParserException("There was an error identifying the CSP ID. Is the ID number well formed?");
		}

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
