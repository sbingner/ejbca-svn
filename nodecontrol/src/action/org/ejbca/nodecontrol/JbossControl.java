package org.ejbca.nodecontrol;

import static org.jboss.seam.ScopeType.EVENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;

import org.ejbca.nodecontrol.util.NodecontrolConfiguration;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

@Name("jbosscontrol")
@Scope(EVENT)
@Restrict("#{identity.loggedIn}") 
public class JbossControl implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Logger Log log;
	@In
	private FacesMessages facesMessages;
	
    @In Identity identity;

	private String running="unknown";
	private String healthcheck="unknown";

	private static final String JBOSS_RUNNING="running";
	private static final String JBOSS_NOT_RUNNING="not running";
	
	@Create
	public String check() {
		log.debug("Checking if JBoss is running");
		running = jbossRunning();
		log.debug("Checking health check of JBoss");
		String url = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.HEALTHCHECKURL);
		String ret = getUrl(url);
		if (ret == null) {
			healthcheck="health check error";
		} else {
			healthcheck=ret;
		}
		return null;
	}
	public String start() {
		log.info("Start request issued by #0", identity.getUsername());
		String startcmd = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.START_CMD);
		try {
			if (jbossRunning().equals(JBOSS_RUNNING)) {
				facesMessages.addToControl("start", "JBoss is already started");
			} else {
				ActionUtils.runSystemCmd(startcmd, false, false, 0); // don't wait, it will hopefully never finish...
				running = "starting...";				
			}
		} catch(Throwable e) {
			log.error("Error starting jboss: ", e);
			running = e.getMessage();
		} 
		return "start";
	}
	
	public String stop() {
		log.info("Stop request issued by #0", identity.getUsername());
		String stopcmd= NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.STOP_CMD);
		try {
			ActionUtils.runSystemCmd(stopcmd, false, true, 0); // Wait for this one, it will be quick
			running = "stopping...";
		} catch(Throwable e) {
			log.error("Error stopping jboss: ", e);
			running = e.getMessage();
		} 
		return "stop";
	}

	public String getPanelopen() {
		return NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.JBOSSPANEL_OPEN);
	}
	public String getRunning() {
		return running;
	}
	public String getHealthcheck() {
		return healthcheck;
	}
	public String getHealthcheckUrl() {
		String url = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.HEALTHCHECKURL);
		return url;
	}
	public String getDate() {
		return new Date().toString();
	}

	//
	// Private helpers
	//

	/**
	 * @returns "running", "not running" or an error message
	 */
	private String jbossRunning() {
		String ret = "unknown";
		// Check if we can find a org.jboss.Main process in the system
		// This is only valid for unix like os'es, e.g linux, macosx, bsd, etc
		// Windows is so discriminated...
		String commandStr = "ps -auxww";
		try {
			String psout = ActionUtils.runSystemCmd(commandStr, true, true, 0);
			if (psout != null) {
				String grepfor = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.RUNNINGGREP);
				if (psout.contains(grepfor)) {
					ret = JBOSS_RUNNING;
				} else {
					ret = JBOSS_NOT_RUNNING;
				}
			}
		} catch(Throwable e) {
			log.error("Error checking for a jboss process: ", e);
			ret = e.getMessage();
		} 
		return ret;
	}

	
	private String getUrl(String urlString) {
		String ret = null;
		try {
			HttpURLConnection con = null;
			URL url;
			url = new URL(urlString);
			con = (HttpURLConnection)url.openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("GET");
			con.connect();

			int rcode = con.getResponseCode();
			if (rcode != 200) {
				log.error("Response code for url \""+urlString+"\" is not 200 but "+rcode);
				String msg = con.getResponseMessage();
				throw new IOException("HTTP Status "+rcode+" - "+msg+" - "+urlString);
			}
			ret = readStringFromStream(con.getInputStream());
		} catch (MalformedURLException e) {
			String msg = "Error: ";
			log.error(msg, e);
			ret = msg + e.getMessage();
		} catch (ProtocolException e) {
			String msg = "Error: ";
			log.error(msg, e);
			ret = msg + e.getMessage();
		} catch (IOException e) {
			String msg = "";
			log.debug(msg, e); // this is normal if we can't reach the url at all
			ret = msg + e.getMessage();
		}
		return ret;
	}
	private String readStringFromStream(InputStream in) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int b = in.read();
		while (b != -1) {
			baos.write(b);
			b = in.read();
		}
		baos.flush();
		in.close();
		byte[] respBytes = baos.toByteArray();
		return new String(respBytes);
	}
}
