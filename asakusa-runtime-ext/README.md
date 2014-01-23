# Adaptive Hadoop local mode configurator

This enables Asakusa batch applications to switch using Hadoop local mode when the input data size is so small.

## Usage

### Deploying Artifact
Put this artifact JAR onto `$ASAKUSA_HOME/core/lib`. 

### Hadoop Settings
This feature will rewrite following properties in Hadoop configuration files (maybe in `mapred-site.xml`):
* `mapred.job.tracker`
* `mapred.local.dir`
* `mapreduce.jobtracker.staging.root.dir`

Please remove `final` modifiers on above properties.

### Asakusa Framework Settings
Add following property in `$ASAKUSA_HOME/core/conf/asakusa-resources.xml`:

```xml
<configuration>
    ...
    
    <property>
        <name>com.asakusafw.autolocal.limit</name>
        <value>[limit input size (in bytes)]</value>
    </property>
    ...
</configurations>
```

Only if the total input size in job is less than specified one, it job will run as local mode.

### Cleaning Up
This feature will create a temporary files on `/tmp/hadoop-${user.name}/autolocal/...`.
