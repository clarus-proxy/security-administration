package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class RegisterModule extends Command {
    // TODO - Put other data from the command as fields of the object
    // In the abstract class it is defined
    // this.loginID (String)
    // this.password (String)
    // this.identityFilePath (String)
    protected String moduleFilename;
    protected String moduleVersion;
    protected String moduleName;

    // Data of the SSH server to copy the file
    private final String scpHostName = "157.159.100.224";
    private final String scpUserName = "notts";
    private final String scpRemotePath = "~";

    // Path of the "id_rsa" file containing the private key to be used for identification
    private final String scpIdentityFilePath = "/Users/diegorivera/.ssh/id_rsa";
    // Passphrase of the "id_rsa" file
    // It can be set manually here or obtained from the console by invoking
    // this.scpIdentityFilePassphrase = new String(System.console().readPassword());
    private String scpIdentityFilePassphrase = "";

    public RegisterModule(String[] args) throws CommandParserException {
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

        int module = -1;
        CommandReturn cr = null;

        // Create the FileTrnasfer object
        FileTransfer transfer = FileTransfer.getInstance("scp");
        // Ask for the password to the user
        System.out.print(this.scpHostName + "'s identity file password?");
        this.scpIdentityFilePassphrase = new String(System.console().readPassword());
        // Initialize the required data
        transfer.init(this.scpUserName, this.scpHostName, this.scpIdentityFilePath, this.scpIdentityFilePassphrase);
        transfer.setSSHPort(24601);
        // Transfer the file
        try {
            transfer.tranferFile(this.moduleFilename, this.scpRemotePath);

            // Insert the register into the admin database.
            CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
            module = dao.registerModule(this.moduleFilename, this.moduleVersion, this.moduleName);
            dao.deleteInstance();
            // This command SHOULD return the Module id
            // In general, the entry in the database should contain all the data gathered and a "enabled" flag

            cr = new CommandReturn(0, "The Module was created with ID " + module);
        } catch (CommandExecutionException e) {
            cr = new CommandReturn(1, "There was an error transfering the file. The module has not been registered");
            e.printStackTrace(); // Delete this line when deploying
        }

        return cr;
    }

    @Override
    public boolean parseCommandArgs(String[] args) throws CommandParserException {
        // First, sanity check
        if (!args[0].toLowerCase().equals("register_module"))
            throw new CommandParserException(
                    "Why a non-'register_module' command ended up in the 'register_module' part of the parser?");

        // Second, parse the filename of the module
        try {
            this.moduleFilename = args[1];
        } catch (IndexOutOfBoundsException e) {
            throw new CommandParserException("The field 'moduleFile' was not given and it is required.");
        }

        // EXTRA ARGUMENT - The name of the module to be stored in the centralized database
        // Third, parse the name of the module
        try {
            this.moduleName = args[2];
        } catch (IndexOutOfBoundsException e) {
            throw new CommandParserException("The field 'moduleName' was not given and it is required.");
        }

        // TODO - Extract the module version from the file!
        this.moduleVersion = "1.0";

        // Parse the creentials of the admin this function asks for any missing information
        this.parseCredentials(args);

        return true;
    }
}
