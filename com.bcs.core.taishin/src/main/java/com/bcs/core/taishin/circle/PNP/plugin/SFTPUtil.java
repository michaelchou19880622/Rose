package com.bcs.core.taishin.circle.PNP.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;

import com.bcs.core.utils.ErrorRecord;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Slf4j(topic = "PnpRecorder")
public class SFTPUtil {

	private Session session;
	private ChannelSftp sftpChannel;
	private List<String> result = new ArrayList<>();

	public static SFTPUtil login(String host, String userName, String password) {
        return new SFTPUtil().connect(host, null, userName, password);
    }

	public static SFTPUtil login(String host, Integer port, String userName, String password) {
	    return new SFTPUtil().connect(host, port, userName, password);
	}

	private SFTPUtil connect(String host, Integer port, String userName, String password) {
	    JSch jsch = new JSch();

	    try {
	        if(port == null) {
	            session = jsch.getSession(userName, host);
	        }
	        else {
	            session = jsch.getSession(userName, host, port);
	        }
            session.setConfig("StrictHostKeyChecking", "no");
            session.setPassword(password);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;
        }
	    catch (JSchException e) {
            log.error(ErrorRecord.recordError(e));

            if(session != null) {
                session.disconnect();
            }
        }

	    return this;
	}

	public void logout() {
	    if(sftpChannel != null) {
	        sftpChannel.exit();
	    }
	    if(session != null) {
	        session.disconnect();
	    }
	}

	public boolean isConnected() {

	    if(session != null && sftpChannel != null &&
	            session.isConnected() && sftpChannel.isConnected()) {
	        return true;
	    }
	    return false;
	}

	public List<String> listFileName(String pathName, int recursionTimes) throws SftpException {
	    return listFileName(pathName, recursionTimes, false);
	}

	private List<String> listFileName(String pathName, int recursionTimes, boolean isRecursion) throws SftpException {
	    if(!isRecursion) {
	        result = new ArrayList<>();
	    }

	    if (pathName.startsWith("/") && pathName.endsWith("/")) {

	        Vector<LsEntry> fileList = sftpChannel.ls(pathName);

	        if(fileList != null && fileList.size() > 0) {

	            for(int i = 0; i < fileList.size(); i++) {
	                LsEntry entry = fileList.get(i);
	                if(entry.getAttrs().isDir()) {
	                    if(recursionTimes != 0 && !".".equals(entry.getFilename()) && !"..".equals(entry.getFilename())) {
	                        listFileName(pathName + entry.getFilename() + "/", recursionTimes-1, true);
	                    }
	                }
	                else {
	                    result.add(pathName + entry.getFilename());
	                }
	            }
	        }
	    }

	    return result;
	}

	public List<String> listFileName(String pathName, String ext, int recursionTimes) throws SftpException {
        return listFileName(pathName, ext, recursionTimes, false);
    }

	private List<String> listFileName(String pathName, String ext, int recursionTimes, boolean isRecursion) throws SftpException {
        if(!isRecursion) {
            result = new ArrayList<>();
        }

        if (pathName.startsWith("/") && pathName.endsWith("/")) {

            Vector<LsEntry> fileList = sftpChannel.ls(pathName);

            if(fileList != null && fileList.size() > 0) {

                for(int i = 0; i < fileList.size(); i++) {
                    LsEntry entry = fileList.get(i);
                    if(entry.getAttrs().isDir()) {
                        if(recursionTimes != 0 && !".".equals(entry.getFilename()) && !"..".equals(entry.getFilename())) {
                            listFileName(pathName + entry.getFilename() + "/", ext, recursionTimes-1, true);
                        }
                    }
                    else {
                        if (entry.getFilename().endsWith(ext)) {
                            result.add(pathName + entry.getFilename());
                        }
                    }
                }
            }
        }

        return result;
    }

	public InputStream readFile(String fileName) throws SftpException {
	    return sftpChannel.get(fileName);
	}

	public void uploadFile(String uploadFile, String targetDir) throws SftpException, FileNotFoundException, UnsupportedEncodingException {
	    File file = new File(uploadFile);

	    if(file.exists()){
	        try {
	            Vector content = sftpChannel.ls(targetDir);
	            if(content == null){
	                this.mkdir(targetDir);
	            }
	        }
	        catch (SftpException e) {
	            this.mkdir(targetDir);
	        }
	        sftpChannel.cd(targetDir);

	        if(file.isFile()){
	            InputStream ins = new FileInputStream(file);
	            sftpChannel.put(ins, new String(file.getName().getBytes(),"UTF-8"));
	        }
	        else {
	            File[] files = file.listFiles();
	            for (File file2 : files) {
	                String dir = file2.getAbsolutePath();
	                if(file2.isDirectory()){
	                    String str = dir.substring(dir.lastIndexOf(file2.separator)+1);
	                    uploadFile(dir, targetDir +"/"+ str);
	                }
	                else {
	                    uploadFile(dir, targetDir);
	                }
	            }
	        }
	    }
	}

	public boolean rename(String from, String to) {
	    try {
            sftpChannel.rename(from, to);
            return true;
        } catch (SftpException e) {
            return false;
        }
	}

	public boolean deleteFile(String fileName) {
	    try {
            sftpChannel.rm(fileName);
            return true;
        } catch (SftpException e) {
            return false;
        }
	}

	public boolean mkdir(String path) {
	    try {
	        if(path.startsWith("/")) {
	            if(!isExists(path)) {
	                String[] arr = path.substring(1).split("/");

	                StringBuffer sb = new StringBuffer();
	                for(int i = 0; i < arr.length; i++) {
	                    sb.append("/"+ arr[i]);
	                    if(!isExists(sb.toString())) {
	                        sftpChannel.mkdir(sb.toString());
	                    }
	                }
	            }

	            return true;
	        }
        } catch (SftpException e) {}

	    return false;
	}

	private boolean isExists(String path) {
	    try {
            if(sftpChannel.ls(path) != null) {
                return true;
            }
        } catch (SftpException e) {}

	    return false;
	}
}
