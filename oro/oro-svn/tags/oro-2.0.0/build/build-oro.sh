#!/bin/sh

#
# $Id: build-oro.sh 54270 2000-07-23 23:08:26Z jon $
#

# --------------------------------------------
# Default == jar
# "lib"            target builds the library
# "examples"       target builds the example programs
# "tools"          target builds the tools
# "clean"          target removes generated files
# "jar"            target builds lib + jar
# "javadoc"        target builds the javadoc
# "package"        target builds lib + jar + javadoc + distribution
# --------------------------------------------
TARGET=${1}

#--------------------------------------------
# No need to edit anything past here
#--------------------------------------------
if test -z "${JAVA_HOME}" ; then
    echo "ERROR: JAVA_HOME not found in your environment."
    echo "Please, set the JAVA_HOME variable in your environment to match the"
    echo "location of the Java Virtual Machine you want to use."
    exit
fi

if test -z "${TARGET}" ; then 
TARGET=jar
fi

if test -f ${JAVA_HOME}/lib/tools.jar ; then
    CLASSPATH=${CLASSPATH}:${JAVA_HOME}/lib/tools.jar
fi

echo "Now building ${TARGET}..."

CP=${CLASSPATH}:ant.jar:xml.jar

echo "Classpath: ${CP}"
echo "JAVA_HOME: ${JAVA_HOME}"

BUILDFILE=./build-oro.xml

${JAVA_HOME}/bin/java -classpath ${CP} org.apache.tools.ant.Main -buildfile ${BUILDFILE} ${TARGET}
