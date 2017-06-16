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
