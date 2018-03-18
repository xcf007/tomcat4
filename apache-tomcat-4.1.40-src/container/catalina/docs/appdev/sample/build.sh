#!/bin/sh
# build.sh -- Build Script for the "Hello, World" Application
# $Id: build.sh 743401 2009-02-11 17:01:58Z markt $

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

# Identify the custom class path components we need
CP=$CATALINA_HOME/lib/ant.jar:$CATALINA_HOME/lib/servlet.jar
CP=$CP:$CATALINA_HOME/lib/jaxp.jar:$CATALINA_HOME/lib/parser.jar
CP=$CP:$JAVA_HOME/lib/tools.jar

# Execute ANT to perform the requested build target
java -classpath $CP:$CLASSPATH org.apache.tools.ant.Main \
  -Dtomcat.home=$CATALINA_HOME "$@"
