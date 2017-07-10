# WindGate example

## files

* `README.md`
  * this file
* `build.gradle`
  * build script
* `src/main/dmdl`
  * DMDL scripts
* `src/main/conf/windgate/profile`
  * WindGate profiles
* `src/main/conf/windgate/script`
  * WindGate scripts

## Example profiles and scripts

* copies `$PROJECT_HOME/{input=>output}.csv` directory
  * profile - `copy`
  * session - *anything*
  * script - `$ASAKUSA_HOME/windgate/script/copy.properties`
  * batch-id - `app`
  * flow-id - `flow`
  * execution-id - *anything*
  * arguments - *don't care*
* put `$PROJECT_HOME/input.csv` into remote `$PROJECT_HOME/tmp.bin` via SSH
  * profile - `copy-remote`
  * session - `begin`
  * script - `$ASAKUSA_HOME/windgate/script/remote-put.properties`
  * batch-id - `app`
  * flow-id - `flow`
  * execution-id - *anything*
  * arguments - *don't care*
* put `$PROJECT_HOME/output.csv` from remote `$PROJECT_HOME/tmp.bin` via SSH
  * profile - `copy-remote`
  * session - `end`
  * script - `$ASAKUSA_HOME/windgate/script/remote-get.properties`
  * batch-id - `app`
  * flow-id - `flow`
  * execution-id - *anything*
  * arguments - *don't care*


## usage

```sh
gradle installAsakusafw
export PROJECT_HOME=/path/to/project
$ASAKUSA_HOME/windgate/bin/process.sh arguments...
```
