package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class UpdateModule extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected int moduleID;
	protected String moduleFilename;
	protected String newModuleVersion;
	
	public UpdateModule(String[] args) throws CommandParserException{
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
        
		// TODO: The version of the new module should be checked
		// Only the most recent module should be kept

		// This command should search the Module on the DB and set the flag to "disabled"
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		int success = dao.updateModule(this.moduleID, this.moduleFilename, this.newModuleVersion);
		dao.deleteInstance();

		int retValue = 0;
		String retMessage = "The Module with ID " + this.moduleID + " was successfully updated.";

		if (success < 0){
			retValue = 3;
			retMessage = "The Module with ID " + this.moduleID + " could not be found.";
		}

		if (success > 0){
			retValue = 4;
			retMessage = "The Module with ID " + this.moduleID + " has higher version already registered.";
		}

		CommandReturn cr = new CommandReturn(retValue, retMessage);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("update_module"))
			throw new CommandParserException("Why a non-'update_module' command ended up in the 'update_module' part of the parser?");

		// Second, parse the Module ID
		try{
			this.moduleID = Integer.parseInt(args[1]);
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'module_id' was not given and it is required.");
		} catch (NumberFormatException e){
			throw new CommandParserException("There was an error identifying the CSP ID. Is the ID number well formed?");
		}

		// Third, parse the filename of the module
		try{
			this.moduleFilename = args[2];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'moduleFile' was not given and it is required.");
		}


		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
