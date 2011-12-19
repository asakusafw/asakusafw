============================
クリーニングツールのデプロイ
============================
この文書では、 クリーニングツールのデプロイ手順について説明します。

クリーニングツールについて
==========================
Asakusa Frameworkでは、ローカルファイル、及び分散ファイルシステム上のファイルをクリーニングするためのツール(Asakusa Cleaner)を提供しています。

クリーニングツールの使用は任意ですが、特に分散ファイルシステムについては、Asakusa Frameworkのデフォルトの動作ではアプリケーションを実行した際に処理したファイルが分散ファイルシステム上に残り続けるため、クリーニングツールを使用して定期的にクリーニングを行うことを推奨します。

クリーニングツールの詳細にはついては、 `Asakusa Cleanerユーザーガイド(PDF)`_ を参照してください。

.. _`Asakusa Cleanerユーザーガイド(PDF)` : https://asakusafw.s3.amazonaws.com/documents/AsakusaCleaner_UserGuide.pdf

クリーニングツールのインストールアーカイブの準備
------------------------------------------------
クリーニングツールのインストールアーカイブを用意します。

クリーニングツールのインストールアーカイブは、アプリケーション開発プロジェクトからMavenの以下のコマンドを実行して生成します
。

..  code-block:: sh

    mvn assembly:single

このコマンドを実行すると、プロジェクトの target ディレクトリ配下にいくつかのファイルが生成されます。このうち以下のファイルがクリーニングツールをインストールするためのアーカイブです。

asakusafw-${asakusafw.version}-prod-cleaner.tar.gz

${asakusafw.version}は使用しているAsakusa Frameworkのバージョンに置き換えます。例えばversion 0.2.4 を使っている場合は、 asakusafw-0.2.4-prod-cleaner.tar.gz になります。

クリーニングツールのデプロイ
----------------------------
クリーニングツールのデプロイ手順を以下に示します。

1. ASAKUSA_HOME配下にHadoopクライアントノード用アーカイブ asakusafw-${asakusafw.version}-prod-cleaner.tar.gz を展開します。展開後、ASAKUSA_HOME配下の*.shに実行権限を追加します。

..  code-block:: sh

    mv asakusafw-*-prod-cleaner.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusafw-*-prod-cleaner.tar.gz
    find $ASAKUSA_HOME -name "*.sh" | xargs chmod u+x

2. クリーニング用ログ設定ファイルを編集します。$ASAKUSA_HOME/cleaner/conf/log4j.xmlを編集し、任意のログディレクトリを指定>します。
    * ログファイル名は「${logfile.basename}.log」のままとしてください。
    * 指定したログディレクトリが存在しない場合はディレクトリを作成しておいてください。ログディレクトリはASAKUSA_USERが書き込み可能である必要があります。

..  note::
    以下手順3～手順4はHDFSクリーニングツールを使う場合に実施します。

3. $ASAKUSA_HOME/cleaner/conf/.clean_hdfs_profileを編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default
    export HADOOP_HOME=/usr/lib/hadoop

4. $ASAKUSA_HOME/cleaner/conf/clean-hdfs-conf.properties を編集し、クリーニングの設定を行います。
    * 「hdfs-protocol-host」は$HADOOP_HOME/conf/core-site.xml の fs.default.name と同じ値に変更します。

..  code-block:: properties

    # File path of log4j.xml (optional)
    log.conf-path=/home/asakusa/asakusa/cleaner/conf/log4j.xml
    # Protocol and host name with HDFS (required)
    hdfs-protocol-host=hdfs://(MASTERNODE_HOSTNAME):8020
    # Directory for cleaning (required)
    clean.hdfs-dir.0=/${user}/target/hadoopwork
    # Cleaning Pattern (required)
    clean.hdfs-pattern.0=.*
    # Preservation period date of file (optional)
    clean.hdfs-keep-date=10

..  note::
    以下手順5～手順6はローカルファイルクリーニングツールを使う場合に実施します。

5. $ASAKUSA_HOME/cleaner/conf/.clean_local_profileを編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default

6. $ASAKUSA_HOME/cleaner/conf/clean-localfs-conf.properties を編集し、クリーニングの設定を行います。

..  code-block:: properties

    # File path of log4j.xml (optional)
    log.conf-path=/home/asakusa/asakusa/cleaner/conf/log4j.xml
    # Directory for cleaning (required)
    clean.local-dir.0=/home/asakusa/asakusa/log
    # Cleaning Pattern (required)
    clean.local-pattern.0=.*\.log\..*
    # Preservation period date of file (optional)
    clean.local-keep-date=10

