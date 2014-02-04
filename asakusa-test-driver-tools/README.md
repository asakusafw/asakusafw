#Testing Utilities based on TestDriver

## BatchTestRunner
BatchTestRunner enable to execute DSL compiled Asakusa Application through TestDriver runtime.

### Synopsis
`java -classpath ... com.asakusafw.testdriver.tools.runner.BatchTestRunner -b <batch_id> [-A <name=value>] ` 

### Description
Please add this artifact to test runtime classpath,
and run `com.asakusafw.testdriver.tools.runner.BatchTestRunner`
with following options.

<dl>
  <dt>-A,--argument <em>name=value</em></dt>
  <dd>batch argument</dd>

  <dt>-b,--batch <em>batch_id</em></dt>
  <dd>batch ID</dd>
</dl>
