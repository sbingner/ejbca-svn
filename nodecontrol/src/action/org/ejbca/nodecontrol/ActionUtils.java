package org.ejbca.nodecontrol;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.ejbca.nodecontrol.util.NodecontrolConfiguration;


public class ActionUtils {

	public static String runSystemCmd(String cmd, boolean getOutput, boolean wait, int timeout) throws IOException, InterruptedException {
		//log.debug(">runSystemCmd: "+cmd);
		String psout = null;
		BufferedReader br = null;
		try {
			StringBuffer output = new StringBuffer();
			String[] realcmd = {
					"/bin/sh",
					"-c",
					cmd
					};
			Process p = Runtime.getRuntime().exec(realcmd);
			if (getOutput) {
				InputStream in = p.getInputStream();
				br = new BufferedReader(new InputStreamReader(in));
				String line = null;
				// Limit the number of lines we will return to the poor process/browser
				int maxlines = NodecontrolConfiguration.instance().getInt(NodecontrolConfiguration.LOG_MAX_LINES);
				int lines = 0;
				while ( ((line = br.readLine()) != null) && (lines < maxlines) ) {
					output.append(line);
					output.append("\n");
					lines++;
				}
				psout = output.toString();
				// if there is anything left to read, just skip it
				br.skip(Integer.MAX_VALUE);
				br.close();	
				//log.debug("Cmd output: "+psout);
			}
			if (wait) {
				p.waitFor();
			} 
			else  {
				ProcessTimeout pt = new ProcessTimeout(p);
				new Thread(pt).start();				
			}
		} finally {
			if (br != null) {
				br.close();
			}
		}
		//log.debug("<runSystemCmd: "+cmd);
		return psout;
	}


}
