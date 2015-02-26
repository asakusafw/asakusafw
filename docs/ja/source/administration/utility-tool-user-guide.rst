==================================
ユーティリティツールユーザーガイド
==================================

この文書では、Asakusa Frameworkが提供するコマンドラインツールの利用方法について解説します。
このコマンドラインツール群を利用することで、運用環境上のメンテナンス作業を軽減します。

ユーティリティツールの配置
==========================

すべてのユーティリティツールは、Asakusa Frameworkをインストールしたパスの :file:`tools/bin` ディレクトリ内に格納されています。

Hadoopに関するユーティリティ
============================

Hadoopに関するユーティリティツールは、 :file:`$ASAKUSA_HOME/tools/bin` ディレクトリ内の :file:`hadoop-` から始まるスクリプトとして提供しています。

いずれのツールも :program:`hadoop` コマンドを経由して実行するため、環境にHadoopがインストールされている必要があります。
また、Hadoopのインストール先を通知するために、以下の環境変数のいずれかが必要です。

``HADOOP_CMD``
  ``hadoop`` コマンドのパス
``HADOOP_HOME``
  Hadoopのインストール先
``PATH``
  :program:`hadoop` コマンドが通っているパス

環境変数の設定方法は、 `コマンドラインツール全体の設定`_ を参照してください。

Hadoopファイルシステムのクリーニング
------------------------------------

:program:`$ASAKUSA_HOME/tools/bin/hadoop-fs-clean.sh` コマンドを利用すると、Hadoopファイルシステム上の古いファイルやディレクトリを一括して削除できます。

以下の形式で指定します。

..  code-block:: sh

    hadoop-fs-clean.sh -k <days> [-r] [-s] <path> [<path> [...]]

コマンドに指定可能な引数は以下のとおりです。

..  program:: hadoop-fs-clean.sh

..  option:: -h , -help

    ヘルプメッセージを表示して終了します。

..  option:: -k <days> , -keep-days <days> (必須)

    最終更新から ``<days>`` 日以上経過したファイルのみを削除します。
    ``0`` を指定した場合には現在時刻よりも古いファイルをすべて削除します。

..  option:: -r , -recursive

    ディレクトリとその内容を再帰的にクリーニングの対象とします。
    クリーニングによってディレクトリの中身が空になった場合、ディレクトリも削除の対象になります。

..  option:: -s , -dry-run

    クリーニング時にファイルやディレクトリの削除を行わず、ログだけを出力します。

..  option:: path (必須)

    クリーニング対象のパスをURI形式で指定します。
    2つ以上のパスを指定することもできます。

    ``*`` を含むパスなど、 :program:`hadoop fs` コマンドで有効なパス式を指定できます。

    なお、 コマンド引数に ``--`` を指定すると以降の引数をすべて ``<path>`` とみなします。
    対象のURIが ``-`` から始まる場合などに有効です。

以下は利用例です。

..  code-block:: sh

    # HDFS上の /user/asakusa/var/logs ディレクトリ直下のうち、1日経過したファイルを削除する
    hadoop-fs-clean.sh -k 1 hdfs://localhost:8020/user/asakusa/var/logs/*

    # HDFS上の /user/asakusa/var/logs ディレクトリ内の、10日経過したファイルやディレクトリを再帰的に削除する
    hadoop-fs-clean.sh -k 10 hdfs://localhost:8020/user/asakusa/var/logs/* -r

    # ローカルファイルシステム上の /tmp/hadoop-asakusa ディレクトリに対する全削除をシミュレーションする
    hadoop-fs-clean.sh -dry-run -k 0 file:///tmp/hadoop-asakusa -r

コマンドラインツール全体の設定
==============================

上記で紹介したコマンドラインツールは、実行前に :file:`$ASAKUSA_HOME/tools/env.sh` を読み込んで必要な環境変数の設定などを行います。

以下は同ファイルの内容を改変し、環境変数 ``HADOOP_CMD`` を設定する例です。

..  code-block:: sh

    export HADOOP_CMD=/usr/bin/hadoop

コマンドラインツールのログ設定
==============================

上記で紹介したコマンドラインツールは、Hadoopが持つLog4Jのルートロガーに対して出力を行います。

多くのHadoopディストリビューションでは、デフォルトではLog4Jのルートロガーは標準エラー出力に対してログ出力を行うようになっています。

