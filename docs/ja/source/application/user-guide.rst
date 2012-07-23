========================
開発環境構築ユーザガイド
========================
この文書では、開発環境を構築する上での注意点を説明します。

.. _development-environment-with-thundergate:

ThunderGateを使ったアプリケーションの開発環境構築
=================================================
ThunderGateを使う場合の開発環境の注意点を説明します。

セキュリティに関する設定
------------------------
開発環境でThunderGateを実行させる場合、以下の設定を行ってください。

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

OSユーザ作成とsshの設定
-----------------------
Asakusa Frameworkによる開発を行うためのOSユーザ（このドキュメントでは「ASAKUSA_USER」と記します）を作成します。

以下の説明ではASAKUSA_USERを「asakusa」として作成したものとします。

ASAKUSA_USER作成後、sshをパスフレーズ無しで実行出来るよう設定します。以下設定例です。

..  code-block:: sh

    ssh-keygen -t dsa -P '' -f ~/.ssh/id_dsa 
    cat ~/.ssh/id_dsa.pub >> ~/.ssh/authorized_keys
    chmod 600 ~/.ssh/authorized_keys


MySQLのインストールとユーザ作成
-------------------------------
開発環境にMySQLをインストールします。

MySQLのインストールとJDBCドライバの取得についてはMySQLの次のサイト等 [#]_ を参考にして下さい。

..  [#] http://dev.mysql.com/doc/refman/5.1/ja/linux-rpm.html

MySQLのインストールが完了したら、Asakusa Frameworkのモデルジェネレータによるモデルクラス生成、およびテストドライバを使ったテスト時に使用するMySQLユーザとデータベースを作成します

:doc:`maven-archetype` で説明するMavenアーキタイプから生成するアプリケーション開発用プロジェクトのデフォルト設定に合わせるため、データベース名とユーザ名、パスワードはそれぞれ「asakusa」を使用することを推奨します。

..  code-block:: sh

    mysql -u root
    > GRANT ALL PRIVILEGES ON *.* TO 'asakusa'@'localhost' IDENTIFIED BY 'asakusa' WITH GRANT OPTION;
    > GRANT ALL PRIVILEGES ON *.* TO 'asakusa'@'%'IDENTIFIED BY 'asakusa' WITH GRANT OPTION;
    > CREATE DATABASE asakusa DEFAULT CHARACTER SET utf8;

..  warning::
    このデータベースはモデルジェネレータの実行毎に再作成(DROP DATABASE/CREATE DATABASE）が行われるので、開発以外の目的では使用しないでください。

スタンドアロンモード/疑似分散モードの切替
-----------------------------------------
開発環境では通常はHadoopのスタンドアロンモードを使用して開発しますが、
疑似分散モード上でAsakusaのアプリケーションを動作させることも可能です。

スタンドアロンモードから疑似分散モードへ切り替えるには、以下の手順に従います。

Hadoopのモードを切り替える
~~~~~~~~~~~~~~~~~~~~~~~~~~
Hadoopを疑似分散モードへ切り替えます。疑似分散モードの設定方法やモードの切替手順については、各Hadoopディストリビューションのドキュメントを参照してください (例えばCDHでは以下を参照 [#]_ )。

..  [#] https://ccp.cloudera.com/display/CDHDOC/CDH3+Deployment+in+Pseudo-Distributed+Mode

.. _development-environment-with-other-cdh:

CDH3以外のHadoopを使ったアプリケーションの開発環境構築
======================================================
Asakusa Framework バージョン |version| では、開発環境に使用するHadoopディストリビューションはCDH3を推奨しています。CDH3以外のHadoopディストリビューションでは、Asakusa Frameworkの動作検証は十分に行われていません。

ここでは、開発環境にCDH3以外のHadoopディストリビューションを使う上でこれまでに判明している注意点や制約について説明します。

テストドライバの実行
--------------------
テストドライバはアプリケーション開発環境で実行するため、テストドライバの実行時クラスパスは Maven, もしくは Eclipse の設定に従って参照されます [#]_ 。また、テストドライバは内部でHadoopのAPIを使用してHadoopファイルシステムに対するファイル操作を行っています。この2点から、環境によっては以下のような問題が発生することがあります。

..  [#] Asakusa Frameworkのバッチアプリケーション用アーキタイプでは、クラスパスの設定は ``pom.xml`` の設定に従って参照されます。また使用しているHadoopライブラリのバージョンは、 ``pom.xml`` のプロパティ ``hadoop.version`` に設定されています。

Hadoopファイルシステム操作用のライブラリの参照
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Hadoopディストリビューションが使用するHadoopファイルシステムによっては、これを操作するために追加のライブラリが必要になることがあります [#]_ 。テストドライバの実行環境に対して、追加のライブラリ参照を行うための設定が必要になることがあります。

..  [#] 例えば、Mapが提供するMapRFSを操作するためには、MapRFS操作用のライブラリが必要となります。

擬似分散モード環境におけるテストドライバの実行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
擬似分散モード環境下でテストドライバを実行する場合、テストドライバが参照するHadoopファイルシステム操作用のライブラリと開発環境に導入されているHadoopのバージョンが一致していない場合、ファイルシステムのプロトコルバージョンの相違によりエラーとなる可能性があります。

スタンドアロンモードのパフォーマンス
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Asakusa Framework バージョン |version| が動作検証を行っているHadoopディストリビューション [#]_ では、CDH3と比べてApache Hadoopはスタンドアロンモードの実行が遅いことが確認されています。テストドライバ経由で大量にHadoopジョブを実行する環境では、このパフォーマンスの差異を考慮する必要があるかもしれません。

..  [#] 動作検証プラットフォームについては、 :doc:`../product/target-platform` を参照




