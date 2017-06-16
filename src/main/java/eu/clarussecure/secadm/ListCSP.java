/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.clarussecure.secadm;

import eu.clarussecure.secadm.dao.CLARUSConfDAO;

import java.util.Set;

/**
 *
 * @author diegorivera
 */
public class ListCSP extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	
	public ListCSP(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
		// FIXME - Change the implementation to meet the refined requirements
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		Set<String> csps = dao.listCSP();
		dao.deleteInstance();

		String list = "";

		for(String csp : csps)
			list += csp + "\n";

		CommandReturn cr = new CommandReturn(0, list);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("list_csp"))
			throw new CommandParserException("Why a non-'list_csp' command ended up in the 'list_csp' part of the parser?");

		// Parse the creentials of the admin
		this.parseCredentials(args);

		return true;
	}
}
