/*************************************************************************
 *                                                                       *
 *  EJBCA: The OpenSource Certificate Authority                          *
 *                                                                       *
 *  This software is free software; you can redistribute it and/or       *
 *  modify it under the terms of the GNU Lesser General Public           *
 *  License as published by the Free Software Foundation; either         *
 *  version 2.1 of the License, or any later version.                    *
 *                                                                       *
 *  See terms of license at gnu.org.                                     *
 *                                                                       *
 *  Copyright (c) PrimeKey Solutions AB.                                 *
 *                                                                       *
 *************************************************************************/
 
RAAdmin Java Project:

Author: Daniel Horn
danh@sio2corp.com
Last Revision: 1/14/2011

This Java application provides a simple UI for creating and querying end entities, and uses EJBCA web services for communicating
with the app server. 
It includes wizard dialogs that prompt the user for the type of certificate to create, that prompt the user to enter values defining
the subject DN, that allow the user to receive an email notification containing the password for the new end entity, that allow the user
to choose between adding the new end entity to the EJBCA database and actually generating the certificate (as either a P12 or JKS file).
 
Further information on this application can be found in troubleshooting.txt (tips on running the app) and todo.txt (notes and issues for developers).
 

Installing and Building the source code:


0)  Prerequisites for building the application:
	Java JDK version 1.6.0_x or later
		(The current Java version being used is 1.6.0_22)
	Eclipse IDE for Java Developers
		(The project has been built with version 1.2.2 and with 1.3.1).


1a)  If the source is provided as a zip file:  
The WebServiceRA.ZIP file contains an Eclipse workspace subdirectory.
Unzip it into your Eclipse workspace directory.
1b)  The source files may be downloaded from SourceForge: 
	http://sourceforge.net/projects/ejbca/files/WebServiceRA



Configuration/Libraries:

**** Some components may not be distributed with this Eclipse project.
The section below describes where they may be downloaded from and how they should be installed.


1)  EJBCA jar files:
The WebServiceRA/lib directory is assumed to contain a copy of the ejbca_3_11_0/dist/ejbca-ws-cli directory.

Note that in addition to the web services API, these libraries include logging (org.apache.commons.logging.*) functionality (used by the ciscavate classes) as well as other (non-server) functionality used by the app.

**** If these jar files are not present, just copy them from the ejbca distribution so that, for example, ejbca_11_0/dist/ejbca-ws-cli/ejbca-ws-cli.jar is
located at WebServiceRA/lib/ejbca-ws-cli/ejbca-ws-cli.jar.  (Alternatively, your version of the Eclipse project could be modified 
to use the missing jars and libs from whatever their location is on your disk).

This should be replaced with the corresponding directory from newer versions of ejbca when new web service features are made available.

While the EJBCA libs currently used are from version 3.11.0, the RAAdmin app should compile (and run against appservers) 
for versions at least as far back as EJBCA version 3.9.x.


2) CJWizard from ciscavate:
This source code provides support for creating multi-page dialogs/wizards in Java and was found at
	http://code.google.com/p/cjwizard/downloads/list
The code license for this is Apache License 2.0.

Note that the source that was unzipped into the CJWizard directory is used for builds rather than the binary zip because the source files are more upto date than the binary zip.
In particular, some classes (eg, StackWizardSettings) are missing from the binaries jar.

In order to avoid issues with modifying this source, all modifications of functionality take place in derived classes (SiO2WizardContainer.java, SiO2WizardPage).


3)  Apple:
The OSXAdapter contains sample code from Apple:
	http://developer.apple.com/library/mac/#samplecode/OSXAdapter/Introduction/Intro.html
When run on a Mac OS X system, the Java application takes on certain features of the Mac.
The adapter code enables the program to behave like a Mac app when appropriate and otherwise when run on other OS's.

**** If the OSXAdapter source is not present in your WebServiceRA project, it can be downloaded from:
	http://developer.apple.com/library/mac/#samplecode/OSXAdapter/Introduction/Intro.html
In particular, the project assumes it is installed as 
	WebServiceRA/OSXAdapter/src/apple/dts/samplecode/osxadapter/OSXAdapter.java
	
Also:
On a Mac, this evidently depends on AppleJavaExtensions.jar (which is meant for development on the Macintosh and probably is not a redistributable).
However, the relevant classes seem to be found at runtime on the Mac as including the .jar file does not seem to be required to build.
Aside from requiring the OSXAdapter code in the build, this does not appear to cause any issues when building or running on any other (ie, non-Mac) platforms.


4)  To see exactly how these jar files are used, see the Eclipse workspace.  In particular, in the Navigator pane, right click on "WebServiceRA" and choose 
"Properties" | "Java Build Path" | "Libraries".


Recommendations:


1)  The user who intends to read the source code may wish to also unzip ejbca_3_11_0.zip into the workspace to simplify browsing the source from within Eclipse.
(For example, when attempting to open the declaration of a type defined in an Ejbca class, if Eclipse 
shows "Source not found", press the "Change Attached Source..." button and assign an appropriate subdirectory of the ejbca source or modules directory).


2)  Many of the JPanel derived classes were laid out using the Jigloo GUI Builder plugin for Eclipse:
	http://www.cloudgarden.com/jigloo/
This is not a requirement for building the app.  Though it has a number of quirks, the developer who designs Swing panels or dialogs may
find it an improvement over other visual editors for Eclipse.



Running the application:

Prerequisites:

0)  The correct version of the Java Runtime Environment, 1.6.0_x or later, available and in the path on the client machine, required to run the client application (a .jar file).
 
1)  A running EJBCA server, version 3.11.0 or greater.

2)  The URL defining the server's web services 
	(eg,
		https://ejbca.course:8443/ejbca/ejbcaws/ejbcaws?wsdl
	).
	
3)  Optional:  Access to the admin web page interface of the EJBCA server:
	to add/edit profiles;
	to verify actions of client application;
	to modify access rules if necessary (see troubleshooting.txt);
	to create item 4) below.  

4)  keyStore and trustStore (.jks) files
	Note that a single file can be used for both of these.
	Typically an end entity for this would be added using the admin web page interface and then the actual JKS Keystore file would be created on the 
	client machine using the public web page interface.
	
	The applications Preferences/Settings dialog needs item 2) above, and the path and password to this keystore store file.
	This information is stored in plain text in the RAAdmin.properties file which should therefore be deemed as containing sensitive information.
	   
	After creating the keystore for the end entity, you may need to go to the admin web interface to:
		query for the end entity;
		get its certificate's serial number;
		make it a member of the appropriate administrator group; and
		make sure it has the correct access rules.

5)  Optional:  If you plan to use the Send Notification support, then email on appserver must be configured correctly.
With JBoss, this means  
getting the SMTP settings correctly in the jboss/server/default/deploy/ejbca-mail-service.xml file.
The end entity profile must also be configured to send notifications with the recipient of USER and a message indicating the user's password.
(For more information, see troubleshooting.txt).


Running/Debugging the app from within Eclipse:

1) In the Navigator pane, right click on src | org | sio2 | ejbca | MainFrame.java and choose "Run As..." (or "Debug As...") | Java Application.

2)  Alternatively, right click on the top level item (WebServiceRA) and choose "Run As..." and choose "MainFrame - org.sio2.ejbca" from the "Select Java Application" screen.
Thereafter, you can pick it from the Run and Debug drop down toolbar buttons.

3)  If running on a Mac OS X system, you may wish to edit the "Run As/Debug" configuration so that the Java package is not what appears as the application in
the main menu.  You can accomplish this, for example, by adding
	-Xdock:name="RA Administrator"
to the "VM Arguments" section of the "Arguments" tab.



Running the app from the command line:

Assuming you have built the app as a jar file:
	java -jar RAAdmin.jar


Building the jar:

From within Eclipse, right click on the project name (WebServiceRA) in the Navigator panel and choose Export.
Choose Java | Runnable Jar File.  If you plan to distribute a jar file built this way, you must design it to respect the 
licenses of the libraries and jar files required to run it.


Redistributing the source/building a zip file:
In the Navigator pane, right click on buildzip.xml and choose Run As... (or Debug As...) and choose the second item "Ant Build...".
In the Edit Configuration dialog that appears, choose the ziprelease target.

This puts everything into a single archive file except for compiled .class files, the OSXAdapter files (from Apple), .DS_Store files (created
in directories on Mac OS X system), the ejbca distribution directory in case you have added it to this project as a subdirectory, and the RAAdmin.properties
file (which is created when you run the application and which contains private information including passwords).


Misc.:
This source includes a version of the ciscavate cjwizard source code which is licensed under the Apache License Version 2.0:
	
	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License.

