
; Tomcat 4 script for Nullsoft Installer
; $Id: tomcat.nsi 718145 2008-11-16 23:50:57Z markt $
!include "MUI.nsh"

Name "Apache Tomcat 4.1"
OutFile tomcat4.exe
CRCCheck on
SetCompress force
SetCompressor /SOLID lzma
SetDatablockOptimize on

!include "StrFunc.nsh"
${StrRep}

!define MUI_COMPONENTSPAGE_SMALLDESC
!define MUI_ICON tomcat.ico
!define MUI_UNICON tomcat.ico

!insertmacro MUI_PAGE_LICENSE "INSTALLLICENSE"

!define MUI_COMPONENTSPAGE_TEXT_TOP "This will install the Apache Tomcat 4.1 servlet container on your computer:"
!insertmacro MUI_PAGE_COMPONENTS

!define MUI_DIRECTORYPAGE_TEXT_TOP "Please select a location to install Tomcat 4.1 (or use the default):"
!insertmacro MUI_PAGE_DIRECTORY

!insertmacro MUI_PAGE_INSTFILES

Page custom configure "" ": Basic settings"

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

InstType Normal
InstType Minimum
InstType "Full (with source code)"
AutoCloseWindow false
ShowInstDetails show
SetOverwrite on
SetDateSave on

InstallDir "$PROGRAMFILES\Apache Software Foundation\Tomcat 4.1"
InstallDirRegKey HKLM "SOFTWARE\Apache Software Foundation\Tomcat\4.1" ""

ReserveFile "config.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS
!insertmacro MUI_RESERVEFILE_LANGDLL

SubSection /e "Main" Section1
  Section "Tomcat (required)" Section2

    SectionIn 1 2 3 RO

    SetOutPath $INSTDIR
    File tomcat.ico
    File LICENSE
    File NOTICE
    SetOutPath $INSTDIR\bin
    File /r /x *.exe bin\*.*
    SetOutPath $INSTDIR\common
    File /r common\*.*
    SetOutPath $INSTDIR\conf
    File /r conf\*.*
    SetOutPath $INSTDIR\shared
    File /nonfatal /r shared\*.*
    SetOutPath $INSTDIR\logs
    File /nonfatal /r logs\*.*
    SetOutPath $INSTDIR\server
    File /r server\*.*
    SetOutPath $INSTDIR\work
    File /nonfatal /r work\*.*
    SetOutPath $INSTDIR\temp
    File /nonfatal /r temp\*.*
    SetOutPath $INSTDIR\webapps
    File webapps\*.xml
    SetOutPath $INSTDIR\webapps\ROOT
    File /r webapps\ROOT\*.*

    Call findJavaPath
    Pop $2

    CopyFiles "$2\lib\tools.jar" "$INSTDIR\common\lib" 4500

    WriteUninstaller "$INSTDIR\uninst-tomcat4.exe"
    WriteRegStr HKLM "SOFTWARE\Apache Software Foundation\Tomcat\4.1" "" $INSTDIR
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 4.1" \
                     "DisplayName" "Apache Tomcat 4.1 (remove only)"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 4.1" \
                     "UninstallString" '"$INSTDIR\uninst-tomcat4.exe"'

  SectionEnd

  Section "NT Service (NT/2k/XP only)" Section3

    SectionIn 3

    Call findJVMPath
    Pop $2

    SetOutPath $INSTDIR\bin
    File /oname=tomcat4.exe bin\tomcat4.exe
    File /oname=tomcat4w.exe bin\tomcat4w.exe

    ExecWait '"$INSTDIR\bin\tomcat4.exe" //IS//Tomcat4 --DisplayName "Apache Tomcat 4.1" --Description "Apache Tomcat @VERSION@ Server - http://tomcat.apache.org/" --LogPath "$INSTDIR\logs" --Install "$INSTDIR\bin\tomcat4.exe" --Jvm "$2" --StartPath "$INSTDIR" --StopPath "$INSTDIR"'

    ExecWait '"$INSTDIR\bin\tomcat4.exe" //US//Tomcat4 --Startup auto'

    ExecWait '"$INSTDIR\bin\tomcat4.exe" //US//Tomcat4 --Classpath "$INSTDIR\bin\bootstrap.jar" --StartClass org.apache.catalina.startup.Bootstrap --StopClass org.apache.catalina.startup.Bootstrap --StartParams start --StopParams stop  --StartMode jvm --StopMode jvm'
    ExecWait '"$INSTDIR\bin\tomcat4.exe" //US//Tomcat4 --JvmOptions "-Dcatalina.home=$INSTDIR#-Dcatalina.base=$INSTDIR#-Djava.endorsed.dirs=$INSTDIR\common\endorsed#-Djava.io.tmpdir=$INSTDIR\temp" --StdOutput auto --StdError auto'
    
    ; Behave like Apache Httpd (put the icon in try on login)
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Run" "ApacheTomcatMonitor" '"$INSTDIR\bin\tomcat4w.exe" //MS//Tomcat4'
    Exec '"$INSTDIR\bin\tomcat4w.exe" //MS//Tomcat4'

    ClearErrors

  SectionEnd

  Section "JSP Development Shell Extensions" Section4

    SectionIn 1 2 3
    ; back up old value of .jsp
    ReadRegStr $1 HKCR ".jsp" ""
    StrCmp $1 "" Label1
    StrCmp $1 "JSPFile" Label1
    WriteRegStr HKCR ".jsp" "backup_val" $1

    Label1:

    WriteRegStr HKCR ".jsp" "" "JSPFile"
    WriteRegStr HKCR "JSPFile" "" "Java Server Pages source"
    WriteRegStr HKCR "JSPFile\shell" "" "open"
    WriteRegStr HKCR "JSPFile\DefaultIcon" "" "$INSTDIR\tomcat.ico"
    WriteRegStr HKCR "JSPFile\shell\open\command" "" 'notepad.exe "%1"'

  SectionEnd

  Section "Tomcat Start Menu Group" Section5

    SectionIn 1 2 3

    Call findJavaPath
    Pop $2

    SetOutPath "$SMPROGRAMS\Apache Tomcat 4.1"

    CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Tomcat Home Page.lnk" \
                   "http://jakarta.apache.org/tomcat"

    CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Uninstall Tomcat 4.1.lnk" \
                   "$INSTDIR\uninst-tomcat4.exe"

    CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Tomcat 4.1 Program Directory.lnk" \
                   "$INSTDIR"

    CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Configure Tomcat.lnk" \
                   "$INSTDIR\bin\tomcat4w.exe" "" \
                   "$INSTDIR\tomcat.ico" 0 SW_SHOWNORMAL

  SectionEnd
SubSectionEnd

SubSection "Documentation and Examples" Section6
  Section "Tomcat Documentation" Section7

    SectionIn 1 3
    SetOutPath $INSTDIR\webapps\tomcat-docs
    File /r webapps\tomcat-docs\*.*

    IfFileExists "$SMPROGRAMS\Apache Tomcat 4.1" 0 NoLinks

    SetOutPath "$SMPROGRAMS\Apache Tomcat 4.1"

    CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Tomcat Documentation.lnk" \
                   "$INSTDIR\webapps\tomcat-docs\index.html"

    NoLinks:

  SectionEnd

  Section "Example Web Applications" Section8

    SectionIn 1 3

    SetOverwrite off
    SetOutPath $INSTDIR\conf
    File conf\server.xml
    SetOverwrite on
    SetOutPath $INSTDIR\webapps\examples
    File /r webapps\examples\*.*
    SetOutPath $INSTDIR\webapps\webdav
    File /r webapps\webdav\*.*

  SectionEnd

SubSEctionEnd
SubSection "Developer Resources" Section9

  Section "Tomcat Source Code" Section10

    SectionIn 3
    SetOutPath $INSTDIR\src
    File /r src\*.*

  SectionEnd

SubSectionEnd

LangString DESC_Section1 ${LANG_ENGLISH} "The core Tomcat components."
LangString DESC_Section2 ${LANG_ENGLISH} "The Tomcat servlet container."
LangString DESC_Section3 ${LANG_ENGLISH} "Additional files and configuration to enable Tomcat to be run as a Windows service."
LangString DESC_Section4 ${LANG_ENGLISH} "Configure NotePad as the default editor for JSP files."
LangString DESC_Section5 ${LANG_ENGLISH} "Add Tomcat icons to the Start menu."
LangString DESC_Section6 ${LANG_ENGLISH} "Optional web applications."
LangString DESC_Section7 ${LANG_ENGLISH} "Deploys the documentation web aplication."
LangString DESC_Section8 ${LANG_ENGLISH} "Deploys the JSP & servlets examples web application and the WebDAV example web application."
LangString DESC_Section9 ${LANG_ENGLISH} "Optional resource for developers."
LangString DESC_Section10 ${LANG_ENGLISH} "Places the Tomcat and Tomcat Connector source code as ZIP files in the Tomcat installation directory."

!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${Section1} $(DESC_Section1)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section2} $(DESC_Section2)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section3} $(DESC_Section3)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section4} $(DESC_Section4)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section5} $(DESC_Section5)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section6} $(DESC_Section6)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section7} $(DESC_Section7)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section8} $(DESC_Section8)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section9} $(DESC_Section9)
  !insertmacro MUI_DESCRIPTION_TEXT ${Section10} $(DESC_Section10)
!insertmacro MUI_FUNCTION_DESCRIPTION_END

Function .onInit

  ClearErrors

  Call findJavaPath
  Pop $1
  MessageBox MB_OK "Using Java Development Kit found in $1" /SD IDOK
  
  InitPluginsDir
  File /oname=$PLUGINSDIR\config.ini config.ini

FunctionEnd


Function .onInstSuccess
  IfSilent +2
  ExecShell open '$SMPROGRAMS\Apache Tomcat 4.1'

FunctionEnd


; =====================
; FindJavaPath Function
; =====================
;
; Find the JAVA_HOME used on the system, and put the result on the top of the
; stack
; Will exit if the path cannot be determined
;
Function findJavaPath

  ClearErrors

  ReadEnvStr $1 JAVA_HOME

  IfErrors 0 FoundJDK

  ClearErrors

  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Development Kit\$2" "JavaHome"
  ReadRegStr $3 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $4 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$3" "RuntimeLib"

  FoundJDK:

  IfErrors 0 NoAbort
    MessageBox MB_OK "Couldn't find a Java Development Kit installed on this \
computer. Please download one from http://java.sun.com. If there is already \ a JDK installed on this computer, set an environment variable JAVA_HOME to the \ pathname of the directory where it is installed."
    Abort

  NoAbort:

  ; Put the result in the stack
  Push $1

FunctionEnd


; ====================
; FindJVMPath Function
; ====================
;
; Find the full JVM path, and put the result on top of the stack
; Will exit if the path cannot be determined
;
Function findJVMPath

  ReadEnvStr $1 JAVA_HOME
  IfFileExists "$1\jre\bin\hotspot\jvm.dll" 0 TryJDK14
    StrCpy $2 "$1\jre\bin\hotspot\jvm.dll"
    Goto EndIfFileExists
  TryJDK14:
  IfFileExists "$1\jre\bin\server\jvm.dll" 0 TryClassic
    StrCpy $2 "$1\jre\bin\server\jvm.dll"
    Goto EndIfFileExists
  TryClassic:
  IfFileExists "$1\jre\bin\classic\jvm.dll" 0 JDKNotFound
    StrCpy $2 "$1\jre\bin\classic\jvm.dll"
    Goto EndIfFileExists
  JDKNotFound:
    SetErrors
  EndIfFileExists:

  IfErrors 0 FoundJVMPath

  ClearErrors

  ReadRegStr $1 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
  ReadRegStr $2 HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$1" "RuntimeLib"
  
  FoundJVMPath:
  
  IfErrors 0 NoAbort
    MessageBox MB_OK "Couldn't find a Java Development Kit installed on this \
computer. Please download one from http://java.sun.com."
    Abort

  NoAbort:

  ; Put the result in the stack
  Push $2

FunctionEnd


; ==================
; Configure Function
; ==================
;
; Display the configuration dialog boxes, read the values entered by the user,
; and build the configuration files
;
Function configure
  ; Output files needed for the configuration dialog
  SetOverwrite on

  Push $1
  InstallOptions::dialog $PLUGINSDIR\config.ini
  Pop $1

  ReadINIStr $R0 $PLUGINSDIR\config.ini "Field 2" State
  ReadINIStr $R1 $PLUGINSDIR\config.ini "Field 5" State
  ReadINIStr $R2 $PLUGINSDIR\config.ini "Field 7" State

  Push $R1
  Call xmlEscape
  Pop $R1
  
  Push $R2
  Call xmlEscape
  Pop $R2

  StrCpy $R4 'port="$R0"'
  StrCpy $R5 '<user name="$R1" password="$R2" roles="admin,manager" />'

  SetOutPath $TEMP\confinstall
  File /r confinstall\*.*

  ; Build final server.xml
  Delete "$INSTDIR\conf\server.xml"
  FileOpen $R9 "$INSTDIR\conf\server.xml" w

  Push "$TEMP\confinstall\server_1.xml"
  Call copyFile
  FileWrite $R9 $R4
  Push "$TEMP\confinstall\server_2.xml"
  Call copyFile

  FileClose $R9

  ; Build final tomcat-users.xml
  Delete "$INSTDIR\conf\tomcat-users.xml"
  FileOpen $R9 "$INSTDIR\conf\tomcat-users.xml" w

  Push "$TEMP\confinstall\tomcat-users_1.xml"
  Call copyFile
  FileWrite $R9 $R5
  Push "$TEMP\confinstall\tomcat-users_2.xml"
  Call copyFile

  FileClose $R9

  ; Creating a few shortcuts
  IfFileExists "$SMPROGRAMS\Apache Tomcat 4.1" 0 NoLinks

  SetOutPath "$SMPROGRAMS\Apache Tomcat 4.1"

  CreateShortCut "$SMPROGRAMS\Apache Tomcat 4.1\Tomcat Administration.lnk" \
                 "http://127.0.0.1:$R0/admin"

 NoLinks:

  Delete $7
  Delete $8
  RMDir /r "$TEMP\confinstall"

  Call startService

  Sleep 500
  BringToFront

FunctionEnd


Function xmlEscape
  Pop $0
  ${StrRep} $0 $0 "&" "&amp;"
  ${StrRep} $0 $0 "$\"" "&quot;"
  ${StrRep} $0 $0 "<" "&lt;"
  ${StrRep} $0 $0 ">" "&gt;"
  Push $0
FunctionEnd


; =================
; CopyFile Function
; =================
;
; Copy specified file contents to $R9
;
Function copyFile

  ClearErrors

  Pop $0

  FileOpen $1 $0 r

 NoError:

  FileRead $1 $2
  IfErrors EOF 0
  FileWrite $R9 $2

  IfErrors 0 NoError

 EOF:

  FileClose $1

  ClearErrors

FunctionEnd


; =====================
; StartService Function
; =====================
;
; Start Tomcat NT Service
;
Function startService

  IfFileExists "$INSTDIR\bin\tomcat4.exe" 0 NoService
  ExecWait 'net start "Apache Tomcat 4.1"'
  Sleep 4000

 NoService:

FunctionEnd


; =====================
; StopService Function
; =====================
;
; Stop Tomcat NT Service
;
Function un.stopService
  IfFileExists "$INSTDIR\bin\tomcat4.exe" 0 NoService
  
  ; Stop Tomcat service monitor if running
  Execwait '"$INSTDIR\bin\tomcat4w.exe" //MQ//Tomcat4'
  ; Delete Tomcat service
  Execwait '"$INSTDIR\bin\tomcat4.exe" //DS//Tomcat4'
  ClearErrors

 NoService:

FunctionEnd


UninstallText "This will uninstall Apache Tomcat 4.1 from your system:"


Section Uninstall

  Delete "$INSTDIR\uninst-tomcat4.exe"

  ; Stopping NT service (if in use)
  Call un.stopService

  ReadRegStr $1 HKCR ".jsp" ""
  StrCmp $1 "JSPFile" 0 NoOwn ; only do this if we own it
    ReadRegStr $1 HKCR ".jsp" "backup_val"
    StrCmp $1 "" 0 RestoreBackup ; if backup == "" then delete the whole key
      DeleteRegKey HKCR ".jsp"
    Goto NoOwn
    RestoreBackup:
      WriteRegStr HKCR ".jsp" "" $1
      DeleteRegValue HKCR ".jsp" "backup_val"
  NoOwn:

  ExecWait '"$INSTDIR\bin\tomcat.exe" -uninstall "Apache Tomcat 4.1"'
  ClearErrors

  DeleteRegKey HKCR "JSPFile"
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\Apache Tomcat 4.1"
  DeleteRegKey HKLM "SOFTWARE\Apache Software Foundation\Tomcat\4.1"
  DeleteRegValue HKLM "Software\Microsoft\Windows\CurrentVersion\Run" "ApacheTomcatMonitor"
  RMDir /r "$SMPROGRAMS\Apache Tomcat 4.1"
  Delete "$INSTDIR\tomcat.ico"
  Delete "$INSTDIR\LICENSE"
  Delete "$INSTDIR\NOTICE"
  RMDir /r "$INSTDIR\bin"
  RMDir /r "$INSTDIR\common"
  Delete "$INSTDIR\conf\*.dtd"
  RMDir /r "$INSTDIR\shared"
  RMDir "$INSTDIR\logs"
  RMDir /r "$INSTDIR\server"
  RMDir "$INSTDIR\webapps\*.xml"
  RMDir /r "$INSTDIR\webapps\ROOT"
  RMDir /r "$INSTDIR\webapps\tomcat-docs"
  RMDir /r "$INSTDIR\webapps\examples"
  RMDir /r "$INSTDIR\webapps\webdav"
  RMDir "$INSTDIR\webapps"
  RMDir /r "$INSTDIR\work"
  RMDir /r "$INSTDIR\temp"
  RMDir /r "$INSTDIR\src"
  RMDir "$INSTDIR"

  ; if $INSTDIR was removed, skip these next ones
  IfFileExists "$INSTDIR" 0 Removed 
    MessageBox MB_YESNO|MB_ICONQUESTION \
      "Remove all files in your Tomcat 4.1 directory? (If you have anything \
 you created that you want to keep, click No)" IDNO Removed
    Delete "$INSTDIR\*.*" ; this would be skipped if the user hits no
    RMDir /r "$INSTDIR"
    Sleep 500
    IfFileExists "$INSTDIR" 0 Removed 
      MessageBox MB_OK|MB_ICONEXCLAMATION \
                 "Note: $INSTDIR could not be removed."
  Removed:

SectionEnd
