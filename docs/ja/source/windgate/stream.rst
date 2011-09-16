=========================
WindGateのTSVファイル連携
=========================

この文書では、WindGateを使ってTSVフォーマットのローカルファイルをインポート/エクスポートするための拡張機能の使い方について説明します。

TSVファイル連携用のPOM設定
==========================
WindGateのTSVファイル連携を使用するためには、WindGate用アーキタイプ ``asakusa-archytype-windgate`` から作成したプロジェクトに対して、 ``pom.xml`` に以下の依存性定義を追加します。

..  list-table:: TSVファイル連携で使用するMavenアーティファクト
    :widths: 5 5 
    :header-rows: 1

    * - グループID
      - アーティファクトID
    * - com.asakusafw.sandbox
      - asakusa-windgate-vocabulary-ext
    * - com.asakusafw.sandbox
      - asakusa-windgate-dmdl-ext
    * - com.asakusafw.sandbox
      - asakusa-windgate-stream

..  code-block:: xml

	<dependencies>
    ...
		<dependency>
			<groupId>com.asakusafw.sandbox</groupId>
			<artifactId>asakusa-windgate-vocabulary-ext</artifactId>
			<version>0.1-SNAPSHOT</version>
		</dependency>
		<dependency>
			<groupId>com.asakusafw.sandbox</groupId>
			<artifactId>asakusa-windgate-dmdl-ext</artifactId>
			<version>0.1-SNAPSHOT</version>
			<scope>compile</scope>
		</dependency>
		<dependency>
			<groupId>com.asakusafw.sandbox</groupId>
			<artifactId>asakusa-windgate-stream</artifactId>
			<version>0.1-SNAPSHOT</version>
			<scope>test</scope>
		</dependency>

``pom.xml`` を編集したら、 このプロジェクトに対して（ ``pom.xml`` が存在するディレクトリ上で） ``mvn eclipse:eclipse`` を実行してEclipse用のクラスパス定義を更新します。

プロファイルの設定
==================

ローカルファイル連携用プロファイル定義
--------------------------------------
WindGateが処理するインポート元、及びエクスポート先としてデフォルトで定義しているJDBC接続情報を削除し、代わりにローカルファイル連携用のプロファイルを定義を追加します。

JDBC接続情報の削除
~~~~~~~~~~~~~~~~~~
以下のJDBCプロファイル定義を削除します。

..  code-block:: properties

    # JDBC
    resource.jdbc=com.asakusafw.windgate.jdbc.JdbcResourceProvider
    resource.jdbc.driver=com.mysql.jdbc.Driver
    resource.jdbc.url=jdbc:mysql://localhost/asakusa
    resource.jdbc.user=asakusa
    resource.jdbc.password=asakusa
    #resource.jdbc.batchPutUnit=10000

ローカルファイル連携用定義の追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
以下のローカルファイル連携用プロファイル定義を追加します。

..  code-block:: properties

    # LOCAL
    resource.local=com.asakusafw.windgate.stream.file.FileResourceProvider

プラグイン用jarファイルの追加
=============================
TSVファイル連携モジュール用のプラグイン( ``asakusa-windgate-stream`` )jarファイルをWindGateのプラグインディレクトリに配置します。

以下のコマンドにて、Mavenローカルリポジトリ上の ``asakusa-windgate-stream`` のアーティファクトのjarファイルを ``$ASAKUSA_HOME/windgate/plugin`` 配下にコピーします。

..  code-block:: sh

    cp -p $HOME/.m2/repository/com/asakusafw/sandbox/asakusa-windgate-stream/0.1-SNAPSHOT/asakusa-windgate-stream-0.1-SNAPSHOT.jar $ASAKUSA_HOME/windgate/plugin

モデルクラスの生成
==================

DMDLに対するTSVファイル用拡張属性の追加
---------------------------------------
TSVファイル連携を行うために、DMDLに対してTSVファイルを扱うことを示す拡張属性を追加します。

各モデルのDMDLスクリプトに対して、モデル名の記述行の前行に拡張属性 ``@windgate.stream_format(type="tsv")`` を付与します。

また、WindGateのJDBC接続を行うためのDMDLスクリプトには、プロパティに対してテーブルカラムとのマッピングを行うための拡張属性 ``@windgate.column`` を定義する必要がありますが、ファイルを扱う場合はこの拡張属性は不要です。

以下にTSVファイルを扱う場合のDMDLスクリプトの例を示します。

..  code-block:: sh

    "テーブルEX1"
    ...
    @windgate.stream_format(type="tsv")
    ex1 = {
        "SID"
        sid : LONG;
        "VALUE"
        value : INT;
        "STRING"
        string : TEXT;
    };

ローカルファイル連携サポートクラスの生成
----------------------------------------
上述のDMDLスクリプトの拡張属性の追加を行った上でモデルジェネレータを実行すると、JDBC接続時と同様モデルクラスの生成と同時にファイル入出力するデータとAsakusa Frameworkのデータモデルクラスの相互変換を行う「ストリームサポートクラス」がモデルクラス作成ディレクトリに ``[モデルクラス名]StreamSupport`` というクラス名で作成されます [#]_ 。

生成されたストリームサポートクラスはJDBC接続時の場合と同様に、ジョブフローDSLのインポート記述/エクスポート記述で指定します。

..  [#] ``<ベースパッケージ名> . <名前空間> . stream . <データモデル名>StreamSupport``

Asakusa DSLの記述
=================
WindGateのファイル連携を使う場合、ジョブフローのインポート記述/エクスポート記述がJDBC接続の場合と異なります。そのほかのDSLについては、JDBC接続を使った場合と同様です。

ローカルのファイルからインポートする
------------------------------------
WindGateと連携してファイルからデータをインポートする場合、 ``FsImporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    インポータが使用するプロファイル名を戻り値に指定します。

    インポータは実行時に $ASAKUSA_HOME/windgate/profile 配下に配置した[プロファイル名].properties に記述された定義ファイルを使用します。

``Class<?> getModelType()``
    インポータが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

    インポータは実行時にモデルクラスを作成する元となったテーブル名に対してインポート処理を行います 。

``String getPath()``
    インポート対象のファイルパスを指定します。

    ここには ${変数名} の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。 利用可能な変数はコンテキストAPIで参照できるものと同様です。
    

``Class<? extends DataModelStreamSupport<?>> getStreamSupport()``
    ファイル経由で入出力データとデータモデルクラスの相互変換を行うためのヘルパークラスを指定します。

    通常は、モデルジェネレータで生成される ``[モデルクラス名]StreamSupport`` クラスを指定します。

..  [#] ``com.asakusafw.vocabulary.windgate.FsImporterDescription``

例：

..  code-block:: java

    public class DocumentFromFile extends FsImporterDescription {

        @Override
        public Class<?> getModelType() {
            return Document.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getPath() {
            return "/tmp/import-document.tsv";
        }

        @Override
        public Class<? extends DataModelStreamSupport<?>> getStreamSupport() {
            return DocumentStreamSupport.class;
        }
    }

ローカルのファイルにエクスポートする
------------------------------------
WindGateと連携してジョブフローの処理結果をローカルのファイルに書き出すには、 ``FsExporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    エクスポータが使用するプロファイル名を戻り値に指定します。

    利用方法はインポータの ``getProfileName()`` と同様です。

``Class<?> getModelType()``
    エクスポータが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

``String getPath()``
    エクスポート対象のファイルパスを指定します。指定したパスのディレクトリが存在しない場合、ディレクトリを作成してファイルを生成します。

    ここには ${変数名} の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。 利用可能な変数はコンテキストAPIで参照できるものと同様です。

``Class<? extends DataModelStreamSupport<?>> getStreamSupport()``
    ファイル経由で入出力データとデータモデルクラスの相互変換を行うためのヘルパークラスを指定します。

    利用方法はインポータの ``getStreamSupport()`` と同様です。

例：

..  code-block:: java

    public class WordIntoFile extends FsExporterDescription {

        @Override
        public Class<?> getModelType() {
            return Word.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getPath() {
            return "/tmp/export-word.tsv";
        }

        @Override
        public Class<? extends DataModelStreamSupport<?>> getStreamSupport() {
            return WordStreamSupport.class;
        }
    }

..  [#] ``com.asakusafw.vocabulary.windgate.FsExporterDescription``

..  warning::
    エクスポート時のファイルパスにすでに同名のファイルが存在していた場合は、このファイルを上書きしてファイルを生成します。

TSVファイルフォーマット仕様
===========================
WindGateのTSV連携機能で扱うTSVファイルのフォーマット仕様について説明します。

..  warning::
    現時点では、本項のTSVファイルフォーマットは暫定仕様です。

TSVフォーマット概要
-------------------
* TSVファイルは、MySQLの ``SELECT ... INTO OUTFILE`` で、次の指定をした場合に生成されるファイルフォーマットと同一です（MySQL 5.1のデフォルト)
    * ``FIELDS TERMINATED BY '\t' ENCLOSED BY '' ESCAPED BY '\\'``
    * ``LINES TERMINATED BY '\n' STARTING BY ''``

TSVフォーマット詳細
-------------------
* 各フィールドをDMDLスクリプトの順番に記述します。
* フィールドの区切り文字にはタブ文字を使用します。
* レコードの区切り文字は改行(LF)を使用します。
    * CR+LF は使用できません。
* エスケープ文字には「\」を使用します。
    * エスケープ文字そのもの、改行(LF)、タブ文字をデータとして扱う場合は「/」を前に付加していエスケープします。
* 引用文字は使用しません。
* 最終レコードにも(LF)が必要です。
* エンコーディングはUTF-8を使用します。
* NULL値は「\n」で表します。
* 空文字はフィールド区切り文字間に何も文字を入れないことで表現します。
* 指数表記は使用しません。
* Booleanは0/1で表します。
    * 0:false , 1:true
* Date, Datetimeは以下の書式で表します。
    * Date: YYYY-MM-DD
    * Datetime: YYYY-MM-DD HH:MM:SS

TSVファイルのサンプル
---------------------
DMDLスクリプトに対応するTSVファイルの例を以下に示します。

サンプル:DMDLスクリプト
~~~~~~~~~~~~~~~~~~~~~~~
..  code-block:: java

    "テーブルEX1"
    ... 
    @windgate.stream_format(type="tsv")
    ex1 = { 
        "SID"
        sid : LONG;
        "VALUE"
        value : INT;
        "STRING"
        string : TEXT;
    };  

サンプル:TSVファイル
~~~~~~~~~~~~~~~~~~~~
..  note::
    以下サンプルのドキュメント上の区切り文字はスペースになっていますが、実際のファイルはタブ文字を使用してください。

..  code-block:: java

    1	111	hoge1
    2	222	fuga2
    3	333	bar3
    4	111	hoge4
    5	222	fuga5
    6	333	bar6
    7	111	hoge7
    8	222	fuga8
    9	444	bar9


