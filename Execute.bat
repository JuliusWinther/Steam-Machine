echo off
set arg1=%1
set arg2=%2
set arg3=%3
set arg4=%4
shift
shift
shift
if %arg1%==0 cd %arg2%
if %arg1%==1 %arg2%
cd %arg3%
start "" %arg4%