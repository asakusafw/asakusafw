==========================
ThunderGate スタートガイド
==========================

この文書では、ThunderGateの簡単な利用方法について紹介します。
なお、 :doc:`../introduction/start-guide` の内容を実施し、その内容を理解している前提で説明を進めているため、そちらも参照してください。

ThunderGateの詳しい利用方法については :doc:`user-guide` を参照してください。

.. _development-environment-with-thundergate:

ThunderGateを使ったアプリケーションの開発環境構築
=================================================
ThunderGateを使ったアプリケーションの開発環境を構築する場合、 :doc:`../introduction/start-guide` - :ref:`startguide-development-environment` で構築した開発環境の設定に加えて、以下に示す設定を行う必要があります。

OSのセキュリティ設定
--------------------
開発環境でThunderGateを実行させる場合、OS毎に以下の設定を行ってください。

CentOS
~~~~~~
SELinuxは無効にしてください。

Ubuntu
~~~~~~
ThunderGateはMySQLのクエリーを使ってローカルファイルへの入出力を行いますが、Ubuntuなどの一部のディストリビューションではデフォルト設定でMySQLのローカルファイルへの入出力がAppArmorサービスによって制限されています。このため、Ubuntuなどの一部のディストリビューションではAppArmorの設定を変更してMySQLのローカルファイルへの入出力を行えるようにする必要があります。

Ubuntuでは、以下のようにAppArmorの設定を変更します。

..  code-block:: sh

    sudo mv /etc/apparmor.d/usr.sbin.mysqld /etc/apparmor.d/disable/
    sudo /etc/init.d/apparmor restart

sshの設定
---------
開発環境上のOSユーザーに対して、localhostに対するssh接続をパスフレーズ無しで実行出来るよう設定します。以下設定例です。

..  code-block:: sh

    ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa 
    cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
    chmod 600 ~/.ssh/authorized_keys

MySQLのインストールとユーザー、データベース作成
-----------------------------------------------
開発環境にMySQLをインストールします。

MySQLのインストールが完了したら、Asakusa Frameworkのデータモデルクラス生成、およびテストドライバーによるテスト時に使われるMySQLユーザーとデータベースを作成します

後述するAsakusa Frameworkのインストールを行う際に、デフォルトの設定ではThunderGateが使用するデータベース名とユーザー名、パスワードはそれぞれ ``asakusa`` に設定されるため、以下ではこのデフォルト値に沿ってデータベースを構築します。

..  code-block:: sh

    mysql -u root
    > GRANT ALL PRIVILEGES ON *.* TO 'asakusa'@'localhost' IDENTIFIED BY 'asakusa' WITH GRANT OPTION;
    > GRANT ALL PRIVILEGES ON *.* TO 'asakusa'@'%'IDENTIFIED BY 'asakusa' WITH GRANT OPTION;
    > CREATE DATABASE asakusa DEFAULT CHARACTER SET utf8;
    exit

..  warning::
    このデータベースはアプリケーションのビルド実行毎に再作成(DROP DATABASE/CREATE DATABASE）が行われるので、開発以外の目的では使用しないでください。


アプリケーションの開発準備
==========================
ThunderGateを利用したバッチアプリケーションを新しく作成する場合、 :doc:`../application/gradle-plugin` で説明するGradleプロジェクトのテンプレートを利用すると簡単です。

以降、このドキュメントではこのテンプレートから作成したプロジェクトを利用して説明を進めます。

Gradleプロジェクトの設定
------------------------
GradleプロジェクトでThunderGateを使用する場合は ``build.gradle`` に対して以下の定義を追加します。

* 開発環境用の `JDBC接続設定`_ ファイルを配置する
   * ``asakusafw`` ブロックに ``thundergate.jdbcFile <JDBC接続設定ファイルのパス>`` を追加
* Framework Organizerに対してThunderGateの構成を有効化する
   * ``asakusafwOrganizer`` ブロックに ``thundergate.enabled true`` を追加
* ThunderGate用SDKを依存関係に追加する
   * ``dependencies`` ブロックの ``compile`` に対して ``asakusa-sdk-thundergate`` を 追加
   * ``dependencies`` ブロックの ``compile`` に対して ``mysql-connector-java`` を 追加

以下、 ``build.gradle`` の設定例です。

..  code-block:: groovy
    :emphasize-lines: 4, 8, 14-15
    
    asakusafw {
        ...
    
        thundergate.jdbcFile 'src/dist/common/bulkloader/conf/asakusa-jdbc.properties'
    }
    
    asakusafwOrganizer {
        thundergate.enabled true
        ...
    }
    
    dependencies {
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-core', version: asakusafw.asakusafwVersion
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-thundergate', version: asakusafw.asakusafwVersion
        compile group: 'mysql', name: 'mysql-connector-java', version: '5.1.25'
        ...

上記の設定後、 ``installAsakusafw`` タスクを実行して開発環境のAsakusa Frameworkを再インストールします。

Eclipseを利用している場合は、 ``eclipse`` タスクを実行してEclipseのプロジェクト情報を再構成します。

JDBC接続設定
------------
ThunderGateはRDBMSに対してJDBC接続を経由してデータの入出力を行います。ThunderGateはジョブフローの入出力データをRDBMSから読み書きする対象を「ターゲット」として抽象化しています。DSLではJDBCの接続情報そのものは指定せずに、入出力を行うターゲット名のみを指定します。

ターゲットに対するJDBC接続設定を定義したプロパティファイルは、 ``$ASAKUSA_HOME/bulkloader/conf`` 配下に ``<ターゲット名>-jdbc.properties`` という名前で配置します。標準ではターゲット名 ``asakusa`` 用のJDBC接続定義ファイルとして ``asakusa-jdbc.properties`` が配置されています。

また、開発環境では :doc:`with-dmdl` で説明する、DDLからDMDLスクリプトを生成する機能が利用できます。
このときDDLの登録先であるMySQLのデータベース接続設定情報をもつJDBC接続設定ファイルを
``build.gradle`` の ``thundergate.jdbcFile`` に設定します。

..  hint::
    このファイルは ``$ASAKUSA_HOME/bulkloader/conf/asakusa-jdbc.properties`` をコピーして作成することもできます。

JDBC接続設定について詳しくは、 :doc:`user-guide` - :ref:`thundergate-jdbc-configuration-file` を参照してください。

データベースノード用設定
------------------------
ThunderGateはRDBMSがインストールされているサーバー（データベースノード）とHadoopクラスター上のある特定のサーバー（Hadoopクライアントマシン）間でデータを転送します。ThunderGateはデータベースノード、HadoopクライアントマシンそれぞれにThunderGateのモジュールをデプロイし、設定を行う必要があります。

データベースノードに対する設定を定義したプロパティファイルは、 ``$ASAKUSA_HOME/bulkloader/conf`` 配下に ``bulkloader-conf-db.properties`` という名前で配置します。開発環境については通常はデフォルトのままで動作するよう設定されていますが、必要に応じて設定を変更してください。

データベースノード用設定について詳しくは、 :doc:`user-guide` - :ref:`thundergate-db-configuration-file` を参照してください。

Hadoopクライアントマシン用設定
------------------------------
このガイドでは、開発環境はデータベースノードとHadoopクライアントマシンは同一のマシンであることを前提とします。このため、開発環境でデータベースノード用の設定に加えてHadoopクライアントマシン用の設定を行います。

Hadoopクライアントマシンに対する設定を定義したプロパティファイルは、 ``$ASAKUSA_HOME/bulkloader/conf`` 配下に ``bulkloader-conf-hc.properties`` という名前で配置します。データベースノード用の設定と同様、この設定ファイルも開発環境については通常はデフォルトのままで動作するよう設定されていますが、必要に応じて設定を変更してください。

Hadoopクライアント用設定について詳しくは、 :doc:`user-guide` - :ref:`thundergate-hc-configuration-file` を参照してください。

サンプルプログラムの実行
========================
`アプリケーションの開発準備`_ で作成したプロジェクトには、サンプルのアプリケーションが用意されています。
このサンプルは :doc:`../introduction/start-guide` のサンプルアプリケーション（カテゴリー別売上金額集計バッチ）の内容をThunderGate向けに書きなおしたもので、以下のデータベーステーブルに対してデータの入出力を行います。

..  list-table:: サンプルアプリケーションが利用するテーブル
    :widths: 3 3 4
    :header-rows: 1

    * - テーブル名
      - 入力/出力
      - 概要 
    * - ``SALES_DETAIL``
      - 入力
      - 売上トランザクション
    * - ``STORE_INFO``
      - 入力
      - 店舗マスタ
    * - ``ITEM_INFO``
      - 入力
      - 商品マスタ
    * - ``CATEGORY_SUMMARY``
      - 出力
      - カテゴリ別売上集計
    * - ``ERROR_RECORD``
      - 出力
      - エラー情報

サンプルアプリケーションのビルド
--------------------------------
サンプルアプリケーションのビルドを行います。処理内容や手順は :doc:`../introduction/start-guide` と同様です。ここではビルドコマンドのみを示します。

..  warning::
    ビルド時に実行されるモデル生成処理(Gradleの ``compileDMDL`` タスクにて実行)時に、
    `MySQLのインストールとユーザー、データベース作成`_ で作成したデータベースが再作成(DROP/CREATE)されます。
    このデータベースには重要なデータを配置しないでください。

コマンドラインコンソールでアプリケーションプロジェクトのディレクトリに移動し、以下のコマンドを実行してください。

..  code-block:: none

    ./gradlew build


サンプルデータの配置
--------------------
サンプルアプリケーションプロジェクトには、 :doc:`../introduction/start-guide` で説明したWindGateのサンプルアプリケーションと同様に ``src/test/example-dataset`` 以下にサンプルの入力ファイルが配置されています。ThunderGateではデータの入出力はMySQLのテーブルとなるため、このCSVをMySQLの各テーブルにインポートします。

サンプルアプリケーションプロジェクトには、このCSVファイルのデータをMySQLの各テーブルにインポートするためのSQLファイルのサンプルが ``src/test/sql/import-example-dataset.sql`` に置かれています。このファイルを使ってサンプルデータをセットする例を以下に示します。

コマンドラインコンソールでアプリケーションプロジェクトのディレクトリに移動し、以下のコマンドを実行してください。

..  code-block:: none

    cp -r src/test/example-dataset /tmp
    mysql -u asakusa -pasakusa -D asakusa < src/test/sql/import-example-dataset.sql


アプリケーションの実行
----------------------
アプリケーション実行の手順は :doc:`../introduction/start-guide` と同様です。

ここではコマンド例のみを示します。詳しくは同文書の :ref:`introduction-start-guide-deploy-app` と :ref:`introduction-start-guide-run-app` を参考にしてください。

..  code-block:: sh

    cd <サンプルアプリケーションプロジェクトのパス>
    cp target/*batchapps*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar xf *batchapps*.jar

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

:ref:`introduction-start-guide-run-app` との相違点として、結果の出力はローカルファイルシステムではなく、MySQLのテーブルに出力されます。

アプリケーション実行結果の確認
------------------------------
サンプルアプリケーションが出力するテーブルを参照します。以下例です。

..  code-block:: sh

    mysql -u asakusa -pasakusa -D asakusa -e "SELECT * FROM CATEGORY_SUMMARY"
    +-----+------------+---------------------+---------------------+---------------+--------------+---------------------+
    | SID | VERSION_NO | RGST_DATETIME       | UPDT_DATETIME       | CATEGORY_CODE | AMOUNT_TOTAL | SELLING_PRICE_TOTAL |
    +-----+------------+---------------------+---------------------+---------------+--------------+---------------------+
    |   1 |       NULL | 2012-07-30 13:15:52 | 2012-07-30 13:15:52 | 1300          |           12 |                1596 |
    |   2 |       NULL | 2012-07-30 13:15:52 | 2012-07-30 13:15:52 | 1401          |           15 |                1470 |
    |   3 |       NULL | 2012-07-30 13:15:52 | 2012-07-30 13:15:52 | 1600          |           28 |                5400 |
    +-----+------------+---------------------+---------------------+---------------+--------------+---------------------+

    mysql -u asakusa -pasakusa -D asakusa -e "SELECT * FROM ERROR_RECORD"
    +-----+------------+---------------------+---------------------+---------------------+------------+---------------+---------+
    | SID | VERSION_NO | RGST_DATETIME       | UPDT_DATETIME       | SALES_DATE_TIME     | STORE_CODE | ITEM_CODE     | MESSAGE |
    +-----+------------+---------------------+---------------------+---------------------+------------+---------------+---------+
    |   1 |       NULL | 2012-07-30 13:15:52 | 2012-07-30 13:15:52 | 1990-01-01 10:40:00 | 0001       | 4922010001000 | ????    |
    |   2 |       NULL | 2012-07-30 13:15:52 | 2012-07-30 13:15:52 | 2011-04-01 19:00:00 | 9999       | 4922010001000 | ????    |
    |   3 |       NULL | 2012-07-30 13:15:52 | 2012-07-30 13:15:52 | 2011-04-01 10:00:00 | 0001       | 9999999999999 | ????    |
    +-----+------------+---------------------+---------------------+---------------------+------------+---------------+---------+


アプリケーションの開発
======================
以降ではアプリケーションの開発における、ThunderGate特有の部分について紹介します。

データモデルクラスの生成
------------------------
データモデルクラスを作成するには、データモデルの定義情報を記述後にGradleの ``compileDMDL`` タスクを実行します。

ThunderGateではモデルをDMDLで記述するほかにThunderGate特有の機能として、ThunderGateが入出力に利用するデータベースのテーブル定義情報を記述したDDLスクリプトや、結合や集計を定義した専用のビュー定義情報を記述したDDLスクリプトから対応するDMDLスクリプトを生成出来るようになっています。

DMDLスクリプトはプロジェクトの ``src/main/dmdl`` ディレクトリ [#]_ 以下に配置し、スクリプトのファイル名には ``.dmdl`` の拡張子を付けて保存します。
DMDLの記述方法については :doc:`../dmdl/start-guide` などを参考にしてください。

またテーブルやビューのDDLスクリプトからDMDLスクリプトを生成する機能を使う場合、DDLスクリプトはプロジェクトの ``src/main/sql/modelgen`` ディレクトリ以下に配置し、DDLスクリプトのファイル名には ``.sql`` の拡張子を付けて保存します。

DDLスクリプトは ``compileDMDL`` タスク実行時に一時的にDMDLスクリプトに変換され [#]_ 、続けて ``src/main/dmdl`` 配下のDMDLと合わせてデータモデルクラスを生成します。
DDLスクリプトの記述方法については :doc:`with-dmdl` を参照してください。

..  [#] ディレクトリはプロジェクトの設定ファイル ``build.properties`` で変更可能です。
..  [#] 一時的に出力されるDMDLスクリプトは、 ``target/dmdl`` ディレクトリ以下に出力されます。このディレクトリはプロジェクトの設定ファイル ``build.properties`` で変更可能です。


Asakusa DSLの記述
-----------------
ThunderGateを利用する場合でも、Asakusa DSLの基本的な記述方法は同様です。

ThunderGate特有の部分は、ThunderGateとの連携を定義するジョブフロー記述の部分になります。ここではMySQLのテーブルに対する入出力の抽出条件や使用するロックの種類などを定義します。詳しくは :doc:`with-dsl` を参照してください。

それ以外の部分については、 :doc:`../dsl/start-guide` などを参照してください。 

アプリケーションのテスト
------------------------
Asakusa DSLの記述と同様、アプリケーションのテストについても基本的な方法は同じで、テストドライバーを利用することが出来ます。

ThunderGateはMySQLに対してデータの入出力を行うため、ジョブフローのテストについてはテストドライバー側でテストデータ定義に基づいてMySQLに対する初期データの投入や結果の取得が行われます。ThunderGateを利用したアプリケーションのテストについて詳しくは :doc:`with-testing` を参照してください。

それ以外の部分については、 :doc:`../testing/start-guide` などを参照してください。


