package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class UserAuthModule extends Command {
    // TODO - Put other data from the command as fields of the object
    // In the abstract class it is defined
    // this.loginID (String)
    // this.password (String)
    // this.identityFilePath (String)
    protected String userAuthConfFile;

    public UserAuthModule(String[] args) throws CommandParserException {
        parseCommandArgs(args);
    }

    @Override
    public CommandReturn execute() throws CommandExecutionException {
        // Authenticate the user
        SimpleMongoUserAccess auth = SimpleMongoUserAccess.getInstance();
        if (!auth.identify(this.loginID)) {
            throw new CommandExecutionException("The user '" + this.loginID + "' was not found as a registered user.");
        }

        if (!auth.authenticate(this.loginID, this.password)) {
            throw new CommandExecutionException("The authentication of the user '" + this.loginID + "' failed.");
        }

        // Check is the user is authroized to execute this command
        if (!auth.userProfile(this.loginID).equals("admin")) {
            throw new CommandExecutionException(
                    "The user '" + this.loginID + "' is not authorized to execute this command.");
        }

        // TODO - Implement the specific behavior of the command
        CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
        dao.userAuthModule(this.userAuthConfFile);
        dao.deleteInstance();

        CommandReturn cr = new CommandReturn(0, "The configuration of the authorization module was correctly updated");
        return cr;
    }

    @Override
    public boolean parseCommandArgs(String[] args) throws CommandParserException {
        // First, sanity check
        if (!args[0].toLowerCase().equals("user_auth_module"))
            throw new CommandParserException(
                    "Why a non-'user_auth_module' command ended up in the 'user_auth_module' part of the parser?");

        // Second, parse the filename of the user file configuration
        try {
            this.userAuthConfFile = args[1];
        } catch (IndexOutOfBoundsException e) {
            throw new CommandParserException(
                    "The field 'auth_module_configuration_file' was not given and it is required.");
        }

        // Parse the creentials of the admin this function asks for any missing information
        this.parseCredentials(args);

        return true;
    }
}
