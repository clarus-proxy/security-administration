/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarussecure.secadm;

import eu.clarussecure.secadm.dao.CLARUSConfDAO;

/**
 *
 * @author diegorivera
 */

public class UserAuthModule extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected String userAuthConfFile;
	
	public UserAuthModule(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
		// TODO - Implement the specific behavior of the command
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		dao.userAuthModule(this.userAuthConfFile);
		dao.deleteInstance();

		CommandReturn cr = new CommandReturn(0, "The configuration of the authorization module was correctly updated");
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("user_auth_module"))
			throw new CommandParserException("Why a non-'user_auth_module' command ended up in the 'user_auth_module' part of the parser?");

		// Second, parse the filename of the user file configuration
		try{
			this.userAuthConfFile = args[2];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'auth_module_configuration_file' was not given and it is required.");
		}

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
