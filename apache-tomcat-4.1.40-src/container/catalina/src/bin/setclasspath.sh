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
#  Set CLASSPATH and Java options
#
#  $Id: setclasspath.sh 743401 2009-02-11 17:01:58Z markt $
# -----------------------------------------------------------------------------

# Make sure prerequisite environment variables are set
if [ -z "$JAVA_HOME" ]; then
  echo "The JAVA_HOME environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi
if [ ! -x "$JAVA_HOME"/bin/java ]; then
  echo "Error: The JAVA_HOME/bin/java directory is missing or not executable."
  echo "The JAVA_HOME environment variable is not defined correctly."
  echo "This environment variable is needed to run this program."
  echo "NB: JAVA_HOME should point to a JDK not a JRE."
  exit 1
fi
if [ ! "$os400" ]; then
  if [ ! -x "$JAVA_HOME"/bin/jdb ]; then
    echo "Error: The JAVA_HOME/bin/jdb directory is missing or not executable."
    echo "The JAVA_HOME environment variable is not defined correctly."
    echo "This environment variable is needed to run this program."
    echo "NB: JAVA_HOME should point to a JDK not a JRE."
    exit 1
  fi
fi
if [ ! -x "$JAVA_HOME"/bin/javac ]; then
  echo "Error: The JAVA_HOME/bin/javac directory is missing or not executable."
  echo "The JAVA_HOME environment variable is not defined correctly."
  echo "This environment variable is needed to run this program."
  echo "NB: JAVA_HOME should point to a JDK not a JRE."
  exit 1
fi
if [ -z "$BASEDIR" ]; then
  echo "The BASEDIR environment variable is not defined"
  echo "This environment variable is needed to run this program"
  exit 1
fi
if [ ! -x "$BASEDIR"/bin/setclasspath.sh ]; then
  echo "The BASEDIR environment variable is not defined correctly"
  echo "This environment variable is needed to run this program"
  exit 1
fi

# Set the default -Djava.endorsed.dirs argument
JAVA_ENDORSED_DIRS="$BASEDIR"/common/endorsed

# Set standard CLASSPATH
CLASSPATH="$JAVA_HOME"/lib/tools.jar

# OSX hack to CLASSPATH
JIKESPATH=
if [ `uname -s` = "Darwin" ]; then
  OSXHACK="/System/Library/Frameworks/JavaVM.framework/Versions/CurrentJDK/Classes"
  if [ -d "$OSXHACK" ]; then
    for i in "$OSXHACK"/*.jar; do
      JIKESPATH="$JIKESPATH":"$i"
    done
  fi
fi

# Set standard commands for invoking Java.
_RUNJAVA="$JAVA_HOME"/bin/java
if [ ! "$os400" ]; then
  _RUNJDB="$JAVA_HOME"/bin/jdb
fi
_RUNJAVAC="$JAVA_HOME"/bin/javac
