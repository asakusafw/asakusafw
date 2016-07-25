==============
リリースノート
==============

Release 0.8.1
=============

Jul 25, 2016

`Asakusa Framework 0.8.1 documentation`_

..  _`Asakusa Framework 0.8.1 documentation`: http://docs.asakusafw.com/0.8.1/release/ja/html/index.html

新機能と主な変更点
------------------

Direct I/O 出力カウンターの改善
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Direct I/O の出力時に表示されるカウンターに、出力ポートごとの統計情報が表示されるようになりました。

..  code-block:: none

    com.asakusafw.directio.output.port.Statistics
      categorySummary.bytes=91
      categorySummary.files=1
      categorySummary.records=3
      errorRecord.bytes=432
      errorRecord.files=1
      errorRecord.records=3

Direct I/O line を正式機能としてリリース
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

バージョン 0.7.5 から試験的機能として公開していた :doc:`Direct I/O line <directio/directio-line>` を正式機能として公開しました。

Direct I/O line は任意のテキストファイルを行ごとに読み書きするための機能です。
Direct I/Oが対応していないファイルフォーマットの入出力や、入力ファイルの整形や形式変換、バリデーションチェックなどの事前処理などに利用することができます。

Direct I/O lineの詳細は、以下のドキュメントを参照してください。

* :doc:`directio/directio-line`

Asakusa Framework チュートリアル
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Asakusa Frameworkのサンプルアプリケーションを作成しながら、フレームワークの基本的な使い方や開発の流れを紹介するチュートリアルを公開しました。

* :basic-tutorial:`Asakusa Framework チュートリアル <index.html>`

その他の変更点
~~~~~~~~~~~~~~

その他、細かな機能改善およびバグフィックスが含まれます。
すべての変更点は :doc:`changelogs` を参照してください。

互換性に関して
--------------

非推奨機能
~~~~~~~~~~

以下の機能の利用が非推奨になりました。

* :ref:`gradle-plugin-v08-specify-asakusafw-version` ( ビルドスクリプトの設定 )

  * バージョン 0.8.0 より、Asakusa FrameworkバージョンはAsakusa Gradle Pluginのバージョンから自動的に設定される値を利用することを推奨しています。
  * 特にバージョン 0.7.6 以前に作成したプロジェクトから移行する場合は :doc:`application/migration-guide` を確認して、必要に応じてビルドスクリプトを修正してください。
* :doc:`application/yaess-log-visualization`

  * この機能はバージョン 0.6.2 から試験的機能として提供していましたが、MapReduce以外の実行プラットフォームでは適切な分析が行えないなどの問題があるため、本バージョンより非推奨となりました。

ライブラリの構成変更
~~~~~~~~~~~~~~~~~~~~

Direct I/O lineが含まれるSDKアーティファクトが変更になりました。
過去バージョンのDirect I/O lineを利用しているプロジェクトについては、:doc:`application/migration-guide` を確認してください。

将来のバージョンにおける非互換性を含む変更
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

将来のバージョンにおいて、以下のプロダクトバージョンを対応プラットフォームから除外することを計画しています。

* Java: JDK7（JDK8にのみ対応）

Release 0.8.0
=============

Apr 08, 2016

`Asakusa Framework 0.8.0 documentation`_

..  _`Asakusa Framework 0.8.0 documentation`: http://docs.asakusafw.com/0.8.0/release/ja/html/index.html

はじめに
--------

Asakusa Frameworkは優れた開発生産性、高いパフォーマンスを発揮するバッチアプリケーションの開発、実行基盤として様々な改善を続けています。

今回のリリースでは、 新しい実行基盤である |ASAKUSA_ON_M3BP| の新規公開、昨年からDeveloper Previewとして公開していた Asakusa on Spark の正式公開など重要なアップデートが多数含まれています。

また今回のリリースでは、より優れたプラットフォームへの対応を積極的に行うために、いくつかの古いプラットフォームの対応を削除しています。

新機能と主な変更点
------------------

|ASAKUSA_ON_M3BP|
~~~~~~~~~~~~~~~~~

|ASAKUSA_ON_M3BP| は、Asakusa DSLを始めとするAsakusa Frameworkの開発基盤を利用して作成したバッチアプリケーションに対して、 |M3BP_ENGINE| (https://github.com/fixstars/m3bp) を実行基盤として利用するための機能セットを提供します。

|M3BP_ENGINE| はDAG (Directed Acyclic Graph; 有向非循環グラフ) の形で表現されたタスクをマルチコア環境で効率よく処理するためのフレームワークで、以下のような特徴があります。

* 単一ノード上のマルチコア/マルチプロセッサ用に最適化
* 細粒度で動的なタスクスケジューリング
* ほぼすべてオンメモリで処理

上記のような特徴のため、 小規模〜中規模のデータを扱うバッチに対して、|ASAKUSA_ON_M3BP| によって単一ノード上で高速に処理できるようになりました。

|ASAKUSA_ON_M3BP| の詳細は、以下のドキュメントを参照してください。

* :asakusa-on-m3bp:`Asakusa on M3BP <index.html>`

Asakusa on Spark
~~~~~~~~~~~~~~~~

2015年からDeveloper Previewとして公開していた Asakusa on Spark を正式機能として公開しました。

Asakusa on Sparkは、Asakusa DSLを始めとするAsakusa Frameworkの開発基盤を利用して作成したバッチアプリケーションに対して、Apache Spark (http://spark.apache.org) を実行基盤として利用するための機能セットを提供します。

特に中規模〜大規模のデータを扱うバッチに対して、Asakusa on Sparkは優れたパフォーマンスを発揮します。

Asakusa on Spark の詳細は、以下のドキュメントを参照してください。

* :asakusa-on-spark:`Asakusa on Spark <index.html>`

Asakusa on Spark Iterative Extensions
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Asakusa on Spark の拡張機能「Iterative Extensions」を試験的機能として公開しました。

Iterative Extensionsは、あるバッチに対してバッチ引数の一部または全部を変えながら同じバッチを連続して実行するための機能です。

Iterative Extensionsを適用したバッチを「反復バッチ」と呼びます。
反復バッチは通常のバッチを連続して実行する場合と比べて、次の点で高速に実行できる可能性があります。

* 連続処理によるリソースの効率的な利用

 連続するバッチアプリケーションを1つのSparkアプリケーションとして実行するため、特にYARN上での実行においては、アプリケーションコンテナの初期化などの分散オーバーヘッドが極小化される、コンテナリソースをシンプルな設定で最大限に利用できる、などの利点があります。

* 差分処理による最適化

 反復バッチでは連続するバッチ間で再計算が不要な箇所は実行結果を再利用することがあるため、特に実行するバッチアプリケーション間での変更箇所が少ない場合には、バッチ間の差分処理による利点が大きくなります。

反復バッチは、日付範囲を指定した日次バッチの一括実行や、パラメータ・スイープによるシミュレーションといった用途に適しています。

Iterative Extensionsは、反復バッチを定義するためのAsakusa DSLの拡張構文、反復バッチを生成するするためのAsakusa DSLコンパイラの拡張、および反復バッチを実行するためのインターフェースや実行モジュールなどを提供します。

Asakusa on Spark Iterative Extensions の詳細は、以下のドキュメントを参照してください。

* :asakusa-on-spark:`Asakusa on Spark Iterative Extensions <iterative-extension.html>`

対応プラットフォームの更新
~~~~~~~~~~~~~~~~~~~~~~~~~~

アプリケーションプロジェクトで使用するGradleの標準バージョンを2.12にアップデートしました。

その他、いくつかの動作検証プラットフォームを更新しています。
詳しくは、 以下のドキュメントを参照してください。

* :doc:`product/target-platform`

また冒頭で述べた通り、今回のリリースではいくつかの古いプラットフォームの対応を削除しています。

詳しくは後述の互換性に関する説明を参照してください。

Asakusa Gradle Pluginの改善
~~~~~~~~~~~~~~~~~~~~~~~~~~~

|ASAKUSA_ON_M3BP| や Asakusa on Spark のリリースに伴い、Gradle Plugin上で複数の実行基盤を統一的な方法で扱うための改善や、ビルド設定をシンプルに管理するための改善などをおこないました。

Asakusa Gradle Pluginの変更点については、以下のドキュメントを参照してください。

* :doc:`application/gradle-plugin-v08-changes`

その他の変更点
~~~~~~~~~~~~~~

その他、細かな機能改善およびバグフィックスが含まれます。

すべての変更点は :doc:`changelogs` を参照してください。

互換性に関して
--------------

変更点
~~~~~~

本リリースでは、対応プラットフォームに関する重要な変更と非互換性があります。

..  warning::
    バージョン 0.8.0 は以前のバージョンからいくつかの重要な変更が行われました。
    過去のバージョンからのマイグレーションを検討する際には必ず以下の内容を確認してください。

Java (JDK)
  Java6、およびJDK 6は非対応になりました。

  Java6、およびJDK 6を利用している場合、Java 7(JDK 7)、またはJava 8 (JDK 8)に移行する必要があります。

Hadoop
  Hadoop1系は非対応となりました。

  開発環境にHadoop1系をインストールしている場合、Hadoop2系をインストールしてAsakusa FrameworkからはHadoop2系を利用するよう設定してください。

  運用環境でHadoop1系を利用している場合、Hadoop2系に移行する必要があります。

Gradle
  Gradleのバージョン1系は非対応になりました。

  また、Asakusa Gradle Pluginにいくつか仕様変更が行われ、一部のタスクの動作やビルドスクリプトの設定方法が変更されています。

Maven
  Mavenの利用は非対応になりました。

  Mavenを利用しているアプリケーションプロジェクトは、Gradleを利用するよう移行する必要があります。

Asakusa Framework
  Hadoop1系が非対応となったことにより、Asakusa Framwork バージョン 0.7.0 から導入された「Hadoopバージョン」が廃止になりました。

  Asakusa Framework 0.7系では、Asakusa Framworkのバージョンは ``<version>-hadoop1``, ``<version>-hadoop2`` のように、利用するHadoopのバージョンを持つバージョン体系を導入していました。

  本リリース以降は、Asakusa Frameworkのバージョンは単一のバージョン体系 ( 例えば本リリースのバージョンは ``0.8.0`` ) を使用します。

変更内容の詳細やマイグレーション手順については、以下のドキュメント説明しています。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

..  attention::
    過去のバージョンからのマイグレーション作業を行う場合、必ず :doc:`application/migration-guide` と :doc:`administration/migration-guide` を確認してください。

リンク
======

* :doc:`previous-release-notes`
* :doc:`changelogs`

