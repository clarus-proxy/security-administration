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
public class UserRepo extends Command{
	//TODO - Put other data from the command as fields of the object
	protected String protocol;
	protected String credentials;
	protected String uri = "";
	
	public UserRepo(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
		// FIXME - Change the implementation to meet the refined requirements
		CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
		dao.setUserRepo(this.protocol, this.credentials, this.uri);
		dao.deleteInstance();

		CommandReturn cr = new CommandReturn(0, "The User Repository data was sucessfully updated.");
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("user_repo"))
			throw new CommandParserException("Why a non-'user_repo' command ended up in the 'user_repo' part of the parser?");

		// Second, parse the protocol
		try{
			this.protocol = args[1];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'protocol' was not given and it is required.");
		}

		try{
			this.credentials = args[2];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'credentials' was not given and it is required.");
		}

		if (this.protocol.toLowerCase().equals("ldap")){
			try{
				this.uri = args[3];
			} catch (IndexOutOfBoundsException e){
				throw new CommandParserException("The field 'uri' was not given and it is required when the protocol is 'ldap'.");
			}
		}

		// Parse the creentials of the admin
		this.parseCredentials(args);

		return true;
	}
}
