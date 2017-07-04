package eu.clarussecure.secadm;

public class CommandParser{
	// Singleton implementation.
	private static CommandParser instance = null;

	private CommandParser(){
	}

	public static CommandParser getInstance(){
		if(CommandParser.instance == null)
			CommandParser.instance = new CommandParser();

		return CommandParser.instance;
	}

	public Command parse(String[] command) throws CommandParserException{
		Command com = null;

		// This is where the delegation occurs.
		// Depending on the command provided, the parser will create the correct instance of the Command object
		// Extend this switch with new cases to support new commands
		switch(command[0].toLowerCase()){
			case "user_repo":
				com = new UserRepo(command);
				break;
			case "list_csp":
				com = new ListCSP(command);
				break;
			case "register_csp":
				com = new RegisterCSP(command);
				break;
			case "delete_csp":
				com = new DeleteCSP(command);
				break;
			case "enable_csp":
				com = new EnableCSP(command);
				break;
			case "disable_csp":
				com = new DisableCSP(command);
				break;
			case "failover":
				com = new Failover(command);
				break;
			case "register_module":
				com = new RegisterModule(command);
				break;
			case "list_modules":
				com = new ListModules(command);
				break;
			case "delete_module":
				com = new DeleteModule(command);
				break;
			case "update_module":
				com = new UpdateModule(command);
				break;
			case "user_auth_module":
				com = new UserAuthModule(command);
				break;
            // non-standard commands
            case "register_protocol":
                com = new RegisterProtocol(command);
                break;
            case "delete_protocol":
                com = new DeleteProtocol(command);
                break;
			default:
				throw new CommandParserException("Unrecognized command '" + command[0] + "'");
		}
		return com;
	}
}
