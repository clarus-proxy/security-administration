package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

import java.util.Set;

public class ListCSP extends Command {
    // TODO - Put other data from the command as fields of the object
    // In the abstract class it is defined
    // this.loginID (String)
    // this.password (String)
    // this.identityFilePath (String)

    public ListCSP(String[] args) throws CommandParserException {
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

        // FIXME - Change the implementation to meet the refined requirements
        CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
        Set<String> csps = dao.listCSP();
        dao.deleteInstance();

        String list = "";

        for (String csp : csps)
            list += csp + "\n";

        CommandReturn cr = new CommandReturn(0, list);
        return cr;
    }

    @Override
    public boolean parseCommandArgs(String[] args) throws CommandParserException {
        // First, sanity check
        if (!args[0].toLowerCase().equals("list_csp"))
            throw new CommandParserException(
                    "Why a non-'list_csp' command ended up in the 'list_csp' part of the parser?");

        // Parse the creentials of the admin
        this.parseCredentials(args);

        return true;
    }
}
