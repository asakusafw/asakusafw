====================================
デプロイメントガイド for ThunderGate
====================================
この文書では、外部システムとの連携にThunderGateを用いる構成における、Asakusa Frameworkとバッチアプリケーションを運用環境にデプロイする手順について解説します。

HadoopクラスターとDBサーバの準備
================================
Asakusa Framework本体とバッチアプリケーションをデプロイするためのHadoopクラスターを用意します。また、Asakusa ThunderGate（以下「ThunderGate」）がデータの入出力を行う対象とするDBサーバを用意します。このDBサーバを本ドキュメントでは「データベースノード」とよびます。

Hadoopクラスターのいずれかのサーバ（通常マスターノード）に、ThunderGateのコンポーネントのうち「Extractor」「Collector」と呼ばれる、DFS(HDFS)に対する入出力処理を行うコンポーネントをインストールします。「Extractor」「Collector」をインストールするサーバを本ドキュメントでは「Hadoopクライアントノード」とよびます。

Asakusa Frameworkのデプロイを行う前に、Hadoopクラスターとデータベースノードに対して次の環境構築が行われていることを確認してください。

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

Asakusa Frameworkのインストールアーカイブの準備
-----------------------------------------------
Asakusa Frameworkのインストールアーカイブを用意します。

Asakusa Frameworkのインストールアーカイブは、アプリケーション開発プロジェクトからMavenの以下のコマンドを実行して生成します
。

..  code-block:: sh

    mvn assembly:single

このコマンドを実行すると、プロジェクトの target ディレクトリ配下にいくつかのファイルが生成されます。このうち以下のファイルがAsakusa FrameworkとWindGateをインストールするためのアーカイブです。

  asakusafw-${asakusafw-version}-prod-thundergate-hc.tar.gz
    HadoopクラスターのHadoopクライアントノードに展開するアーカイブ。
  asakusafw-${asakusafw-version}-prod-thundergate-db.tar.gz
    データベースノードに展開するアーカイブ。
  asakusafw-${asakusafw-version}-prod-cleaner.tar.gz
    Asakusa Frameworkが提供するクリーニングツールのデプロイに使用するアーカイブ

${asakusafw.version}は使用しているAsakusa Frameworkのバージョンに置き換えます。例えばversion 0.2.4 を使っている場合は、 asakusafw-0.2.4-prod-thundergate-hc.tar.gz などとなります。 

バッチアプリケーションのデプロイに必要なファイル
------------------------------------------------
以下のファイル一式は、アプリケーション開発者が開発環境上で作成／用意します。

1. バッチコンパイルにより生成したバッチアプリケーションのjarファイル（「${artifactid}-batchapps-${version}.jar」）
    * 開発環境上でバッチコンパイルを実行すると、ワークスペース上の「target」配下に作成されます。
    * 詳しくは  :doc:`../application/maven-archetype` の :ref:`maven-archetype-batch-compile` を参照してください。
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

3. ASAKUSA_HOMEディレクトリを作成し、ASAKUSA_HOME配下にHadoopクライアントノード用アーカイブ「asakusafw-${asakusafw-version}-prod-thundergate-hc.tar.gz」を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mkdir $ASAKUSA_HOME
    mv asakusafw-*-prod-thundergate-hc.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusadw-*-prod-thundergate-hc.tar.gz
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

3. ASAKUSA_HOMEディレクトリを作成し、ASAKUSA_HOME配下にデータベースノード用アーカイブ「asakusafw-${asakusafw-version}-prod-thundergate-db.tar.gz」を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mkdir $ASAKUSA_HOME
    mv asakusafw-*-prod-thundergate-db.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusafw-*-prod-thundergate-db.tar.gz
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
Asakusa Frameworkが提供するアプリケーション開発用アーキタイプから生成したプロジェクトに含まれるサンプルアプリケーションをexperimental.shで実行し [#]_ 、Asakusaで作成したMapReduceアプリケーションとThunderGateの一連の動作を確認します。

本章で説明する手順の実施は任意ですが、Asakusa Frameworkが正常にデプロイ出来ていることを確認するため、実施することを推奨します。  

なお本章の手順を実施する場合、本番環境用のアプリケーションプロジェクトとは別にアーキタイプからプロジェクトを作成し、サンプルアプリケーションのみが存在する状態でバッチコンパイルしたものをデプロイします。

..  [#] :doc:`YAESS <../yaess/index>` も利用できます。
    このドキュメントは、将来YAESSを利用した手順に変更される予定です。


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
      DELETE_FLAG CHAR(1)          NULL,
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
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (1,111,'hoge1',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (2,222,'fuga2',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (3,333,'bar3',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (4,111,'hoge4',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (5,222,'fuga5',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (6,333,'bar6',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (7,111,'hoge7',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (8,222,'fuga8',null,null,null,0);
    INSERT INTO asakusa.EX1 (SID, VALUE, STRING, VERSION_NO, RGST_DATETIME, UPDT_DATETIME, DELETE_FLAG)
      VALUES (9,444,'bar9',null,null,null,0);
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
    mysql -u appuser -pappuser -D appdb < create_table.sql
    mysql -u appuser -pappuser -D appdb < insert_import_table_lock.sql

..  warning::
    バッチアプリケーションを更新した際に、テーブルモデルが増えた場合にもこの手順（ThunderGate用のテーブル作成スクリプトの再実行）の実施が必要です。

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

実行時プラグインの設定については、 :doc:`deployment-runtime-plugins` を参照してください。

バッチアプリケーションの実行
----------------------------
デプロイしたバッチアプリケーションを実行し、正常に動作することを確認します。

1. MySQLにアプリケーション入力用データを投入します。

2. バッチアプリケーション用のexperimental.shを実行します。

..  code-block:: sh

    $ASAKUSA_HOME/batchapps/(バッチID)/bin/experimental.sh

3. バッチアプリケーションの実行結果を以下の方法で確認します。

* バッチアプリケーションが正常終了したことの確認
    * 標準出力に「Finished: SUCCESS」が表示される、もしくはexperimental.shのリターンコードが0であることを確認します。
* バッチアプリケーションの処理内容の確認
    * MySQLの出力結果テーブルを確認します。

クリーニングツールのデプロイ
============================
クリーニングツールのデプロイについては、 :doc:`deployment-cleaner` を参照してください。

