@echo off
set arg1=%1
set arg2=%2
shift
shift
RAR.exe a -ep1 -r -u %arg1% %arg2%