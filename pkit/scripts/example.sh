#!/bin/bash
CP=${CLASSPATH}:../lib/bcprov-jdk14-115.jar:../lib/ca.jar:../lib/log4j-1.2.jar:./dist/pkit.jar
exec java -cp ${CP} pkit.examples.CertificateTool "../etc/signer.properties" 
