==============================
Asakusa Framework 管理者ガイド
==============================

この文書では、Asakusa FrameworkとバッチアプリケーションをHadoopクラスター上にデプロイする手順について解説します。

HadoopクラスターとDBサーバの準備
================================
Asakusa Framework本体とバッチアプリケーションをデプロイするためのHadoopクラスターを用意します。また、Asakusa ThunderGate（以下「ThunderGate」）がデータの入出力を行う対象とするDBサーバを用意します。このDBサーバを本ドキュメントでは「データベースノード」とよびます。

Hadoopクラスターのいずれかのサーバ（通常マスターノード）に、ThunderGateのコンポーネントのうち「Extractor」「Collector」と呼ばれる、DFS(HDFS)に対する入出力処理を行うコンポーネントをインストールします。「Extractor」「Collector」をインストールするサーバを本ドキュメントでは「Hadoopクライアントノード」とよびます。

Asakusa Frameworkのデプロイを行う前に、Hadoopクラスターとデータベースノードに対して次の環境構築が行われていることを確認してください。なお、各ソフトウェアの対応バージョンについては、GitHubのWikiに公開されているドキュメント「Target Platform ja」 [#]_ を参照してください。

..  [#] https://github.com/asakusafw/asakusafw/wiki/Target-Platform-ja

Hadoopクラスターの環境構築
--------------------------
Hadoopクラスターに対して以下の環境構築を実施し、動作確認を行ってください。

* Asakusa管理用のOSユーザ(以下ASAKUSA_USER)を作成する。
* Hadoopのインストールを行い、完全分散モードの設定を行う。
* ASAKUSA_USERから完全分散モードでHadoopのサンプルジョブを実行し、正常に動作することを確認する。
 
データベースノード
------------------
データベースノードに対して以下の環境構築を実施し、動作確認を行ってください。

* Asakusa管理用のOSユーザ(以下ASAKUSA_USER)を作成する。
* MySQL Serverのインストールと動作確認を行う。
* データベースノードのASAKUSA_USERからHadoopクライアントノードのASAKUSA_USERに対してパスフレーズ無しでsshの実行を可能とする。

..  warning::
    HadoopクラスターとデータベースノードのASAKUSA_USERは必ず同一ユーザ名で作成してください。

デプロイに使用するファイル
==========================
Asakusa Framework本体とバッチアプリケーションのデプロイ作業を行うにあたって、以下のファイルを用意します。

Asakusa Framworkのデプロイに必要なファイル
------------------------------------------
Asakusa Frameworkのデプロイ対象ファイル一式はGitHub上のソースから作成する必要があります。

以下の作業はAsakusa Frameworkの開発環境と同じバージョンのMavenが必要です。開発環境上にて作業することを推奨します。

Asakusa Frameworkのソースアーカイブを取得
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Asakusa FrameworkのGitHubリポジトリ [#]_ から、Asakusa Frameworkのソースアーカイブを取得します。

..  [#] https://github.com/asakusafw/asakusafw

以下はwgetを使ってAsakusa Framework ver0.2.0を取得する例です。

..  code-block:: sh

    wget http://github.com/asakusafw/asakusafw/zipball/0.2.0

Asakusa Frameworkのビルド
~~~~~~~~~~~~~~~~~~~~~~~~~
アーカイブを展開し、アーカイブに含まれるプロジェクト「asakusa-aggregator」のpom.xmlに対してinstallフェーズを実行し、Asakusa Frameworkの全モジュールをビルドします。

以下の例に沿ってビルドを実施して下さい。「BUILD SUCCESS」が出力されることを確認してください。

..  code-block:: sh

    unzip asakusafw-asakusafw-*.zip
    cd asakusafw-asakusafw-*/asakusa-aggregator
    mvn clean install -Dmaven.test.skip=true

Asakusa Frameworkのデプロイアーカイブ生成
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アーカイブに含まれるプロジェクト「asakusa-distribution」のpom.xmlに対してassebmbly:singleゴールを実行し、Hadoopクラスターにデプロイするアーカイブファイルを作成します。

以下の例に沿ってビルドを実施して下さい。「BUILD SUCCESS」が出力されることを確認してください。

..  code-block:: sh

    cd ../asakusa-distribution
    mvn clean assembly:single

デプロイアーカイブの確認
~~~~~~~~~~~~~~~~~~~~~~~~
「asakusa-distribution」のtagetディレクトリ配下に、以下のファイルが作成されていることを確認します。

1. asakusa-distribution-${version}-prod-hc.tar.gz
    * HadoopクラスターのHadoopクライアントノードに展開するアーカイブ。
2. asakusa-distribution-${version}-prod-db.tar.gz
    * データベースノードに展開するアーカイブ。
3. asakusa-distribution-${version}-prod-cleaner.tar.gz
    * Asakusa Frameworkが提供するクリーニングツールのデプロイに使用するアーカイブ

また、targetディレクトリには以下のファイルも作成されますが、
このファイルは開発環境で使用するアーカイブのため本ドキュメントによる手順では使用しません。

4. asakusa-distribution-${version}-dev.tar.gz
    * Asakusa Frameworkの開発環境のインストールに使用するアーカイブ

バッチアプリケーションのデプロイに必要なファイル
------------------------------------------------
以下のファイル一式は、アプリケーション開発者が開発環境上で作成／用意します。

1. バッチコンパイルにより生成したバッチアプリケーションのjarファイル（「${artifactid}-batchapps-${version}.jar」）
    * 開発環境上でバッチコンパイルを実行すると、ワークスペース上の「target」配下に作成されます。
    * 詳しくは  :doc:`maven-archetype` の :ref:`maven-archetype-batch-compile` を参照してください。
2. アプリケーション共通ライブラリ
    * バッチアプリケーションで使用する共通ライブラリ（Hadoopによって提供されているライブラリ以外のもの、例えばApache Commons Lang等）が必要な場合はそのjarファイルを用意します。
3. アプリケーション用テーブルDDL
    * アプリケーションの初期セットアップに使用するDDL。データベースノード上のDBMSに適用する。

Asakusa Frameworkのデプロイ
===========================
Asakusa FrameworkをHadoppクラスターとデータベースノードにデプロイします。

..  note::
    本ドキュメント中の以降の手順は、指定がない限り「ASAKUSA_USER」で実施します。

HadoopクライアントノードへAsakusa Frameworkをデプロイ
-----------------------------------------------------
Hadoopのクライアントノード上にAsakusa Frameworkをデプロイします。

1. ASAKUSA_USERの~/.bash_profileに環境変数HADOOP_HOME, ASAKUSA_HOMEを追加します。
    * 以降の手順ではASAKUSA_HOMEに「$HOME/asakusa」を指定するものとします。

..  code-block:: sh

    export ASAKUSA_HOME=$HOME/asakusa
    export HADOOP_HOME=/usr/lib/hadoop

2. 1で追加した環境変数をシェルに反映します。

..  code-block:: sh

    $ source ~/.bash_profile

3. ASAKUSA_HOMEディレクトリを作成し、ASAKUSA_HOME配下にHadoopクライアントノード用アーカイブ「asakusa-distribution-${version}-prod-hc.tar.gz」を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mkdir $ASAKUSA_HOME
    mv asakusa-distribution-*-prod-hc.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusa-distribution-*-prod-hc.tar.gz
    find $ASAKUSA_HOME -name "*.sh" | xargs chmod u+x

4. $ASAKUSA_HOME/bulkloader/bin/bulkloader_hc_profile を$HOMEに移動します。

..  code-block:: sh

    mv $ASAKUSA_HOME/bulkloader/bin/.bulkloader_hc_profile $HOME

5. $HOME/.bulkloader_hc_profileを編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export ASAKUSA_HOME=$HOME/asakusa
    export JAVA_HOME=/usr/java/default
    export HADOOP_HOME=/usr/lib/hadoop

6. $ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-hc.propertiesを編集します。
    * 「hdfs-protocol-host」を$HADOOP_HOME/conf/core-site.xml の fs.default.name と同じ値に変更します。

..  code-block:: sh

    hdfs-protocol-host=hdfs://(MASTERNODE_HOSTNAME):8020

7. ThunderGate用ログ設定ファイルを編集します。$ASAKUSA_HOME/bulkloader/conf/log4j.xmlを編集し、任意のログディレクトリを指定します。
    * ログファイル名は「${logfile.basename}.log」のままとしてください。
    * 指定したログディレクトリが存在しない場合はディレクトリを作成しておいてください。ログディレクトリはASAKUSA_USERが書き込み可能である必要があります。

データベースノードへAsakusa Frameworkをデプロイ
-----------------------------------------------
データベースノード上にAsakusa Frameworkをデプロイします。

1. ASAKUSA_USERの~/.bash_profileに環境変数ASAKUSA_HOMEを追加します。

..  code-block:: sh

    export ASAKUSA_HOME=$HOME/asakusa

2. 1で追加した環境変数をシェルに反映します。

..  code-block:: sh

    $ source ~/.bash_profile

3. ASAKUSA_HOMEディレクトリを作成し、ASAKUSA_HOME配下にデータベースノード用アーカイブ「asakusa-distribution-${version}-prod-db.tar.gz」を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mkdir $ASAKUSA_HOME
    mv asakusa-distribution-*-prod-db.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusa-distribution-*-prod-db.tar.gz
    find $ASAKUSA_HOME -name "*.sh" | xargs chmod u+x

4. $ASAKUSA_HOME/bulkloader/bin/.bulkloader_db_profile を$HOMEに移動します。

..  code-block:: sh

    mv $ASAKUSA_HOME/bulkloader/bin/.bulkloader_db_profile $HOME

5. $HOME/.bulkloader_db_profileを編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export ASAKUSA_HOME=$HOME/asakusa
    export JAVA_HOME=/usr/java/default

6. $ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.propertiesを編集し、以下のプロパティを環境に合わせて設定します。
    * 「hadoop-cluster.host」にHadoopクライアントノードのホスト名を指定します
    * 「hadoop-cluster.user」にASAKUSA_USERの値を指定します。
    * 「import.tsv-create-dir」「export.tsv-create-dir」に任意のディレクトリパスを指定します。ディレクトリ作成時の注意点は後述の手順8を参照してください。
    * 「import.extractor-shell-name」「export.collector-shell-name」はHadoopクライアントノードの$ASAKUSA_HOMEを「$HOME/asakusa」以外に指定した場合のみ変更が必要です。
        * extractor.sh/collector.shのパスを絶対パス、もしくは$HOMEからの相対パスで指定します。

..  code-block:: sh

    hadoop-cluster.host=(HADOOP_MASTER_NODE_HOSTNAME)
    hadoop-cluster.user=(ASAKUSA_USER)

    import.tsv-create-dir=/var/tmp/asakusa/importer
    import.extractor-shell-name=asakusa/bulkloader/bin/extractor.sh

    export.tsv-create-dir=/var/tmp/asakusa/exporter
    export.collector-shell-name=asakusa/bulkloader/bin/collector.sh

7. ThunderGate用ログ設定ファイルを編集します。$ASAKUSA_HOME/bulkloader/conf/log4j.xmlを編集し、任意のログディレクトリを指定します。
    * ログファイル名は「${logfile.basename}.log」のままとしてください。
    * 指定したログディレクトリが存在しない場合はディレクトリを作成しておいてください。ログディレクトリはASAKUSA_USERが書き込み可能である必要があります。

8. 6で「import.tsv-create-dir」,「export.tsv-create-dir」プロパティに指定したディレクトリを作成します。
    * これらのディレクトリのパーミッションはASAKUSA_USERとMySQL実行ユーザの両ユーザが読み込み、書き込み可能な権限を設定します。

..  code-block:: sh

    mkdir -p -m 777 /var/tmp/asakusa/importer
    mkdir -p -m 777 /var/tmp/asakusa/exporter
    chown -R mysql:mysql /var/tmp/asakusa

..  note::
    この作業は必要に応じてrootで（もしくはsudoを使って）実施してください。

サンプルアプリケーションのデプロイと動作確認
============================================
Asakusa Frameworkが提供するアプリケーション開発用アーキタイプから生成したプロジェクトに含まれるサンプルアプリケーションをexperimental.shで実行し、Asakusaで作成したMapReduceアプリケーションとThunderGateの一連の動作を確認します。

本章で説明する手順の実施は任意ですが、Asakusa Frameworkが正常にデプロイ出来ていることを確認するため、実施することを推奨します。  

なお本章の手順を実施する場合、本番環境用のアプリケーションプロジェクトとは別にアーキタイプからプロジェクトを作成し、サンプルアプリケーションのみが存在する状態でバッチコンパイルしたものをデプロイします。

Hadoopクライアントノードへサンプルアプリケーションをデプロイ
------------------------------------------------------------
1. サンプルアプリケーションのアプリケーションファイルを「$ASAKUSA_HOME/batchapps」配下に配置します。以下はサンプルプロジェクト「batchapp」上でバッチコンパイルしたjarファイルを$HOME/workに配置した状態でアプリケーションをデプロイする例です。

..  code-block:: sh

    cp batchapp-batchapps-*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar -xf batchapp-batchapps-*.jar
    find . -name "*.sh" | xargs chmod u+x
    rm -f batchapp-batchapps-*.jar
    rm -fr META-INF

..  warning::
    デプロイ対象とするjarファイルを間違えないようにしてください。デプロイ対象ファイルは「${artifactId}-**batchapps**-{version}.jar」のようにアーティファクトIDの後に **batchapps** が付くjarファイルです。
    
    例えばサンプルプロジェクト「batchapp」上でバッチコンパイルを行った場合、target配下には以下3つのファイルが作成されます。
    
    * **batchapp-batchapps-{version}.jar** ：デプロイ対象ファイルです。
    * batchapp-{version}-sources.jar：デプロイ対象ファイルではありません。
    * batchapp-{version}.jar：デプロイ対象ファイルではありません。

..  warning::
    $ASAKUSA_HOME/batchapps ディレクトリ直下にはバッチIDを示すディレクトリのみが配置されるようにして下さい。展開前のjarファイルや、jarを展開した結果作成されるMETA-INFディレクトリなどは上述のコマンド例のように削除してください。

データベースノードへサンプルアプリケーションをデプロイ
------------------------------------------------------
1. サンプルアプリケーションのアプリケーションファイルを「$ASAKUSA_HOME/batchapps」配下に配置します。Hadoopクラスターへデプロイしたファイルと同じファイルを同様の手順で配置します。

..  code-block:: sh

    cp batchapp-batchapps-*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar -xf batchapp-batchapps-*.jar
    find . -name "*.sh" | xargs chmod u+x
    rm -f batchapp-batchapps-*.jar
    rm -fr META-INF

2. $ASAKUSA_HOME/bulkloader/conf/[targetname]-jdbc.properties をコピーし、同ディレクトリにasakusa-jdbc.properties を作成します。

..  code-block:: sh

    cp $ASAKUSA_HOME/bulkloader/conf/[targetname]-jdbc.properties \
      $ASAKUSA_HOME/bulkloader/conf/asakusa-jdbc.properties 

3. サンプルアプリケーション用のデータベースを作成します。以下のSQLをMySQLに対して実行します。 

..  code-block:: mysql

    DROP DATABASE IF EXISTS asakusa;
    CREATE DATABASE asakusa DEFAULT CHARACTER SET utf8;
    GRANT ALL PRIVILEGES ON *.* TO 'asakusa'@'localhost'
      IDENTIFIED BY 'asakusa' WITH GRANT OPTION;
    GRANT ALL PRIVILEGES ON *.* TO 'asakusa'@'%'
      IDENTIFIED BY 'asakusa' WITH GRANT OPTION;

    DROP TABLE IF EXISTS asakusa.EX1;
    CREATE TABLE asakusa.EX1 (
      SID BIGINT AUTO_INCREMENT,
      VALUE  INT                   NULL,
      STRING VARCHAR(255)          NULL,
      VERSION_NO BIGINT            NULL,
      RGST_DATETIME DATETIME       NULL,
      UPDT_DATETIME DATETIME       NULL,
      PRIMARY KEY (SID) ) type=InnoDB;
    DROP TABLE IF EXISTS asakusa.EX1_RL;
    CREATE TABLE asakusa.EX1_RL (
      SID BIGINT PRIMARY KEY,
      JOBFLOW_SID BIGINT NULL
    ) type=InnoDB;
    DROP TABLE IF EXISTS asakusa.EX1_RC;
    CREATE TABLE asakusa.EX1_RC (
      SID BIGINT PRIMARY KEY ,
      CACHE_FILE_SID VARCHAR(45) NULL ,
      CREATE_DATE DATETIME NULL
    ) type=InnoDB;

    TRUNCATE TABLE asakusa.EX1;
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (1,111,'hoge1',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (2,222,'fuga2',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (3,333,'bar3',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (4,111,'hoge4',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (5,222,'fuga5',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (6,333,'bar6',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (7,111,'hoge7',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (8,222,'fuga8',null,null,null);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME)
      VALUES (9,444,'bar9',null,null,null);
    -- END;

4. ThnderGate用の管理テーブル作成スクリプトを実行する。

..  code-block:: sh

    cd $ASAKUSA_HOME/bulkloader/sql
    mysql -u asakusa -pasakusa -D asakusa < create_table.sql 
    mysql -u asakusa -pasakusa -D asakusa < insert_import_table_lock.sql

..  note::
    データベースノードとHadoopクライアントノードが同一ホストである場合は、以降の手順（手順5～手順8）は実施しないでください。

5. experimental.sh用hadoop_job_run用SSHブリッジスクリプト（$ASAKUSA_HOME/experimental/bin/hadoop_job_run_ssh_bridge.sh）をコピーする。

..  code-block:: sh

    cp $ASAKUSA_HOME/experimental/bin/hadoop_job_run_ssh_bridge.sh \
      $ASAKUSA_HOME/experimental/bin/hadoop_job_run.sh

6. 5でコピーしたhadoop_job_run.shを編集し、以下の項目を修正する。

..  code-block:: sh

    REMOTE_HADOOP_JOB_RUN_SH=$ASAKUSA_HOME/experimental/bin/hadoop_job_run.sh
    SSHPATH=/usr/bin/ssh
    HCHOST=(MASTERNODE_HOSTNAME) <= Hadoopクライアントノードのホスト名を指定します
    HCUSER=(ASAKUSA_USER)

7. 6で編集したhadoop_job_run.sh からexperimental.sh用clean_hadoop_work用SSHブリッジスクリプトを作成する。

..  code-block:: sh

    cp $ASAKUSA_HOME/experimental/bin/hadoop_job_run.sh \
      $ASAKUSA_HOME/experimental/bin/clean_hadoop_work.sh

8. 7でコピーしたclean_hadoop_work.shを編集し、以下の項目を修正する。

..  code-block:: sh

    REMOTE_HADOOP_JOB_RUN_SH=$ASAKUSA_HOME/experimental/bin/clean_hadoop_work.sh

サンプルアプリケーションの実行
------------------------------
デプロイしたサンプルアプリケーションを実行し、正常に動作することを確認します。

1. サンプルアプリケーション用のexperimental.shを実行

..  code-block:: sh

    $ASAKUSA_HOME/batchapps/ex/bin/experimental.sh

2. experimental.shが正常終了し、MySQLのテーブル「asakusa.EX1」に含まれる数件のレコードについてVALUEの値とUPDT_DATETIMEが更新されていれば成功です。

開発環境で作成したバッチアプリケーションのデプロイと動作確認
============================================================
開発環境で作成したバッチアプリケーションのデプロイと動作確認を行います。

Hadoopクライアントノードへバッチアプリケーションをデプロイ
----------------------------------------------------------
1. バッチアプリケーションのアプリケーションファイルを「$ASAKUSA_HOME/batchapps」配下に配置します。以下はバッチアプリケーションプロジェクト「abcapp」上でバッチコンパイルしたjarファイルを$HOME/workに配置した状態でアプリケーションをデプロイする例です。

..  code-block:: sh

    cp abcapp-batchapps-*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar -xf abcapp-batchapps-*.jar
    find . -name "*.sh" | xargs chmod u+x
    rm -f abcapp-batchapps-*.jar
    rm -fr META-INF

..  warning::
    デプロイ対象とするjarファイルを間違えないようにしてください。デプロイ対象ファイルは「${artifactId}-**batchapps**-{version}.jar」のようにアーティファクトIDの後に **batchapps** が付くjarファイルです。
    
    例えばサンプルプロジェクト「abcapp」上でバッチコンパイルを行った場合、target配下には以下3つのファイルが作成されます。
    
    * **abcapp-batchapps-{version}.jar** ：デプロイ対象ファイルです。
    * abcapp-{version}-sources.jar：デプロイ対象ファイルではありません。
    * abcapp-{version}.jar：デプロイ対象ファイルではありません。

..  warning::
    $ASAKUSA_HOME/batchapps ディレクトリ直下にはバッチIDを示すディレクトリのみが配置されるようにして下さい。展開前のjarファイルや、jarを展開した結果作成されるMETA-INFディレクトリなどは上述のコマンド例のように削除してください。

2. アプリケーション共通ライブラリを配置します。バッチアプリケーションで使用する共通ライブラリ（Hadoopによって提供されているライブラリ以外のもの、例えばApache Commons Lang等）を使用している場合、jarファイルを $ASAKUSA_HOME/ext/lib ディレクトリに配置します。以下はApache Commons Langを配置する例です。

..  code-block:: sh

    cp commons-lang-2.6.jar $ASAKUSA_HOME/ext/lib

データベースノードへバッチアプリケーションをデプロイ
----------------------------------------------------
1. バッチアプリケーションのアプリケーションファイルを「$ASAKUSA_HOME/batchapps」配下に配置します。Hadoopクラスターへデプロイしたファイルと同じファイルを同様の手順で配置します。

..  code-block:: sh

    cp abcapp-batchapps-*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar -xf abcapp-batchapps-*.jar
    find . -name "*.sh" | xargs chmod u+x
    rm -f abcapp-batchapps-*.jar
    rm -fr META-INF

2. $ASAKUSA_HOME/bulkloader/conf/[targetname]-jdbc.properties をコピーし、アプリケーションで使用するデータソース（target)に合わせたデータソース定義ファイルを作成します。以下はtarget「appdb」に対応するデータソース定義ファイルを作成する例です。

..  code-block:: sh

    cp $ASAKUSA_HOME/bulkloader/conf/[targetname]-jdbc.properties \
      $ASAKUSA_HOME/bulkloader/conf/appdb-jdbc.properties 

3. 2で作成したデータソース定義ファイルを編集し、環境に合わせてデータベースの接続設定を定義します。

..  code-block:: properties

    # JDBC driver's name (required)
    jdbc.driver = com.mysql.jdbc.Driver
    # URL of connected data base (required)
    jdbc.url = jdbc:mysql://dbserver/appdb
    # User of connected data base (required)
    jdbc.user = appuser
    # Password of connected data base (required)
    jdbc.password = appuser
    ※以降の項目は変更不要

4. アプリケーション用データベースを作成します。アプリケーション側で管理しているDDLを実行してください。

5. ThunderGate用のシステム情報テーブルを作成します。
    * ThunderGateのImport/Export対象テーブルには、Import/Export処理用に付随するシステムテーブル（「テーブル名_RL」が必要となります。
    * これらのテーブルを作成するためのDDLは、開発環境上でモデルジェネレータを実行した際にbuild.propertiesのキー「asakusa.bulkloader.genddl」で指定したパス（デフォルトはアプリケーションプロジェクトの「target/sql/bulkloader_generated_table.sql」）に生成され、これを使用することも出来ますが、このDDLには中間データ格納用のモデルを作成するためのDDLも含まれるため、アプリケーション側で必要なテーブルに対するDDLを別途管理し、実行することを推奨します。

6. ThunderGate用のテーブル作成スクリプトを実行します。ここで実行するSQLにはデータベースに格納されている全テーブル名を使ってレコードを生成する処理が含まれるため、「サンプルアプリケーションのデプロイ」で実施した場合でも、この手順は必ず再度実施してください。

..  code-block:: sh

    cd $ASAKUSA_HOME/bulkloader/sql
    mysql -u asakusa -pasakusa -D asakusa < create_table.sql
    mysql -u appuser -pappuser -D appdb < insert_import_table_lock.sql

..  note::
    データベースノードとHadoopクライアントノードが同一ホストである場合は、以降の手順（手順7～手順10）は実施しないでください。

..  note::
    「サンプルアプリケーションのデプロイ」を実施している場合は、以降の手順（手順7～手順10）は不要です。

7. experimental.sh用hadoop_job_run用SSHブリッジスクリプト（$ASAKUSA_HOME/experimental/bin/hadoop_job_run_ssh_bridge.sh）をコピーする。

..  code-block:: sh

    cp $ASAKUSA_HOME/experimental/bin/hadoop_job_run_ssh_bridge.sh \
      $ASAKUSA_HOME/experimental/bin/hadoop_job_run.sh

8. 7でコピーしたhadoop_job_run.shを編集し、以下の項目を修正する。

..  code-block:: sh

    REMOTE_HADOOP_JOB_RUN_SH=$ASAKUSA_HOME/experimental/bin/hadoop_job_run.sh
    SSHPATH=/usr/bin/ssh
    HCHOST=(MASTERNODE_HOSTNAME) <= Hadoopクライアントノードのホスト名を指定します
    HCUSER=(ASAKUSA_USER)

9. 8で編集したhadoop_job_run.sh からexperimental.sh用clean_hadoop_work用SSHブリッジスクリプトを作成する。

..  code-block:: sh

    cp $ASAKUSA_HOME/experimental/bin/hadoop_job_run.sh \
      $ASAKUSA_HOME/experimental/bin/clean_hadoop_work.sh

10. 9でコピーしたclean_hadoop_work.shを編集し、以下の項目を修正する。

..  code-block:: sh

    REMOTE_HADOOP_JOB_RUN_SH=$ASAKUSA_HOME/experimental/bin/clean_hadoop_work.sh

実行時プラグインの設定
----------------------
Asakusa Frameworkを拡張したアプリケーション固有の実行時プラグインを動作させる必要がある場合は、実行時プラグインの設定を行います。

実行時プラグインの設定は、$ASAKUSA_HOME/core/conf/asakusa-resources.xml を編集します。以下のように、１つの設定項目に対して <property>エレメントを作成し、設定名を<name>要素に、設定値を<value>要素にそれぞれ設定します。

..  code-block:: sh

    <configuration>
    <!--
    Default Implementations (for Development)
    -->
        <property>
            <name>com.asakusafw.runtime.core.Report.Delegate</name>
            <value>com.asakusafw.runtime.core.Report$Default</value>
        </property>
        <property>
            <name>com.asakusafw.runtime.tool.Numbering.Delegate</name>
            <value>com.asakusafw.runtime.tool.Numbering$Default</value>
        </property>
        <property>
            <name>com.asakusafw.runtime.tool.BatchDate.Delegate</name>
            <value>com.asakusafw.runtime.tool.BatchDate$Default</value>
        </property>
    </configuration>

バッチアプリケーションの実行
----------------------------
デプロイしたバッチアプリケーションを実行し、正常に動作することを確認します。

1. MySQLにアプリケーション入力用データを投入します。

2. バッチアプリケーション用のexperimental.shを実行します。

..  code-block:: sh

    $ASAKUSA_HOME/batchapps/(バッチID)/bin/experimental.sh

3. MySQLの出力結果テーブルを確認します。

クリーニングツールのデプロイ
============================
Asakusa Frameworkにはローカルファイル、及び分散ファイルシステム上のファイルをクリーニングするためのツールが付属しています。

クリーニングツールの使用は任意ですが、特に分散ファイルシステムについては、Asakusa Frameworkのデフォルトの動作では
アプリケーションを実行した際に処理したファイルが分散ファイルシステム上に残り続けるため、
クリーニングツールを使用して定期的にクリーニングを行うことを推奨致します。

本ドキュメントではクリーニングツールのデプロイ方法を説明します。クリーニングツールの設定等の詳細については「Asakusa Cleaner User Guide」 [#]_ を参照して下さい

..  [#] https://asakusafw.s3.amazonaws.com/documents/AsakusaCleaner_UserGuide.pdf

各ノードへのクリーニングツールのデプロイ
----------------------------------------
クリーニングツールはデータベースノードとHadoopクラスタの各ノードへデプロイします。全ノードでデプロイ手順は共通です。

1. ASAKUSA_HOME配下にHadoopクライアントノード用アーカイブ「asakusa-distribution-${version}-prod-cleaner.tar.gz」を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mv asakusa-distribution-*-prod-hc.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusa-distribution-*-prod-cleaner.tar.gz
    find $ASAKUSA_HOME -name "*.sh" | xargs chmod u+x

2. クリーニング用ログ設定ファイルを編集します。$ASAKUSA_HOME/cleaner/conf/log4j.xmlを編集し、任意のログディレクトリを指定します。
    * ログファイル名は「${logfile.basename}.log」のままとしてください。
    * 指定したログディレクトリが存在しない場合はディレクトリを作成しておいてください。ログディレクトリはASAKUSA_USERが書き込み可能である必要があります。

..  note::
    以下手順3～手順4はHDFSクリーニングツールを使う場合に実施します。

3. $ASAKUSA_HOME/cleaner/conf/.clean_hdfs_profileを編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default
    export HADOOP_HOME=/usr/lib/hadoop

4. $ASAKUSA_HOME/cleaner/conf/clean-hdfs-conf.properties を編集し、クリーニングの設定を行います。
    * 「hdfs-protocol-host」は$HADOOP_HOME/conf/core-site.xml の fs.default.name と同じ値に変更します。

..  code-block:: properties

    # File path of log4j.xml (optional)
    log.conf-path=/home/asakusa/asakusa/cleaner/conf/log4j.xml
    # Protocol and host name with HDFS (required)
    hdfs-protocol-host=hdfs://(MASTERNODE_HOSTNAME):8020
    # Directory for cleaning (required)
    clean.hdfs-dir.0=/${user}/target/hadoopwork
    # Cleaning Pattern (required)
    clean.hdfs-pattern.0=.*
    # Preservation period date of file (optional)
    clean.hdfs-keep-date=10

..  note::
    以下手順5～手順6はローカルファイルクリーニングツールを使う場合に実施します。

5. $ASAKUSA_HOME/cleaner/conf/.clean_local_profileを編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default

6. $ASAKUSA_HOME/cleaner/conf/clean-localfs-conf.properties を編集し、クリーニングの設定を行います。

..  code-block:: properties

    # File path of log4j.xml (optional)
    log.conf-path=/home/asakusa/asakusa/cleaner/conf/log4j.xml
    # Directory for cleaning (required)
    clean.local-dir.0=/home/asakusa/asakusa/log
    # Cleaning Pattern (required)
    clean.local-pattern.0=.*\.log\.*
    # Preservation period date of file (optional)
    clean.local-keep-date=10
