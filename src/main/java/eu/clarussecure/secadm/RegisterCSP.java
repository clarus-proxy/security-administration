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
public class RegisterCSP extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)

	protected String name;
	protected String credentials;
	protected String baseEndpoint;
	
	public RegisterCSP(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
		// FIXME - Change the implementation to meet the refined requirements
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		int csp = dao.registerCSP(this.name, this.credentials, this.baseEndpoint);
		dao.deleteInstance();

		// This command SHOULD return the CSP id
		// In general, the entry in the database should contain all the data gathered and a "enabled" flag

		CommandReturn cr = new CommandReturn(0, "The CSP '" + this.name + "' was created with ID " + csp);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("register_csp"))
			throw new CommandParserException("Why a non-'register_csp' command ended up in the 'register_csp' part of the parser?");

		// Second, parse the name of the CSP
		try{
			this.name = args[1];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'name' was not given and it is required.");
		}

		// Third, parse the credentials of the CSP
		try{
			this.credentials = args[2];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'CSPcredentials' was not given and it is required.");
		}

		// Fourth, parse the baseEndpoint
		try{
			this.baseEndpoint = args[3];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'base_endpoint' was not given and it is required.");
		}

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
