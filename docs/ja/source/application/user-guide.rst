=================================================
Asakusa Framework アプリケーション ユーザーガイド
=================================================

この文書では、Linux OS上にAsakusa Frameworkの開発環境を構築する手順を解説します。

開発環境の構築
==============
Asakusa Frameworkの開発環境を構築します。なお、各ソフトウェアの対応バージョンについては、GitHubのWikiに公開されているドキュメント「Target Platform ja」 [#]_ を参照してください。

..  [#] https://github.com/asakusafw/asakusafw/wiki/Target-Platform-ja

Linuxデスクトップ環境の準備
---------------------------
Asakusa Frameworkを開発するためのLinuxデスクトップ環境（以下開発環境）を用意します。

開発環境にはsshが必要です。OpenSSHが入っていない場合はインストールしてください。

..  note::
    仮想環境を使用してLinuxデスクトップ環境を準備することもできます。
    Asakusa Framwworkが動作検証を行っている仮想環境は以下の通りです。

    * VMWare Player 3.1.3 以上(Windows)
    * VMWare Fusion 3.1.2 以上(MacOS)
    * VMWare Server 2.0.2 (Linux)

ディストリビューション固有の環境設定
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
開発環境でAsakusa ThunderGateを実行させる場合、以下の設定を行ってください。

CentOS
^^^^^^

* SELinuxは無効にしてください。

Ubuntu
^^^^^^

* AppArmorのMySQL用プロファイルを無効にしてください。以下設定例です。

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

ホームディレクトリのパーミッションの変更
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
スタンドアロンモードでHadoopを使用する場合に、ホームディレクトリのパーミッションにOtherのRead,Execute権限が無い場合にHadoopの一部の機能（分散キャッシュ）が正常に動作しません。

このためCentOSなどの一部のディストリビューションではホームディレクトリのパーミッションを変更する必要があります。

CentOSでは、以下のようにホームディレクトリのパーミッションを変更します。

..  code-block:: sh

    chmod a+rx $HOME

JDK/Hadoopのインストール
-------------------------
開発環境にJDKとHadoopをインストールします。

CDH3のインストールについてはClouderaの次のサイト等 [#]_ を参考にしてください。

..  [#] https://ccp.cloudera.com/display/CDHDOC/CDH3+Installation

ASAKUSA_USERからスタンドアロンモードでHadoopのサンプルジョブが正常に実行出来ることを確認してください。  
以降の作業ではスタンドアロンモードを使用するため、分散モードになっている場合はスタンドアロンモードに切り替えて下さい。

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

AppArmorのMySQLプロファイルを無効化
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Asakusa FrameworkのコンポーネントであるAsakusa ThunderGateはMySQLのクエリーを使ってローカルファイルへの入出力を行いますが、Ubuntuなどの一部のディストリビューションではデフォルト設定でMySQLのローカルファイルへの入出力がAppArmorサービスによって制限されています。

このため、Ubuntuなどの一部のディストリビューションではAppArmorの設定を変更してMySQLのローカルファイルへの入出力を行えるようにする必要があります。

Ubuntuでは、以下のようにAppArmorの設定を変更します。

..  code-block:: sh

    sudo mv /etc/apparmor.d/usr.sbin.mysqld /etc/apparmor.d/disable/
    sudo /etc/init.d/apparmor restart

Mavenのインストール
-------------------
開発環境にMavenをインストールします。

MavenのインストールについてはMavenの次のサイト等 [#]_ を参考にして下さい。

..  [#] http://maven.apache.org/users/index.html

ASAKUSA_USERの環境変数設定
--------------------------
ASAKUSA_USERに必須の環境変数を設定します。

* JAVA_HOME: JDKインストールディレクトリパス。
* HADOOP_HOME: Hadoopのインストールディレクトリパス。
* ASAKUSA_HOME: Asakusa Frameworkのインストールディレクトリパス。$HOME/asakusa を推奨。

..  note::
    ASAKUSA_HOMEを$HOME/asakusa 以外にした場合、$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-db.properties の以下のプロパティ値の変更が必要です。

    * import.extractor-shell-name=($HOMEからの相対パス)
    * export.extractor-shell-name=($HOMEからの相対パス)

また、Mavenのインストールディレクトリ/bin にPATHを通しておくとよいでしょう。

ASAKUSA_USERの環境変数の設定例は以下の通りです。

..  code-block:: sh

    JAVA_HOME=/usr/java/default
    export JAVA_HOME
    HADOOP_HOME=/usr/lib/hadoop
    export HADOOP_HOME
    ASAKUSA_HOME=$HOME/asakusa
    export ASAKUSA_HOME

    PATH=$JAVA_HOME/bin:$PATH:$HADOOP_HOME/bin:/opt/apache-maven-3.0.3/bin
    export PATH

開発環境にEclipse等のIDE環境を使う場合は、上記の環境変数が適用されたシェルから起動してください。

なお、IDEをデスクトップから起動する場合は一旦ログアウトし、再ログインしてからIDEを起動してください。

.. _user-guide-eclipse:

Eclipseを使ったアプリケーションの開発
-------------------------------------
アプリケーションの開発にEclipseを使用する場合、まずEclipseのワークスペースに対してクラスパス変数M2_REPOを設定します。ワークスペースをデフォルト値($HOME/workspce)に指定して起動した場合は以下のコマンドを実行します。

..  code-block:: sh

    mvn -Declipse.workspace=$HOME/workspace eclipse:add-maven-repo

開発環境上でEclipseをダウンロード [#]_ し、Eclipseを起動します。ワークスペースは上記で-Declipse.workspaceに指定した値と同じディレクトリを指定します。

..  warning::
    Eclipseをデスクトップ環境のファイラーやショートカットから起動する場合、ログインシェルに環境変数を適用する必要があるためEclipse起動前にいったんログアウトして再ログインしてください。

作業したいアプリケーション用プロジェクトに対して、Eclipseプロジェクト用の定義ファイルを作成します。

..  code-block:: sh

    mvn eclipse:eclipse

これでEclipseからプロジェクトをImport出来る状態になりました。Eclipseのメニューから [File] -> [Import] -> [General] -> [Existing Projects into Workspace] を選択し、プロジェクトディレクトリを指定してEclipseにインポートします。

..  [#] http://www.eclipse.org/downloads/

スタンドアロンモード/疑似分散モードの切替
-----------------------------------------
開発環境では通常はHadoopのスタンドアロンモードを使用して開発しますが、
疑似分散モード上でAsakusaのアプリケーションを動作させることも可能です。

スタンドアロンモードから疑似分散モードへ切り替えるには、
以下の手順に従います。

Hadoopのモードを切り替える
~~~~~~~~~~~~~~~~~~~~~~~~~~
Hadoopを疑似分散モードへ切り替えます。疑似分散モードの設定方法やモードの切替手順については、以下を参照して下さい [#]_ 。

..  [#] https://ccp.cloudera.com/display/CDHDOC/CDH3+Deployment+in+Pseudo-Distributed+Mode

ThunderGateの設定変更
~~~~~~~~~~~~~~~~~~~~~
ThunderGateの以下の設定ファイルを変更します。

$ASAKUSA_HOME/bulkloader/conf/bulkloader-conf-hc.propertiesを編集します。
    * 「hdfs-protocol-host」を$HADOOP_HOME/conf/core-site.xml の fs.default.name と同じ値 (デフォルト値は"hdfs://localhost:8020")に変更します。
    * 「hadoop-cluster.workingdir.use」をfalseに変更します。

..  code-block:: sh
    
    # Protocol and host name with HDFS(required)
    hdfs-protocol-host=hdfs://localhost:8020
    # Is the file I/O position made work directory (optional)
    hadoop-cluster.workingdir.use=false

疑似分散モードからスタンドアロンモードに戻す場合は、
上記で変更した設定を元に戻し、Hadoopのデーモンを停止します。

Hadoopモード切替スクリプト
~~~~~~~~~~~~~~~~~~~~~~~~~~
Asakusa Framework の contrib リポジトリには、HadoopとAsakusa Frameworkのモード切替を行うためのスクリプトが公開されています。

..  [#] https://raw.github.com/asakusafw/asakusafw-contrib/master/quick-start/cdh3vm/bin/switch_to_pseudo.sh
..  [#] https://raw.github.com/asakusafw/asakusafw-contrib/master/quick-start/cdh3vm/bin/switch_to_standalone.sh

