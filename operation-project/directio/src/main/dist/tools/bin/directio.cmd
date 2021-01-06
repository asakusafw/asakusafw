@REM
@REM Copyright 2011-2021 Asakusa Framework Team.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM     http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off
setlocal

if not defined ASAKUSA_HOME (
    echo environment variable %%ASAKUSA_HOME%% must be defined
    exit /b 1
)

set CALLER_CWD=%cd%
cd /d %USERPROFILE%

set libdir=%ASAKUSA_HOME%\tools\lib
set java_classpath=%libdir%\asakusa-directio-tools.jar
set main_class=com.asakusafw.operation.tools.directio.bootstrap.Bootstrap

set java_props=-Dcli.name=directio
set java_props=%java_props% -Dhadoop.home.dir=%ASAKUSA_HOME%\hadoop

java %ASAKUSA_CLIENT_OPTS% -classpath %java_classpath% %java_props% %main_class% %*
