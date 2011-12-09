===================
YAESSスタートガイド
===================

この文書では、 :doc:`../introduction/start-guide` の構成で、YAESSの使い方について簡単に紹介します。
その他のプロジェクト構成でYAESSを利用する場合には、 :doc:`user-guide` を参照してください。

開発環境でのYAESSの実行
=======================

Mavenアーキタイプを利用したプロジェクトでは、バッチアプリケーションのビルド構成に、あらかじめYAESSと連携するための設定が含まれています。
YAESSを開発環境に導入するには、プロジェクトディレクトリ上で ``mvn assembly:single antrun:run`` コマンドを実行します。

導入に成功した場合、 ``$ASAKUSA_HOME/yaess`` と ``$ASAKUSA_HOME/yaess-hadoop`` にそれぞれディレクトリが作成されます。

バッチの作成
------------

プロジェクトディレクトリでコマンド ``mvn package`` を指定してビルドすると、 ``<プロジェクトディレクトリ>/target/batchc/<バッチID>`` というディレクトリが生成されます。
Mavenアーキタイプを利用して作成したプロジェクトでは、さらにディレクトリ内に ``etc/yaess-script.properties`` というファイルが自動的に作成されます。
これは、バッチ全体のワークフローの構造をYAESS向けに表しています。これをYAESSの機能を利用して表示するには、コマンドラインから次の用に入力します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-explain.sh <ビルド結果>/etc/yaess-script.properties

この結果、以下のようなJSON形式のバッチの構造が表示されます。

..  code-block:: javascript

    {
      "id": "ex",
      "jobflows": [
        {
          "id": "ex",
          "blockers": [],
          "phases": [
            "setup",
            "import",
            "main",
            "epilogue",
            "export",
            "finalize",
            "cleanup"
          ]
        }
      ]
    }


バッチの実行
------------

バッチを実行するには、まずビルド結果生成されたディレクトリ ``<プロジェクトディレクトリ>/target/batchc/<バッチID>`` を ``$ASAKUSA_HOME/batchapps/<バッチID>`` にコピーしてください。
その後、コマンドラインから ``$ASAKUSA_HOME/yaess/bin/yaess-batch.sh <バッチID>`` と入力します。

..  code-block:: sh

    asakusa@asakusa:~$ $ASAKUSA_HOME/yaess/bin/yaess-batch.sh ex
    Starting YAESS
       Profile: /home/asakusa/asakusa/yaess/bin/../conf/yaess.properties
        Script: /home/asakusa/asakusa/batchapps/ex/etc/yaess-script.properties
      Batch ID: ex
    ...
    Finished: SUCCESS

出力の最後に ``Finished: SUCCESS`` と表示されればバッチ処理は成功です。

なお、バッチ処理の結果はコマンドの終了コードでも確認できます。
YAESSではUnixの方式に従い、正常終了の場合は ``0`` , それ以外の場合は ``0`` でない終了コードを返します。

また、バッチに起動引数を指定する場合、コマンドラインの末尾に ``-A <変数名>=<値>`` のように記述します。
複数の起動引数を指定する場合には、スペース区切りで繰り返します。

たとえば、次のようにコマンドを記述します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh ex -A date=2011-03-31 -A wether=fine


実行環境構成の変更
==================

YAESSはプロファイルセットとよぶ実行環境の構成をもっていて、これはユーザーが自由に設定できます。
例えば、次のようなものを変更できます。

* バッチの実行排他制御の仕組み
* バッチのログメッセージの通知方法
* バッチ内のジョブスケジューリング方法
* Hadoopジョブの起動方法
* ThunderGateやWindGateの起動方法

ここでは、各種ジョブの設定を変更する方法について紹介します。
YAESSのプロファイルセットは、 ``$ASAKUSA_HOME/yaess/conf/yaess.properties`` から編集できます。


SSHを経由したHadoopの実行
-------------------------

YAESSの標準的な仕組みを利用すると、SSHを経由してリモートコンピューターにログインし、そこからHadoopのジョブを発行するような環境構成を作成できます。
まず、YAESSをリモートコンピューター上にもインストールしておきます [#]_ 。また、 ``$ASAKUSA_HOME/yaess-hadoop`` ディレクトリ以下を、リモートコンピューター上にも配置してください。

次に、テキストエディタでローカルのYAESSのプロファイルセット ( ``$ASAKUSA_HOME/yaess/conf/yaess.properties`` ) を開いてください。
既定の構成では、YAESSはローカルのコンピューターにインストールされたHadoopを利用して、Hadoopのジョブを実行しています。

..  code-block:: properties

    hadoop = com.asakusafw.yaess.basic.BasicHadoopScriptHandler
    hadoop.workingDirectory = target/hadoopwork/${execution_id}
    hadoop.resource = hadoop-master
    hadoop.env.HADOOP_HOME = ${HADOOP_HOME}
    hadoop.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

この行を削除するか行頭に ``#`` を追加してコメントアウトします。
代わりに、以下の内容を追加してください。

..  list-table:: SSHを経由してHadoopを実行する際の設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``hadoop``
      - ``com.asakusafw.yaess.jsch.SshHadoopScriptHandler``
    * - ``hadoop.workingDirectory``
      - ジョブフローごとの出力先パス [#]_
    * - ``hadoop.ssh.user``
      - ログイン先のユーザー名
    * - ``hadoop.ssh.host``
      - SSHのリモートホスト名
    * - ``hadoop.ssh.port``
      - SSHのリモートポート番号
    * - ``hadoop.ssh.privateKey``
      - ローカルの秘密鍵の位置
    * - ``hadoop.ssh.passPhrase``
      - 秘密鍵のパスフレーズ
    * - ``hadoop.env.HADOOP_HOME``
      - リモートのHadoopのインストール先
    * - ``hadoop.env.ASAKUSA_HOME``
      - リモートのAsakusa Frameworkのインストール先

以下は設定例です。

..  code-block:: properties

    hadoop = com.asakusafw.yaess.jsch.SshHadoopScriptHandler
    hadoop.workingDirectory = target/hadoopwork/${execution_id}
    hadoop.ssh.user = hadoop
    hadoop.ssh.host = hadoop.example.com
    hadoop.ssh.port = 22
    hadoop.ssh.privateKey = ${HOME}/.ssh/id_dsa
    hadoop.ssh.passPhrase = 
    hadoop.resource = hadoop-master
    hadoop.env.HADOOP_HOME = /usr/lib/hadoop
    hadoop.env.ASAKUSA_HOME = /opt/hadoop/asakusa

..  [#] 実際には ``$ASAKUSA_HOME/yaess-hadoop`` 以下のみが必要です。
        また、ローカルコンピューターには同ディレクトリは不要になります。

..  [#] ここには、プロジェクトディレクトリの ``build.properties`` で設定した ``asakusa.hadoopwork.dir`` の値を指定します。
        ここで指定されたパスは、ジョブフローの実行が成功した際にクリーニングされます。
        クリーニングを行わない場合にはこの設定自体を削除してください。


SSHを経由したThunderGate/WindGateの実行
---------------------------------------

Hadoopと同様に、ThunderGateやWindGateなどの外部連携コマンドもSSHを経由して実行できます。テキストエディタでYAESSのプロファイルセット ( ``$ASAKUSA_HOME/yaess/conf/yaess.properties`` ) を開いてください。

既定の構成では、YAESSはローカルのコンピューターにインストールされたコマンドを実行しています。


..  code-block:: properties

    command.* = com.asakusafw.yaess.basic.BasicCommandScriptHandler
    command.*.resource = asakusa
    command.*.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

上記の行を削除し、次の内容に変更します。

..  list-table:: SSHを経由してコマンドを実行する際の設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``command.*``
      - ``com.asakusafw.yaess.jsch.SshCommandScriptHandler``
    * - ``command.*.ssh.user``
      - ログイン先のユーザー名
    * - ``command.*.ssh.host``
      - SSHのリモートホスト名
    * - ``command.*.ssh.port``
      - SSHのリモートポート番号
    * - ``command.*.ssh.privateKey``
      - ローカルの秘密鍵の位置
    * - ``command.*.ssh.passPhrase``
      - 秘密鍵のパスフレーズ
    * - ``command.*.env.ASAKUSA_HOME``
      - リモートのAsakusa Frameworkのインストール先

以下は具体的な設定例です。


..  code-block:: properties

    command.* = com.asakusafw.yaess.jsch.SshCommandScriptHandler
    command.*.ssh.user = windgate
    command.*.ssh.host = windgate.example.com
    command.*.ssh.port = 22
    command.*.ssh.privateKey = ${HOME}/.ssh/id_dsa
    command.*.ssh.passPhrase =
    command.*.resource = asakusa
    command.*.env.ASAKUSA_HOME = /home/windgate/asakusa


コマンド実行方法の振り分け
--------------------------

複数のThunderGateやWindGateが異なるコンピューターにインストールされている場合、
YAESSでは「プロファイル」という考え方でそれぞれのコマンドを振り分けて実行できます。

ThunderGateには「ターゲット名」、WindGateには「プロファイル名」という実行構成の名前がそれぞれあります。
これらの名前別に実行構成を指定するには、YAESSのプロファイルセット ( ``$ASAKUSA_HOME/yaess/conf/yaess.properties`` ) 内で
``command.<構成の名前>`` から始まる設定を追加します。

以下は ``asakusa`` という名前のプロファイルに対するコマンド実行方法の記述です。

..  code-block:: properties

    command.asakusa = com.asakusafw.yaess.jsch.SshCommandScriptHandler
    command.asakusa.ssh.user = asakusa
    command.asakusa.ssh.host = asakusa.example.com
    command.asakusa.ssh.port = 22
    command.asakusa.ssh.privateKey = ${HOME}/.ssh/id_dsa
    command.asakusa.ssh.passPhrase =
    command.asakusa.resource = asakusa
    command.asakusa.env.ASAKUSA_HOME = /home/asakusa/asakusa

ここに追加する内容は ``command.*`` から始まる内容と同様です。

プロファイルセットにあらかじめ記載された ``command.*`` という構成は、名前付きのプロファイルが見つからなかった際に利用されます。
上記のように名前付きの構成を指定した場合、ターゲット名やプロファイル名が一致すれば名前付きの構成が優先されます。

