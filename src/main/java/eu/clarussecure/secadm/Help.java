package eu.clarussecure.secadm;

import java.io.IOException;
import java.io.InputStream;

public class Help extends Command{
	
	public Help(String[] args) throws CommandParserException{
		parseCommandArgs(args);
	}

	public CommandReturn execute() throws CommandExecutionException{
		// Prepare the output
		String data = "";

		try{
			// Get the embedded resource from the jar file
			InputStream f = Help.class.getClassLoader().getResource("help.txt").openStream();
			byte[] buf = new byte[(int) f.available()];
			
			// Read the content
			f.read(buf);
			data = new String(buf, "UTF-8");
		} catch (IOException e){
			data = "help not found";
		}

		CommandReturn cr = new CommandReturn(0, data);
		return cr;
	}

	public boolean parseCommandArgs(String[] args) throws CommandParserException{
		// Default case, nothing to do here

		return true;
	}
}
