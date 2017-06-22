package eu.clarussecure.secadm;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.IOException;
import eu.clarussecure.proxy.access.SimpleMongoUserAccess;
import eu.clarussecure.secadm.dao.CLARUSConfDAO;
import java.io.File;
import java.io.FileInputStream;

public class RegisterModule extends Command{
	// TODO - Put other data from the command as fields of the object
	// In the abstract class it is defined
	// this.loginID (String)
	// this.password (String)
	// this.identityFilePath (String)
	protected String moduleFilename;
	protected String moduleVersion;
    
    // Credentials for the ssh connection
    private final String scpHostName = "";
    private final String scpUserName = "";
    private final String scpPassword = "";
    private final String scpRemotePath = "";
    private final int scpPort = 22;

	
	public RegisterModule(String[] args) throws CommandParserException{
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
        
        int module = -1;
        try{
            JSch jsch = new JSch();
            Session session  = jsch.getSession(this.scpUserName, this.scpHostName, this.scpPort);
            session.setPassword(this.scpPassword);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ((ChannelSftp) channel).cd(this.scpRemotePath);
            File f = new File(this.moduleFilename);
            ((ChannelSftp) channel).put(new FileInputStream(f), f.getName());
            channel.disconnect();
            session.disconnect();

            // Insert the register into the admin database.
            CLARUSConfDAO dao = CLARUSConfDAO.getInstance();
            module = dao.registerModule(this.moduleFilename, this.moduleVersion);
            dao.deleteInstance();
        } catch (JSchException | SftpException | IOException e){
            // TODO
            e.printStackTrace();
        }
        
        // This command SHOULD return the Module id
        // In general, the entry in the database should contain all the data gathered and a "enabled" flag
        
		CommandReturn cr = new CommandReturn(0, "The Module was created with ID " + module);
		return cr;
	}

    @Override
	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// First, sanity check
		if (!args[0].toLowerCase().equals("register_module"))
			throw new CommandParserException("Why a non-'register_module' command ended up in the 'register_module' part of the parser?");

		// Second, parse the filename of the module
		try{
			this.moduleFilename = args[1];
		} catch (IndexOutOfBoundsException e){
			throw new CommandParserException("The field 'moduleFile' was not given and it is required.");
		}

		// TODO - Extract the module version from the file!
		this.moduleVersion = "1.0";

		// Parse the creentials of the admin this function asks for any missing information
		this.parseCredentials(args);

		return true;
	}
}
