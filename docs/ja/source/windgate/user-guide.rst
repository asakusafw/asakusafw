======================
WindGateユーザーガイド
======================

この文書では、WindGateの利用方法について紹介します。

WindGateプロファイル
====================

WindGateは「Hadoopクラスターとデータベース」または「Hadoopクラスターとローカルファイルシステム」などの二点間で様々なデータをやり取りするツールです。
主に、Asakusa Frameworkでバッチアプリケーションを作成する際に、データフローの入力データや出力データを取り扱うために利用します。

WindGateでは、「Hadoopクラスター」や「データベース」、「ローカルファイルシステム」などのそれぞれを「リソース」という考え方で同じように取り扱います。
WindGateの「プロファイル」にそれぞれのリソースとアクセス方法を記述することで、リソース間で自由にデータをやり取りできるようにします。

また、このプロファイルはWindGateに複数登録することもでき、複数のデータベースやローカルファイルシステム上のパス、またはアクセスするHadoopクラスターへの通信方法など、複数の組み合わせを個別に管理できます。

WindGateのプロファイルは、 :file:`$ASAKUSA_HOME/windgate/profile/<プロファイル名>.properties` (以降、構成ファイル)内に記述します。
``<プロファイル名>`` の部分には、特定のプロファイルごとに名前を付けてその名前を指定します。

それぞれの構成ファイルには、Javaの一般的なプロパティファイルの文法で、主に下記のセクションを記述します。

..  list-table:: プロパティファイルの項目
    :widths: 3 7
    :header-rows: 1

    * - セクション名
      - 内容
    * - ``core``
      - `WindGate本体の設定`_
    * - ``session``
      - `セッションの設定`_
    * - ``process.basic``
      - `プロセスの設定`_
    * - ``resource.hadoop``
      - `Hadoopクラスターの設定`_
    * - ``resource.local``
      - `ローカルファイルシステムの設定`_
    * - ``resource.jdbc``
      - `データベースの設定`_

ここでの「セクション」とは、該当するセクション名またはそのサブセクションとなるプロパティの項目のことをいいます。
たとえば、構成ファイル内の ``core`` , ``core.maxProcesses`` などはいずれも ``core`` セクション内の要素です。

WindGate本体の設定
------------------

WindGate本体の設定は、構成ファイル内の ``core`` セクション内に記述します。


同時に実行するデータ転送の設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

WindGateは、一度の処理で複数のリソース間でのデータ転送を行います。
それぞれのデータ転送を同時に実行するには、次の設定を追加します。

..  list-table:: 同時に実行するデータ転送の設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``core.maxProcesses``
      - 同時に実行可能なデータ転送数の最大

..  attention::
    この設定はプロファイルごとに個別のものです。
    ここで同時実行数を1に設定しても、複数のプロファイルに対する処理を同時に行った場合、データ転送は二つ以上が同時に実行する場合があります。

..  hint::
    この設定は、主にデータベースを利用するリソースなどでコネクション数に制限がある場合などを想定しています。
    特に制限がない場合には、WindGateを実行するコンピューターのコア数などと同じ数値にするのが良いでしょう。

セッションの設定
----------------

WindGateと連携したバッチアプリケーションを作成する場合、通常はそれぞれのジョブフローの入力と出力時にそれぞれWindGateの処理が実行されます。
たとえば、ジョブフローの処理の開始時にWindGateを利用して、データベースの内容をHadoopクラスター上に展開し（インポート）、処理の終了時に計算結果をデータベースに書き戻します（エクスポート）。

それぞれのジョブフローは、一連のトランザクション処理とみなされているため、このWindGateを利用したインポートとエクスポートは一連の処理として関連付けられていなければなりません。
この、処理を関連付けるための仕組みを「セッション」と呼びます。

WindGateのセッションはジョブフローの先頭でインポートを行う際に作成され、エクスポートが完了した後に破棄されます。
セッションの作成から破棄までの間にジョブフローが失敗した場合、このセッションの情報を元に何らかの復旧作業を行います。

また、同じセッションが同時に複数作成されようとした場合、WindGateを間違えて多重に起動してしまっている可能性があります。
このような間違いを防ぐためにも、セッションは利用されます。

セッションの設定は、構成ファイル内の ``session`` セクション内に記述します。

..  note::
    現在のところ、WindGateには `ローカルファイルシステムを利用したセッション`_ のみが提供されています。

ローカルファイルシステムを利用したセッション
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

セッション情報をローカルファイルシステム上に保持するには、 ``session`` セクションに下記の内容を指定します。

..  list-table:: ローカルファイルシステムを利用したセッションの設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``session``
      - ``com.asakusafw.windgate.file.session.FileSessionProvider``
    * - ``session.directory``
      - セッション情報を保持させるディレクトリ

ここで、 ``session.directory`` で指定したディレクトリ以下にセッションの情報に関するファイルが作成されます。
このプロパティには、 ``${環境変数名}`` の形式で環境変数を利用できます。
特に、 ``${WINDGATE_PROFILE}`` という値に利用中のプロファイル名が格納されているので、その値を利用するとプロファイルごとにディレクトリを分けられて便利です。

..  attention::
    複数のプロファイルを利用する場合、かならずそれぞれのプロファイルで利用するディレクトリを分けてください。
    同じディレクトリにした場合、それぞれのプロファイルを参照するWindGateを同時に実行した際に、正しく動作しない可能性があります。

..  note::
    この機能は、OSのファイルロックを利用して実現しています。
    ファイルロックが正しく動作しないOSやファイルシステム上では予想外の動作をするかもしれません。

プロセスの設定
--------------

WindGateは二つのリソースの間でデータを転送するツールです。
この転送時に二つのリソースを仲立ちするのが「プロセス」で、入力元からデータを取り出して、出力先にそのデータを書き出す処理を行います。

また、WindGateは一度の処理内で、複数のリソース間のデータ転送を行います。
入力と出力の対になるリソース間ごとにプロセスが作成され、同時に実行するプロセスの個数は `同時に実行するデータ転送の設定`_ で指定できます。

プロセスの設定は、構成ファイル内の ``process.basic`` セクション内に記述します。

..  note::
    ここでのセクション名が ``process.basic`` となっているのは、このプロセスが「通常の方法でデータ転送を行う」という役割を持っているためです。
    将来、キャッシュの機能などがサポートされる際には、 ``process`` セクションも増える予定です。

..  note::
    ここでの「プロセス」はUNIXのプロセスとは別物です。
    実際、WindGateのプロセスは、同一JavaVM上のそれぞれのスレッドで実行されます。

通常のデータ転送プロセス
~~~~~~~~~~~~~~~~~~~~~~~~

標準的なデータ転送プロセスを利用するには、 ``process.basic`` セクションに以下のように記述します。

..  list-table:: 通常のデータ転送プロセスの設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``process.basic``
      - ``com.asakusafw.windgate.core.process.BasicProcessProvider``

この項目には、特に追加の設定はありません。

.. _windgate-userguide-retryable-plugin:

再試行可能なデータ転送プロセス
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

再試行可能なデータ転送プロセスを利用するには、 ``process.basic`` セクションに以下のように記述します。

..  list-table:: 再試行可能なデータ転送プロセスの設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``process.basic``
      - ``com.asakusafw.windgate.retryable.RetryableProcessProvider``
    * - ``process.basic.component``
      - ``com.asakusafw.windgate.core.process.BasicProcessProvider``
    * - ``process.basic.retryCount``
      - リトライ回数
    * - ``process.basic.retryInterval``
      - 再試行までの待機時間 (秒)

``process.basic.component`` は実際に利用するデータ転送プロセスを設定します。
現在利用可能なプロセスは `通常のデータ転送プロセス`_ のみであるため、ここには ``com.asakusafw.windgate.core.process.BasicProcessProvider`` を指定します。

再試行可能なデータ転送プロセスでは、 ``process.basic.component`` に指定したデータ転送プロセスを利用し、通常の方法でデータ転送を行います。

データ転送に失敗した場合、 ``process.basic.retryCount`` に設定された回数を上限として、成功するまで上記プロセスを再実行します。
また、 ``process.basic.retryInterval`` が指定されている場合、その時間だけ待機後にプロセスが再実行されます。
``process.basic.retryInterval`` が指定されていない場合は即座に再実行します。

なお、このプロセスを利用するには、プラグインライブラリに ``asakusa-windgate-retryable`` の追加が必要です。
詳しくは `プラグインライブラリの管理`_ を参照してください。

Hadoopクラスターの設定
----------------------

Asakusa Frameworkで作成したバッチからWindGateを利用する場合、リソースの片方にはHadoopクラスターを利用します。

Hadoopクラスターとの通信方法は、構成ファイル内の ``resource.hadoop`` セクション内に記述します。

同一環境上のHadoopを利用する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

WindGateを起動したコンピュータ上のHadoopを利用するには、 ``resource.hadoop`` セクションに以下のように記述します。

..  list-table:: 同一環境上のHadoopを利用する設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``resource.hadoop``
      - ``com.asakusafw.windgate.hadoopfs.HadoopFsProvider``
    * - ``resource.hadoop.basePath``
      - 転送先のベースパス (省略可)

``resource.hadoop.basePath`` は転送先のベースパスで、省略時はHadoopのデフォルト設定を利用します。
URI形式で、 ``hdfs://<host>:8080/user/asakusa`` 等のHadoopファイルシステム上のパスを指定できます。

上記の設定のうち、先頭の ``resource.hadoop`` を除くすべての項目の値の中に ``${環境変数名}`` という形式で環境変数を含められます。

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-hadoopfs`` の追加が必要です。
詳しくは `プラグインライブラリの管理`_ を参照してください。

..  attention::
    Asakusa Framework ``0.7.0`` より、設定 ``resource.hadoop.compression`` は利用できなくなりました。
    転送時の圧縮はフレームワークが規定する内部の形式を利用するようになります。

..  hint::
    通常の利用方法では、 ``resource.hadoop.basePath`` を設定する必要はありません。
    既定値以外のファイルシステムを利用する場合などに利用することを想定しています。

Hadoopを利用する際の環境変数
^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Hadoopクラスターと通信するリソースを利用するには、WindGateの起動時にHadoopの設定がすべて利用可能である必要があります。
WindGate起動時のHadoopの設定と、バッチで利用するHadoopの設定が異なる場合、正しく動作しない可能性があります。

環境変数の設定方法は `WindGateの環境変数設定`_ を参照してください。

.. _windgate-userguide-ssh-hadoop:

SSH経由でリモートのHadoopを利用する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

WindGateからリモートコンピュータにSSHで接続し、そこにインストールされたHadoopを利用するには、 ``resource.hadoop`` セクションに以下のように記述します。
また、 `Hadoopブリッジ`_ をリモートコンピュータ上にインストールしておく必要があります。

..  list-table:: SSH経由でリモートのHadoopを利用する設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``resource.hadoop``
      - ``com.asakusafw.windgate.hadoopfs.jsch.JschHadoopFsProvider``
    * - ``resource.hadoop.user``
      - ログイン先のユーザー名
    * - ``resource.hadoop.host``
      - SSHのリモートホスト名
    * - ``resource.hadoop.port``
      - SSHのリモートポート番号
    * - ``resource.hadoop.privateKey``
      - ローカルの秘密鍵の位置
    * - ``resource.hadoop.passPhrase``
      - 秘密鍵のパスフレーズ
    * - ``resource.hadoop.env.ASAKUSA_HOME``
      - ログイン先の Asakusa Framework のインストール先
    * - ``resource.hadoop.env.<name>``
      - ログイン先の環境変数 ``<name>`` の値

上記の設定のうち、先頭の ``resource.hadoop`` を除くすべての項目の値の中に ``${環境変数名}`` という形式で環境変数を含められます。

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-hadoopfs`` 、および :file:`$ASAKUSA_HOME/windgate/lib` ディレクトリに JSch [#]_ の追加が必要です。
詳しくは `プラグインライブラリの管理`_ を参照してください。

リモートと通信する際に、SSHで接続する元でもHadoopの設定が必要です。
必要な環境変数については `Hadoopを利用する際の環境変数`_ を参照してください。

..  attention::
    Asakusa Framework ``0.7.0`` より、設定 ``resource.hadoop.compression`` は利用できなくなりました。
    転送時の圧縮はフレームワークが規定する内部の形式を利用するようになります。

..  [#] http://www.jcraft.com/jsch/

Hadoopブリッジ
^^^^^^^^^^^^^^

WindGateからSSHを経由してHadoopにアクセスする際に、HadoopブリッジとよぶAsakusa Frameworkのツールを経由します。

このツールは通常 :file:`$ASAKUSA_HOME/windgate-ssh` というディレクトリにインストールされていて、リモートコンピューターのAsakusa Frameworkにも同様のディレクトリが必要です。
また、プロファイルの ``resource.hadoop.env.ASAKUSA_HOME`` には、リモートコンピューターのAsakusa Frameworkのインストール先をフルパスで指定してください。

このツールの内部では、以下の順序で :program:`hadoop` コマンドを検索し、そのコマンドでHadoopクラスターの操作を行います。

* 環境変数 ``HADOOP_CMD`` が設定されている場合、 ``$HADOOP_CMD`` を :program:`hadoop` コマンドとみなして利用します。
* 環境変数 ``HADOOP_HOME`` が設定されている場合、 :program:`$HADOOP_HOME/bin/hadoop` コマンドを利用します。
* :program:`hadoop` コマンドのパスが通っている場合、それを利用します。

上記のうち、必要な環境変数をプロファイル内の ``resource.hadoop.env.<name>`` や、リモート環境上の :file:`$ASAKUSA_HOME/windgate-ssh/conf/env.sh` ファイル内で設定してください。
結果としてコマンドが見つからなかった場合にはエラーになります。

また、ログの設定は :file:`$ASAKUSA_HOME/windgate-ssh/conf/logback.xml` で行えます。
WindGate本体と同様に、SLF4JとLogbackを利用しています [#]_ 。

..  attention::
    HadoopブリッジはSSH経由で実行され、標準入出力を利用してWindGateとデータのやり取りを行います。
    ブリッジのJavaプログラム内で標準出力を利用しようとした場合、標準エラー出力にリダイレクトされるようになっています。
    そのため、ログの設定を行う際には、ログメッセージの出力先に注意してください。
    通常はログ出力先に標準出力を設定しないようにしてください。

    また、 :file:`$ASAKUSA_HOME/windgate-ssh/conf/env.sh` に指定した
    ``HADOOP_USER_CLASSPATH_FIRST`` の設定は、ログの設定を有効にするためにも必要です。
    特別な理由でHadoopのクラスパスを優先したい時を除き、 ``HADOOP_USER_CLASSPATH_FIRST`` の設定を変更しないようにしてください。

..  [#] `WindGateのログ設定`_ を参照

ローカルファイルシステムの設定
------------------------------

WindGateのリソースとして、WindGateを起動したコンピュータのファイルシステムを指定できます [#]_ 。

構成ファイル内の ``resource.local`` セクション内に以下の設定を記述します。

..  list-table:: ローカルファイルシステムを利用する設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``resource.local``
      - ``com.asakusafw.windgate.stream.file.FileResourceProvider``
    * - ``resource.local.basePath``
      - ベースパス

``resource.local.basePath`` は絶対パスで指定し、WindGateはそのパス以下のみを利用します。
また、 ``resource.local.basePath`` には ``${環境変数名}`` の形式で環境変数を指定できます。

上記の設定のうち、先頭の ``resource.local`` を除くすべての項目の値の中に ``${環境変数名}`` という形式で環境変数を含められます。

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-stream`` の追加が必要です。
詳しくは `プラグインライブラリの管理`_ を参照してください。

..  warning::
    開発環境では、ベースパスに壊れてもよいディレクトリを指定してください。
    ここで指定したパスはテスト実行時などにテストドライバーが削除したり変更したりします。

..  [#] WindGateを起動したコンピュータから、OSのファイルシステムを利用するというだけですので、ネットワークファイルシステム等でもファイルシステム上にマウントしてあれば利用可能です。
    なお、「ローカル」と書いているのは、Hadoopのファイルシステムと区別するためです。

データベースの設定
------------------

WindGateのリソースとして、JDBCをサポートするデータベースを指定できます。

現在の構成では、WindGateから直接JDBCドライバーを利用して対象のデータベースにアクセスします。
また、データの取得にはテーブルを ``SELECT`` 文で取得し、データの書き戻しにはテーブルを ``TRUNCATE`` した後にバッチモードで ``INSERT`` 文を発行します。

..  warning::
    この構成では、データの書き出し前に対象のテーブルの内容を完全に削除します。
    そのため、書き出し先のテーブルには通常利用するテーブルとは別のテーブルを指定し、WindGateの外側でマージ処理等を行ってください。

..  attention::
    この構成では、データの取得時にアプリケーション側でのページネーション等を行いません。
    そのため、MySQLなどのカーソル機能が十分でないデータベースでは、巨大なデータを取得する際に十分なパフォーマンスが得られません。
    特に、MySQLの場合には設定に ``resource.jdbc.batchGetUnit=1000`` , ``resource.jdbc.properties.useCursorFetch=true`` 等を指定し、カーソルを利用するようにしてください。

構成ファイル内の ``resource.jdbc`` セクション内に以下の設定を記述します。

..  list-table:: データベースを利用する設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``resource.jdbc``
      - ``com.asakusafw.windgate.jdbc.JdbcResourceProvider``
    * - ``resource.jdbc.driver``
      - JDBCドライバーのクラス名
    * - ``resource.jdbc.url``
      - 接続先データベースのJDBC URL
    * - ``resource.jdbc.user``
      - データベースのユーザー名
    * - ``resource.jdbc.password``
      - データベースのパスワード
    * - ``resource.jdbc.batchGetUnit``
      - 一度に取得するデータの件数 (読み出し時) [#]_
    * - ``resource.jdbc.batchPutUnit``
      - 一度に挿入するデータの件数 (書き込み時) [#]_
    * - ``resource.jdbc.connect.retryCount``
      - 接続時のリトライ回数 (省略時にはリトライなし)
    * - ``resource.jdbc.connect.retryInterval``
      - 接続リトライまでの間隔 (秒、省略時には10秒)
    * - ``resource.jdbc.statement.truncate``
      - テーブルの内容を削除する際の文形式 [#]_ (省略時には ``TRUNCATE`` 文)
    * - ``resource.jdbc.properties.<キー名>``
      - コネクションプロパティの値

上記の設定のうち、先頭の ``resource.jdbc`` を除くすべての項目の値の中に ``${環境変数名}`` という形式で環境変数を含められます。

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-jdbc`` とJDBCドライバーライブラリの追加が必要です。
詳しくは `プラグインライブラリの管理`_ を参照してください。

..  [#] この値は ``Statement.setFetchSize()`` に設定します。
    PostgreSQL等ではこの設定によってカーソルを利用するモードになります。
    この値が未設定の場合や ``0`` を設定した場合、 ``Statement.getFetchSize()`` は既定値が利用されます。

..  [#] 大きすぎる値を指定するとメモリ不足で正しく動作しません。
    1000から10000程度での動作を確認しています。

..  [#] この設定は ``java.text.MessageFormat`` の形式で指定し、削除対象のテーブル名は ``{0}`` で指定してください。
    省略時には ``TRUNCATE TABLE {0}`` が利用され、代わりに ``DELETE FROM {0}`` などを指定できます。
    なお、 ``MessageFormat`` ではシングルクウォート ( ``'`` ) が特殊文字として取り扱われることに注意が必要です。

その他のWindGateの設定
======================

WindGateプロファイルのほかに、WindGate全体の設定に関するものがいくつか用意されています。

WindGateの環境変数設定
----------------------

WindGateの実行に特別な環境変数を利用する場合、 :file:`$ASAKUSA_HOME/windgate/conf/env.sh` 内でエクスポートして定義できます。

WindGateをAsakusa Frameworkのバッチから利用する場合、以下の環境変数が必要です。

..  list-table:: WindGateの実行に必要な環境変数
    :widths: 10 60
    :header-rows: 1

    * - 名前
      - 備考
    * - ``ASAKUSA_HOME``
      - Asakusaのインストール先パス。
    * - ``HADOOP_USER_CLASSPATH_FIRST``
      - `WindGateのログ設定`_ 時にHadoopのログ機構を利用しないための設定。 ``true`` を指定する。

特別な理由がない限り、 ``ASAKUSA_HOME`` はWindGateを実行する前にあらかじめ定義しておいてください。
:file:`$ASAKUSA_HOME/windgate/conf/env.sh` では、その他必要な環境変数を定義するようにしてください。

..  hint::
    :doc:`YAESS <../yaess/index>` を経由してWindGateを実行する場合、WindGateで利用する環境変数 ``ASAKUSA_HOME`` はYAESS側の設定で行えます。
    詳しくは :doc:`../yaess/user-guide` を参照してください。

その他、以下の環境変数を利用可能です。

..  list-table:: WindGateで利用可能な環境変数
    :widths: 10 60
    :header-rows: 1

    * - 名前
      - 備考
    * - ``HADOOP_CMD``
      - 利用する :program:`hadoop` コマンドのパス。
    * - ``HADOOP_HOME``
      - Hadoopのインストール先パス。
    * - ``WINDGATE_OPTS``
      - WindGateを実行するJava VMの追加オプション。

なお、WindGateの本体は、以下の規約に従って起動します (上にあるものほど優先度が高いです)。

* 環境変数に ``HADOOP_CMD`` が設定されている場合、 ``$HADOOP_CMD`` コマンドを経由して起動します。
* 環境変数に ``HADOOP_HOME`` が設定されている場合、 :file:`$HADOOP_HOME/bin/hadoop` コマンドを経由して起動します。
* :program:`hadoop` コマンドのパスが通っている場合、 :program:`hadoop` コマンドを経由して起動します。
* :program:`java` コマンドから直接起動します。

このため、 ``HADOOP_CMD`` と ``HADOOP_HOME`` の両方を指定した場合、 ``HADOOP_CMD`` の設定を優先します。

..  hint::
    :program:`hadoop` コマンドが見つからない場合、WindGateは代わりに :program:`java` コマンドを利用してアプリケーションを起動します。
    前者はHadoopに関する設定やクラスライブラリなどが有効になりますが、後者は :file:`$ASAKUSA_HOME/windgate/lib` 以下のライブラリのみをクラスパスに通し、Hadoopに関する設定を行いません。

    特別な理由がない限り、 :file:`$ASAKUSA_HOME/windgate/conf/env.sh` 内で ``HADOOP_CMD`` や ``HADOOP_HOME`` を設定しておくのがよいでしょう。
    または、 :doc:`YAESS <../yaess/index>` を利用して外部から環境変数を設定することも可能です。

WindGateのログ設定
------------------

WindGateは内部のログ表示に ``SLF4J`` [#]_ 、およびバックエンドに ``Logback`` [#]_ を利用しています。
ログの設定を変更するには、 :file:`$ASAKUSA_HOME/windgate/conf/logback.xml` を編集してください。

また、WindGateの実行時には以下の値がシステムプロパティとして設定されます。

..  list-table:: WindGate実行時のシステムプロパティ
    :widths: 20 10
    :header-rows: 1

    * - 名前
      - 値
    * - ``com.asakusafw.windgate.log.batchId``
      - バッチID
    * - ``com.asakusafw.windgate.log.flowId``
      - フローID
    * - ``com.asakusafw.windgate.log.executionId``
      - 実行ID

Logback以外のログの仕組みを利用する場合、 :file:`$ASAKUSA_HOME/windgate/lib` にあるLogback関連のライブラリを置換した上で、設定ファイルを :file:`$ASAKUSA_HOME/windgate/conf` 以下に配置します (ここは実行時にクラスパスとして設定されます)。

..  [#] http://www.slf4j.org/
..  [#] http://logback.qos.ch/

プラグインライブラリの管理
--------------------------

WindGateの様々な機能は、プラグイン機構を利用して実現しています。
それぞれのプラグイン、およびプラグインが利用する依存ライブラリは、 :file:`$ASAKUSA_HOME/windgate/plugin` ディレクトリ直下に配置してください。

たとえば、WindGateはHadoopクラスターにアクセスする際にもプラグインが必要です。
標準的なものはWindGate導入時に自動的にプラグインが追加されますが、その他のプラグインは拡張モジュールとして提供されるため、必要に応じて拡張モジュールを導入してください。

..  seealso::
    拡張モジュールの一覧やその導入方法については、
    :doc:`../application/gradle-plugin` や
    :doc:`../administration/deployment-guide` を参照してください。

標準プラグインライブラリ
~~~~~~~~~~~~~~~~~~~~~~~~

Asakusa Frameworkのデプロイメントアーカイブには、デフォルトのWindGate用プラグインライブラリとして、あらかじめ以下のプラグインライブラリと、プラグインライブラリが使用する依存ライブラリが同梱されています。

..  list-table:: WindGate標準プラグインライブラリ
    :widths: 4 6
    :header-rows: 1

    * - プラグインライブラリ
      - 説明
    * - ``asakusa-windgate-stream``
      - ローカルのファイルシステムと連携するためのプラグイン
    * - ``asakusa-windgate-jdbc``
      - JDBC経由でDBMSと連携するためのプラグイン
    * - ``asakusa-windgate-hadoopfs``
      - Hadoopと連携するためのプラグイン

ローカルファイルシステムの入出力
================================

Asakusa FrameworkのバッチアプリケーションからWindGateを利用してローカルファイルシステムの入出力を行うには、対象のプロファイルに `ローカルファイルシステムの設定`_ を追加します。

また、データモデルとバイトストリームをマッピングする ``DataModelStreamSupport`` [#]_ の実装クラスを作成します。
この実装クラスは、DMDLコンパイラの拡張を利用して自動的に生成できます。

なお、以降の機能を利用するには次のライブラリやプラグインが必要です [#]_ 。

..  list-table:: WindGateで利用するライブラリ等
    :widths: 50 50
    :header-rows: 1

    * - ライブラリ
      - 概要
    * - ``asakusa-windgate-vocabulary``
      - DSL用のクラス群
    * - ``asakusa-windgate-plugin``
      - DSLコンパイラプラグイン
    * - ``asakusa-windgate-test-moderator``
      - テストドライバープラグイン
    * - ``asakusa-windgate-dmdl``
      - DMDLコンパイラプラグイン

..  hint::
    :doc:`../application/gradle-plugin` の手順に従ってプロジェクトテンプレートから作成したプロジェクトは、これらのライブラリやプラグインがSDKアーティファクトという依存性定義によってデフォルトで利用可能になっています。詳しくは :doc:`../application/sdk-artifact` を参照してください。

..  [#] :javadoc:`com.asakusafw.runtime.directio.DataFormat`
..  [#] :javadoc:`com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport`

CSV形式のDataModelStreamSupportの作成
-------------------------------------

CSV形式 [#]_ に対応した ``DataModelStreamSupport`` の実装クラスを自動的に生成するには、対象のデータモデルに ``@windgate.csv`` を指定します。

..  code-block:: dmdl

    @windgate.csv
    document = {
        "the name of this document"
        name : TEXT;

        "the content of this document"
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、 ``<出力先パッケージ>.csv.<データモデル名>CsvSupport`` というクラスが自動生成されます。
このクラスは ``DataModelStreamSupport`` を実装し、データモデル内のプロパティが順番に並んでいるCSVを取り扱えます。

また、 単純な `ローカルファイルシステムを利用するインポーター記述`_ と `ローカルファイルシステムを利用するエクスポーター記述`_ の骨格も自動生成します。
前者は ``<出力先パッケージ>.csv.Abstract<データモデル名>CsvImporterDescription`` 、後者は ``<出力先パッケージ>.csv.Abstract<データモデル名>CsvExporterDescription`` というクラス名で生成します。必要に応じて継承して利用してください。

..  [#] ここでのCSV形式は、 :rfc:`4180` で提唱されている形式を拡張したものです。
    文字セットをASCIIの範囲外にも拡張したり、CRLF以外にもCRのみやLFのみも改行と見なしたり、ダブルクウォート文字の取り扱いを緩くしたりなどの拡張を加えています。
    `CSV形式の注意点`_ も参照してください。

CSV形式の設定
~~~~~~~~~~~~~

``@windgate.csv`` 属性には、次のような要素を指定できます。

..  list-table:: CSV形式の設定
    :widths: 10 10 20 60
    :header-rows: 1

    * - 要素
      - 型
      - 既定値
      - 内容
    * - ``charset``
      - 文字列
      - ``"UTF-8"``
      - ファイルの文字エンコーディング
    * - ``has_header``
      - 論理値
      - ``FALSE``
      - ``TRUE`` でヘッダの利用を許可。 ``FALSE`` で不許可
    * - ``force_header``
      - 論理値
      - ``FALSE``
      - ``TRUE`` でヘッダの利用を許可し、ヘッダの形式チェックを行わない。 ``FALSE`` で不許可
    * - ``true``
      - 文字列
      - ``"true"``
      - ``BOOLEAN`` 型の ``TRUE`` 値の表現形式
    * - ``false``
      - 文字列
      - ``"false"``
      - ``BOOLEAN`` 型の ``FALSE`` 値の表現形式
    * - ``date``
      - 文字列
      - ``"yyyy-MM-dd"``
      - ``DATE`` 型の表現形式
    * - ``datetime``
      - 文字列
      - ``"yyyy-MM-dd HH:mm:ss"``
      - ``DATETIME`` 型の表現形式

なお、 ``date`` および ``datetime`` には ``SimpleDateFormat`` [#]_ の形式で日付や時刻を指定します。

以下は記述例です。

..  code-block:: dmdl

    @windgate.csv(
        charset = "ISO-2022-JP",
        has_header = TRUE,
        true = "1",
        false = "0",
        date = "yyyy/MM/dd",
        datetime = "yyyy/MM/dd HH:mm:ss",
    )
    model = {
        ...
    };


..  [#] ``java.text.SimpleDateFormat``

ヘッダの設定
~~~~~~~~~~~~

`CSV形式の設定`_ でヘッダを有効にしている場合、出力の一行目にプロパティ名が表示されます。
ここで表示される内容を変更するには、それぞれのプロパティに ``@windgate.csv.field`` 属性を指定し、さらに ``name`` 要素でフィールド名を指定します。

以下は利用例です。

..  code-block:: dmdl

    @windgate.csv
    document = {
        "the name of this document"
        @windgate.csv.field(name = "題名")
        name : TEXT;

        "the content of this document"
        @windgate.csv.field(name = "内容")
        content : TEXT;
    };

ファイル情報の取得
~~~~~~~~~~~~~~~~~~

解析中のCSVファイルに関する属性を取得する場合、それぞれ以下の属性をプロパティに指定します。

..  list-table:: ファイル情報の取得に関する属性
    :widths: 20 10 10
    :header-rows: 1

    * - 属性
      - 型
      - 内容
    * - ``@windgate.csv.file_name``
      - ``TEXT``
      - ファイル名
    * - ``@windgate.csv.line_number``
      - ``INT`` , ``LONG``
      - テキスト行番号 (1起算)
    * - ``@windgate.csv.record_number``
      - ``INT`` , ``LONG``
      - レコード番号 (1起算)

上記の属性が指定されたプロパティは、CSVのフィールドから除外されます。

..  attention::
    これらの属性はCSVの解析時のみ有効です。
    CSVを書き出す際には無視されます。

CSVから除外するプロパティ
~~~~~~~~~~~~~~~~~~~~~~~~~
特定のプロパティをCSVのフィールドとして取り扱いたくない場合、プロパティに ``@windgate.csv.ignore`` を指定します。

CSV形式の注意点
~~~~~~~~~~~~~~~

自動生成でサポートするCSV形式を利用するうえで、いくつかの注意点があります。

* CSVに空の文字列を書き出しても、読み出し時に ``null`` として取り扱われます
* 論理値は復元時に、値が ``true`` で指定した文字列の場合には ``true`` , 空の場合には ``null`` , それ以外の場合には ``false`` となります
* ヘッダが一文字でも異なる場合、解析時にヘッダとして取り扱われません
* 1レコードが10MBを超える場合、正しく解析できません

ローカルファイルシステムを利用するインポーター記述
--------------------------------------------------

WindGateと連携してファイルからデータをインポートする場合、 ``FsImporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
  インポーターが使用するプロファイル名を戻り値に指定します。

``String getPath()``
  インポート対象のファイルパスを ``resource.local.basePath`` からの相対パスで指定します。

  ここには ``${変数名}`` の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。
  利用可能な変数はコンテキストAPIで参照できるものと同様です。

``Class<?> getModelType()``
  インポーターが処理対象とするデータモデルオブジェクトの型を表すクラスを戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``Class<? extends DataModelStreamSupport<?>> getStreamSupport()``
  ``DataModelStreamSupport`` の実装クラスを戻り値に指定します。

    このメソッドは、自動生成される骨格ではすでに宣言されています。

以下は実装例です。

..  code-block:: java

    public class DocumentFromFile extends FsImporterDescription {

        @Override
        public Class<?> getModelType() {
            return Document.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getPath() {
            return "example/input.csv";
        }

        @Override
        public Class<? extends DataModelStreamSupport<?>> getStreamSupport() {
            return DocumentCsvSupport.class;
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.FsImporterDescription`

ローカルファイルシステムを利用するエクスポーター記述
----------------------------------------------------

WindGateと連携してジョブフローの処理結果をローカルのファイルに書き出すには、 ``FsExporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
  エクスポーターが使用するプロファイル名を戻り値に指定します。

``String getPath()``
  エクスポート対象のファイルパスを ``resource.local.basePath`` からの相対パスで指定します。

  ここには ``${変数名}`` の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。
  利用可能な変数はコンテキストAPIで参照できるものと同様です。

``Class<?> getModelType()``
  エクスポーターが処理対象とするデータモデルオブジェクトの型を表すクラスを戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``Class<? extends DataModelStreamSupport<?>> getStreamSupport()``
    ``DataModelStreamSupport`` の実装クラスを戻り値に指定します。

    このメソッドは、自動生成される骨格ではすでに宣言されています。

..  attention::
    ``getPath()`` で指定した出力先に既にファイルが存在する場合、エクスポート時に上書きされます。

以下は実装例です。

..  code-block:: java

    public class WordIntoFile extends FsExporterDescription {

        @Override
        public Class<?> getModelType() {
            return Word.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getPath() {
            return "example/output.csv";
        }

        @Override
        public Class<? extends DataModelStreamSupport<?>> getStreamSupport() {
            return WordCsvSupport.class;
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.FsExporterDescription`

データベースの入出力
====================

Asakusa FrameworkのバッチアプリケーションからWindGateを利用してデータベースの入出力を行うには、
対象のプロファイルに `データベースの設定`_ を追加します。

また、データモデルと ``PreparedStatement`` , ``ResultSet`` をマッピングする ``DataModelJdbcSupport`` [#]_ の実装クラスを作成します。
この実装クラスは、DMDLコンパイラの拡張を利用して自動的に生成できます。

なお、以降の機能を利用するには次のライブラリやプラグインが必要です。

..  list-table:: WindGateで利用するライブラリ等
    :widths: 50 50
    :header-rows: 1

    * - ライブラリ
      - 概要
    * - ``asakusa-windgate-vocabulary``
      - DSL用のクラス群
    * - ``asakusa-windgate-plugin``
      - DSLコンパイラプラグイン
    * - ``asakusa-windgate-test-moderator``
      - テストドライバープラグイン
    * - ``asakusa-windgate-dmdl``
      - DMDLコンパイラプラグイン

..  hint::
    :doc:`../application/gradle-plugin` の手順に従ってプロジェクトテンプレートから作成したプロジェクトは、これらのライブラリやプラグインがSDKアーティファクトという依存性定義によってデフォルトで利用可能になっています。詳しくは :doc:`../application/sdk-artifact` を参照してください。

..  [#] :javadoc:`com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport`

DataModelJdbcSupportの自動生成
------------------------------

データモデルから ``DataModelJdbcSupport`` の実装クラスを自動的に生成するには、それぞれのプロパティに ``@windgate.jdbc.column`` を指定してさらに ``name`` 要素で対応するカラム名を記述します。
また、テーブル名を指定するにはデータモデルに ``@windgate.jdbc.table`` を指定して ``name`` 要素内に記述します [#]_ 。

..  code-block:: dmdl

    @windgate.jdbc.table(name = "DOCUMENT")
    document = {
        "the name of this document"
        @windgate.jdbc.column(name = "NAME")
        name : TEXT;

        "the content of this document"
        @windgate.jdbc.column(name = "CONTENT")
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、
``<出力先パッケージ>.jdbc.<データモデル名>JdbcSupport`` というクラスが自動生成されます。
このクラスは ``DataModelJdbcSupport`` を実装し、 ``@windgate.jdbc.column`` で指定したカラムが利用可能です。

また、 ``@windgate.jdbc.table`` を指定した場合、単純な `データベースを利用するインポーター記述`_ と `データベースを利用するエクスポーター記述`_ の骨格も自動生成します。
前者は ``<出力先パッケージ>.jdbc.Abstract<データモデル名>JdbcImporterDescription`` 、後者は ``<出力先パッケージ>.jdbc.Abstract<データモデル名>JdbcExporterDescription`` というクラス名で生成します。

この自動生成されたインポーター/エクスポーター記述の骨格は指定されたテーブルのすべてのカラムを利用します。
必要に応じて継承して利用してください。

..  [#] ``@windgate.jdbc.table`` の指定は必須ではありません。

DMDLとJDBCの型の対応
~~~~~~~~~~~~~~~~~~~~

DMDLとJDBCの型の対応は以下の通りです。

.. list-table:: DMDLとJavaとJDBCのデータ型

   * - 説明
     - DMDL
     - Javaクラス
     - JDBC
   * - 32bit符号付き整数
     - INT
     - int (IntOption)
     - int
   * - 64bit符号付き整数
     - LONG
     - long (LongOption)
     - long
   * - 単精度浮動小数点
     - FLOAT
     - float (FloatOption)
     - float
   * - 倍精度浮動小数点
     - DOUBLE
     - double (DoubleOption)
     - double
   * - 文字列
     - TEXT
     - Text (StringOption)
     - String
   * - 10進数
     - DECIMAL
     - BigDecimal (DecimalOption)
     - BigDecimal
   * - 日付
     - DATE
     - Date (DateOption)
     - java.sql.Date
   * - 日時
     - DATETIME
     - DateTime (DateTime)
     - java.sql.Timestamp
   * - 論理値
     - BOOLEAN
     - boolean (BooleanOption)
     - boolean
   * - 8bit符号付き整数
     - BYTE
     - byte (ByteOption)
     - byte
   * - 16bit符号付き整数
     - SHORT
     - short (ShortOption)
     - short

データベースを利用するインポーター記述
--------------------------------------

WindGateと連携してデータベースのテーブルからデータをインポートする場合、 ``JdbcImporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
  インポーターが使用するプロファイル名を戻り値に指定します。

``Class<?> getModelType()``
  インポーターが処理対象とするデータモデルオブジェクトの型を表すクラスを戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``String getTableName()``
  インポート対象のテーブル名を戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``List<String> getColumnNames()``
  インポート対象のカラム名を戻り値に指定します。
  ここで指定したカラム名のみインポートを行います。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``Class<? extends DataModelJdbcSupport<?>> getJdbcSupport()``
  ``DataModelJdbcSupport`` の実装クラスを戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``String getCondition()``
  インポーターが利用する抽出条件をSQLの条件式で指定します（省略可能）。

  指定する文字列はSQL文の ``WHERE`` 句以降の文字列（ ``WHERE`` の部分は不要）である必要があります。
  省略時にはテーブル全体を入力の対象にとります。

  ここには ``${変数名}`` の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。
  利用可能な変数はコンテキストAPIで参照できるものと同様です。
  変数がそのまま文字列として展開されるため、文字列リテラルを利用する場合などには注意が必要です。

以下は実装例です。

..  code-block:: java

    public class DocumentFromDb extends JdbcImporterDescription {

        @Override
        public Class<?> getModelType() {
            return Document.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getTableName() {
            return "DOCUMENT";
        }

        @Override
        public List<String> getColumnNames() {
            return Arrays.asList("NAME", "CONTENT");
        }

        @Override
        public Class<? extends DataModelJdbcSupport<?>> getJdbcSupport() {
            return DocumentJdbcSupport.class;
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.JdbcImporterDescription`

データベースを利用するエクスポーター記述
----------------------------------------

WindGateと連携してジョブフローの処理結果をデータベースのテーブルに書き出すには、 ``JdbcExporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
  エクスポーターが使用するプロファイル名を戻り値に指定します。

``Class<?> getModelType()``
  エクスポーターが処理対象とするデータモデルオブジェクトの型を表すクラスを戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``String getTableName()``
  エクスポート対象のテーブル名を戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``List<String> getColumnNames()``
  エクスポート対象のカラム名を戻り値に指定します。
  ここで指定したカラム名のみエクスポートを行います。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

``String getCustomTruncate()``
  テーブルの内容を削除する際のクエリー文を指定します。
  省略時には `データベースの設定`_ に従ったクエリーが実行されます。

``Class<? extends DataModelJdbcSupport<?>> getJdbcSupport()``
  ``DataModelJdbcSupport`` の実装クラスを戻り値に指定します。

  このメソッドは、自動生成される骨格ではすでに宣言されています。

以下は実装例です。

..  code-block:: java

    public class WordIntoDb extends JdbcExporterDescription {

        @Override
        public Class<?> getModelType() {
            return Word.class;
        }

        @Override
        public String getProfileName() {
            return "example";
        }

        @Override
        public String getTableName() {
            return "WORD";
        }

        @Override
        public List<String> getColumnNames() {
            return Arrays.asList("STRING", "FREQUENCY");
        }

        @Override
        public Class<? extends DataModelJdbcSupport<?>> getJdbcSupport() {
            return WordJdbcSupport.class;
        }
    }

..  [#] :javadoc:`com.asakusafw.vocabulary.windgate.JdbcExporterDescription`

WindGateと連携したテスト
========================

WindGateを利用したジョブフローやバッチのテストは、Asakusa Frameworkの通常のテスト方法で行えます。
通常のテストについては :doc:`../testing/index` を参照してください。

..  attention::
    テストドライバーは、テストのたびにWindGateのプラグイン用のClassLoaderを作成し、プラグインライブラリをクラスパスに通します。

    クラスロードに関する問題が発生した場合には、テストを実行する際のクラスパスにそれらのライブラリを含めてください。

