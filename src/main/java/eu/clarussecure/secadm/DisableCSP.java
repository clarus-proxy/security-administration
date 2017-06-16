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
public class DisableCSP extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected int cspID;
	
	public DisableCSP(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
		// This command should search the CSP on the DB and set the flag to "disabled"
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		boolean success = dao.disableCSP(this.cspID);
		dao.deleteInstance();

		int retValue = 0;
		String retMessage = "The CSP with ID " + this.cspID + " was successfully disabled.";

		if (!success){
			retValue = 3;
			retMessage = "The CSP with ID " + this.cspID + " could not be found.";
		}

		CommandReturn cr = new CommandReturn(retValue, retMessage);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("disable_csp"))
			throw new CommandParserException("Why a non-'disable_csp' command ended up in the 'disable_csp' part of the parser?");

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
