=========================
Direct I/O スタートガイド
=========================

この文書では、Direct I/Oの簡単な利用方法について紹介します。
なお、 :doc:`../introduction/start-guide` の内容を理解している前提で説明を進めているため、そちらも参照してください。

Direct I/Oの詳しい利用方法については :doc:`user-guide` を参照してください。


アプリケーションの開発準備
==========================
Direct I/Oを利用したバッチアプリケーションを新しく作成する場合、 Mavenのアーキタイプ ``asakusa-archetype-directio`` を利用すると簡単です。

以降、このドキュメントではこのアーキタイプから作成したプロジェクトを利用して説明を進めます。

..  note::
    Asakusa Frameworkのバージョン ``0.2.5`` 以降、ほかのアーキタイプ ( ``asakusa-archetype-windgate`` など ) でも
    Direct I/Oを利用可能ですが、Direct I/O向けのサンプルアプリケーションはこのアーキタイプのみに含まれます。


アプリケーション開発プロジェクトの作成
--------------------------------------
コマンドラインコンソールでアプリケーションプロジェクトを作成したいディレクトリに移動し、以下のコマンドを実行してください。

..  code-block:: sh

    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog-0.4.xml

コマンドを実行すると、Asakusa Frameworkが提供するプロジェクトテンプレートのうち、どれを使用するかを選択する画面が表示されます。
ここでは、 ``asakusa-archetype-directio`` のテンプレートを選択します。

以降、質問に順に答えていきアプリケーション開発プロジェクトを作成します。
成功すると、アプリケーションのプロジェクト名 ( ``artifactId`` ) で指定した名前のディレクトリが作成されます。

Asakusa Frameworkのインストール
-------------------------------
次に、Asakusa Framework本体をインストールします。
コマンドラインコンソールから先ほど作成したアプリケーションプロジェクトのディレクトリに移動し、以下のコマンドを実行します。

..  code-block:: sh

    mvn assembly:single antrun:run

以降では、このサンプルアプリケーションをビルドおよび実行する前に、Direct I/Oの設定方法を紹介します。

..  attention::
    これから説明するアプリケーションのビルド時に単体テストが自動的に実行されますが、
    この際にHadoopのファイルシステムの特定ディレクトリの削除（クリーニング）と書き込みが行われます。
    
    必ずビルド前にDirect I/Oの設定を確認し、処理対象のディレクトリを確認しておきましょう。

データソースの設定
==================
Direct I/Oの機構を利用するには、入出力の仲介を行う「データソース」の設定が必要です。
主に以下のような設定を行います。

* データソースの実装
* データソースを配置する論理パス
* データソースが実際に利用するファイルシステム上のパス

これらの設定は、 ``$ASAKUSA_HOME`` で指定したディレクトリ以下の ``core/conf/asakusa-resources.xml`` (以下、設定ファイル)内に、Hadoopの設定ファイルと同様の形式でそれぞれ記述していきます。

データソースのマッピング
------------------------
`Asakusa Frameworkのインストール`_ の手順を行った場合、 ``$ASAKUSA_HOME/core/conf/asakusa-resources.xml`` にはデフォルトの設定ファイルが作成されます。

これは以下のような内容になっています。

..  code-block:: xml

    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
    <!--
    Default Implementations (for Development)
    -->
        <property>
            <name>com.asakusafw.runtime.core.Report.Delegate</name>
            <value>com.asakusafw.runtime.core.Report$Default</value>
        </property>
    
        <property>
            <name>com.asakusafw.directio.root</name>
            <value>com.asakusafw.runtime.directio.hadoop.HadoopDataSource</value>
        </property>
        <property>
            <name>com.asakusafw.directio.root.path</name>
            <value>/</value>
        </property>
        <property>
            <name>com.asakusafw.directio.root.fs.path</name>
            <value>target/testing/directio</value>
        </property>
    </configuration>

このうち、Direct I/Oに関係する項目 [#]_ は以下のとおりです。

``com.asakusafw.directio.root``
    データソースのJavaでの実装クラス名です。

    Direct I/Oでは、それぞれのデータソースを識別するための識別子を ``com.asakusafw.directio.<DSID>`` の形式で指定します。
    デフォルトの設定では ``root`` というIDのデータソースが設定されていることになります。
    
    Hadoopのファイルシステムを利用するには :javadoc:`com.asakusafw.runtime.directio.hadoop.HadoopDataSource` と指定します。

``com.asakusafw.directio.root.path``
    このデータソースを配置する「論理パス」を表します。

    論理パスは、 ``com.asakusafw.directio.<DSID>.path`` の形式で指定します。
    DSLからはこの論理パスでデータソースを指定します。
    
    論理パスはUnixのディレクトリのような構造を取り、
    ``alpha/beta/gamma`` のように名前をスラッシュ ( ``/`` ) で区切って指定します。
    
    特別な論理パスとして、ルートパスは ``/`` 一文字で指定します [#]_ 。

``com.asakusafw.directio.root.fs.path``
    データソースが実際に利用するファイルシステム上のパス「ファイルシステムパス」を表します。

    ファイルシステムパスは、 ``com.asakusafw.directio.<DSID>.fs.path`` の形式で指定します。
    Direct I/Oを利用したアプリケーションは、ここに指定されたファイルシステムパス以下のファイルを利用します。

    ファイルシステムパスには ``target/testing/directio`` のように相対パスを使用したり [#]_ 、
    ``hdfs://localhost:8020/user/asakusa`` のように完全URIを使用することができます。

    ファイルシステムパスの形式について、詳しくは :doc:`user-guide` の :ref:`directio-filesystem-path-format` を参照してください。

..  [#] Direct I/Oに無関係の項目は、 :doc:`実行時プラグイン <../administration/deployment-runtime-plugins>` の設定です。
..  [#] デフォルトの設定ではデータソース識別子 ``root`` にルートパスを表す論理パス ``/`` が割り当てられていますが、データソース識別子 ``root`` は特別な値ではなく、任意の値に変更することが出来ます。
..  [#] この場合、Hadoopのデフォルトファイルシステムのワーキングディレクトリからの相対パスが利用されます。Hadoopのデフォルトファイルシステムとは、Hadoopの設定ファイル ``core-site.xml`` 内の ``fs.default.name`` に指定したファイルシステムです。また、デフォルトのワーキングディレクトリは、多くのHadoopディストリビューションではアプリケーション実行ユーザのホームディレクトリです。

..  warning::
    ファイルシステムパス以下はテスト実行時に削除されます。
    特にスタンドアロンモードのHadoopを利用時にデフォルトの設定のような相対パスを指定した場合、
    ホームディレクトリを起点としたパスと解釈されるため注意が必要です。

    例えばホームディレクトリが ``/home/asakusa`` であった場合でデフォルト設定の相対パスを利用する場合、
    テスト実行の都度 ``/home/asakusa/target/testing/directio`` ディレクトリ以下が削除されることになります。


サンプルアプリケーションの実行
==============================
`アプリケーションの開発準備`_ で作成したプロジェクトには、サンプルのアプリケーションが用意されています。
このサンプルは :doc:`../introduction/start-guide` のサンプルアプリケーションの内容をDirect I/O向けに書きなおしたもので、DSLから以下の論理パスを利用しています。

..  list-table:: サンプルアプリケーションが利用するパス
    :widths: 3 7
    :header-rows: 1

    * - 論理パス
      - 概要
    * - ``master``
      - マスタデータを配置するパス
    * - ``sales``
      - 売上データを配置するパス
    * - ``result``
      - 計算結果を出力するパス

上記の論理パスに対応するデータソースをそれぞれ配置するか、またはデフォルト設定のようにルート ( ``/`` ) に対してデータソースを配置してください。

以降の説明ではデータソースの設定にデフォルト設定をそのまま利用する例を示します。

サンプルアプリケーションのビルド
--------------------------------
データソースの設定が完了したら、サンプルアプリケーションのビルドを行います。

..  warning::
    先述した通り、ビルド時に実行されるテストで設定したデータソースのファイルシステムパスの内容がクリアされます。
    対応付けたファイルシステムパスをもう一度確認し、重要なデータがないようにしてください。

コマンドラインコンソールでアプリケーションプロジェクトのディレクトリに移動し、以下のコマンドを実行してください。

..  code-block:: none

    mvn clean package

サンプルデータの配置
--------------------
サンプルアプリケーションプロジェクトには ``src/test/example-dataset`` 以下にサンプルの入力ファイルが配置されています。
これは以下のような構成になっています。

..  list-table:: サンプルアプリケーションが利用するパス
    :widths: 30 20
    :header-rows: 1

    * - サンプルデータの位置
      - 対応する論理パス
    * - ``src/test/example-dataset/master``
      - ``master``
    * - ``src/test/example-dataset/sales``
      - ``sales``

``hadoop fs -put`` コマンドを利用して、設定したファイルシステムパス上にサンプルデータを配置してください。
以下はデフォルト設定を利用した場合のコマンド例です。

..  code-block:: sh
    
    # スタンドアロンモードに対応するため、ホームディレクトリに移動しておく
    cd ~
    # ファイルシステムパス上のデータをクリアしておく
    hadoop fs -rmr target/testing/directio
    # サンプルデータを配置する
    hadoop fs -put <サンプルアプリケーションプロジェクトのパス>/src/test/example-dataset/master target/testing/directio/master
    hadoop fs -put <サンプルアプリケーションプロジェクトのパス>/src/test/example-dataset/sales target/testing/directio/sales

..  attention::
    直前に `サンプルアプリケーションのビルド`_ を実行している場合、ファイルシステムパス上にはテスト時に利用したデータが残っていることがあるため、データを配置する際にはファイルシステムパス上のデータをクリアするようにしてください。

アプリケーションの実行
----------------------
アプリケーション実行の手順は :doc:`../introduction/start-guide` と同様です。

ここではコマンド例のみを示します。詳しくは同文書の :ref:`introduction-start-guide-deploy-app` と :ref:`introduction-start-guide-run-app` を参考にしてください。

..  code-block:: sh

    cd <サンプルアプリケーションプロジェクトのパス>
    cp target/*batchapps*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar xf *batchapps*.jar

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

:ref:`introduction-start-guide-run-app` との相違点として、結果の出力はローカルファイルシステムではなく、論理パス ``result`` 上に行われます。つまり、出力データの実体は 論理パス ``result`` に配置したデータソースが実際に利用するファイルシステム上に出力されます。

アプリケーション実行結果の確認
------------------------------
Direct I/Oでは論理パスに配置したデータソース内のファイルやディレクトリ一覧をリストアップするコマンド ``$ASAKUSA_HOME/directio/bin/list-file.sh`` を提供しています。このコマンドを利用して、サンプルアプリケーションの出力結果を確認します。

``list-file.sh`` は 第一引数にリストアップの対象とするベースパス、第二引数にベースパスからの相対パスや :ref:`directio-file-name-pattern` を指定します。ここでは、論理パス ``result`` 配下のすべてのファイルをサブディレクトリ含めてリストするようコマンドを実行してみます。

..  code-block:: sh

    $ASAKUSA_HOME/directio/bin/list-file.sh result "**/*"
.. ***

上記のコマンドを実行した場合、サンプルデータでは以下のような結果が表示されます。

..  code-block:: sh
     
    Starting List Direct I/O Files:
     Hadoop Command: /usr/lib/hadoop/bin/hadoop
              Class: com.asakusafw.directio.tools.DirectIoList
          Libraries: /home/asakusa/asakusa/directio/lib/asakusa-directio-tools-0.4.0.jar,...
          Arguments: result **/*
    file:/home/asakusa/target/testing/directio/result/category
    file:/home/asakusa/target/testing/directio/result/error
    file:/home/asakusa/target/testing/directio/result/error/20110401.csv
    file:/home/asakusa/target/testing/directio/result/category/result.csv

デフォルト設定の場合、論理パス ``result`` に対応するデータソースはルートパス ``/`` に対応するデータソース ``root`` が使用されます。また、データソース ``root`` に対応するファイルシステムパスは相対パス ``target/testing/directio`` が使用されます。

上記はスタンドアロンモード上のHadoop対して実行しているため、Hadoopのワーキングディレクトリであるユーザのホームディレクトリ ( 上記の例では ``/home/asakusa`` )配下の相対パスに結果が出力されています。

``hadoop fs -text`` コマンドを利用して ``list-file.sh`` が出力したファイルシステムパスのファイル内容を確認します。

..  code-block:: sh
    
    hadoop fs -text file:/home/asakusa/target/testing/directio/result/category/result.csv

上記のコマンドを実行した場合、サンプルデータでは以下のような結果が表示されます。

..  code-block:: sh
    
    カテゴリコード,販売数量,売上合計
    1600,28,5400
    1300,12,1596
    1401,15,1470

このように、売上合計の降順で整列されたCSVになっています。

アプリケーションの開発
======================
以降ではアプリケーションの開発における、Direct I/O特有の部分について紹介します。

データモデルクラスの生成
------------------------
データモデルクラスを作成するには、データモデルの定義情報を記述後にMavenの ``generate-sources`` フェーズを実行します。

Direct I/OではモデルをDMDLで記述します。
DMDLスクリプトはプロジェクトの ``src/main/dmdl`` ディレクトリ以下に配置し、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて保存します。
DMDLの記述方法については「 :doc:`../dmdl/start-guide` 」などを参考にしてください。


CSV入出力への対応
~~~~~~~~~~~~~~~~~
データモデルの定義情報を作成したら、CSVの入出力に対応させたいデータモデルに対して、 ``@directio.csv`` という属性を指定します。
この属性が指定されたデータモデルは、宣言されたプロパティと同じ順序のフィールドを持つCSVファイルの入出力に対応します。

この属性は、データモデルの宣言の直前に指定します。
以下は記述例です。

..  code-block:: none

    @directio.csv
    example_model = {
        // ... プロパティの定義
    };

この状態でデータモデルを作成すると、データモデルのほかに以下の3つのクラスが作成されます。

#. ``<パッケージ名>.csv.<データモデル名>CsvFormat``
#. ``<パッケージ名>.csv.Abstract<データモデル名>CsvInputDescription``
#. ``<パッケージ名>.csv.Abstract<データモデル名>CsvOutputDescription``

より細かな設定については :doc:`user-guide` を参照してください。

Asakusa DSLの記述
-----------------
Direct I/Oを利用する場合でも、Asakusa DSLの基本的な記述方法は同様です。
Direct I/O特有の部分は、以下に示す `ファイルシステム上のCSVファイルを入力に利用する`_ と `ファイルシステム上にCSVファイルを出力する`_ 部分のみです。

それ以外の部分については、 :doc:`../dsl/start-guide` を参照してください。 

ファイルシステム上のCSVファイルを入力に利用する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
開発環境のCSVファイルをインポートしてHadoopの処理を行う場合、 `CSV入出力への対応`_ で生成した ``<パッケージ名>.csv.Abstract<データモデル名>CsvInputDescription`` クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getBasePath()``
    入力に利用する論理パスを戻り値に指定します。

    論理パスについては `データソースのマッピング`_ を参照してください。

``String getResourcePattern()``
    入力に利用するファイル名のパターンを戻り値に指定します。
    ``getBasePath()`` で指定したパスを起点に、このパターンの名前を持つファイルを検索します。

    パターンには ``*`` (ワイルドカード) や ``{alpha|beta|gamma}`` (選択)などを利用できます。
    パターンの完全なドキュメントについては :doc:`user-guide` を参照してください。

以下は ``Document`` というデータモデルを宣言した場合の実装例です。

..  code-block:: java

    public class DocumentFromCsv extends AbstractDocumentCsvInputDescription {

        @Override
        public String getBasePath() {
            return "input";
        }

        @Override
        public String getResourcePattern() {
            return "documents-*.csv";
        }
    }

ここで記述した内容は、ジョブフローの入力に対して、 ``@Import`` を利用して指定します。

ファイルシステム上にCSVファイルを出力する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
ジョブフローの処理結果をCSVファイルにエクスポートする場合、 `CSV入出力への対応`_ で生成した ``<パッケージ名>.csv.Abstract<データモデル名>CsvOutputDescription`` クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getBasePath()``
    出力に利用する論理パスを戻り値に指定します。

    論理パスについては `データソースのマッピング`_ を参照してください。

``String getResourcePattern()``
    出力先のファイル名のパターンを戻り値に指定します。
    ``getBasePath()`` で指定したパスを起点に、このパターンの名前でファイルを作成します。

    パターンには ``{property_name}`` (プレースホルダ) や ``[0..100]`` (ランダムな値 [#]_ ) などを利用できます。
    ここに指定したプロパティの文字列表現がファイル名に埋め込まれます。
    プロパティ名はDMDLのプロパティ名と同様、すべて小文字で単語をアンダースコア ( ``_`` ) で区切ってください。

    パターンの完全なドキュメントについては :doc:`user-guide` を参照してください。

``List<String> getOrder()``
    それぞれの出力ファイルの内容をソートするプロパティを指定します。
    
    それぞれのプロパティは ``+property_name`` で昇順、 ``-property_name`` で降順を表します。
    プロパティ名はDMDLのプロパティ名と同様、すべて小文字で単語をアンダースコア ( ``_`` ) で区切ってください。

以下は ``Document`` というデータモデルを宣言した場合の実装例です。
このデータモデルにはそれぞれ、カテゴリ名を表す ``category`` と、作成日付を表す ``date`` というプロパティがあるものとします。

..  code-block:: java

    public class DocumentToCsv extends AbstractDocumentCsvOutputDescription {

        @Override
        public String getBasePath() {
            return "output";
        }

        @Override
        public String getResourcePattern() {
            return "documents-{category}.csv";
        }

        @Override
        public List<String> getOrder() {
            return Arrays.asList("+date");
        }
    }

上記の例では、論理パス ``output`` 以下に ``documents-<カテゴリ名>.csv`` というファイルをカテゴリごとに作成し、内容を作成日付の昇順でソートします。

ここで記述した内容は、ジョブフローの入力に対して、 ``@Export`` を利用して指定します。

..  attention::
    出力するデータが存在しない場合、ファイルは一つも作成されません。
    これは、ファイル名にプレースホルダを指定していない場合でも同様です。

..  [#] 「ランダムな値」を指定した場合、レコードごとにランダムな番号を生成して宛先のファイルを振り分けます。レコード数が少ない場合、ランダムな番号が偏ってしまって、範囲にあるすべてのファイルが生成されるとは限りません。

