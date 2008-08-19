package org.ejbca.nodecontrol;

import static org.jboss.seam.ScopeType.SESSION;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.ejbca.nodecontrol.util.NodecontrolConfiguration;
import org.hibernate.validator.Length;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

@Name("logcontrol")
@Scope(SESSION)
@Restrict("#{identity.loggedIn}") 
public class LogControl implements Serializable {

	private static final long serialVersionUID = 1L;

	@Logger Log log;
	@In private FacesMessages facesMessages;

	@In
	private FacesContext facesContext;

	@In Identity identity;

	@In(required=false)
	String logdir;
	
	@In(required=false)
	String logfile;

	@In(required=false)
	String grepstring;
	
	@In(required=false)
	String tailstring;

	@In(required=false)
	@Out(required=false)
	@Length(min=1,max=30)
	String pipehost;
	
	@In(required=false)
	@Out(required=false)
	@Length(min=1,max=5)
	String pipeport;
	
	@In(required=false)
	@Out(required=false)
	@Length(min=1,max=5)
	String pipelines;


	@In(required=false) 
	@Out(required=false)
	String logresult;

	List<SelectItem> logfileselectitems;
	List<String>logfiles;

	List<SelectItem> logdirselectitems;
	List<String>logdirs;

	@Create
	public String create() {
		// Default values for remote host and port for piping
		pipeport = "9999";
		pipelines = "500";
		ExternalContext externalContext = facesContext.getExternalContext();
		HttpServletRequest req = (HttpServletRequest)externalContext.getRequest();
		pipehost = req.getRemoteHost();
		
		// list files
		listFiles();
		return null;
	}
	
	public String listFiles() {
		log.debug("Listing files from dir #0.", logdir);
		if (logdir != null) {
			logfiles = new ArrayList<String>();
			try {
				File[] files = listFilesInDir(logdir);
				for (File file : files) {
					if (file.isFile()) {
						logfiles.add(file.getAbsolutePath());
					}
				}			
			} catch (IllegalArgumentException e) {
				facesMessages.addToControl("logcontrol", e.getMessage());
			}
		}
		if (logfiles == null) {
			logfiles = new ArrayList<String>();
		}
		Collections.sort(logfiles);
		return null;
	}

	public String grep() {
		log.debug("Grep '#0' on logfile '#1'", grepstring, logfile);
		if (!checkLogFileInAllowedDir(logfile)) {
			facesMessages.add("File #0 is not in an allowed directory", logfile);
			return null;
		}
		if (StringUtils.isNotEmpty(grepstring)) {
			String cmd = "grep -n -h \""+grepstring+"\" "+logfile;
			log.info("User #0 executing command '#1'", identity.getUsername(), cmd);
			if (StringUtils.isNotEmpty(grepstring)) {
				log.debug("Executing command: #0", cmd);
				try {
					logresult = ActionUtils.runSystemCmd(cmd, true, true, 60*5); // max runtime for grep command is 5 minutes
				} catch(Throwable e) {
					log.error("Error executing grep: ", e);
				} 			
			}			
		}
		log.debug("<grep");
		return null;
	}
	public String tail() {
		// If we haven't entered a value, use a good default
		if (StringUtils.isEmpty(tailstring)) {
			tailstring = "25";
		}
		String cmd = "tail -"+tailstring+" "+logfile;
		if (!checkLogFileInAllowedDir(logfile)) {
			facesMessages.add("File #0 is not in an allowed directory", logfile);
			return null;
		}
		if (StringUtils.isNotEmpty(tailstring)) {
			log.info("User #0 executing command '#1'", identity.getUsername(), cmd);
			try {
				logresult = ActionUtils.runSystemCmd(cmd, true, true, 60*5); // max runtime for tail command is 5 minutes
			} catch(Throwable e) {
				log.error("Error executing tail: ", e);
			} 
		}
		return null;
	}

	public String pipe() {
		String pipecmd = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.LOG_PIPE_CMD);
		if (StringUtils.isNotEmpty(pipecmd)) {
			if (pipecmd.contains("{lines}") && (StringUtils.isEmpty(logfile) || !StringUtils.isNumeric(pipelines))) {
				facesMessages.add("Must enter no of lines to pipe");
				return null;
			}
			if (pipecmd.contains("{file}") && StringUtils.isEmpty(logfile)) {
				facesMessages.add("Must select a file to pipe");
				return null;
			}
			if (!checkLogFileInAllowedDir(logfile)) {
				facesMessages.add("File #0 is not in an allowed directory", logfile);
				return null;
			}
			if (pipecmd.contains("{host}") && StringUtils.isEmpty(pipehost)) {
				facesMessages.add("Must select a host to pipe to");
				return null;
			}
			if (pipecmd.contains("{port}") && !StringUtils.isNumeric(pipeport)) {
				facesMessages.add("Must select a port on the host to pipe to");
				return null;
			}
			pipecmd = StringUtils.replace(pipecmd, "{lines}", pipelines);
			pipecmd = StringUtils.replace(pipecmd, "{file}", logfile);
			pipecmd = StringUtils.replace(pipecmd, "{host}", pipehost);
			pipecmd = StringUtils.replace(pipecmd, "{port}", pipeport);
			log.info("User #0 executing command '#1'", identity.getUsername(), pipecmd);
			try {
				ActionUtils.runSystemCmd(pipecmd, false, false, 30); // Don't read input, don't wait, max time to run shell is 10 sec
			} catch(Throwable e) {
				log.error("Error executing tail: ", e);
			} 
		}
		return null;
	}

	public String save() {
		// If we haven't got a result ignore this
		if (StringUtils.isEmpty(logresult)) {
			return null;
		}
		if (StringUtils.isEmpty(logfile)) {
			logfile = "logfile.txt";
		}
		File f = new File(logfile);
		String name = f.getName();
		sendTextData(logresult, name);
		return null;
	}

	public String getPanelopen() {
		return NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.LOGPANEL_OPEN);
	}

	public List<SelectItem> getLogfileItems() {
		log.debug(">getLogfileItems");
		if (logfiles == null) {
			listFiles();
		}
		if (logfiles != null) {
			logfileselectitems = new ArrayList<SelectItem>();
			for (String file : logfiles) {
				log.debug("Adding logfile item #0", file);
				File f = new File(file);
				String name = f.getName();
				logfileselectitems.add( new SelectItem(file,name));
			}			
		}
		return logfileselectitems;
	}

		
	public List<SelectItem> getLogdirItems() {
		log.debug(">getLogDirItems");
		logdirselectitems = new ArrayList<SelectItem>();
		Collection<String> dirs = getDirectories();
		for (String dir : dirs) {
			logdirselectitems.add( new SelectItem(dir,dir));			
		}
		return logdirselectitems;
	}

	public String getPipeUserInfo() {
		String pipeinfo = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.LOG_PIPE_USERINFO);
		return pipeinfo;
	}
	public String getPipeUserInfoFormat() {
		String pipeinfo = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.LOG_PIPE_USERINFOFORMAT);
		return pipeinfo;
	}
	public String getMaxlines() {
		int maxlines = NodecontrolConfiguration.instance().getInt(NodecontrolConfiguration.LOG_MAX_LINES);
		return String.valueOf(maxlines);
	}
	
	//
	// Private helpers
	//
	
	private void sendTextData(String text, String filename) {
		if (text == null) return;
		log.debug("Data is: "+text.length());
		try {
			// Find out which content type we are
			String contentType = "application/octet-stream";
			ExternalContext externalContext = facesContext.getExternalContext();
			HttpServletResponse resp = (HttpServletResponse)externalContext.getResponse();
			ServletUtils.removeCacheHeaders(resp);
			resp.setContentType(contentType);
			resp.setContentLength(text.length());
			resp.setHeader( "Content-disposition", "inline; filename=\""+filename+"\"");
			resp.getOutputStream().write(text.getBytes());
		} catch (IOException e) {
			log.error("IOException writing image to client: ", e);
		}
		facesContext.responseComplete();
	}


	/**
	 * method to check if the file we want to operate on is really in one of the directories we
	 * have configured to look in. Used to prevent directory traversal (if it was ever possible)
	 */
	private boolean checkLogFileInAllowedDir(String file) {
		boolean ret = false;
		if (file == null) return false;
		File f = new File(file); 
		String dir = f.getParent();
		log.debug("Parent is #0", dir);
		Collection<String> dirs = getDirectories();
		if (dirs.contains(dir)) {
			ret = true;
		}
		return ret;
	}
	private Collection<String> getDirectories() {
		String dir = "";
		int i = 1;
		ArrayList<String> dirs = new ArrayList<String>();
		while (dir != null) {
			dir = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.LOG_DIR+"."+i);
			if (dir != null) {
				dirs.add(dir);
				i++;
			}		
		}
		return dirs;
	}
	
	private File[] listFilesInDir(String directory) {
		File dir = new File(directory);
		try {
			if (dir == null || dir.isDirectory() == false) {
				log.error(dir.getCanonicalPath()+ " is not a directory.");
				throw new IllegalArgumentException(dir.getCanonicalPath()+ " is not a directory.");                
			}
			
			File files[] = dir.listFiles(new LogFileFilter());
			if (files == null || files.length == 0) {
				log.error("There are no files in directory "+dir.getCanonicalPath());                
			}
			return files;
		} catch (IOException e) {
			String errMsg = "Error reading authorized certificates";
			log.error(errMsg, e);
			throw new IllegalArgumentException(errMsg);
		}

	}

	private class LogFileFilter implements FileFilter {

		private int logdaystoshow = 30;
		public LogFileFilter() {
			String s = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.LOG_DAYS_TO_SHOW);
			logdaystoshow = Integer.valueOf(s);
		}
		public boolean accept(File pathname) {
			boolean ret = false;
			long time = pathname.lastModified();
			if (time > 0L) {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.DAY_OF_MONTH, -logdaystoshow);
				if (cal.getTime().before(new Date(time))) {
					ret = true;
					log.error("File #0 accepted", pathname.getName());
				} else {
					log.error("File #0 not accepted", pathname.getName());
				}
			}
			return ret;
		}
		
	}
}
