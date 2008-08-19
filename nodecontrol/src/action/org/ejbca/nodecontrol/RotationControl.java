package org.ejbca.nodecontrol;

import static org.jboss.seam.ScopeType.EVENT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

import org.ejbca.nodecontrol.util.NodecontrolConfiguration;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.log.Log;
import org.jboss.seam.security.Identity;

@Name("rotationcontrol")
@Scope(EVENT)
@Restrict("#{identity.loggedIn}") 
public class RotationControl implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Logger Log log;
	
    @In Identity identity;

	private String rotationString="unknown";
	
	@Create
	public String check() {
		log.debug("Checking rotation control file");
		String fileName = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.ROTATION_FILE);
		try {
			FileReader fr = new FileReader(fileName);
			BufferedReader br = new BufferedReader(fr);
			try {
				String line = br.readLine();
				rotationString = line;
			} finally {
				fr.close();
				br.close();
			}
		} catch (IOException e) {
			log.error("Rotation file #0 does not exist or can not be read.", fileName);
			rotationString="file corrupt";
		}
		return null;
	}
	public String removeFromRotation() {
		log.info("Removing from rotation by user #0", identity.getUsername());
		writeRotationFile("true");
		check();
		return null;
	}
	
	public String addToRotation() {
		log.info("Adding to rotation by user #0", identity.getUsername());
		writeRotationFile("false");
		check();
		return null;
	}

	public String getPanelopen() {
		return NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.ROTATIONPANEL_OPEN);
	}

	public String getRotationString() {
		return rotationString;
	}
	
	//
	// Private helpers
	//
	
	private void writeRotationFile(String value) {
		String fileName = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.ROTATION_FILE);
		String key = NodecontrolConfiguration.instance().getString(NodecontrolConfiguration.ROTATION_KEY);
		try {
			FileWriter fw = null;
			try {
				fw = new FileWriter(fileName);
				fw.write(key+"="+value);				
			} finally {
				fw.close();
			}
		} catch (IOException e) {
			log.error("Rotation file #0 can not be read or written written.", fileName, e);
			rotationString="file error: "+e.getMessage();
		} 		
	}
}
