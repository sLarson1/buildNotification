@echo off
title Check Build
call ant build -listener buildNotification.CheckBuild