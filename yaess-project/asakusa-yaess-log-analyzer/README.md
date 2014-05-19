# YAESS Log Analyzer

## Synopsis
```sh
java -cp ... com.asakusafw.yaess.tools.log.cli.Main \
    -i <class extends com.asakusafw.yaess.tools.log.YaessLogInput> \
    -o <class extends com.asakusafw.yaess.tools.log.YaessLogOutput> \
   [-I <input-option-name=value>]* \
   [-O <output-option-name=value>]*
```

## Basic usage
For summarizing job executions from basic YAESS log file:

```sh
java -cp ... com.asakusafw.yaess.tools.log.cli.Main \
    -i com.asakusafw.yaess.tools.log.basic.BasicYaessLogInput \
    -o com.asakusafw.yaess.tools.log.summarize.SummarizeYaessLogOutput \
    -I file=</path/to/yaess.log> \
   [-I encoding=<log-encoding>] \
    -O file=</path/to/output.csv> \
    -O 'code=YS-CORE-\w04\d{4}' \
   [-O encoding=<csv-encoding>]
```
