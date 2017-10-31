package eu.clarussecure.secadm;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class SFTPProtocol extends FileTransfer {

    public SFTPProtocol() {
    }

    @Override
    public void tranferFile(String filename, String remotePath) throws CommandExecutionException {
        try {
            JSch jsch = new JSch();
            this.scpIdentityFilePassphrase = new String(System.console().readPassword());// read the password from console
            jsch.addIdentity(this.scpIdentifyFilePath, this.scpIdentityFilePassphrase);
            Session session = jsch.getSession(this.scpUserName, this.scpHostName, this.scpPort);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            Channel channel = session.openChannel("sftp");
            channel.connect();
            ((ChannelSftp) channel).cd(remotePath);
            File f = new File(filename);
            ((ChannelSftp) channel).put(new FileInputStream(f), f.getName());
            channel.disconnect();
            session.disconnect();
        } catch (SftpException | IOException | JSchException ex) {
            CommandExecutionException e = new CommandExecutionException("The SFTP Transfer could not be completed");
            e.initCause(ex);
            throw e;
        }
    }

}
