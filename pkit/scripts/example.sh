#!/bin/bash
# an example shell script to run CertificateTool from the commandline on Linux/Unix
CP=${CLASSPATH}:../lib/bcprov-jdk14-115.jar:../lib/ca.jar:../lib/log4j-1.2.jar:./dist/pkit.jar
exec java -cp ${CP} pkit.examples.CertificateTool
