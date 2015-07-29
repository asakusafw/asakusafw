==========================
開発環境にHadoopを導入する
==========================

この文書は、Asakusa Frameworkのバッチアプリケーション開発環境で利用するHadoopのインストールやセットアップに関して説明します。

..  seealso::
    運用環境(Hadoopクラスター)で利用するHadoopの導入に関しては、 :doc:`../administration/deployment-guide` を参照してください。

開発環境で利用するHadoopについて
================================

開発環境で利用するHadoopは、おもにバッチアプリケーションをテスト実行する目的で使用します。
開発環境では、Hadoopを「スタンドアロンモード」と呼ばれる分散処理を行わず単一JVM上で実行するモードで利用することを想定しています。

Hadoopディストリビューション
----------------------------

Asakusa Framework バージョン |version| では、開発環境で利用するHadoopディストリビューションは `Apache Hadoop`_ の利用を強く推奨しています。

その他のHadoopディストリビューションを利用する場合、一部の機能が利用できなかったり、開発環境に追加の設定が必要な場合があります [#]_ 。

なお、運用環境で利用するHadoopのディストリビューションは開発環境で使用する `Apache Hadoop`_ 以外の様々なHadoopディストリビューションを利用できます。
Asakusa Frameworkが動作検証を行なっているHadoopディストリビューションについては、 :doc:`../product/target-platform` を参照してください。

..  [#] 例えば、Mapが提供するMapRFSを操作するためには、MapRFS操作用のライブラリが必要となります。

Hadoopバージョン
----------------

Asakusa Framework バージョン |version| では、開発環境で利用するHadoopのバージョンは `Apache Hadoop`_ 1系の安定バージョン( ``1.2.x`` )を推奨しています [#]_ 。

このため、Asakusa Frameworkが提供するバッチアプリケーション開発用のプロジェクトテンプレート [#]_ の初期設定はHadoop1系を利用するよう設定されています。
詳細は後述の `開発環境のHadoopに関するプロジェクト設定`_ を参照してください。

開発環境と運用環境では異なるHadoopバージョンを利用することができます。
例えば、Hadoop1系の開発環境で生成したバッチアプリケーションはHadoop2系を利用する運用環境で実行することができます。
このとき、運用環境に対してはHadoopバージョンに対応したAsakusa Frameworkをデプロイする必要があります。

開発環境と運用環境で異なるHadoopバージョンを利用する場合の設定や導入方法については、:doc:`../administration/deployment-guide` を参照してください。

..  note::
    Asakusa Frameworkの開発環境では主にHadoopをスタンドアロンモードで実行することを想定していますが、現時点でAsakusa Frameworkが動作検証を行なっているHadoopバージョンでは、Hadoop1系に比べてHadoop2系ではスタンドアロンモードにおけるHadoopジョブ実行のオーバーヘッドが大きいため、フローDSLに対するアプリケーションのテスト実行などに時間がかかるようです。
    
..  [#] Apache Hadooop のリリースバージョンについては、Apache Hadoop の ドキュメント `Hadoop Releases`_ などを参照してください。

..  [#] プロジェクトテンプレートについて詳しくは :doc:`gradle-plugin` などを参照してください。

..  _`Apache Hadoop`: http://hadoop.apache.org/
..  _`Hadoop Releases`: http://hadoop.apache.org/releases.html

開発環境のHadoopに関するプロジェクト設定
========================================

ここでは、開発環境のHadoopに関するGradleプロジェクトの設定について説明します。

..  hint::
    開発環境に `Apache Hadoop`_ 1系を利用する場合は、通常は標準のプロジェクト設定のまま開発を行うことができます。

Hadoopバージョンに関するプロジェクト設定
----------------------------------------

以下は、プロジェクトテンプレートに含まれる標準のビルドスクリプト ( :file:`build.gradle` ) のうち、開発環境のHadoopバージョンに関する設定部分を抜粋しています。

..  code-block:: groovy
     
        asakusafw {
            asakusafwVersion '0.7.4-hadoop1'
            ...
        }
        
        asakusafwOrganizer {
            profiles.prod {
                asakusafwVersion asakusafw.asakusafwVersion
            }
        }
        
        dependencies {
            compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-core', version: asakusafw.asakusafwVersion
            compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-directio', version: asakusafw.asakusafwVersion
            compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-windgate', version: asakusafw.asakusafwVersion
        
            provided (group: 'org.apache.hadoop', name: 'hadoop-client', version: '1.2.1') {
                exclude module: 'junit'
                exclude module: 'mockito-all'
                exclude module: 'slf4j-log4j12'
            }
        }

``asakusafw`` ブロックの ``asakusafwVersion`` は開発環境で使用するAsakusa Frameworkのライブラリバージョンを指定します。
Hadoop2系向けのAsakusa Frameworkライブラリを指定する場合、 ``0.7.4-hadoop2`` のように指定します。

``dependencies`` ブロックの ``org.apache.hadoop:hadoop-client`` で指定している ``version`` は、開発環境で使用するHadoopのライブラリバージョンを指定します。
Hadoop2系向けのHadoopライブラリを指定する場合、 ``2.4.1`` のように指定します。

..  attention::
    Asakusa FrameworkのライブラリバージョンとHadoopのライブラリバージョンで異なるHadoopバージョン系を指定した場合、テスト実行時にバリデーションエラーとなります。

また、 ``asakusafwOrganizer`` ブロックの ``profiles.prod`` ブロックに含まれる ``asakusafwVersion`` は、運用環境にインストールするAsakusa Frameworkのバージョンを指定します。
開発環境と運用環境で異なるHadoopバージョンを利用する場合の設定や導入方法については、:doc:`../administration/deployment-guide` を参照してください。

開発環境向けのHadoopのセットアップ
==================================

ここでは、開発環境のHadoopのセットアップ方法について説明します。

ここでは、Asakusa Frameworkの推奨環境である `Apache Hadoop`_ 1系を使ったセットアップ例を示します。

Apache Hadoopのインストール
---------------------------

Apache Hadoopのインストール方法はOS毎に提供されているインストールパッケージを使う方法や、tarballを展開する方法などがありますが、ここではtarballを展開する方法でインストールします。

Apache Hadoopのダウンロードサイト (http://www.apache.org/dyn/closer.cgi/hadoop/common/) からHadoop本体のコンポーネントのtarball :file:`hadoop-1.2.X.tar.gz` ( ``X`` はバージョン番号 )  をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてApache Hadoopをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf hadoop-*.tar.gz
    sudo chown -R root:root hadoop-*/
    sudo mv hadoop-*/ /usr/lib
    sudo ln -s /usr/lib/hadoop-* /usr/lib/hadoop

環境変数の設定
--------------

Asakusa Frameworkを通じてHadoopを実行する場合、実行する :program:`hadoop` コマンドの配置場所を環境変数を利用して指定する必要があります。

:program:`hadoop` コマンドを利用するAsakusa Frameworkの各コンポーネントは、次の手順で :program:`hadoop` コマンドを検索します。

* 環境変数 ``HADOOP_CMD`` が設定されている場合、 ``$HADOOP_CMD`` を :program:`hadoop` コマンドとみなして利用します。
* :program:`hadoop` コマンドのパス ( 環境変数 ``PATH`` ) が通っている場合、それを利用します。

以下は、環境変数 ``HADOOP_CMD`` の設定例です。

..  code-block:: sh
    
    export HADOOP_CMD=/usr/lib/hadoop/bin/hadoop
