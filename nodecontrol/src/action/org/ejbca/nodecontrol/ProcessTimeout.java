package org.ejbca.nodecontrol;

public class ProcessTimeout implements Runnable {    	
	private Process p;

	public ProcessTimeout(Process p) {
		this.p = p;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		while(true) {
			try {
				// Wait for 10 seconds before destroying the process
				Thread.sleep(10000);
				p.destroy();
			} catch( Throwable t ) {
				t.printStackTrace();
			}
		}
	}
}

