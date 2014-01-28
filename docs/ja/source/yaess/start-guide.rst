===================
YAESSスタートガイド
===================
この文書では、 :doc:`../introduction/start-guide` の構成で、YAESSの使い方について簡単に紹介します。
その他のプロジェクト構成でYAESSを利用する場合には、 :doc:`user-guide` を参照してください。

開発環境でのYAESSの実行
=======================
:doc:`../introduction/start-guide` や :doc:`../application/gradle-plugin` 、:doc:`../application/maven-archetype` の手順で作成されたプロジェクトでは、バッチアプリケーションのビルド構成に、あらかじめYAESSと連携するための設定が含まれています。

バッチの実行
------------
YAESSを使ってバッチを実行するには、コマンドラインから ``$ASAKUSA_HOME/yaess/bin/yaess-batch.sh <バッチID>`` と入力します。
また、バッチに起動引数を指定する場合、コマンドラインの末尾に ``-A <変数名>=<値>`` のように記述します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

出力の最後に ``Finished: SUCCESS`` と表示されればバッチ処理は成功です。
また、バッチ処理の結果はコマンドの終了コードでも確認できます。
YAESSではUnixの方式に従い、正常終了の場合は ``0`` , それ以外の場合は ``0`` でない終了コードを返します。

..  code-block:: sh

    Starting YAESS
         Profile: /home/asakusa/asakusa/yaess/conf/yaess.properties
          Script: /home/asakusa/asakusa/batchapps/example.summarizeSales/etc/yaess-script.properties
        Batch ID: example.summarizeSales
    ...

    Finished: SUCCESS

..  attention::
    YAESSでバッチアプリケーションを実行するには、バッチアプリケーションが規定のディレクトリにデプロイ済みである必要があります。開発環境にバッチコンパイルしたアプリケーションをデプロイする方法については、 :doc:`../introduction/start-guide` などを参照してください。


ワークフロー記述
----------------
Asakusa DSLで記述したバッチアプリケーションをバッチコンパイル ( ``./gradlew compileBatchapp`` ) すると、
バッチアプリケーションにはYAESS用のワークフロー記述として
YAESSスクリプト ( ``<バッチID>/etc/yaess-script.properties`` )というファイルが含まれます。
YAESSはYAESSスクリプトの定義内容に基づいてバッチアプリケーションを実行します。

YAESSスクリプトはバッチ全体のワークフローの構造をYAESS向けに表しています。
YAESSスクリプトの内容を確認するには、コマンドラインから
``$ASAKUSA_HOME/yaess/bin/yaess-explain.sh <バッチアプリケーションのパス>/yaess-script.properties``
と入力します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-explain.sh $ASAKUSA_HOME/batchapps/example.summarizeSales/etc/yaess-script.properties

この結果、以下のようなJSON形式のバッチの構造が表示されます。

..  code-block:: javascript

    {
      "id": "example.summarizeSales",
      "jobflows": [
        {
          "id": "byCategory",
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


実行環境構成の変更
==================
YAESSはプロファイルセットとよぶ実行環境の構成をもっており、これは
``$ASAKUSA_HOME/yaess/conf/yaess.properties`` (以降、「構成ファイル」) 
を編集することでユーザが自由にカスタマイズすることができます。

例えば、次のようなものを変更できます。

* バッチの実行排他制御の仕組み
* バッチのログメッセージの通知方法
* バッチ内のジョブスケジューリング方法
* Hadoopジョブの起動方法
* ThunderGateやWindGateの起動方法

ここでは、いくつかの設定を変更する方法について紹介します。

SSHを経由したHadoopの実行
-------------------------
YAESSを利用すると、SSHを経由してリモートコンピューターにログインし、
リモートコンピュータ上に導入されているHadoopを利用して
Hadoopのジョブを発行するような環境構成を作成できます。

ここではそのような構成を行う設定方法を説明します。まず、YAESSをリモートコンピューター上にもインストールしておきます [#]_ 。

次に、ローカルのYAESSの構成ファイル ( ``$ASAKUSA_HOME/yaess/conf/yaess.properties`` ) を編集します。
既定の構成は以下のようになっており、これはYAESSはローカルのコンピューターにインストールされたHadoopを利用して、Hadoopのジョブを実行するよう設定されています。

..  code-block:: properties

    hadoop = com.asakusafw.yaess.basic.BasicHadoopScriptHandler
    hadoop.resource = hadoop-master
    hadoop.env.HADOOP_CMD = /usr/bin/hadoop
    hadoop.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

これをリモートのHadoopを実行するよう変更するため、以下のプロパティの内容に変更してください [#]_ 。

..  list-table:: SSHを経由してHadoopを実行する際の設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``hadoop``
      - :javadoc:`com.asakusafw.yaess.jsch.SshHadoopScriptHandler`
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
    * - ``hadoop.env.HADOOP_CMD``
      - リモートの ``hadoop`` コマンドのパス
    * - ``hadoop.env.ASAKUSA_HOME``
      - リモートのAsakusa Frameworkのインストール先

以下は設定例です。

..  code-block:: properties

    hadoop = com.asakusafw.yaess.jsch.SshHadoopScriptHandler
    hadoop.ssh.user = hadoop
    hadoop.ssh.host = hadoop.example.com
    hadoop.ssh.port = 22
    hadoop.ssh.privateKey = ${HOME}/.ssh/id_dsa
    hadoop.ssh.passPhrase = 
    hadoop.resource = hadoop-master
    hadoop.env.HADOOP_CMD = /usr/bin/hadoop
    hadoop.env.ASAKUSA_HOME = /opt/hadoop/asakusa

..  [#] リモートコンピュータでは、実際には ``$ASAKUSA_HOME/yaess-hadoop`` のみを利用します。
        これは「Hadoopブリッジ」というツールで、YAESSからHadoopジョブを起動する際に利用されます。
        詳しくは :doc:`user-guide` を参照してください。
..  [#] デフォルトで定義されているローカルのHadoopを実行するための設定は不要なため、
        これらの行は削除するか、行頭に ``#`` を追加してコメントアウトします。

SSHを経由したThunderGate/WindGateの実行
---------------------------------------
Hadoopと同様に、ThunderGateやWindGateなどの外部連携コマンドもSSHを経由してリモートコンピュータから実行できます。

上記と同様、ローカルのYAESSの構成ファイル ( ``$ASAKUSA_HOME/yaess/conf/yaess.properties`` ) を編集します。
既定の構成は以下のようになっており、これはローカルのコンピューターにインストールされたコマンドを実行するよう設定されています。

..  code-block:: properties

    command.* = com.asakusafw.yaess.basic.BasicCommandScriptHandler
    command.*.resource = asakusa
    command.*.env.HADOOP_CMD = /usr/bin/hadoop
    command.*.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

これを、次の内容に変更します。

..  list-table:: SSHを経由してコマンドを実行する際の設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``command.*``
      - :javadoc:`com.asakusafw.yaess.jsch.SshCommandScriptHandler`
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
    * - ``command.*.env.HADOOP_CMD``
      - リモートの ``hadoop`` コマンドのパス

以下は設定例です。

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
これらの名前別に実行構成を指定するには、YAESSの構成ファイル ( ``$ASAKUSA_HOME/yaess/conf/yaess.properties`` ) 内で
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
    command.asakusa.env.HADOOP_CMD = /usr/bin/hadoop
    command.asakusa.env.ASAKUSA_HOME = /home/asakusa/asakusa

ここに追加する内容は ``command.*`` から始まる内容と同様です。

構成ファイルにあらかじめ記載された ``command.*`` という構成は、名前付きのプロファイルが見つからなかった際に利用されます。
上記のように名前付きの構成を指定した場合、ターゲット名やプロファイル名が一致すれば名前付きの構成が優先されます。
