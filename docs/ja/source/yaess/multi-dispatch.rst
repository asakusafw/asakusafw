==============================
ジョブ実行クラスターの振り分け
==============================

この文書では、ジョブの実行クラスターの振り分けを行うYAESSプラグイン ``asakusa-yaess-multidispatch`` の利用方法について解説します。

このプラグインを利用すると、個々のバッチやジョブフローなどを異なるクラスター上に振り分けて実行したり、それぞれ異なる設定で起動したりできるようになります。

クラスターの振り分け方法
========================

このプラグインでは、バッチに含まれるそれぞれのジョブを実行する際に、それぞれ異なる設定を使ったジョブの実行が行えます。
バッチごとに異なる設定のクラスター上で処理を行ったり、ジョブフローごとに異なるHadoopパラメーターを利用するなど様々な設定が可能です。

このプラグインを利用する場合、YAESS本体の構成ファイルの ``hadoop`` セクション [#]_ や ``command.<プロファイル名>`` セクション [#]_ に、専用のハンドラを登録します。
また、ハンドラの設定の中に、振り分け先のクラスター上でジョブを実行するような別のハンドラを複数登録します。
ここでは、このプラグインが提供するハンドラを「振り分けハンドラ」、振り分け先のハンドラのことを「サブハンドラ」と呼びます。

構成ファイルについての詳しい情報は、 `Hadoopジョブの振り分け設定`_ や `コマンドラインジョブの振り分け設定`_ をそれぞれ参照してください。

振り分ける規則は、「振り分け設定ファイル」というファイルを作成し、その中に記述します。
このファイルは対象のバッチごとに :file:`<バッチID>.properties` というプロパティファイルとして作成します。

振り分け設定ファイル内には、ジョブフロー、フェーズ、ジョブがそれぞれどのサブハンドラを利用してジョブを起動するかを記載します。
振り分けハンドラは、振り分け設定ファイル内の情報をもとに、適切なサブハンドラを選択してそれぞれのジョブを実行します。

振り分け設定ファイルについての詳しい情報は、 `ジョブの振り分け先設定`_ を参照してください。

..  [#] ``hadoop`` セクションについては :doc:`user-guide` - :ref:`yaess-profile-hadoop-section` を参照してください。
..  [#] ``command`` セクションについては :doc:`user-guide` - :ref:`yaess-profile-command-section` を参照してください。

プラグインの利用方法
====================

プラグインの登録
----------------

このプラグインを利用するには、 ``asakusa-yaess-multidispatch`` というプラグインライブラリをYAESSに登録します。
このプラグインは バージョン |version| ではYAESSの標準構成に含まれています。

Hadoopジョブの振り分け設定
--------------------------

Hadoopジョブをサブハンドラに振り分けて実行する場合、構成ファイルの ``hadoop`` セクションに以下の内容を設定します。

..  list-table:: Hadoopジョブの振り分け設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``hadoop``
      - ``com.asakusafw.yaess.multidispatch.HadoopScriptHandlerDispatcher``
    * - ``hadoop.conf.directory``
      - 振り分け設定ファイルを配置するディレクトリ
    * - ``hadoop.conf.setup``
      - ``setup`` フェーズを実行するサブハンドラ名
    * - ``hadoop.conf.cleanup``
      - ``cleanup`` フェーズを実行するサブハンドラ名
    * - ``hadoop.<サブハンドラ名>``
      - サブハンドラのクラス名
    * - ``hadoop.<サブハンドラ名>.<設定名>``
      - サブハンドラの設定値

``hadoop`` には振り分けハンドラを利用するためのディスパッチャクラスを指定します。
YAESS導入時には ``hadoop`` には標準的なハンドラクラスが設定されているので、この設定を変更します。

``hadoop.conf.directory`` は振り分け設定ファイルを配置するディレクトリへの絶対パスを指定します。
このディレクトリの直下に対象バッチのバッチIDと同名の振り分け設定ファイルを作成し、それぞれのファイルで `ジョブの振り分け先設定`_ を行います。

``hadoop.conf.setup`` と ``hadoop.conf.cleanup`` はいずれも省略可能です。
省略した場合は振り分け設定ファイルの内容を元にサブハンドラを選択します。
上記にサブハンドラ名を設定した場合、振り分け設定ファイルの内容によらず指定のサブハンドラで ``setup`` フェーズや ``cleanup`` フェーズのジョブをそれぞれ実行します。

``hadoop.<サブハンドラ名>`` や ``hadoop.<サブハンドラ名>.<設定名>`` では、サブハンドラの設定を行います。
ここでは、通常の ``hadoop`` セクションの代わりに ``hadoop.<サブハンドラ名>`` というサブセクションを作成し、それぞれのサブハンドラの設定を行ってください。
サブハンドラ名には半角の英数字のみを利用してください。

例えば、サブハンドラ名に `remote` を指定し、このハンドラに対して :ref:`yaess-profile-hadoop-section-ssh` 設定を適用する場合、以下例のようになります [#]_ 。

..  code-block:: properties
    :caption: yaess.properties
    :name: yaess.properties-yaess-multi-dispatch-1

    hadoop.remote = com.asakusafw.yaess.jsch.SshHadoopScriptHandler
    hadoop.remote.ssh.user=asakusa
    hadoop.remote.ssh.host=example.com
    hadoop.remote.ssh.port=22

サブハンドラは複数定義することが出来ますが、必ず ``default`` という名前のサブハンドラの設定を含めてください。
これは、振り分け設定ファイルで振り分け先のサブハンドラが明示的に指定されなかった場合に利用されるサブハンドラとなります。

..  attention::
    ``default`` という名前のサブハンドラが設定されていない場合、YAESSの初期化時にエラーとなります。

上記のうち、先頭の ``hadoop`` を除くすべての項目には ``${変数名}`` という形式で、YAESSを起動した環境の環境変数を含められます。
ただし、サブハンドラについてはサブハンドラごとの設定項目によって環境変数を利用可能かどうかが決まります。

..  [#] ここでは設定の一部のみを記載しています。サブハンドラの設定については後述の `設定例`_ も参考にしてください。

コマンドラインジョブの振り分け設定
----------------------------------

コマンドラインジョブをサブハンドラに振り分けて実行する場合、構成ファイルの ``command.<プロファイル名>`` セクションに以下の内容を設定します。

..  list-table:: コマンドラインジョブの振り分け設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``command.<プロファイル名>``
      - ``com.asakusafw.yaess.multidispatch.CommandScriptHandlerDispatcher``
    * - ``command.<プロファイル名>.conf.directory``
      - 振り分け設定ファイルを配置するディレクトリ
    * - ``command.<プロファイル名>.conf.setup``
      - ``setup`` フェーズを実行するサブハンドラ名
    * - ``command.<プロファイル名>.conf.cleanup``
      - ``cleanup`` フェーズを実行するサブハンドラ名
    * - ``command.<プロファイル名>.<サブハンドラ名>``
      - サブハンドラのクラス名
    * - ``command.<プロファイル名>.<サブハンドラ名>.<設定名>``
      - サブハンドラの設定値

``command.<プロファイル名>`` には振り分けハンドラを利用するためのディスパッチャクラスを指定します。
YAESS導入時には ``command.<プロファイル名>`` には標準的なハンドラクラスが設定されているので、この設定を変更します。

``command.<プロファイル名>.conf.directory`` は振り分け設定ファイルを配置するディレクトリへの絶対パスを指定します。
このディレクトリの直下に対象バッチのバッチIDと同名の振り分け設定ファイルを作成し、それぞれのファイルで `ジョブの振り分け先設定`_ を行います。

``command.<プロファイル名>.conf.setup`` と ``command.<プロファイル名>.conf.cleanup`` はいずれも省略可能です。
省略した場合は振り分け設定ファイルの内容を元にサブハンドラを選択します。
上記にサブハンドラ名を設定した場合、振り分け設定ファイルの内容によらず指定のサブハンドラで ``setup`` フェーズや ``cleanup`` フェーズのジョブをそれぞれ実行します。

``command.<プロファイル名>.<サブハンドラ名>`` や ``command.<プロファイル名>.<サブハンドラ名>.<設定名>`` では、サブハンドラの設定を行います。
ここでは、通常の ``command.<プロファイル名>`` セクションの代わりに ``command.<プロファイル名>.<サブハンドラ名>`` というサブセクションを作成し、それぞれのサブハンドラの設定を行ってください。
サブハンドラ名には半角の英数字のみを利用してください。

例えば、サブハンドラ名に `remote` を指定し、このハンドラに対してコマンドラインジョブのプロファイル `asakusa` に対して :ref:`yaess-profile-command-section-ssh` 設定を適用する場合、以下のようになります [#]_ 。

..  code-block:: properties
    :caption: yaess.properties
    :name: yaess.properties-yaess-multi-dispatch-2

    command.asakusa.remote = com.asakusafw.yaess.jsch.SshCommandScriptHandler
    command.asakusa.remote.ssh.user=asakusa
    command.asakusa.remote.ssh.host=example.com
    command.asakusa.remote.ssh.port=22

..  attention::
    コマンドラインジョブの振り分け機能を使うと、 :ref:`yaess-profile-command-section` で説明するプロファイル単位で実行方法を切り替える機能の代替として、単一のプロファイル( ``command.*`` )のみを指定し、ジョブフロー単位でコマンドラインジョブを振り分けることで同様の振る舞いを実現可能な場合がありますが、この方法は推奨できません。

    プロファイル単位で実行方法を分ける必要がある場合は、できるだけプロファイル名を分けて個別の ``command.<プロファイル名>`` セクションを用意して設定を切り替えるべきです。

サブハンドラは複数定義することが出来ますが、必ず ``default`` という名前のサブハンドラの設定を含めてください。
これは、振り分け設定ファイルで振り分け先のサブハンドラが明示的に指定されなかった場合に利用されるサブハンドラとなります。

..  attention::
    ``default`` という名前のサブハンドラが設定されていない場合、YAESSの初期化時にエラーとなります。

上記のうち、先頭の ``command.<プロファイル名>`` を除くすべての項目には ``${変数名}`` という形式で、YAESSを起動した環境の環境変数を含められます。
ただし、サブハンドラについてはサブハンドラごとの設定項目によって環境変数を利用可能かどうかが決まります。

..  [#] ここでは設定の一部のみを記載しています。サブハンドラの設定については後述の `設定例`_ も参考にしてください。

ジョブの振り分け先設定
----------------------

ジョブをサブハンドラに振り分ける際には、「振り分け設定ファイル」を利用して振り分け先を判断します。

この振り分け設定ファイルは、 ``hadoop.conf.directory`` や ``command.<プロファイル名>.conf.directory`` で指定したディレクトリの直下に ``<バッチID>.properties`` という名前で作成します [#]_ 。

振り分け設定ファイルには、以下のような行をJavaのプロパティファイルの形式で記載します。

..  list-table:: ジョブの振り分け先設定
    :widths: 1 6 4
    :header-rows: 1

    * - 優先順位
      - 行の内容
      - 概要
    * - 1
      - ``<フローID>.<フェーズ名>.<ステージID> = <サブハンドラ名>``
      - 対象のジョブを指定のサブハンドラで実行する
    * - 2
      - ``<フローID>.<フェーズ名>.* = <サブハンドラ名>``
      - 対象フェーズ [#]_ のすべてのジョブを指定のサブハンドラで実行する
    * - 3
      - ``<フローID>.* = <サブハンドラ名>``
      - 対象ジョブフローのすべてのジョブを指定のサブハンドラで実行する
    * - 4
      - ``* = <サブハンドラ名>``
      - 対象バッチのすべてのジョブを指定のサブハンドラで実行する

一つの設定ファイルには複数の設定を記載することができます。
あるジョブの実行が2つ以上の設定に該当する場合、優先順位が最も小さい行に従います [#]_ 。

対応する振り分け設定ファイルが存在しない場合や、設定ファイル内にマッチする行が存在しない場合、 ``default`` という名前のサブハンドラを利用してジョブを実行します。

..  attention::
    現在のAsakusa Frameworkでは、バッチコンパイルのたびにステージIDがランダムに決定されます。
    ステージIDまで指定して設定を振り分ける場合にはアプリケーションの再デプロイの際に意図した設定が効かなくなる可能性があるため注意が必要です。

..  hint::
    ステージIDについては、 :doc:`../dsl/user-guide` - :ref:`compiled-batch-application-components` を参照してください。

..  [#] 例えばバッチIDが `example.summarizeSales` の場合、振り分け設定ファイル名は `example.summarizeSales.properties` となります。
..  [#] 利用可能なフェーズについては :doc:`user-guide` - :ref:`yaess-batch-structure` を参照してください。
        なお、 ``setup`` と ``cleanup`` フェーズは振り分けハンドラ側の設定が優先されます。
..  [#] 振り分け設定ファイル内に記載した行の順序は、設定の優先度に影響しません。

設定例
======

複数の実行環境にジョブフローを振り分ける例
------------------------------------------

以下はローカル環境上のHadoopの設定と、リモート環境上のHadopの設定を定義し、ジョブフロー単位で使用するHadoopを振り分ける設定例(構成ファイルの一部)です。
2つのHadoopクラスターを処理に応じて使い分ける場合などを想定しています。

ローカル環境上の設定に対するサブハンドラには ``default`` を、リモート環境の設定に対するサブハンドラには ``remote`` という名前をそれぞれ指定しています。

..  code-block:: properties
    :caption: yaess.properties
    :name: yaess.properties-yaess-multi-dispatch-3

    # 振り分けハンドラ本体
    hadoop = com.asakusafw.yaess.multidispatch.HadoopScriptHandlerDispatcher
    hadoop.conf.directory = ${ASAKUSA_HOME}/yaess/conf/multidispatch/

    command.* = com.asakusafw.yaess.multidispatch.CommandScriptHandlerDispatcher
    command.*.conf.directory = ${ASAKUSA_HOME}/yaess/conf/multidispatch/

    # ローカル環境向けサブハンドラ (default)
    hadoop.default = com.asakusafw.yaess.basic.BasicHadoopScriptHandler
    hadoop.default.resource = hadoop-local
    hadoop.default.env.HADOOP_CMD = /usr/bin/hadoop
    hadoop.default.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

    command.*.default = com.asakusafw.yaess.basic.BasicCommandScriptHandler
    command.*.default.resource = asakusa-local
    command.*.default.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

    # リモート環境向けサブハンドラ (remote)
    hadoop.remote = com.asakusafw.yaess.jsch.SshHadoopScriptHandler
    hadoop.remote.ssh.user=asakusa
    hadoop.remote.ssh.host=example.com
    hadoop.remote.ssh.port=22
    hadoop.remote.ssh.privateKey=${HOME}/.ssh/id_dsa
    hadoop.remote.resource = hadoop-remote
    hadoop.remote.env.HADOOP_CMD = /usr/bin/hadoop
    hadoop.remote.env.ASAKUSA_HOME = /home/asakusa/asakusafw

    command.*.remote = com.asakusafw.yaess.jsch.SshCommandScriptHandler
    command.*.remote.ssh.user=asakusa
    command.*.remote.ssh.host=example.com
    command.*.remote.ssh.port=22
    command.*.remote.ssh.privateKey=${HOME}/.ssh/id_dsa
    command.*.remote.resource = asakusa-remote
    command.*.remote.env.ASAKUSA_HOME = /home/asakusa/asakusafw


そして、 ``md.batch`` というバッチに含まれる ``farexec`` というジョブフローのみをリモート環境で実行し、それ以外のすべての処理をローカル環境で動作させる場合を考えます。

まず、各 ``.conf.directory`` で指定したディレクトリ以下に、バッチ ``md.batch`` に対応する振り分け設定ファイルとして ``md.batch.properties`` というファイルを作成します。
上記の例では、 :file:`${ASAKUSA_HOME}/yaess/conf/multidispatch/md.batch.properties` というパスになります。
このファイルを以下のように定義します。

..  code-block:: properties
    :caption: md.batch.properties
    :name: md.batch.properties-yaess-multi-dispatch-1

    # farexec だけ remote で実行
    farexec.* = remote

    # それ以外は default で実行
    * = default

..  note::
    上記のように完全に異なる2つ以上の環境を併用する場合、ジョブフローまたはバッチの単位で振り分けを行うとよいでしょう。
    フェーズやジョブなどジョブフローより細かい単位で振り分けを行った場合、ジョブフロー実行中の中間結果がジョブ間で共有されないため、通常は正しく動作しません。

    なお、複数のクラスターでデフォルトのファイルシステムを共有している場合、上記は問題になりません。

単一の実行環境を異なる設定で利用する例
--------------------------------------

以下は同一のHadoopを異なる設定で利用する設定例(構成ファイルの一部)です。
振り分けの設定をチューニンパラメータとして利用する場合などを想定しています。

デフォルトの設定を利用するサブハンドラには ``default`` を、Reduceタスク数を4に設定したサブハンドラには ``reduce4`` を、Reduceタスク数を8に設定したサブハンドラには ``reduce8`` という名前をそれぞれ指定しています。

..  code-block:: properties
    :caption: yaess.properties
    :name: yaess.properties-yaess-multi-dispatch-5

    # 振り分けハンドラ本体
    hadoop = com.asakusafw.yaess.multidispatch.HadoopScriptHandlerDispatcher
    hadoop.conf.directory = ${HOME}/.asakusa/multidispatch

    # デフォルト設定を利用するサブハンドラ (default)
    hadoop.default = com.asakusafw.yaess.basic.BasicHadoopScriptHandler
    hadoop.default.resource = hadoop
    hadoop.default.env.HADOOP_CMD = /usr/bin/hadoop
    hadoop.default.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

    # 別の設定を利用するサブハンドラ (reduce4)
    hadoop.reduce4 = com.asakusafw.yaess.basic.BasicHadoopScriptHandler
    hadoop.reduce4.resource = hadoop
    hadoop.reduce4.prop.mapred.reduce.tasks = 4
    hadoop.reduce4.env.HADOOP_CMD = /usr/bin/hadoop
    hadoop.reduce4.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

    # 別の設定を利用するサブハンドラ (reduce8)
    hadoop.reduce8 = com.asakusafw.yaess.basic.BasicHadoopScriptHandler
    hadoop.reduce8.resource = hadoop
    hadoop.reduce8.prop.mapred.reduce.tasks = 8
    hadoop.reduce8.env.HADOOP_CMD = /usr/bin/hadoop
    hadoop.reduce8.env.ASAKUSA_HOME = ${ASAKUSA_HOME}

そして、 ``md.batch`` というバッチに含まれる ``medium`` というジョブフローの ``epilogue`` フェーズのみで ``mapred.reduce.tasks = 4`` が有効になり、同ジョブフローのそれ以外のフェーズでは ``mapred.reduce.tasks = 8`` が有効になるような例を考えます。

上記の例では、 :file:`${HOME}/.asakusa/multidispatch/md.batch.properties` というファイルを以下のように定義します。

..  code-block:: properties
    :caption: md.batch.properties
    :name: md.batch.properties-yaess-multi-dispatch-2

    medium.epilogue.* = reduce4
    medium.* = reduce8

この場合、 ``medium.epilogue.* = reduce4`` の方が ``medium.* = reduce8`` よりも優先されるため、 ``epilogue`` フェーズではサブハンドラ ``reduce4`` を利用します。
また、 それ以外のフェーズでは ``reduce8`` を利用します。

なお、上記に記載されていないジョブフローでは、デフォルト設定の ``default`` を利用します。

