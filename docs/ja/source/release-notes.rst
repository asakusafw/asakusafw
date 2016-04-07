==============
リリースノート
==============

Release 0.8.0
=============

(開発中)

はじめに
--------

Asakusa Frameworkは優れた開発生産性、高いパフォーマンスを発揮するバッチアプリケーションの開発、実行基盤として様々な改善を続けています。

今回のリリースでは、 新しい実行基盤である |ASAKUSA_ON_M3BP| の新規公開、昨年からDeveloper Previewとして公開していた Asakusa on Spark の正式公開など重要なアップデートが多数含まれています。

また今回のリリースでは、より優れたプラットフォームへの対応を積極的に行うために、いくつかの古いプラットフォームの対応を削除しています。

新機能と主な変更点
------------------

|ASAKUSA_ON_M3BP|
~~~~~~~~~~~~~~~~~

|ASAKUSA_ON_M3BP| は、Asakusa DSLを始めとするAsakusa Frameworkの開発基盤を利用して作成したバッチアプリケーションに対して、 |M3BP_ENGINE| をその実行基盤として利用するための機能セットを提供します [#]_ 。

|M3BP_ENGINE| はDAG (Directed Acyclic Graph; 有向非循環グラフ) の形で表現されたタスクをマルチコア環境で効率よく処理するためのフレームワークで、以下のような特徴があります。

* 単一ノード上のマルチコア/マルチプロセッサ用に最適化
* 細粒度で動的なタスクスケジューリング
* ほぼすべてオンメモリで処理

上記のような特徴のため、Hadoop MapReduceやSparkに比べて、小〜中規模データサイズのバッチ処理に非常に適しています。

Asakusa Frameworkの適用領域においても、中間結果が全てメモリ上に収まる規模のバッチにおいてはAsakusa on Sparkよりも高速で、かつ高いコストパフォーマンスになることを確認しています。

.. [#] https://github.com/fixstars/m3bp

|ASAKUSA_ON_M3BP| の詳細は、以下のドキュメントを参照してください。

* :asakusa-on-m3bp:`Asakusa on M3BP <index.html>`

Asakusa on Spark
~~~~~~~~~~~~~~~~

2015年からDeveloper Previewとして公開していた Asakusa on Spark を正式機能として公開しました。

Asakusa on Sparkは、Asakusa DSLを始めとするAsakusa Frameworkの開発基盤を利用して作成したバッチアプリケーションに対して、Apache Sparkをその実行基盤として利用するための機能セットを提供します。

複雑なデータフローを扱う場合、大容量データを扱う場合などの多くのケースにおいて、Asakusa on Sparkの上で実行するバッチアプリケーションは優れたパフォーマンスを発揮します。

Asakusa on Spark の詳細は、以下のドキュメントを参照してください。

* :asakusa-on-spark:`Asakusa on Spark <index.html>`

Asakusa on Spark Iterative Extentions
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

*  `互換性に関して`_

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

