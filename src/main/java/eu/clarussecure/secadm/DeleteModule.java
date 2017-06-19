/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

/**
 *
 * @author diegorivera
 */
public class DeleteModule extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	private int moduleID;
	
	public DeleteModule(String[] args) throws CommandParserException{
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
		boolean success = dao.deleteModule(this.moduleID);
		dao.deleteInstance();

		int retValue = 0;
		String retMessage = "The Module with ID " + this.moduleID + " was successfully deleted.";

		if (!success){
			retValue = 3;
			retMessage = "The Module with ID " + this.moduleID + " could not be found.";
		}

		CommandReturn cr = new CommandReturn(retValue, retMessage);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("delete_module"))
			throw new CommandParserException("Why a non-'delete_module' command ended up in the 'delete_module' part of the parser?");

		// Second, parse the Module ID
		try{
			this.moduleID = Integer.parseInt(args[1]);
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'module_id' was not given and it is required.");
		} catch (NumberFormatException e){
			throw new CommandParserException("There was an error identifying the Module ID. Is the ID number well formed?");
		}

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
