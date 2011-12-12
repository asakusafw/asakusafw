=================================
デプロイメントガイド for WindGate
=================================

この文書では、外部システムとの連携にWindGateを用いる構成における、Asakusa Frameworkとバッチアプリケーションを運用環境にデプロイする手順について解説します。

用語の定義
==========
本書では、マシン構成に関しての用語を以下のように定義します。

  Hadoopクラスター
    Hadoopが提供する各サービス（デーモン）が稼働するマシンと、Hadoopのジョブを起動するためのクライアントマシンから構成されるサーバ群です。一般的にはJobTrackerおよびNameNodeが可動する「マスターノード」と、TaskTrackerおよびDataNodeから構成される「スレーブノード」から構成されます。

  データベースノード
    DBMSがインストールされているマシンを表します。WindGateをデータベースと連携する構成で使用する場合における、バッチ処理のデータ入出力先となります。

  Hadoopクライアントノード
    Hadoopクラスターのうち、Hadoopのジョブを起動するためのクライアントマシンを表します。

  Asakusa Frameworkノード
    Asakusa Frameworkをインストールするマシンを表します。

    通常はAsakusa FrameworkをHadoopクライアントノードと同一のマシンにインストールしますが、Hadoopクライアントノードとは異なるマシンにインストールする必要がある場合、WindGateが提供する「Hadoopブリッジ」と呼ばれるモジュールをHadoopクライアントノードにインストールして、Asakusa FrameworkノードとHadoopクライアントノード間をSSHによる通信を行うことで入出力データの受け渡しを行います。

システム構成の検討
==================
WindGateを用いた場合のシステム構成について、いくつか例を見ていきます。

WindGate/CSVによるシンプルな構成例
----------------------------------
WindGateをローカルのCSVファイルと連携する構成における、シンプルな構成例を以下に示します。

..  todo:: 図 

この構成では、HadoopクライアントとAsakusa Framework実行ノードを同じマシン上に構成しています。HadoopクライアントとAsakusa Framework実行ノードが同一マシンの場合、WindGateに対してHadoopへのアクセスに関する設定は特に行う必要がありません。

上図ではHadoopクライアントノードはHadoopクラスタ内の独立したマシンとして構成していますが、マスターノードがHadoopクライアントノードを兼ねるケースも多いでしょう。この場合、マスターノード上にAsakusa Frameworkをインストールします。

WinddGate/DBMSによるHadoopブリッジを使った構成例
------------------------------------------------
WindGateをDBMSと連携し、かつHadoopクライアントノードとはHadoopブリッジを使って連携する場合の構成例を以下に示します。

..  todo:: 図 

Asakusa Frameworkノードからデータベースノードに対しては、JDBCによる接続が行える必要があります。ここで、ネットワーク構成の検討において、Hadoopクライアントノードから直接データベースノードに対してはJDBCによる接続が行えない構成であったとします。

このような場合上図のように、Asakusa Frameworkをデータベースノードに対してはJDBC接続が可能な場所に、かつHadoopクライアントノードに対してはSSH接続が可能な場所に配置します。そしてHadoopクライアントノードに対しては、Asakusa FrameworkノードからのSSH接続を受け付けて、入出力データの受け渡しを行うためのHadoopブリッジをインストールします。

運用環境の構築
==============
運用環境の構築を以下の流れで説明します。

1. Hadoopクラスターの構築
2. Asakusa Frameworkのインストールアーカイブの準備
3. Asakusa Frameworkのインストール
4. WindGateのプロファイル設定
5. WindGate用プラグインライブラリの配置
6. Asakusa Framework実行時プラグインの設定（オプション）
7. バッチアプリケーションのデプロイ
8. バッチアプリケーションの実行

また、Hadoopブリッジを使用する場合は、HadoopクライアントノードにHadoopブリッジをインストールし、Hadoopブリッジを使用するための設定を行う必要があります。

本書では、まずシンプルな構成例に基づく運用環境の構築手順を説明します。その後Hadoopブリッジを使う場合の構築手順を説明します。

Hadoopクラスターの構築
----------------------
Hadoopクラスターを分散モードで動作するように構築します。Hadoopクラスターの具体的な構築手順は、Hadoopを提供している各ディストリビューションのドキュメント等を参考にして下さい。

Hadoopクラスターの構築が完了したら、HadoopクライアントノードにAsakusa Framework管理用のOSユーザを作成します。以後、このユーザを ASAKUSA_USER を表記します。

ASAKUSA_USER からHadoopが提供しているサンプルアプリケーションのジョブをhadoopコマンドを使って実行し、ジョブが正常に実行されることを確認して下さい。

Asakusa Frameworkのインストールアーカイブの準備
-----------------------------------------------
Asakusa Frameworkのインストールアーカイブを用意します。

Asakusa Frameworkのインストールアーカイブは、アプリケーション開発プロジェクトからMavenの以下のコマンドを実行して生成します。

..  code-block:: sh

    mvn assembly:single

このコマンドを実行すると、プロジェクトの target ディレクトリ配下にいくつかのファイルが生成されます。このうち以下のファイルがAsakusa FrameworkとWindGateをインストールするためのアーカイブです。

asakusafw-${asakusafw.version}-prod-windgate.tar.gz

${asakusafw.version}は使用しているAsakusa Frameworkのバージョンに置き換えます。例えばversion 0.2.4 を使っている場合は、 asakusafw-0.2.4-prod-windgate.tar.gz になります。

Asakusa Frameworkのインストール
-------------------------------
Hadoopのクライアントノード上にAsakusa Frameworkをインストールします。以下インストール手順です。

1. ASAKUSA_USERのプロファイル設定に環境変数JAVA_HOME, HADOOP_HOME, ASAKUSA_HOMEを追加します。
    * ここでは、プロファイル設定は~/.bash_profileに設定するものとします。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default
    export ASAKUSA_HOME=$HOME/asakusa
    export HADOOP_HOME=/usr/lib/hadoop

2. 1で追加した環境変数をシェルに反映します。

..  code-block:: sh

    $ source ~/.bash_profile

3. ASAKUSA_HOMEディレクトリを作成し、ASAKUSA_HOME配下にAsakusa Framework用のインストールアーカイブ(asakusafw-${asakusafw.version}-prod-windgate.tar.gz)を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mkdir $ASAKUSA_HOME
    cp asakusafw-*-prod-windgate.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusafw-*-prod-windgate.tar.gz
    find $ASAKUSA_HOME -name "*.sh" | xargs chmod u+x

WindGateのプロファイル設定
--------------------------
WindGateのプロファイル設定を環境に応じて設定します。

WindGateのプロファイル設定についての詳細は、 :doc:`../windgate/user-guide` などを参考にしてください。

WindGate用プラグインライブラリの配置
------------------------------------
WindGateのプラグインが利用する依存ライブラリを配置します。

WindGateのCSV連携を使用する場合は特に追加のプラグインライブラリは不要です。

WindGateのデータベース(JDBC)連携を使用する場合は、使用するJDBCドライバライブラリが含まれるJDBCドライバのjarファイルを、$ASAKUSA_HOME/windgate/plugin ディレクトリ配下に配置してください。

WindGateのプラグインライブラリについては、 :doc:`../windgate/user-guide` も参考にしてください。

..  note::
    Asakusa Frameworkのインストールアーカイブには、デフォルトのWindGate用プラグインライブラリとして、あらかじめ以下の3つのプラグインライブラリと、プラグインライブラリが使用する依存ライブラリが同梱されています。
    
    * asakusa-windgate-stream: ローカルのファイルと連携するためのプラグイン
    * asakusa-windgate-jdbc: JDBC経由でDBMSと連携するためのプラグイン
    * asakusa-windgate-hadoopfs: Hadoopブリッジを使用してHadoopと連携するためのプラグイン
    * jsch: asakusa-windgate-hadoopfsが依存するSSH接続用ライブラリ

Asakusa Framework実行時プラグインの設定
---------------------------------------
バッチアプリケーションが拡張用の実行時プラグインを使用する場合、実行時プラグインのインストールと設定を行います。

実行時プラグインの設定についての詳細は、 :doc:`deployment-runtime-plugins` を参考にしてください。

..  _deploy-batchapp:

バッチアプリケーションのデプロイ
--------------------------------
開発したバッチアプリケーションをデプロイします。ここでは :doc:`../introduction/start-guide` で作成したサンプルアプリケーションをインストールする例を示します。

開発環境で作成したアプリケーション用のアーカイブファイルを「$ASAKUSA_HOME/batchapps」配下に配置します。

..  code-block:: sh

    cp sample-app-batchapps-*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar -xf sample-app--batchapps-*.jar
    find . -name "*.sh" | xargs chmod u+x
    rm -f batchapp-batchapps-*.jar
    rm -fr META-INF

..  warning::
    デプロイ対象とするjarファイルを間違えないよう注意してください。デプロイ対象ファイルは ${artifactId}-**batchapps**-{version}.jar のようにアーティファクトIDの後に **batchapps** が付くjarファイルです。

    アプリケーションのビルドとデプロイについては、 :doc:`../introduction/start-guide` の「サンプルアプリケーションのビルド」「サンプルアプリケーションのデプロイ」も参考にしてください。
    
..  note::
    $ASAKUSA_HOME/batchapps ディレクトリ直下にはバッチIDを示すディレクトリのみを配置するとよいでしょう。上記例では、展開前のjarファイルや、jarを展開した結果作成されるMETA-INFディレクトリなどを削除しています。

バッチアプリケーションの実行
----------------------------
デプロイしたバッチアプリケーションをYAESSで実行します。

実行方法は、 :doc:`../introduction/start-guide` の「サンプルアプリケーションの実行」で説明したYAESSの実行方法と同じです。$ASAKUSA_HOME/yaess/bin/yaess-batch.sh にバッチIDとバッチ引数を指定して実行します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-01-01

バッチの実行が成功すると、コマンドの標準出力の最終行に「Finished: SUCCESS」と出力されます。

..  code-block:: sh

    ...
    2011/12/08 16:54:38 INFO  [JobflowExecutor-example.summarizeSales] END PHASE - example.summarizeSales|byCategory|CLEANUP@cc5c8cfd-604b-4652-a387-b2ea4d463943
    2011/12/08 16:54:38 DEBUG [JobflowExecutor-example.summarizeSales] Completing jobflow "byCategory": example.summarizeSales
    Finished: SUCCESS

成功した場合、ディレクトリ /tmp/windgate-$USER/result に集計データがCSVファイルとして出力されます。

SSH経由でHadoopと接続する
=========================
..  note::
    Asakusa FrameworkノードとHadoopクライアントノードが同一マシンの場合、以降の手順は実施しないてください。

Asakusa FrameworkノードとHadoopクライアントノードをSSHで接続する場合の環境構築手順を説明します。

Asakusa Frameworkノードの構築
-----------------------------
Asakusa Frameworkノードについては、基本的には先に説明した運用環境構築の流れの手順通りにデプロイを行います。その上で、Hadoopブリッジと連携するための追加の設定を行う必要があります。

Hadoopのインストール
~~~~~~~~~~~~~~~~~~~~
SSH経由でHadoopと接続する場合、Asakusa FrameworkノードはHadoopクラスターの一部ではありませんが、Asakusa Frameworkは内部でHadoopのライブラリを使用するため、Asakusa FrameworkノードにHadoopのインストールが行われている必要があります。

Asakusa FrameworkにインストールするHadoopは、Hadoopクラスタとしての設定を行う必要はありません。そのため、Asakusa FrameworkノードへのHadoopのインストールはHadoopのtarballを展開するといった方法でよいでしょう。

WindGateのプロファイル変更
~~~~~~~~~~~~~~~~~~~~~~~~~~
WindGateのプロファイル設定を変更し、Hadoopブリッジを使うように設定を行います。

プロファイルの設定については、 :doc:`../windgate/user-guide` の「SSH経由でリモートのHadoopを利用する」を参照してください。

YAESS構成ファイルの変更
~~~~~~~~~~~~~~~~~~~~~~~
YAESS構成ファイル ($ASAKUSA_HOME/yaess/conf/yaess.properties) を変更し、SSHを経由してHadoopジョブを実行するよう設定を行います。

YAESS構成ファイルの変更については、 :doc:`../yaess/user-guide` の「SSHを経由してHadoopジョブを実行する」を参照してください。

Hadoopクライアントノードの構築
------------------------------
HadoopクライアントノードにHadoopブリッジをインストールする手順は以下になります。

1. Hadoopブリッジのインストールアーカイブの準備
2. Hadoopブリッジのインストール
3. Asakusa Framework実行時プラグインの設定（オプション）
4. バッチアプリケーションのデプロイ

なお、Hadoopクラスターの構築については先の説明を参照して下さい。Hadoopクライアントノード上のASAKUSA_USERでHadoopのサンプルジョブが正常に実行できることを確認してください。

Hadoopブリッジのインストールアーカイブの準備
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Hadoopブリッジのインストールアーカイブを用意します。

HadoopブリッジのインストールアーカイブはAsakusa Frameworkのインストールアーカイブと同時に作成されます。アプリケーション開発プロジェクトからMavenの以下のコマンドを実行して生成します。

..  code-block:: sh

    mvn assembly:single

このコマンドを実行すると、プロジェクトの target ディレクトリ配下にいくつかのファイルが生成されます。このうち以下のファイルがHadoopブリッジ用ののアーカイブです。

asakusafw-${asakusafw.version}-prod-windgate-ssh.tar.gz

${asakusafw.version}は使用しているAsakusa Frameworkのバージョンに置き換えます。例えばversion 0.2.4 を使っている場合は、 asakusafw-0.2.4-prod-windgate.tar.gz になります。

Hadoopブリッジのインストール
~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Hadoopのクライアントノード上にHadoopブリッジをインストールします。以下インストール手順です。

1. ASAKUSA_USERのプロファイル設定に環境変数JAVA_HOME, HADOOP_HOME, ASAKUSA_HOMEを追加します。
    * ここでは、プロファイル設定は~/.bash_profileに設定するものとします。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default
    export ASAKUSA_HOME=$HOME/asakusa
    export HADOOP_HOME=/usr/lib/hadoop

2. 1で追加した環境変数をシェルに反映します。

..  code-block:: sh

    $ source ~/.bash_profile

3. ASAKUSA_HOMEディレクトリを作成し、ASAKUSA_HOME配下にHadoopブリッジ用のインストールアーカイブ(asakusafw-${asakusafw.version}-prod-windgate-ssh.tar.gz)を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mkdir $ASAKUSA_HOME
    cp asakusafw-*-prod-windgate-ssh.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusafw-*-prod-windgate-ssh.tar.gz
    find $ASAKUSA_HOME -name "*.sh" | xargs chmod u+x

4. Hadoopクライアントノード上の$HADOOP_HOME が /usr/lib/hadoop 以外の場合、$ASAKUSA_HOME/windgate-ssh/conf/env.sh に定義されているHADOOP_HOME変数を環境に合わせて修正します。

..  code-block:: sh

    export HADOOP_HOME="/usr/lib/hadoop"
    export HADOOP_USER_CLASSPATH_FIRST=true

Asakusa Framework実行時プラグインの設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
バッチアプリケーションが拡張用の実行時プラグインを使用する場合、実行時プラグインのインストールと設定を行います。

実行時プラグインの設定についての詳細は、 :doc:`deployment-runtime-plugins` を参考にしてください。

バッチアプリケーションのデプロイ
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
開発したバッチアプリケーションをデプロイします。

デプロイ手順はAsakusa Frameworkノードへのバッチアプリケーションのデプロイ( :ref:`deploy-batchapp` )と同じです。

..  warning::
    Asakusa FrameworkノードとHadoopクライアントノード上のバッチアプリケーションは必ず同一のアーカイブから展開してください。2つのノード間でバッチアプリケーションの内容が異なる場合、内容の不整合によってアプリケーションが正常に動作しない可能性があります。

