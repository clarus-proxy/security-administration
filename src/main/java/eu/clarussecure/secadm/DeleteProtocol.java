package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class DeleteProtocol extends Command {

    protected String protocolName;

    public DeleteProtocol(String[] command) throws CommandParserException {
        parseCommandArgs(command);
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

        CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
        boolean success = dao.deleteProtocol(this.protocolName);
        dao.deleteInstance();

        // This command SHOULD return the CSP id
        // In general, the entry in the database should contain all the data gathered and a "enabled" flag
        CommandReturn cr;
        if (success) {
            cr = new CommandReturn(0, "The Protocol '" + this.protocolName + "' was deleted from the CLARUS platform");
        } else {
            cr = new CommandReturn(1, "The Protocol could not be deleted. Is the name of the protocol correct?");
        }

        return cr;
    }

    @Override
    public boolean parseCommandArgs(String[] args) throws CommandParserException {
        // First, sanity check
        if (!args[0].toLowerCase().equals("delete_protocol"))
            throw new CommandParserException(
                    "Why a non-'delete_protocol' command ended up in the 'delete_protocol' part of the parser?");

        // Second, parse the name of the CSP
        try {
            this.protocolName = args[1];
        } catch (IndexOutOfBoundsException e) {
            throw new CommandParserException("The field 'protocolName' was not given and it is required.");
        }

        // Parse the creentials of the admin this function asks for any missing information
        this.parseCredentials(args);

        return true;
    }
}
