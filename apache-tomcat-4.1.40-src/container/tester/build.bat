@echo off

rem   Licensed to the Apache Software Foundation (ASF) under one or more
rem   contributor license agreements.  See the NOTICE file distributed with
rem   this work for additional information regarding copyright ownership.
rem   The ASF licenses this file to You under the Apache License, Version 2.0
rem   (the "License"); you may not use this file except in compliance with
rem   the License.  You may obtain a copy of the License at
rem 
rem       http://www.apache.org/licenses/LICENSE-2.0
rem 
rem   Unless required by applicable law or agreed to in writing, software
rem   distributed under the License is distributed on an "AS IS" BASIS,
rem   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem   See the License for the specific language governing permissions and
rem   limitations under the License.

rem ---------------------------------------------------------------------------
rem build.bat - Build Script for Tester
rem
rem Environment Variable Prerequisites:
rem
rem   ANT_HOME         Must point at your Ant installation [../jakarta-ant]
rem
rem   ANT_OPTS         Command line options to the Java runtime
rem                    that executes Ant [NONE]
rem
rem   JAVA_HOME        Must point at your Java Development Kit [REQUIRED]
rem
rem   XERCES_HOME      Must point at your Xerces installation
rem                    [../../xerces-1_3_0]
rem
rem $Id: build.bat 743401 2009-02-11 17:01:58Z markt $
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables ------------------------------------------

set _CLASSPATH=%CLASSPATH%
set _ANT_HOME=%ANT_HOME%
set _XERCES_HOME=%XERCES_HOME%


rem ----- Verify and Set Required Environment Variables -----------------------


if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=../../jakarta-ant
:gotAntHome

if not "%XERCES_HOME%" == "" goto gotXercesHome
set XERCES_HOME=../../xerces-1_3_0
:gotXercesHome


rem ----- Set Up The Runtime Classpath ----------------------------------------

if not "%CLASSPATH%" == "" set CLASSPATH=%CLASSPATH%;
set CLASSPATH=%CLASSPATH%;%ANT_HOME%\lib\ant.jar;%JAVA_HOME%\lib\tools.jar


rem ----- Execute The Requested Build -----------------------------------------

%JAVA_HOME%\bin\java %ANT_OPTS% org.apache.tools.ant.Main -Dant.home="%ANT_HOME%" -Djava.home="%JAVA_HOME%" -Dxerces.home="%XERCES_HOME%" %1 %2 %3 %4 %5 %6 %7 %8 %9


rem ----- Restore Environment Variables ---------------------------------------

:cleanup
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set ANT_HOME=%_ANT_HOME%
set _ANT_HOME=
set XERCES_HOME=%_XERCES_HOME%
set _XERCES_HOME=
:finish

