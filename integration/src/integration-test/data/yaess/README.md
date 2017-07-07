# YAESS example

## files

* `README.md`
  * this file
* `build.gradle`
  * build script
* `src/main/conf/yaess/conf`
  * YAESS profile template
* `src/main/conf/bin/put.sh`
  * Example YAESS command
* `src/main/conf/batchapps/testing`
  * Example batch application
* `src/main/conf/batchapps/whadoop`
  * Example batch application
* `src/main/java`
  * Example application for `whadoop` batch

## Example batch application

* batch: `testing`
  * jobflow: `prepare`
    * main phase
      * `put` - `bin/put.sh "prepare" > "$PROJECT_HOME/prepare.txt"`
  * jobflow: `work`, depends on `prepare`
    * import phase
      * `put` - `bin/put.sh "import" > "$PROJECT_HOME/import.txt"`
    * main phase
      * `put` - `bin/put.sh "main" > "$PROJECT_HOME/main.txt"`
    * export phase
      * `put` - `bin/put.sh "export" > "$PROJECT_HOME/export.txt"`
    * finalize phase
      * `put` - `bin/put.sh "finalize" > "$PROJECT_HOME/finalize.txt"`
* batch: `whadoop`
  * jobflow: `work`
    * main phase
      * `hadoop` - `com.asakusafw.integration.core.yaess.Main`
        * -> put file onto `$PROJECT_HOME/hadoop.txt`
    * (cleanup phase)
      * `hadoop` - `com.asakusafw.runtime.stage.CleanupStageClient`
        * -> put file onto `$PROJECT_HOME/cleanup.txt`

## usage

```sh
gradle installAsakusafw
$ASAKUSA_HOME/yaess/bin/yaess-batch.sh testing -V PROJECT_HOME=/path/to/project
```
