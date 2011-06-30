===================================
Asakusa Framework スタートガイド
===================================

この文書では、Clouderaから提供されているHadoopがインストール済みのVMWare仮想マシン `Cloudera's Hadoop Demo VM`_ (以下 **Cloudera VM** と表記)上にAsakusa Frameworkをインストールし、Asakusa DSLで記述したサンプルアプリケーションを実行する手順を紹介します。

.. _Cloudera's Hadoop Demo VM: https://ccp.cloudera.com/display/SUPPORT/Cloudera's+Hadoop+Demo+VM

なお、Cloudera VMを使わずに任意のLinux OS上に開発環境を構築する場合の手順は、 :doc:`user-guide` を参照してください。

Cloudera VMの起動
=================
Cloudera VMはVMWare上で動作します。VMWare Player(Windows/Linux)やVMWare Fusion(Mac)をインストール済みのマシン上で、次のURL [#]_ から仮想イメージファイルをダウンロードし、この仮想マシンを実行してください。

..  [#] https://downloads.cloudera.com/cloudera-demo-0.3.7.vmwarevm.tar.bz2

ログイン画面が表示されたらユーザ: ``cloudera`` を選択します。ユーザ選択後、画面下からキーボード配列を選択出来ます。デフォルトではキーボードレイアウトがUSに設定されているため、必要に応じて"Japan"(日本語レイアウト)を選択するとよいでしょう。

パスワード: ``cloudera`` を入力してログインします。

Asakusa Frameworkのセットアップ
===============================
Asakusa FrameworkはCloudera VM用にセットアップスクリプトを提供しています。このセットアップスクリプトを実行すると以下のモジュールがインストールされ、Asakusa Frameworkを利用するための初期設定が行われます。

 * Asakusa Framework 本体のインストールと初期設定
 * Asakusa Framework サンプルプログラムのインストール
 * Ruby 1.8 のインストール
 * Git 1.7 のインストール
 * MySQL Server 5.1 のインストールと初期設定
 * Apache Maven 3.0 のインルトール
 * SSHの初期設定

Terminalを起動し、以下の手順に沿ってAsakusa Frameworkをインストールしてください。

..  code-block:: sh

    wget https://github.com/downloads/asakusafw/asakusafw-contrib/quickstart-cdh3vm.tar.gz
    tar xvzf quickstart-cdh3vm.tar.gz
    cd cdh3vm
    ./setup.sh

成功したら屋形船が表示されます。

Terminal上で実行中のシェルに対してAsakusa Framework用の環境変数を適用します。

..  code-block:: sh

    source ~/.asakusarc

サンプルプログラムの実行
========================
セットアップスクリプトを実行すると、$HOME/workspace配下にAsakusa Frameworkを使って実装されたいくつかのサンプルアプリケーション用プロジェクトが配置されます。

このうち、簡単な在庫引当のバッチアプリケーション（商品マスタと注文明細の突合を行い、商品ごとの注文数の合計を算出）を実行してみます。

..  code-block:: sh

    cd $HOME/workspace/asakusafw-examples/example-tutorial
    mvn clean test -Dtest=TutorialBatchTest
    
このテストプログラムの実行によって、Asakusa Frameworkは以下のような処理が行われています。

#. Asakusa TestDriverがアプリケーションの処理対象データがMySQLのテーブルに登録する。
#. Asakusa ThunderGateがMySQLのテーブルがHadoopのHDFS上に配置する。
#. Ashigel CompilerがAsakusa DSLからMapReduceプログラム（ジョブ）を生成し、MapReduceジョブがHadoop上で実行される。
#. Asakusa ThunderGateがMapReduceジョブの処理結果をMySQLのテーブルに登録する。
#. Asakusa TestDriverがMySQLに登録されたアプリケーションの実行結果を事前に作成したテスト期待値と突合せ、テストの成否を出力する。

なお、Cloudera VM上で動作するHadoopは、デフォルトでは「擬似分散モード」で起動しています。Asakusa Frameworkによって実行されたMapReduceジョブやHDFSの状態は、Hadoopが提供する管理画面から確認することができます（Cloudera VMのFirefoxは、あらかじめブックマークツールバーにHadoop管理画面へのリンクが登録されています）。

またMySQLは、 ``asakusa`` という名前のデータベースを使ってテストプログラムが実行されるようセットアップされています。テストプログラムの処理結果データはこのデータベースの内容を見ることで確認することができます。

Eclipseを使ったアプリケーションの開発
=====================================
Eclipseを使ったアプリケーションの開発については、 :doc:`user-guide` の :ref:`user-guide-eclipse` を参照して下さい。

バッチアプリケーションの実装
============================
Asakusa Framework では Asakusa DSL と呼ばれるバッチアプリケーション記述用のDSLを使ってアプリケーションを実装します。Asakusa DSL については :doc:`../dsl/start-guide` を参照してください。
