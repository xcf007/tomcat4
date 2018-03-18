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
rem tester.bat - Execute Test Application Client
rem
rem Environment Variable Prequisites:
rem
rem   ANT_HOME       Ant runtime directory
rem
rem   CATALINA_HOME (Optional) May point at your Catalina "build" directory.
rem                 If not present, the current working directory is assumed.
rem
rem   CATALINA_OPTS (Optional) Java runtime options used when the "start",
rem                 "stop", or "run" command is executed.
rem
rem   JAVA_HOME     Must point at your Java Development Kit installation.
rem
rem $Id: tester.bat 743401 2009-02-11 17:01:58Z markt $
rem ---------------------------------------------------------------------------


rem ----- Save Environment Variables That May Change --------------------------

set _ANT_HOME=%ANT_HOME%
set _CATALINA_HOME=%CATALINA_HOME%
set _CLASSPATH=%CLASSPATH%
set _CP=%CP%


rem ----- Verify and Set Required Environment Variables -----------------------

if not "%JAVA_HOME%" == "" goto gotJavaHome
echo You must set JAVA_HOME to point at your Java Development Kit installation
goto cleanup
:gotJavaHome

if not "%ANT_HOME%" == "" goto gotAntHome
set ANT_HOME=../../jakarta-ant
:gotAntHome

if not "%CATALINA_HOME%" == "" goto gotCatalinaHome
set CATALINA_HOME=.
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okCatalinaHome
set CATALINA_HOME=..\..\..\build
:gotCatalinaHome
if exist "%CATALINA_HOME%\bin\catalina.bat" goto okCatalinaHome
echo Unable to determine the value of CATALINA_HOME
goto cleanup
:okCatalinaHome


rem ----- Prepare Appropriate Java Execution Commands -------------------------

if not "%OS%" == "Windows_NT" goto noTitle
set _RUNJAVA="%JAVA_HOME%\bin\java"
goto gotTitle
:noTitle
set _RUNJAVA="%JAVA_HOME%\bin\java"
:gotTitle

rem ----- Set Up The Runtime Classpath ----------------------------------------

set CP=%CATALINA_HOME%\webapps\tester\WEB-INF\lib\tester.jar;%CATALINA_HOME%\server\lib\jaxp.jar;%CATALINA_HOME%\server\lib\crimson.jar;%ANT_HOME%\lib\ant.jar;%ANT_HOME%\lib\ant-launcher.jar
set CLASSPATH=%CP%
echo Using CLASSPATH: %CLASSPATH%


rem ----- Execute The Requested Command ---------------------------------------

%_RUNJAVA% %CATALINA_OPTS% org.apache.tools.ant.Main -Dant.home="%ANT_HOME%" -Dcatalina.home="%CATALINA_HOME%" -buildfile tester.xml" %1 %2 %3 %4 %5 %6 %7 %8 %9

:cleanup
set CATALINA_HOME=%_CATALINA_HOME%
set _CATALINA_HOME=
set CLASSPATH=%_CLASSPATH%
set _CLASSPATH=
set CP=%_CP%
set ANT_HOME=%_ANT_HOME%
set _ANT_HOME=
set _RUNJAVA=
:finish
