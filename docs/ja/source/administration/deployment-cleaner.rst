============================
クリーニングツールのデプロイ
============================
この文書では、 クリーニングツールのデプロイ手順について説明します。

..  attention::
    Asakusa Frameworkのバージョン ``0.4.0`` 以降、本機能はレガシーモジュール [#]_ として扱われます。クリーニングツールは :doc:`utility-tool-user-guide` で説明している ``hadoop-fs-clean.sh`` コマンドを使用することを推奨します。

..  warning::
    Asakusa Cleaner はファイルパスの解決方法に問題があるため、Hadoopファイルシステムを扱うために追加のライブラリが必要になるHadoopディストリビューションに対応していません。 バージョン |version| 時点では、MapRで提供されるMapRFS上で正常に動作しないことが確認されています。

..  [#] レガシーモジュールについては、 :doc:`../application/legacy-module-guide` を参照

クリーニングツールについて
==========================
Asakusa Frameworkでは、ローカルファイル、及び分散ファイルシステム上のファイルをクリーニングするためのツール(Asakusa Cleaner)を提供しています。

クリーニングツールの使用は任意ですが、特に分散ファイルシステムについては、Asakusa Frameworkのデフォルトの動作ではアプリケーションを実行した際に処理したファイルが分散ファイルシステム上に残り続けるため、クリーニングツールを使用して定期的にクリーニングを行うことを推奨します。

クリーニングツールの詳細にはついては、 `Asakusa Cleanerユーザーガイド(PDF)`_ を参照してください。

.. _`Asakusa Cleanerユーザーガイド(PDF)` : https://docs.asakusafw.com/AsakusaCleaner_UserGuide.pdf

クリーニングツールのデプロイ
----------------------------
実行環境に Asakusa Cleaner 用の拡張モジュール ``ext-cleaner`` をデプロイします。

拡張モジュールのデプロイ方法については、 :doc:`../administration/deployment-extension-module` を参照してください。

クリーニングツールの設定
------------------------
以下の手順に従い、Asakusa Cleanerの設定を行います。

1. クリーニング用ログ設定ファイルを編集します。 ``$ASAKUSA_HOME/cleaner/conf/log4j.xml`` を編集し、任意のログディレクトリを指定します。

* 設定ファイルのログファイル名部分は ``${logfile.basename}.log`` のままとしてください。
* 指定したログディレクトリが存在しない場合はディレクトリを作成しておいてください。ログディレクトリはクリーニングツールの実行ユーザーが書き込み可能である必要があります。

..  note::
    以下手順2～手順3はHDFSクリーニングツールを使う場合に実施します。

2. ``$ASAKUSA_HOME/cleaner/conf/.clean_hdfs_profile`` を編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default

3. ``$ASAKUSA_HOME/cleaner/conf/clean-hdfs-conf.properties`` を編集し、クリーニングの設定を行います。

* ``hdfs-protocol-host`` は ``core-site.xml`` のプロパティ ``fs.default.name`` と同じ値に変更します 。

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

..  attention::
    ``hdfs-protocol-host`` に指定できるファイルシステムは ``file://`` もしくは ``hdfs://`` のみです。これら以外のファイルシステムには対応していません。

..  note::
    以下手順4～手順5はローカルファイルクリーニングツールを使う場合に実施します。


4. ``$ASAKUSA_HOME/cleaner/conf/.clean_local_profile`` を編集し、以下の変数を環境に合わせて設定します。

..  code-block:: sh

    export JAVA_HOME=/usr/java/default

5. ``$ASAKUSA_HOME/cleaner/conf/clean-localfs-conf.properties`` を編集し、クリーニングの設定を行います。

..  code-block:: properties

    # File path of log4j.xml (optional)
    log.conf-path=/home/asakusa/asakusa/cleaner/conf/log4j.xml
    # Directory for cleaning (required)
    clean.local-dir.0=/home/asakusa/asakusa/log
    # Cleaning Pattern (required)
    clean.local-pattern.0=.*\.log\..*
    # Preservation period date of file (optional)
    clean.local-keep-date=10

