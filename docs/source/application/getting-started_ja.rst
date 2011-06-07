===================================
Asakusa Framework スタートガイド
===================================

この文書では、Clouderaから提供されているHadoopがインストール済みのVMWare仮想マシン `Cloudera's Hadoop Demo VM`_ (以下 **Cloudera VM** と表記)上にAsakusa Frameworkをインストールし、Asakusa DSLで記述したサンプルアプリケーションを実行する手順を紹介します。

.. _Cloudera's Hadoop Demo VM: https://ccp.cloudera.com/display/SUPPORT/Cloudera's+Hadoop+Demo+VM

Cloudera VMの起動
=================
Cloudera VMはVMWare上で動作します。VMWare Player(Windows/Linux)やVMWare Fusion(Mac)をインストール済みのマシン上で、次のURL [#]_ から仮想イメージファイルをダウンロードし、この仮想マシンを実行してください。

..  [#] https://downloads.cloudera.com/cloudera-demo-0.3.7.vmwarevm.tar.bz2

ログイン画面が表示されたらユーザ: ``cloudera`` を選択し、パスワード: ``cloudera`` を入力してログインします。

Cloudera VMはデフォルトでキーボードレイアウトがUSに設定されているため、必要に応じて以下の手順で日本語レイアウトに変更します。

#. メニューから [System] -> [Preferences] -> [Keyboard] を選択する。
#. [Layouts]タブを選択し、[Add...]ボタンを押す。
#. [Country]プルダウンから"Japan"を選択し、[Add]ボタンを押す。
#. レイアウト一覧から"Japan"を選択して[Move Up]ボタンを押し、"Japan"が一番上の状態にする。 
#. [Close]ボタンを押す。

Asakusa Frameworkのセットアップ
===============================
Asakusa FrameworkはCloudera VM用にセットアップスクリプトを提供しています。このセットアップスクリプトを実行すると以下のモジュールがインストールされ、Asakusa Frameworkを利用するための初期設定が行われます。

 * MySQL Server 5.1
 * Apache Maven 3.0
 * Asakusa Framework 本体
 * Asakusa Framework サンプルプログラム

Terminalを起動し、以下の手順に沿ってAsakusa Frameworkをインストールしてください。

..  code-block:: sh

    wget https://github.com/downloads/asakusafw/asakusafw-contrib/quickstart-cdh3vm.tar.gz
    tar xvzf quickstart-cdh3vm.tar.gz
    cd cdh3vm
    ./setup.sh
    source ~/.asakusarc
    

成功したら屋形船が表示されます。

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

また、MySQLについては、 ``asakusa`` という名前のデータベースを使ってテストプログラムが実行されるようセットアップされているので、テストプログラムの実行結果の様子はこのデータベースの内容を見ることで確認することができます。

Eclipseを使ったアプリケーションの開発
=====================================
Cloudera VM上でEclipseをダウンロード [#]_ し、Eclipseを起動します。ワークスペースは任意のディレクトリを指定します。

起動時に作成されたワークスペースディレクトリに対してクラスパス変数M2_REPOを設定します。ワークスペースをデフォルト値($HOME/workspce)に指定して起動した場合は以下のコマンドを実行します。

..  code-block:: sh

    mvn -Declipse.workspace=$HOME/workspace eclipse:add-maven-repo

次に、アプリケーション用プロジェクトに対してEclipseプロジェクト用の定義ファイルを作成します。

..  code-block:: sh

    mvn eclipse:eclipse

これでEclipseからプロジェクトをImport出来る状態になりました。Eclipseのメニューから [File] -> [Import] -> [General] -> [Existing Projects into Workspace] を選択し、プロジェクトディレクトリを指定してEclipseにインポートします。

..  [#] http://www.eclipse.org/downloads/
