package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class UserRepo extends Command{
	//TODO - Put other data from the command as fields of the object
	protected String protocol;
	protected String credentials;
	protected String uri = "";
	
	public UserRepo(String[] args) throws CommandParserException{
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
