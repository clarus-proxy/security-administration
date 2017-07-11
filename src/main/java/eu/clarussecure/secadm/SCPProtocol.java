package eu.clarussecure.secadm;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SCPProtocol extends FileTransfer {
    
    public SCPProtocol(){
    }

    @Override
    public void tranferFile(String filename, String remotePath) throws CommandExecutionException {
        try{
            JSch jsch = new JSch();
            jsch.addIdentity(this.scpIdentifyFilePath, this.scpIdentityFilePassphrase);
            Session session  = jsch.getSession(this.scpUserName, this.scpHostName, this.scpPort);
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            this.copyLocalToRemote(session, filename, remotePath);
        } catch (IOException | JSchException e){
            CommandExecutionException ex = new CommandExecutionException("The SCP Transfer could not be completed");
            ex.initCause(e);
            throw ex;
        }
    }
    
    // These two functions were extracted from the JSch examples.
    // http://www.jcraft.com/jsch/examples/ScpTo.java.html
    // https://medium.com/@ldclakmal/scp-with-java-b7b7dbcdbc85
    private void copyLocalToRemote(Session session, String from, String to) throws JSchException, IOException {
        // This function implements the SSH protocol of SCP
        // Execute 'scp -t rfile' remotely
        // This is what SCP does internally
        System.out.println("File is being transfered. Please wait...");
        String command = "scp -p -t " + to;
        Channel channel = session.openChannel("exec");
        ((ChannelExec) channel).setCommand(command);

        // Get I/O streams for remote scp
        OutputStream out = channel.getOutputStream();
        InputStream in = channel.getInputStream();

        channel.connect();

        if (checkAck(in) != 0) {
            throw new IOException("The initial connect has failed");
        }

        // Send the datastamps of the file
        File _lfile = new File(from);
        command = "T" + (_lfile.lastModified() / 1000) + " 0"; // last modified
        // The access time should be sent here,
        // but it is not accessible with JavaAPI ;-<
        command += (" " + (_lfile.lastModified() / 1000) + " 0\n"); // last accessed
        // Write the command
        out.write(command.getBytes());
        out.flush();
        if (checkAck(in) != 0) {
            throw new IOException("The file datastamps could not be sent");
        }

        // Send "C0644 filesize filename", where filename should not include '/'
        long filesize = _lfile.length();
        // C0644... maybe these are the remote permisions of the file?????
        command = "C0644 " + filesize + " ";
        // Extract the filename as the substring from the last ocurrence of "/" (or "\" if Windows) +1
        if (from.lastIndexOf(File.separator) > 0) {           
            command += from.substring(from.lastIndexOf(File.separator) + 1);
        } else {
            command += from;
        }
        command += "\n";
        out.write(command.getBytes());
        out.flush();

        if (checkAck(in) != 0) {
            throw new IOException("The data permissions (?), file size and file name could not be sent.");
        }

        // Send a content of lfile in blocks of 1024 bytes
        FileInputStream fis = new FileInputStream(from);
        byte[] buf = new byte[1024];
        while (true) {
            int len = fis.read(buf, 0, buf.length);
            if (len <= 0){
                break;
            }
            out.write(buf, 0, len); //out.flush();
        }

        // Send '\0' to finish the transfer
        buf[0] = 0;
        out.write(buf, 0, 1);
        out.flush();

        if (checkAck(in) != 0) {
            throw new IOException("The file content could not be sent.");
        }
        
        // Close the file and the communication channel
        out.close();
        fis.close();

        channel.disconnect();
        session.disconnect();
        
        System.out.println("The file was successfully transfered.");
    }

    private int checkAck(InputStream in) throws IOException {
        int b = in.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //         -1
        if (b == 0) return b;
        if (b == -1) return b;

        if (b == 1 || b == 2) {
            StringBuffer sb = new StringBuffer();
            int c;
            do {
                c = in.read();
                sb.append((char) c);
            }
            while (c != '\n');
            if (b == 1) { // error
                System.out.print(sb.toString());
            }
            if (b == 2) { // fatal error
                System.out.print(sb.toString());
            }
        }
        return b;
    }
}
