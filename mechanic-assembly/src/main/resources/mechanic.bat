@echo off
if "%OS%" == "Windows_NT" setlocal

setlocal enabledelayedexpansion

rem %~dp0 is expanded pathname of the current script under NT
set MECHANIC_HOME="%~dp0"

set CP=.
for /R %MECHANIC_HOME% %%f in (mechanic*.jar) do set CP=!CP!;%%f
for /R %MECHANIC_HOME%\lib %%f in (*.jar) do set CP=!CP!;%%f

rem get command line args into a variable
set CMD_LINE_ARGS=%1
if ""%1""=="""" goto done
shift
:setup
if ""%1""=="""" goto done
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto setup
:done

IF NOT DEFINED JAVA_OPTS set JAVA_OPTS=

java -cp "%CP%" %JAVA_OPTS% org.lorislab.mechanic.Main %CMD_LINE_ARGS%
