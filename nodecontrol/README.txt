Node Control
------------

Node control has three major features:
1. JBoss Control
   - Start/stop of JBoss
   - Healtch check of EJBCA
2. Rotation Control
   - Toggle maintenance file for EJBCA health check
     "Remove from rotation" makes health check respond with an error to the load balancer
3. Log Control
   - Define a set of directories where we can read logfile
   
Configuration is primarily done in resources/nodecontrol.properties, which is built into 
the deployment war file. More about configuration files down below.
All configuration options are documented in resources/nodecontrol.properties.

JBoss Control
-------------
The "Check" button performs two different actions. First it runs a ps command and sees if 
any jboss process is running. Second it makes a http get from the EJBCA health check servlet 
and displays the returned answer. The command run to look for a running process is:
ps -auxww 
The result is parsed and the word configured in {nodecontrol.running.grepfor} is searched for.

The Stop button runs a script to stop JBoss (or something else).

The Start button runs a script to start JBoss (or something else).

Relevant configuration options:
nodecontrol.healthcheckurl
nodecontrol.running.grepfor
nodecontrol.startcmd
nodecontrol.stopcmd

Rotation Control
----------------
Rotation control simply instructs the EJBCA health check to return an error message or not.
If a load balancer is monitoring the EJBCA health check url, it will remove this node from the 
cluster when it returns an error message.
This is useful in order to be able to deliberately remove a node from the cluster for testing 
and maintenance.

Relevant configuration options:
nodecontrol.rotation.control.file - matches against healthcheck.maintenancefile in ejbca.properties
nodecontrol.rotation.control.key - matches against healthcheck.maintenancepropertyname in ejbca.properties

Log Control
-----------
Log control allows you to view log files without having to log into the server.

There are three ways to view the log files:
1. Tail a specified number of lines from the file.
2. Grep the file for an expression.
3. Pipe the file (tail -f), through openssl, to a remote host.

To run a pipe you usually run the following command on your client:
openssl s_server -accept 9999
You have to have a file called server.pem with a certificate and a private key in order
for openssl to start in ssl server mode.

When you give the options 'lines of history', hostname and port to NodeControl it in turn
runs the following command:
tail -n {lines} -f {file} | openssl s_client -connect {host}:{port}
Where {lines}, {file}, {host} and {port} are replaced with the values from the web page.

Relevant configuration options:
nodecontrol.log.directory.x
nodecontrol.log.daystolist
nodecontrol.log.maxlines
nodecontrol.log.pipecmd
nodecontrol.log.pipeuserinfo
nodecontrol.log.pipeuserinfoformat

Other options
-------------
There are other options that control how the web application or GUI behaves by default.
allow.external-dynamic.configuration
nodecontrol.jbosspanel.open
nodecontrol.rotationpanel.open
nodecontrol.logpanel.open
