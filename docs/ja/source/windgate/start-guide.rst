==============================
WindGateスタートガイド - Draft
==============================

この文書では、WindGateの使い方を概説します。この文書は現時点ではAsakusa Framework/ThunderGateの使い方に関する知識を有することを前提としています。

WindGate用アプリケーション開発プロジェクトの作成
================================================
WindGateを使ったバッチアプリケーションを開発するには、Mavenアーキタイプ ``asakusa-archetype-windgate`` を使ってアプリケーション開発用プロジェクトを作成します。

..  code-block:: sh

    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml
    ...
    Choose archetype:
    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> asakusa-archetype-batchapp (-)
    2: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> asakusa-archetype-windgate (-)
    Choose a number: : ※2を入力
    ...
    Choose version: 
    1: 0.2-SNAPSHOT
    2: 0.2.2-BETA1
    Choose a number: : ※2を入力
    ...
    Define value for property 'groupId': : com.example ※任意の値を入力
    Define value for property 'artifactId': : batchapp-sample ※任意の値を入力
    Define value for property 'version':  1.0-SNAPSHOT ※任意の値を入力
    Define value for property 'package':  com.example ※任意の値を入力
    ...
    Y: : Yを入力

Asakusa Frameworkの開発環境用インストール
=========================================
Asakusa Frameworkを開発環境へインストールするには、ThunderGateを使ったセットアップ手順と同様に、Mavenの ``assembly:single`` と ``antrun:run`` ゴールを実行します。

..  code-block:: sh

    cd batchapp-sample
    mvn assembly:single antrun:run

プロジェクトのディレクトリ構成
------------------------------
作成されたプロジェクトのディレクトリ構成はThunderGateを使った開発と同様です。

詳しくは、 :doc:`../application/maven-archetype` を参照してください。

プロファイルの設定
==================
WindGateは、外部システムとの入出力の定義を「プロファイル」として定義します。プロファイルを定義したプロパティファイルは、 ``$ASAKUSA_HOME/windgate/profile`` 配下に[プロファイル名].properties という名前で配置します。デフォルトではプロファイル名「asakusa」用のプロファイル定義ファイルとして ``asakusa.properties`` が配置されています。
プロファイルの定義情報は環境に合わせて変更する必要があります。

JDBC接続用プロファイル定義
--------------------------
WindGateが処理するインポート元、及びエクスポート先のJDBC接続情報を、以下のプロパティに設定します。デフォルトではローカルのMySQLに対して接続を行うよう設定されています。

..  code-block:: none

    # JDBCドライバ名
    resource.jdbc.driver=com.mysql.jdbc.Driver
    # JDBC接続URL
    resource.jdbc.url=jdbc:mysql://localhost/asakusa
    # 接続ユーザ
    resource.jdbc.user=asakusa
    # 接続パスワード
    resource.jdbc.password=asakusa

データベースにMySQL以外を使用する場合、JDBCドライバのjarファイルを ``$ASAKUSA_HOME/windgate/plugin`` 配下に配置してください。

..  _profile-hadoop:

Hadoopクライアント接続用プロファイル定義
----------------------------------------
WindGateが処理するインポート先、及びエクスポート元のHadoopクライアント用ノードに対する設定を行います。

デフォルトではWindGateが動作するマシンにインストールされているHadoopに対して処理を行うよう設定されています。開発環境など、ローカルのHadoopに対して処理を行う場合は追加の設定は不要です。

Hadoopクライアント用ノードがWindGateが動作するマシンのリモートに配置されている場合、WindGateがHadoopクライアントノードに対してSSH接続を行うための設定を行う必要があります。

SSH接続情報は以下のプロパティに設定します。

..  code-block:: none

    # Hadoop File System
    # ローカルに対するHadoopの使用を無効化するため、以下の設定行をコメントアウトします。
    #resource.hadoop=com.asakusafw.windgate.hadoopfs.HadoopFsProvider

    # Hadoop File System (for Remote Hadoop Cluster via SSH)
    # SSH経由によるHadoopのアクセスを有効化するため、以下のプロパティのコメントアウトを解除します。
    # (resource.hadoop から resource.hadoop.passPhrase までの各行のコメントアウトを解除します)
    resource.hadoop=com.asakusafw.windgate.hadoopfs.jsch.JschHadoopFsProvider

    # 接続先のHadoopクライアント用ノードに配置するWindGate用SSH接続モジュールの配置場所
    # ホームディレクトリが[/home/asakusa],$ASAKUSA_HOMEが[asakusa]以外の場合、
    # このプロパティを環境に合わせて変更する必要があります。
    resource.hadoop.target=/home/asakusa/asakusa/windgate-ssh

    # SSH接続ユーザ
    resource.hadoop.user=asakusa

    # SSH接続ホスト
    resource.hadoop.host=localhost

    # SSH接続ポート
    resource.hadoop.port=22

    # SSH接続秘密鍵ファイルパス
    resource.hadoop.privateKey=${HOME}/.ssh/id_dsa

    # SSH秘密鍵パスフレーズ
    resource.hadoop.passPhrase=

    # 圧縮の有効/無効化(コメントアウト時には圧縮が無効です)
    #resource.hadoop.compression=org.apache.hadoop.io.compress.DefaultCodec

サンプルプログラムの実行
========================
アーキタイプから生成されたプロジェクトには、動作確認用のサンプルバッチプログラムが配置されています。このプログラムをテストドライバー、またはexperimental.sh経由で実行する場合、事前にサンプルプログラム用テーブル作成用DDL ``src/main/sql/example_model_ddl.sql`` を実行してデータベースを作成してください。

モデルクラスの生成
==================
モデルクラスを作成するには、モデルの定義情報を記述後にMavenの ``generate-sources`` フェーズを実行します。

WindGateではモデルの定義情報をDMDLで記述形式のみサポートしています。ThunderGateで提供しているモデル定義情報をDDL(SQL)で記述しこれをモデルに変換する機能はWindGateにはありませんので注意して下さい。

DMDLスクリプトはプロジェクトの ``src/main/dmdl`` ディレクトリ以下に配置し、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて保存します。DMDLの記述方法については以下のドキュメント [#]_ などを参考にしてください。

..  [#] :doc:`../dmdl/start-guide` 

モデルプロパティとデータベースカラムのマッピング定義
----------------------------------------------------
WindGate向けのDMDLを記述する場合、モデルプロパティに対応するデータベースカラム名のマッピング定義を行う必要があります。

モデルプロパティに対するデータベースカラム名の指定は、DMDLのプロパティに対してWindGate用の拡張属性 ``@windgate.column(value = "カラム名")`` を指定します。

以下の例では、モデルのプロパティに対して同名の大文字のデータベースカラム名にマッピングを行っています。

..  code-block:: none

    document = {
        "the name of this document"
        @windgate.column(value = "NAME")
        name : TEXT;

        "the content of this document"
        @windgate.column(value = "CONTENT")
        content : TEXT;
    };

    word = {
        "the string representation of this word"
        @windgate.column(value = "STRING")
        string : TEXT;

        "frequency of this word in documents"
        @windgate.column(value = "FREQUENCY")
        frequency : INT;
    };

JDBC接続サポートクラスの生成
----------------------------
WindGateでは、モデルジェネレータの実行時にモデルクラスの生成と同時に、JDBC経由で入出力するデータとAsakusa Frameworkのデータモデルクラスの相互変換を行うJDBC接続サポートクラスがモデルクラス作成ディレクトリに ``[モデルクラス名]JdbCSupport`` というクラス名で作成されます [#]_ 。

生成されたJDBC接続サポートクラスは後述するジョブフローDSLのインポート記述/エクスポート記述で指定します。

..  [#] ``<ベースパッケージ名> . <名前空間> . jdbc . <データモデル名>JdbcSupport``

Asakusa DSLの記述
=================
WindGateを使う場合、ジョブフローのインポート記述/エクスポート記述がThunderGateの場合と異なります。そのほかのDSLについては、ThunderGateを使った場合と同様です。

データベースのテーブルからインポートする
----------------------------------------
WindGateと連携してデータベースのテーブルからデータをインポートする場合、 ``JdbcImporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    インポータが使用するプロファイル名を戻り値に指定します。

    インポータは実行時に $ASAKUSA_HOME/windgate/profile 配下に配置した[プロファイル名].properties に記述されたデータベース接続情報定義ファイルを使用してデータベースに対するアクセスを行います。

``Class<?> getModelType()``
    インポータが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

    インポータは実行時にモデルクラスを作成する元となったテーブル名に対してインポート処理を行います 。

``String getTableName()``
    インポート対象のテーブル名を指定します。

``List<String> getColumnNames()``
    インポート対象のカラム名を指定します。ここで指定したカラム名のみインポートを行います。

``Class<? extends DataModelJdbcSupport<?>> getJdbcSupport()``
    JDBC経由で入出力データとデータモデルクラスの相互変換を行うためのヘルパークラスを指定します。

    通常は、モデルジェネレータで生成される ``[モデルクラス名]JdbcSupport`` クラスを指定します。

``String getWhere()``
    インポータが利用する抽出条件をSQLの条件式で指定します。

    指定する文字列はMySQL形式の ``WHERE`` 以降の文字列である必要があります。

..  [#] ``com.asakusafw.vocabulary.windgate.JdbcImporterDescription``

例：

..  code-block:: java

    public class DocumentFromDb extends JdbcImporterDescription {

        @Override
        public Class<?> getModelType() {
            return Document.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getTableName() {
            return "DOCUMENT";
        }

        @Override
        public List<String> getColumnNames() {
            return Arrays.asList("NAME", "CONTENT");
        }

        @Override
        public Class<? extends DataModelJdbcSupport<?>> getJdbcSupport() {
            return DocumentJdbcSupport.class;
        }
    }

データベースのテーブルにエクスポートする
----------------------------------------
WindGateと連携してジョブフローの処理結果をデータベースのテーブルに書き出すには、 ``JdbcExporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    エクスポータが使用するプロファイル名を戻り値に指定します。

    利用方法はインポータの ``getProfileName()`` と同様です。

``Class<?> getModelType()``
    エクスポータが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

    エクスポータは実行時にモデルクラスを作成する元となったテーブル名に対してエクスポート処理を行います 。

``String getTableName()``
    エクスポート対象のテーブル名を指定します。

``List<String> getColumnNames()``
    エクスポート対象のカラム名を指定します。ここで指定したカラム名のみエクスポートを行います。

``Class<? extends DataModelJdbcSupport<?>> getJdbcSupport()``
    JDBC経由で入出力データとデータモデルクラスの相互変換を行うためのヘルパークラスを指定します。

    利用方法はインポータの ``getJdbcSupport()`` と同様です。

例：

..  code-block:: java

    public class WordIntoDb extends JdbcExporterDescription {

        @Override
        public Class<?> getModelType() {
            return Word.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getTableName() {
            return "WORD";
        }

        @Override
        public List<String> getColumnNames() {
            return Arrays.asList("STRING", "FREQUENCY");
        }

        @Override
        public Class<? extends DataModelJdbcSupport<?>> getJdbcSupport() {
            return WordJdbcSupport.class;
        }
    }

..  [#] ``com.asakusafw.vocabulary.windgate.JdbcExporterDescription``

WindGateのエクスポート動作
--------------------------
WindGateのエクスポータはThunderGateのエクスポータと大きく異なる点があります。以下の点をご注意下さい。

..  warning::
    WindGateのエクスポートは、エクスポート対象のテーブルに対してtruncateを行い、エクスポートデータをinsertするよう動作します。
    このため、通常はtruncateしてはいけない業務データ用のテーブルに対してエクスポート対象としないよう注意して下さい。
    
    WindGateでは、アプリケーション用のテーブルを更新する場合の想定として、まずWindGate用のテンポラリテーブルに対してエクスポートを行った後、
    Asakusa Framework外のアプリケーションでテンポラリテーブルデータを入力として業務データに更新処理を行うといった使い方を想定しています。

Asakusa DSLのバッチコンパイルとアプリケーションアーカイブの生成
===============================================================
Asakusa DSLで記述したバッチアプリケーションをHadoopクラスタにデプロイするためには従来と同様にMavenの ``package`` フェーズを実行し、バッチアプリケーション用のアーカイブファイルを作成します。

..  code-block:: sh

    mvn package
    
Asakusa FrameworkとWindGateのHadoopクラスタへのデプロイ
=======================================================
Asakusa FrameworkとWindGateのHadoopクラスタへのデプロイについては、基本的な流れはThunderGateの場合( :doc:`../application/administrator-guide` )と同様ですが、デプロイするAsakusa FrameworkのアーカイブはWindGate用のアーカイブを使用します。以下を使用して下さい。

WindGate用デプロイアーカイブ
----------------------------
WindGate用のデプロイアーカイブは以下を使用します。以下のアーカイブをそれぞれのホストの $ASAKUSA_HOME に配置して下さい。

1. asakusa-distribution-${version}-prod-windgate.tar.gz
    * Asakusa Framework と WindGate が格納されているアーカイブ。
2. asakusa-distribution-${version}-prod-windgate-ssh.tar.gz
    * HadoopクラスターのHadoopクライアントノードに展開するアーカイブ。
    * WindGateを配置するノードとHadoopクライアントとなるノードが異なるノードの場合は、このモジュールをHadoopクライアントノードに展開します。
    * このモジュールを配置した上で、WindGateのプロファイル設定にて 「Hadoopクライアント用ノードがWindGateが動作するマシンのリモートに配置されている場合の設定」( :ref:`profile-hadoop` ) を指定します。

配置後のコンフィグレーション
----------------------------
プロファイル定義ファイルを開発環境と同様の手順で適宜編集してください。

また、Hadoopクライアントノード上の$HADOOP_HOME が ``/usr/lib/hadoop`` 以外の場合、 ``$ASAKUSA_HOME/windgate-ssh/conf/env.sh`` に定義されているHADOOP_HOME変数を環境に合わせて修正してください。


