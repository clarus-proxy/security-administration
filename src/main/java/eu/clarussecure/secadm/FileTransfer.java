package eu.clarussecure.secadm;

abstract class FileTransfer {
    // Interface for Transfering files.
    // Sub Classes should implement new file transfer protocols

    // Credentials for the ssh connection
    protected String scpHostName = "";
    protected String scpUserName = "";

    // Path of the "id_rsa" file containing the private key to be used for identification
    protected String scpIdentifyFilePath = "";
    // Passphrase of the "id_rsa" file
    protected String scpIdentityFilePassphrase = "";
    protected int scpPort = 22;

    public static FileTransfer getInstance(String protocol) {
        switch (protocol) {
        case "scp":
            return new SCPProtocol();
        case "sftp":
            return new SFTPProtocol();
        default:
            throw new UnsupportedOperationException("Protocol '" + protocol + "' is not supprted");
        }
    }

    public void init(String username, String host, String identityFilePath, String identityFilePassphrase) {
        this.scpHostName = host;
        this.scpUserName = username;
        this.scpIdentifyFilePath = identityFilePath;
        this.scpIdentityFilePassphrase = identityFilePassphrase;
    }

    public void setSSHPort(int port) {
        this.scpPort = port;
    }

    public abstract void tranferFile(String filename, String remotePath) throws CommandExecutionException;
}
