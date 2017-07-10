# Direct I/O transactions example

## files

* `README.md`
  * this file
* `build.gradle`
  * build script
* `src/main/conf/var/system`
  * Direct I/O system directory template
* `src/main/conf/var/work`
  * Direct I/O root working directory template
* `src/main/conf/var/data`
  * Direct I/O root data directory template
* `src/main/conf/core/conf/asakusa-resources.xml`
  * configuration file to access the custom Direct I/O locations

## Direct I/O configurations

* root data source name
  * `root`
* data directory of root data source
  * `$ASAKUSA_HOME/var/data`
* temporary working directory of root data source
  * `$ASAKUSA_HOME/var/work`
* Direct I/O system directory
  * `$ASAKUSA_HOME/var/system`

## in-progress transactions

This project includes following transactions:

* applied transaction
  * status: applied
  * files: `/applied.txt`
* committed transaction
  * execution ID: `committed`
  * status: committed
  * files: `/committed.txt`
* uncommitted transaction
  * execution ID: `uncommitted`
  * status: NOT committed
  * files: `/uncommitted.txt`

## usage

```sh
gradle installAsakusafw

$ASAKUSA_HOME/directio/bin/<command>.sh
```
