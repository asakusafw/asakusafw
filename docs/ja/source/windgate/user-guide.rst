======================
WindGateユーザーガイド
======================

この文書では、WindGateの利用方法について紹介します。
WindGateの導入方法については :doc:`../application/administrator-guide` のWindGateに関する項目を参照してください。

WindGateプロファイル
====================
WindGateは「Hadoopクラスタとデータベース」または「Hadoopクラスタとローカルファイルシステム」などの二点間で様々なデータをやり取りするツールです。主に、Asakusa Frameworkでバッチアプリケーションを作成する際に、データフローの入力データや出力データを取り扱うために利用します。

WindGateでは、「Hadoopクラスタ」や「データベース」、「ローカルファイルシステム」などのそれぞれを「リソース」という考え方で同じように取り扱います。WindGateの「プロファイル」にそれぞれのリソースとアクセス方法を記述することで、リソース間で自由にデータをやり取りできるようにします。

また、このプロファイルはWindGateに複数登録することもでき、複数のデータベースやローカルファイルシステム上のパス、またはアクセスするHadoopクラスタへの通信方法など、複数の組み合わせを個別に管理できます。

WindGateのプロファイルは、 ``$ASAKUSA_HOME/windgate/profile/<プロファイル名>.properties`` (以降、構成ファイル)内に記述します。 ``<プロファイル名>`` の部分には、特定のプロファイルごとに名前を付けてその名前を指定します。

それぞれの構成ファイルには、Javaの一般的なプロパティファイルの文法で、主に下記のセクションを記述します。

..  list-table:: プロパティファイルの項目
    :widths: 10 60
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
      - `Hadoopクラスタの設定`_
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
WindGateは、一度の処理で複数のリソースペア間でのデータ転送を行います。
それぞれのデータ転送を同時に実行するには、次の設定を追加します。

..  list-table:: 同時に実行するデータ転送の設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``core.maxProcesses``
      - 同時に実行可能なデータ転送数の最大

..  note::
    この設定は、主にデータベースを利用するリソースなどでコネクション数に制限がある場合などを想定しています。
    特に制限がない場合には、WindGateを実行するコンピューターのコア数などと同じ数値にするのが良いでしょう。

..  attention::
    この設定はプロファイルごとに個別のものです。
    ここで同時実行数を1に設定しても、複数のプロファイルに対する処理を同時に行った場合、データ転送は二つ以上が同時に実行する場合があります。


セッションの設定
----------------
WindGateと連携したバッチアプリケーションを作成する場合、通常はそれぞれのジョブフローの入力と出力時にそれぞれWindGateの処理が実行されます。
たとえば、ジョブフローの処理の開始時にWindGateを利用して、データベースの内容をHadoopクラスター上に展開し（インポート）、
処理の終了時に計算結果をデータベースに書き戻します（エクスポート）。

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

また、WindGateは一度の処理内で、複数のリソースペア間のデータ転送を行います。
入力と出力のリソースペアごとにプロセスが作成され、同時に実行するプロセスの個数は `同時に実行するデータ転送の設定`_ で指定できます。

プロセスの設定は、構成ファイル内の ``process.basic`` セクション内に記述します。

..  note::
    ここでのセクション名が ``process.basic`` となっているのは、このプロセスが「通常の方法でデータ転送を行う」という役割を持っているためです。
    将来、キャッシュの機能などがサポートされる際には、 ``process`` セクションも増える予定です。
    現在のところ、 `通常のデータ転送プロセス`_ のみが提供されています。

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


Hadoopクラスタの設定
--------------------
Asakusa Frameworkで作成したバッチからWindGateを利用する場合、リソースの片方にはHadoopクラスタを利用します。

Hadoopクラスタとの通信方法は、構成ファイル内の ``resource.hadoop`` セクション内に記述します。


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
    * - ``resource.hadoop.compression``
      - 転送時に利用する圧縮コーデッククラス名 (省略可)

``resource.hadoop.compression`` には、 ``org.apache.hadoop.io.compress.CompressionCodec`` のサブタイプのクラス名を指定します [#]_ 。
この項目を省略した場合、非圧縮のシーケンスファイルを配置します。

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-hadoopfs`` の追加が必要です。
詳しくは `プラグインライブラリの管理`_ や :doc:`../application/administrator-guide` を参照してください。

..  attention::
    このリソースを利用するには、WindGateの起動時にHadoopの設定がすべて利用可能である必要があります。
    WindGate起動時のHadoopの設定と、バッチで利用するHadoopの設定が異なる場合、正しく動作しない可能性があります。
    
    なお、WindGateの本体は、環境変数に ``HADOOP_HOME`` が設定されている場合に ``$HADOOP_HOME/bin/hadoop`` コマンドを経由して起動します。
    環境変数の設定方法は `WindGateの環境変数設定`_ を参照してください。

..  [#] ``org.apache.hadoop.io.compress.DefaultCodec`` などが標準で用意されています

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
    * - ``resource.hadoop.target``
      - リモートコンピューター上の `Hadoopブリッジ`_ のインストール先
    * - ``resource.hadoop.ssh.user``
      - ログイン先のユーザー名
    * - ``resource.hadoop.ssh.host``
      - SSHのリモートホスト名
    * - ``resource.hadoop.ssh.port``
      - SSHのリモートポート番号
    * - ``resource.hadoop.ssh.privateKey``
      - ローカルの秘密鍵の位置
    * - ``resource.hadoop.ssh.passPhrase``
      - 秘密鍵のパスフレーズ
    * - ``resource.hadoop.compression``
      - 転送時に利用する圧縮コーデッククラス名 (省略可)

上記のうち、 ``resource.hadoop.ssh.privateKey`` には ``${変数名}`` という形式で環境変数を含められます。
この項目には通常、 ``${HOME}/.ssh/id_rsa`` を指定します。

`同一環境上のHadoopを利用する`_ 際と同様に、 ``resource.hadoop.compression`` には、 ``org.apache.hadoop.io.compress.CompressionCodec`` のサブタイプのクラス名を指定します。
この項目を省略した場合、非圧縮のシーケンスファイルを配置します。

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-hadoopfs`` と JSch [#]_ の追加が必要です。
詳しくは `プラグインライブラリの管理`_ や :doc:`../application/administrator-guide` を参照してください。

..  [#] http://www.jcraft.com/jsch/ (Version 0.1.44-1以上)

Hadoopブリッジ
^^^^^^^^^^^^^^
WindGateからSSHを経由してHadoopにアクセスする際に、Hadoopブリッジとよぶツールを経由します。
このツールは通常 ``$ASAKUSA_HOME/windgate-ssh`` というディレクトリにインストールされているため、これをリモートコンピューター上にコピーして利用します。
また、プロファイルの ``resource.hadoop.target`` にはインストール先のディレクトリ名をフルパスで指定してください。

このツールの内部では、リモートコンピューター上の ``$HADOOP_HOME/bin/hadoop`` コマンドを利用してHadoopクラスタの操作を行います。
環境変数 ``HADOOP_HOME`` は ``windgate-ssh/conf/env.sh`` 内で設定してください。

また、ログの設定は ``windgate-ssh/conf/logback.xml`` で行えます。
WindGate本体と同様に、SLF4JとLogbackを利用しています [#]_ 。

..  warning::
    HadoopブリッジはSSH経由で実行され、標準入出力を利用してWindGateとデータのやり取りを行います。
    ログを出力する際には、標準エラー出力やファイルなどに出力し、標準出力は利用しないようにしてください。
    また、 ``windgate-ssh/conf/env.sh`` に指定した ``HADOOP_USER_CLASSPATH_FIRST`` の設定は、ログの設定を有効にするためにも必要です。

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
      - ベースパス (省略可)

``resource.local.basePath`` が指定された場合、WindGateはそのパス以下のみを利用するようになります。
この指定がない場合、WindGateはアプリケーションで指定された任意のファイルを操作します。

また、 ``resource.local.basePath`` には ``${環境変数名}`` の形式で環境変数を指定できます。

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-stream`` の追加が必要です。
詳しくは `プラグインライブラリの管理`_ や :doc:`../application/administrator-guide` を参照してください。

..  warning::
    開発環境では、ベースパスに壊れてもよいディレクトリを必ず指定してください。
    ここで指定したパスはテスト実行時などにテストドライバが削除したり変更したりします。
    また、指定がない場合にはアプリケーションの内容によっては、任意のファイルが削除される可能性があります。

..  [#] WindGateを起動したコンピュータから、OSのファイルシステムを利用するというだけですので、
    ネットワークファイルシステム等でもファイルシステム上にマウントしてあれば利用可能です。
    なお、「ローカル」と書いているのは、Hadoopのファイルシステムと区別するためです。


データベースの設定
------------------
WindGateのリソースとして、JDBCをサポートするデータベースを指定できます。

現在の構成では、WindGateから直接JDBCドライバを利用して対象のデータベースにアクセスします。
また、データの取得にはテーブルを ``SELECT`` 文で取得し、データの書き戻しにはテーブルを ``TRUNCATE`` した後にバッチモードで ``INSERT`` 文を発行します。

..  warning::
    この構成では、データの書き出し前に対象のテーブルの内容を完全に削除します。
    そのため、書き出し先のテーブルには通常利用するテーブルとは別のテーブルを指定し、WindGateの外側でマージ処理等を行ってください。

..  attention::
    この構成では、データの取得時にアプリケーション側でのページネーション等を行いません。
    そのため、MySQLなどのカーソル機能が十分でないデータベースでは、巨大なデータを取得する際に十分なパフォーマンスが得られません。
    特に、MySQLの場合にはドライバの指定時に ``?useCursorFetch=true&defaultFetchSize=1024`` などカーソルを利用する設定が必要になります。

構成ファイル内の ``resource.jdbc`` セクション内に以下の設定を記述します。

..  list-table:: データベースを利用する設定
    :widths: 10 40
    :header-rows: 1

    * - 名前
      - 値
    * - ``resource.jdbc``
      - ``com.asakusafw.windgate.jdbc.JdbcResourceProvider``
    * - ``resource.jdbc.driver``
      - JDBCドライバのクラス名
    * - ``resource.jdbc.url``
      - 接続先データベースのJDBC URL
    * - ``resource.jdbc.user``
      - データベースのユーザ名
    * - ``resource.jdbc.password``
      - データベースのパスワード
    * - ``resource.jdbc.batchPutUnit``
      - 一度のバッチで挿入するデータの件数 (書き込み時) [#]_

なお、このリソースを利用するには、プラグインライブラリに ``asakusa-windgate-jdbc`` とJDBCドライバライブラリの追加が必要です。
詳しくは `プラグインライブラリの管理`_ や :doc:`../application/administrator-guide` を参照してください。

..  [#] 大きすぎる値を指定するとメモリ不足で正しく動作しません。
    1000から10000程度での動作を確認しています。


その他のWindGateの設定
----------------------
構成ファイルのほかに、WindGate全体の設定に関するものがいくつか用意されています。

WindGateの環境変数設定
~~~~~~~~~~~~~~~~~~~~~~
WindGateの実行に特別な環境変数を利用する場合、 ``$ASAKUSA_HOME/windgate/conf/env.sh`` 内でエクスポートして定義できます。

WindGateをAsakusa Frameworkのバッチから利用する場合、通常は以下の環境変数が必要です。

..  list-table:: WindGateが利用する環境変数
    :widths: 10 60
    :header-rows: 1

    * - 名前
      - 備考
    * - ``ASAKUSA_HOME``
      - Asakusaのインストール先パス。
    * - ``HADOOP_HOME``
      - Hadoopのインストール先パス。未指定の場合はHadoopに関するクラスパスを通さない。
    * - ``HADOOP_USER_CLASSPATH_FIRST``
      - `WindGateのログ設定`_ 時にHadoopのログ機構を利用しないための設定。 ``true`` を指定する。

WindGateのログ設定
~~~~~~~~~~~~~~~~~~
WindGateは内部のログ表示に ``SLF4J`` [#]_ 、およびバックエンドに ``Logback`` [#]_ を利用しています。
ログの設定を変更するには、 ``$ASAKUSA_HOME/windgate/conf/logback.xml`` を編集してください。

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

Logback以外のログの仕組みを利用する場合、 ``$ASAKUSA_HOME/windgate/lib`` にあるLogback関連のライブラリを置換した上で、
設定ファイルを ``$ASAKUSA_HOME/windgate/conf`` 以下に配置します (ここは実行時にクラスパスとして設定されます)。

..  [#] http://www.slf4j.org/
..  [#] http://logback.qos.ch/

プラグインライブラリの管理
~~~~~~~~~~~~~~~~~~~~~~~~~~
WindGateの様々な機能は、プラグイン機構を利用して実現しています [#]_ 。
それぞれのプラグイン、およびプラグインが利用する依存ライブラリは、 ``$ASAKUSA_HOME/windgate/plugin`` ディレクトリ直下に配置してください。

..  [#] たとえば、WindGateはHadoopクラスタにアクセスする際にもプラグインが必要です。
    標準的なものは導入時に自動的にプラグインが追加されますが、必要に応じてプラグインやライブラリを配置してください。


ローカルファイルシステムの入出力
================================
Asakusa FrameworkのバッチアプリケーションからWindGateを利用してローカルファイルシステムの入出力を行うには、対象のプロファイルに `ローカルファイルシステムの設定`_ を追加します。

また、データモデルとバイトストリームをマッピングする ``DataModelStreamSupport`` [#]_ の実装クラスを作成します。
この実装クラスは、DMDLコンパイラの拡張を利用して自動的に生成できます。

..  [#] ``com.asakusafw.windgate.core.vocabulary.DataModelStreamSupport``


CSV形式のDataModelStreamSupportの作成
-------------------------------------
CSV形式 [#]_ に対応した ``DataModelStreamSupport`` の実装クラスを自動的に生成するには、対象のデータモデルに ``@windgate.csv`` を指定します。

..  code-block:: none

    @windgate.csv
    document = {
        "the name of this document"
        name : TEXT;

        "the content of this document"
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、 ``<出力先パッケージ>.csv.<データモデル名>CsvSupport`` というクラスが自動生成されます。
このクラスは ``DataModelStreamSupport`` を実装し、データモデル内のプロパティが順番に並んでいるCSVを取り扱えます。

また、 単純な `ローカルファイルシステムを利用するインポーター記述`_ と `ローカルファイルシステムを利用するエクスポーター記述`_ の骨格も自動生成します。前者は ``<出力先パッケージ>.csv.Abstract<データモデル名>CsvImporterDescription`` 、後者は ``<出力先パッケージ>.csv.Abstract<データモデル名>CsvExporterDescription`` というクラス名で生成します。必要に応じて継承して利用してください。

..  [#] ここでのCSV形式は、RFC 4180 (http://www.ietf.org/rfc/rfc4180.txt) で提唱されている形式を拡張したものです。
    文字セットをASCIIの範囲外にも拡張したり、CR, LFのみを改行と見なしたり、ダブルクウォート文字の取り扱いを緩くしたりなどの拡張を加えています。
    `CSV形式の注意点`_ も参照してください。

CSV形式の設定
~~~~~~~~~~~~~
``@windgate.csv`` 属性には、次のような要素を指定できます。

..  list-table:: WindGate実行時のシステムプロパティ
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

..  code-block:: none

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

..  code-block:: none

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
    ``resource.local.basePath`` を指定しない場合には、絶対パスを指定します。

    ここには ``${変数名}`` の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。
    利用可能な変数はコンテキストAPIで参照できるものと同様です。

``Class<?> getModelType()``
    インポーターが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

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

..  [#] ``com.asakusafw.vocabulary.windgate.FsImporterDescription``

ローカルファイルシステムを利用するエクスポーター記述
----------------------------------------------------
WindGateと連携してジョブフローの処理結果をローカルのファイルに書き出すには、 ``FsExporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    エクスポーターが使用するプロファイル名を戻り値に指定します。

``String getPath()``
    エクスポート対象のファイルパスを ``resource.local.basePath`` からの相対パスで指定します。
    ``resource.local.basePath`` を指定しない場合には、絶対パスを指定します。

    ここには ``${変数名}`` の形式で、バッチ起動時の引数やあらかじめ宣言された変数を利用できます。
    利用可能な変数はコンテキストAPIで参照できるものと同様です。

``Class<?> getModelType()``
    エクスポーターが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

    このメソッドは、自動生成される骨格ではすでに宣言されています。

``Class<? extends DataModelStreamSupport<?>> getStreamSupport()``
    ``DataModelStreamSupport`` の実装クラスを戻り値に指定します。

    このメソッドは、自動生成される骨格ではすでに宣言されています。

..  warning::
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

..  [#] ``com.asakusafw.vocabulary.windgate.FsExporterDescription``


データベースの入出力
====================
Asakusa FrameworkのバッチアプリケーションからWindGateを利用してデータベースの入出力を行うには、対象のプロファイルに `データベースの設定`_ を追加します。

また、データモデルと ``PreparedStatement`` , ``ResultSet`` をマッピングする ``DataModelJdbcSupport`` [#]_ の実装クラスを作成します。
この実装クラスは、DMDLコンパイラの拡張を利用して自動的に生成できます。

..  [#] ``com.asakusafw.windgate.core.vocabulary.DataModelJdbcSupport``

DataModelJdbcSupportの自動生成
------------------------------

データモデルから ``DataModelJdbcSupport`` の実装クラスを自動的に生成するには、それぞれのプロパティに ``@windgate.jdbc.column`` を指定してさらに ``name`` 要素で対応するカラム名を記述します。
また、テーブル名を指定するにはデータモデルに ``@windgate.jdbc.table`` を指定して ``name`` 要素内に記述します [#]_ 。

..  code-block:: none

    @windgate.jdbc.table(name = "DOCUMENT")
    document = {
        "the name of this document"
        @windgate.jdbc.column(name = "NAME")
        name : TEXT;

        "the content of this document"
        @windgate.jdbc.column(name = "CONTENT")
        content : TEXT;
    };

上記のように記述してデータモデルクラスを生成すると、 ``<出力先パッケージ>.jdbc.<データモデル名>JdbcSupport`` というクラスが自動生成されます。
このクラスは ``DataModelJdbcSupport`` を実装し、 ``@windgate.jdbc.column`` で指定したカラムが利用可能です。

また、 ``@windgate.jdbc.table`` を指定した場合、単純な `データベースを利用するインポーター記述`_ と `データベースを利用するエクスポーター記述`_ の骨格も自動生成します。前者は ``<出力先パッケージ>.jdbc.Abstract<データモデル名>JdbcImporterDescription`` 、後者は ``<出力先パッケージ>.jdbc.Abstract<データモデル名>JdbcExporterDescription`` というクラス名で生成します。この自動生成されたインポーター/エクスポーター記述の骨格は指定されたテーブルのすべてのカラムを利用します。必要に応じて継承して利用してください。

この機能を利用するには、DMDLコンパイラのプラグインに ``asakusa-windgate-dmdl`` を追加する必要があります。
DMDLコンパイラについては :doc:`../dmdl/user-guide` を参照してください。

..  note::
    Asakusa Framework 0.2.3 までの ``@windgate.column`` 属性も利用可能です。
    0.2.4 以降では ``@windgate.jdbc.column`` の利用を推奨します。

..  [#] ``@windgate.jdbc.table`` の指定は必須ではありません。

データベースを利用するインポーター記述
--------------------------------------
WindGateと連携してデータベースのテーブルからデータをインポートする場合、 ``JdbcImporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    インポーターが使用するプロファイル名を戻り値に指定します。

``Class<?> getModelType()``
    インポーターが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

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

    指定する文字列はMySQL形式の ``WHERE`` 以降の文字列（ ``WHERE`` の部分は不要）である必要があります。
    省略時にはテーブル全体を入力の対象にとります。

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

..  [#] ``com.asakusafw.vocabulary.windgate.JdbcImporterDescription``

データベースを利用するエクスポーター記述
----------------------------------------
WindGateと連携してジョブフローの処理結果をデータベースのテーブルに書き出すには、 ``JdbcExporterDescription`` [#]_ クラスのサブクラスを作成して必要な情報を記述します。

このクラスでは、下記のメソッドをオーバーライドします。

``String getProfileName()``
    エクスポーターが使用するプロファイル名を戻り値に指定します。

``Class<?> getModelType()``
    エクスポーターが処理対象とするモデルオブジェクトの型を表すクラスを戻り値に指定します。

    このメソッドは、自動生成される骨格ではすでに宣言されています。

``String getTableName()``
    エクスポート対象のテーブル名を戻り値に指定します。

    このメソッドは、自動生成される骨格ではすでに宣言されています。

``List<String> getColumnNames()``
    エクスポート対象のカラム名を戻り値に指定します。
    ここで指定したカラム名のみエクスポートを行います。

    このメソッドは、自動生成される骨格ではすでに宣言されています。

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

..  [#] ``com.asakusafw.vocabulary.windgate.JdbcExporterDescription``

WindGateと連携したテスト
========================
WindGateを利用したジョブフローやバッチのテストは、Asakusa Frameworkの通常のテスト方法で行えます。
通常のテストについては :doc:`../testing/index` を参照してください。

..  attention::
    テストドライバは、テストのたびにWindGateのプラグイン用のClassLoaderを作成し、プラグインライブラリをクラスパスに通します。
    クラスロードに関する問題が発生した場合には、テストを実行する際のクラスパスにそれらのライブラリを含めてください。

