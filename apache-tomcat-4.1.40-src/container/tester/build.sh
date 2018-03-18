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
# build.sh - Build Script for Tester
#
# Environment Variable Prerequisites:
#
#   ANT_HOME         Must point at your Ant installation [../jakarta-ant]
#
#   ANT_OPTS         Command line options to the Java runtime
#                    that executes Ant [NONE]
#
#   JAVA_HOME        Must point at your Java Development Kit [REQUIRED]
#
#   XERCES_HOME      Must point at your Xerces installation
#                    [../../xerces-1_3_0]
#
# $Id: build.sh 743401 2009-02-11 17:01:58Z markt $
# -----------------------------------------------------------------------------


# ----- Verify and Set Required Environment Variables -------------------------

if [ "$JAVA_HOME" = "" ] ; then
  echo You must set JAVA_HOME to point at your Java Development Kit install
  exit 1
fi

if [ "$ANT_HOME" = "" ] ; then
  ANT_HOME=../jakarta-ant
fi

if [ "$ANT_OPTS" = "" ] ; then
  ANT_OPTS=""
fi

if [ "$XERCES_HOME" = "" ] ; then
  XERCES_HOME="../../xerces-1_3_0"
fi


# ----- Set Up The Runtime Classpath ------------------------------------------

CP=$ANT_HOME/lib/ant.jar:$JAVA_HOME/lib/tools.jar

if [ "$CLASSPATH" != "" ] ; then
  CP=$CLASSPATH:$CP
fi

# ----- Execute The Requested Build -------------------------------------------

$JAVA_HOME/bin/java $ANT_OPTS -classpath $CP org.apache.tools.ant.Main \
 -Dant.home=$ANT_HOME \
 -Djava.home=$JAVA_HOME \
 -Dxerces.home=$XERCES_HOME \
 "$@"
