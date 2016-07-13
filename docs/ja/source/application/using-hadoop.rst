==========================
開発環境にHadoopを導入する
==========================

この文書は、Asakusa Frameworkのバッチアプリケーション開発環境で利用するHadoopのインストールやセットアップに関して説明します。

..  seealso::
    運用環境(Hadoopクラスター)で利用するHadoopの導入に関しては、 :doc:`../administration/deployment-guide` を参照してください。

開発環境で利用するHadoopについて
================================

開発環境で利用するHadoopは、おもにバッチアプリケーションをテスト実行する目的で使用します。
また、開発環境でYAESSを使ったバッチアプリケーションの実行を確認する場合に使用します。
開発環境では、Hadoopを「スタンドアロンモード」と呼ばれる分散処理を行わず単一JVM上で実行するモードで利用することを想定しています。

なおテストの実行に関して、Asakusa FrameworkではHadoopジョブを実行せずにバッチアプリケーションのテストを実行するための「エミュレーションモード」を提供しています。
エミュレーションモードを利用する場合、開発環境に対するHadoopの設定は不要です。また多くの場合、Hadoopのスタンドアロンモードよりも高速にテストを実行することができます。

エミュレーションモードの利用方法については、 :doc:`../testing/emulation-mode` を参照してください。

Hadoopディストリビューションとバージョン
----------------------------------------

Asakusa Framework バージョン |version| では、開発環境で利用するHadoopは `Apache Hadoop`_ 2系の安定バージョン( ``2.x.x`` )を推奨しています [#]_ 。

..  attention::
    Asakusa Framework バージョン 0.8.0 から、Hadoop1系 ( ``1.x.x`` ) は非対応となりました。

開発環境で `Apache Hadoop`_ 以外のHadoopディストリビューションを利用する場合、一部の機能が利用できなかったり、開発環境に追加の設定が必要な場合があります [#]_ 。

なお、運用環境で利用するHadoopのディストリビューションは、 `Apache Hadoop`_ 以外の様々なHadoopディストリビューションを利用できます。

..  seealso::
    Asakusa Frameworkが動作検証を行なっているHadoopディストリビューションについては、 :doc:`../product/target-platform` を参照してください。

..  [#] Apache Hadooop のリリースバージョンについては、Apache Hadoop の ドキュメント `Hadoop Releases`_ などを参照してください。

..  [#] 例えば、Mapが提供するMapRFSを操作するためには、MapRFS操作用のライブラリが必要となります。

..  _`Apache Hadoop`: http://hadoop.apache.org/
..  _`Hadoop Releases`: http://hadoop.apache.org/releases.html

開発環境向けのHadoopのセットアップ
==================================

ここでは、開発環境のHadoopのセットアップ方法について説明します。

Apache Hadoopのインストール
---------------------------

`Apache Hadoopのダウンロードページ`_ からHadoop本体のコンポーネントのtarball :file:`hadoop-2.X.X.tar.gz` ( ``X`` はバージョン番号 )  をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてApache Hadoopをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf hadoop-*.tar.gz
    sudo chown -R root:root hadoop-*/
    sudo mv hadoop-*/ /usr/lib
    sudo ln -s /usr/lib/hadoop-* /usr/lib/hadoop

..  _`Apache Hadoopのダウンロードページ`: http://hadoop.apache.org/releases.html#Download

環境変数の設定
--------------

Asakusa Frameworkを通じてHadoopを実行する場合、実行する :program:`hadoop` コマンドの配置場所を環境変数を利用して指定する必要があります。

:program:`hadoop` コマンドを利用するAsakusa Frameworkの各コンポーネントは、次の手順で :program:`hadoop` コマンドを検索します。

#. 環境変数 ``HADOOP_CMD`` が設定されている場合、 ``$HADOOP_CMD`` を :program:`hadoop` コマンドとみなして利用する
#. (非推奨) 環境変数 ``HADOOP_HOME`` にHadoopのインストール先が指定されている場合、 :program:`$HADOOP_HOME/bin/hadoop` を利用する
#. :program:`hadoop` コマンドのパス ( 環境変数 ``PATH`` ) が通っている場合、それを利用する

以下は、環境変数 ``HADOOP_CMD`` の設定例です。

..  code-block:: sh

    export HADOOP_CMD=/usr/lib/hadoop/bin/hadoop

また、テスト実行の際には上記で検出したHadoopの設定とは異なる設定でテストを実行することも可能です。
テストドライバーが利用するHadoopの設定は次の順番で検出しています。

#. 環境変数 ``HADOOP_CONF`` が指定されている場合、その内容を設定ディレクトリへのパスとして利用する
#. ``hadoop`` コマンドを実行し、そこで利用されている設定ディレクトリを利用する

上記のうち、環境変数 ``HADOOP_CONF`` を指定する際には :file:`core-site.xml` などのHadoopの設定情報が格納されたディレクトリへのパスを指定してください。
