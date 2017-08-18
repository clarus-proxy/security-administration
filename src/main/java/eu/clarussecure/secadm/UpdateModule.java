package eu.clarussecure.secadm;

import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;

public class UpdateModule extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected int moduleID;
	protected String moduleFilename;
	protected String newModuleVersion;
    
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
	
	public UpdateModule(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

    @Override
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
        
		// TODO: The version of the new module should be checked
		// Only the most recent module should be kept
        // Remove the points in the new version to correctly compare versions
        this.newModuleVersion = this.newModuleVersion.replace(".", "");

        // This command should search the Module on the DB and set the flag to "disabled"
        int success = 0;
        CommandReturn cr = null;

		int retValue = 0;
		String retMessage = "The Module with ID " + this.moduleID + " was successfully updated.";
        
        CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
        int oldVersion = dao.getModuleVersion(this.moduleID);
        int newVersion = Integer.parseInt(this.newModuleVersion);
        
        if (oldVersion < 0){
            // Error, the moduleID could not be found
			retValue = 3;
			retMessage = "The Module with ID " + this.moduleID + " could not be found.";
            cr = new CommandReturn(retValue, retMessage);
            return cr;
        }
        
        if (oldVersion < newVersion){
            success = dao.updateModule(this.moduleID, this.moduleFilename, this.newModuleVersion);
            dao.deleteInstance();
            // Create the FileTrnasfer object
            FileTransfer transfer = FileTransfer.getInstance("scp");
            // Ask for the password to the user
            System.out.print(this.scpHostName + "'s identity file password?");
            this.scpIdentityFilePassphrase = new String(System.console().readPassword());
            // Initialize the required data
            transfer.init(this.scpUserName, this.scpHostName, this.scpIdentityFilePath, this.scpIdentityFilePassphrase);
            transfer.setSSHPort(24601);
            // Transfer the file
            try{
                transfer.tranferFile(this.moduleFilename, this.scpRemotePath);
            }catch (CommandExecutionException e){
                cr = new CommandReturn(1, "There was an error transfering the file. The module has not been registered");
                e.printStackTrace(); // Delete this line when deploying
                return cr;
            }

            if (success < 0){
                retValue = 3;
                retMessage = "The Module with ID " + this.moduleID + " could not be found.";
            }

            if (success > 0){
                retValue = 4;
                retMessage = "The Module with ID " + this.moduleID + " has higher version already registered.";
            }
        } else {
			retValue = 4;
			retMessage = "The Module with ID " + this.moduleID + " has higher version already registered.";
        }

        cr = new CommandReturn(retValue, retMessage);
		return cr;
	}

    @Override
	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("update_module"))
			throw new CommandParserException("Why a non-'update_module' command ended up in the 'update_module' part of the parser?");

		// Second, parse the Module ID
		try{
			this.moduleID = Integer.parseInt(args[1]);
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'module_id' was not given and it is required.");
		} catch (NumberFormatException e){
			throw new CommandParserException("There was an error identifying the CSP ID. Is the ID number well formed?");
		}

		// Third, parse the filename of the module
		try{
			this.moduleFilename = args[2];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'moduleFile' was not given and it is required.");
		}

        this.newModuleVersion = "1.1";

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
