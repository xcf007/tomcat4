#!/bin/sh

#
#  Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  See the NOTICE file distributed with
#  this work for additional information regarding copyright ownership.
#  The ASF licenses this file to You under the Apache License, Version 2.0
#  (the "License"); you may not use this file except in compliance with
#  the License.  You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#

# -----------------------------------------------------------------------------
# tester.sh - Execute Test Application Client
#
# Environment Variable Prequisites
#
#   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
#                 If not present, the current working directory is assumed.
#
#   CATALINA_OPTS (Optional) Java runtime options used when the "start",
#                 "stop", or "run" command is executed.
#
#   JAVA_HOME     Must point at your Java Development Kit installation.
#
# $Id: tester.sh 743401 2009-02-11 17:01:58Z markt $
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$CATALINA_HOME" = "" ] ; then
  CATALINA_HOME=`pwd`
fi

if [ "$CATALINA_OPTS" = "" ] ; then
  CATALINA_OPTS=""
fi

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit installation
  exit 1
fi

# ----- Cygwin Unix Paths Setup -----------------------------------------------

# Cygwin support.  $cygwin _must_ be set to either true or false.
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  *) cygwin=false ;;
esac
 
# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$CATALINA_HOME" ] &&
    CATALINA_HOME=`cygpath --unix "$CATALINA_HOME"`
    [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
    [ -n "$ANT_HOME" ] &&
    ANT_HOME=`cygpath --unix "$ANT_HOME"`
fi

# ----- Set Up The System Classpath -------------------------------------------

CP=$CATALINA_HOME/webapps/tester/WEB-INF/lib/tester.jar:$CATALINA_HOME/server/lib/jaxp.jar:$CATALINA_HOME/server/lib/crimson.jar:$ANT_HOME/lib/ant.jar

# ----- Cygwin Windows Paths Setup --------------------------------------------

# convert the existing path to windows
if $cygwin ; then
   CP=`cygpath --path --windows "$CP"`
   CATALINA_HOME=`cygpath --path --windows "$CATALINA_HOME"`
   JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
fi

echo Using CLASSPATH: $CP

# ----- Execute The Requested Command -----------------------------------------

$JAVA_HOME/bin/java $CATALINA_OPTS -classpath $CP org.apache.tools.ant.Main \
 -Dant.home=$ANT_HOME \
 -Dcatalina.home=$CATALINA_HOME \
 -buildfile $CATALINA_HOME/bin/tester.xml \
 "$@"
