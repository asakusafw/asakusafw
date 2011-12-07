======================
WindGateスタートガイド
======================

..  todo::
    introduction/start-guide にとばす

この文書では、ローカルファイルシステム上のCSVファイルを利用してバッチアプリケーションを実行する例を元に、WindGateの使い方を概説します。

WindGate用アプリケーション開発プロジェクトの作成
================================================
WindGateを使ったバッチアプリケーションを開発するには、Mavenアーキタイプ ``asakusa-archetype-windgate`` を使ってアプリケーション開発用プロジェクトを作成します。

..  code-block:: sh

    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml
    ...
    Choose archetype:
    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> asakusa-archetype-batchapp (-)
    2: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> asakusa-archetype-windgate (-)
    Choose a number: : ※asakusa-archetype-windgateの方を指定
    ...
    Choose version: 
    1: 0.2-SNAPSHOT
    2: 0.2....
    Choose a number: : ※ 0.2.4以降のバージョンを指定
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
WindGateは、外部システムとの入出力の定義を「プロファイル」として定義します。
プロファイルを定義したプロパティファイルは、 ``$ASAKUSA_HOME/windgate/profile`` 配下に ``<プロファイル名>.properties`` という名前で配置します。
標準ではプロファイル名 ``asakusa`` 用のプロファイル定義ファイルとして ``asakusa.properties`` が配置されています。

プロファイルの定義情報は環境に合わせて変更してください。


このガイドでの構成
------------------
このガイドでは、次のような前提でアプリケーションを作成します。

* Hadoopは開発環境にインストールされている
* CSVファイルは開発環境上に配置する

開発環境の外でHadoopを利用する場合や、CSVファイルでなくデータベースを利用したい場合などでは、 :doc:`user-guide` を参考に設定を行ってください。

Hadoopの設定
------------
インストール直後の設定で、WindGateの ``asakusa`` プロファイルでは開発環境上のHadoopクラスタを利用します。
そのため、環境変数 ``$HADOOP_HOME`` が正しく設定されていることを確認してください。

それ以外の設定は特に必要ありません。


ベースディレクトリの設定
------------------------
CSVファイルを格納するディレクトリは、プロファイル上で設定します。

``$ASAKUSA_HOME/windgate/profile/asakusa.properties`` (プロファイル ``asakusa`` 用の構成) の
ファイルをエディタで開き、 ``resource.local.basePath`` の内容にディレクトリへのフルパスを指定します。

なお、初期状態ではここに ``/tmp/windgate-${USER}`` が指定されています [#]_ 。
このドキュメントでは、この値を変更せず、初期設定のままにしたという前提で進めます。

..  [#] ``${環境変数名}`` という形式で環境変数を指定できます。
    初期状態では、 ``/tmp/windgate-<ログインユーザ名>`` をベースディレクトリとして利用しています。

サンプルプログラムの実行
========================
アーキタイプから生成されたプロジェクトには、動作確認用のサンプルバッチプログラムが配置されています。

Asakusa Frameworkが正しくインストールされていることを確認するには、作成したプロジェクトで ``mvn test`` コマンドを実行してください。


データモデルクラスの生成
========================
データモデルクラスを作成するには、データモデルの定義情報を記述後にMavenの ``generate-sources`` フェーズを実行します。

WindGateではモデルをDMDLで記述します。
DMDLスクリプトはプロジェクトの ``src/main/dmdl`` ディレクトリ以下に配置し、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて保存します。
DMDLの記述方法については以下のドキュメント [#]_ などを参考にしてください。

..  [#] :doc:`../dmdl/start-guide` 


CSV入出力への対応
-----------------
データモデルの定義情報を作成したら、CSVの入出力に対応させたいデータモデルに対して、 ``@windgate.csv`` という属性を指定します。
この属性が指定されたデータモデルは、宣言されたプロパティと同じ順序のフィールドを持つCSVファイルの入出力に対応します。

この属性は、データモデルの宣言の直前に指定します。
以下は記述例です。

..  code-block:: none

    @windgate.csv
    example_model = {
        // ... プロパティの定義
    };

この状態でデータモデルを作成すると、データモデルのほかに以下の3つのクラスが作成されます。

#. ``<パッケージ名>.csv.<データモデル名>CsvSupport``
#. ``<パッケージ名>.csv.Abstract<データモデル名>ImporterDescription``
#. ``<パッケージ名>.csv.Abstract<データモデル名>ExporterDescription``

より細かな設定については :doc:`user-guide` を参照してください。


Asakusa DSLの記述
=================
WindGateを利用する場合でも、Asakusa DSLの基本的な記述方法は同様です。
WindGate特有の部分は、 `CSVファイルをインポートする`_ と `CSVファイルをエクスポートする`_ 部分のみです。

それ以外の部分については、 :doc:`../dsl/start-guide` を参照してください。 


CSVファイルをインポートする
---------------------------
開発環境のCSVファイルをインポートしてHadoopの処理を行う場合、 `CSV入出力への対応`_ で生成した ``<パッケージ名>.csv.Abstract<データモデル名>ImporterDescription`` クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    インポータが使用するプロファイル名を戻り値に指定します。

    インポータは実行時に ``$ASAKUSA_HOME/windgate/profile`` 以下の ``<プロファイル名>.properties`` に記述された設定を元に動作します。
    今回はデフォルトを利用するので、 ``"asakusa"`` という文字列を ``return`` 文に指定してください。

``String getPath()``
    インポートするCSVファイルのパスを指定します。

    ここでは `ベースディレクトリの設定`_ で指定した ``resource.local.basePath`` からの相対パスで指定します。
    ベースディレクトリの設定と合わせて ``/tmp/windgate-<ログインユーザ名>/<指定したパス>`` というファイルを利用します。


以下は ``Document`` というデータモデルを宣言した場合の実装例です。

..  code-block:: java

    public class DocumentFromCsv extends AbstractDocumentCsvImporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "input.csv";
        }
    }

ここで記述した内容は、ジョブフローの入力に対して、 ``@Import`` を利用して指定します。


CSVファイルをエクスポートする
-----------------------------
ジョブフローの処理結果をCSVファイルにエクスポートする場合、 `CSV入出力への対応`_ で生成した ``<パッケージ名>.csv.Abstract<データモデル名>ExporterDescription`` クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    エクスポータが使用するプロファイル名を戻り値に指定します。

    インポータと同様に ``"asakusa"`` という文字列を ``return`` 文に指定してください。

``String getPath()``
    エクスポートするCSVファイルのパスを指定します。

    インポータと同様に `ベースディレクトリの設定`_ で指定した ``resource.local.basePath`` からの相対パスで指定します。
    ベースディレクトリの設定と合わせて ``/tmp/windgate-<ログインユーザ名>/<指定したパス>`` というファイルに結果を出力します。

    なお、出力先にすでにファイルが存在する場合、エクスポート時に上書きされます。
    インポートに指定したファイルや、他のエクスポート処理で使用するファイルとは別のファイルを指定するようにしてください。


以下は ``Document`` というデータモデルを宣言した場合の実装例です。

..  code-block:: java

    public class DocumentToCsv extends AbstractDocumentCsvExporterDescription {

        @Override
        public String getProfileName() {
            return "asakusa";
        }

        @Override
        public String getPath() {
            return "output.csv";
        }
    }

ここで記述した内容は、ジョブフローの入力に対して、 ``@Export`` を利用して指定します。


アプリケーションの実行
======================
アプリケーションのビルドや実行方法は、通常のAsakusa Frameworkのアプリケーション開発と同様です。

`CSVファイルをインポートする`_ で指定したように、入力データのCSVファイルは  ``/tmp/windgate-<ログインユーザ名>/input.csv`` など、 ``getPath()`` で指定したものを利用してください。
出力結果は同様に、 `CSVファイルをエクスポートする`_  で指定したように、 ``/tmp/windgate-<ログインユーザ名>/output.csv`` など、 ``getPath()`` で指定したパスに出力されているはずです。

:doc:`../application/start-guide` などを参照してください。


