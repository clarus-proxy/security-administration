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
public class Failover extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected boolean enabledFailover = false;
	protected String masterAddress = "";
	
	public Failover(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
		// This command should search the CSP on the DB and set the flag to "disabled"
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		boolean success = dao.setFailoverMode(this.enabledFailover, this.masterAddress);
		dao.deleteInstance();

		int retValue = 0;
		String retMessage = "The Failover mode was successfully " + (this.enabledFailover ? "enabled." : "disabled.");

		if (!success){
			retValue = 3;
			retMessage = "There was an error trying to " +  (this.enabledFailover ? "enable" : "disable") + " the Failover mode";
		}

		CommandReturn cr = new CommandReturn(retValue, retMessage);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("failover"))
			throw new CommandParserException("Why a non-'failover' command ended up in the 'failover' part of the parser?");

		// Second, parse the boolean (enabled or desabled?)
		try{
			String param = args[1];
			// This is the list of "keywords" that will enable the Failover mode
			// Any other key will be interpreted as "false", disabling the Failover mode
			if (param.equals("true") || param.equals("enabled") || param.equals("1") || param.equals("enable") || param.equals("on") || param.equals("yes"))
				this.enabledFailover = true;
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'param' was not given and it is required.");
		}

		// Third, parse the address of the new master node.
		// NOTE: This parameter might not be present
		try{
			if (!args[2].startsWith("-"))
				this.masterAddress = args[2];
		} catch (IndexOutOfBoundsException e){
			// Nothing to do here if the argument is not given
		}

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
