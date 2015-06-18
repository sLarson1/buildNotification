@echo off
title Check Deployment
REM Example:
%JAVA_HOME%\bin\java -jar BuildNotification-0.0.1-SNAPSHOT.jar http://localhost 60 10 300

timeout /T 300