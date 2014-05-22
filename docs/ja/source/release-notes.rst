==============
リリースノート
==============

Release 0.6.2
=============
May 22, 2014

`Asakusa Framework 0.6.2 documentation`_

..  _`Asakusa Framework 0.6.2 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.6.2/release/ja/html/index.html

新機能と主な変更点
------------------

「小さなジョブ」の実行に関する最適化オプションの追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Asakusa Frameworkのアプリケーション実行時における最適化設定として、
以下のオプションを追加しました。

* Mapperごとにジョブの入力データサイズを判定し、データが小さい場合にMapperに対する入力スプリットを1つにまとめる: ``com.asakusafw.input.combine.tiny.limit``
* ジョブの入力データサイズを判定し、データが小さい場合に起動するReduceタスクを ``1`` に再設定する: ``com.asakusafw.reducer.tiny.limit``

実行するアプリケーションの特性に応じてこれらのオプションを有効にすることで、
計算リソースの無駄遣いを抑制したり、タスク起動のオーバーヘッドを削減したりすることで
アプリケーション実行時のパフォーマンスが向上する可能性があります。

詳しくは、 :doc:`administration/configure-hadoop-parameters` の
上記設定項目の説明を参照してください。

対応プラットフォームのアップデート
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
動作検証プラットフォームのHadoopディストリビューションに CDH5 [#]_ を追加しました。
また、Amazon EMR [#]_ など一部のHadoopディストリビューションの動作検証バージョンをアップデートしました。

Hadoop2系での動作については、MRv2(YARN)上でアプリケーションを実行した際に
不適切な最適化が適用されることによる性能上の問題や、
MRv1上でアプリケーションが正常に実行されないことがある不具合などを修正し、
安定性を向上させています。

アプリケーション開発環境については、Ubuntu Desktop 14.04 [#]_  や Gradle 1.12 [#]_ など
動作検証プラットフォームのアップデートを行いました。

対応プラットフォームの一覧は、 :doc:`product/target-platform` を
参照してください。

..  attention::
    本バージョンでは、Hadoop2系の対応は試験的機能として提供されます。
    Hadoop2系の利用について詳しくは :doc:`administration/deployment-hadoop2` を
    参照してください。

..  [#] http://www.cloudera.co.jp/products-services/cdh/cdh.html
..  [#] http://aws.amazon.com/jp/elasticmapreduce/
..  [#] http://www.ubuntu.com/desktop
..  [#] http://www.gradle.org/

YAESSログの可視化
~~~~~~~~~~~~~~~~~
試験的機能として、YAESSの実行時ログからCSV形式のレポートファイルを生成する
YAESS Log Analyzerツール を追加しました。
アプリケーションの実行時間の分析などに有用です。

詳しくは、 :doc:`application/yaess-log-visualization` を
参照してください。

互換性に関して
--------------
本リリースでは過去バージョンとの互換性に関する特別な情報はありません。

過去バージョンからのマイグレーション情報については、
以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.6.1
=============
Mar 19, 2014

`Asakusa Framework 0.6.1 documentation`_

..  _`Asakusa Framework 0.6.1 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.6.1/release/ja/html/index.html

新機能と主な変更点
------------------
本リリースの新機能と主な変更点は以下の通りです。

* 以下の機能をSandboxから標準機能に昇格
   * テストドライバのエミュレーションモード実行: :doc:`testing/emulation-mode`
   * バッチテストランナーAPI: :doc:`testing/user-guide` - :ref:`testing-userguide-integration-test`
* Direct I/O の入力ファイルが存在しない場合にエラーとせず処理を続行するオプションを追加。
   * ``DirectFileInputDescription#isOptional()`` : :doc:`directio/user-guide`
* Asakusa Gradle Plugin が ThunderGate に対応、また内部動作と拡張性に関する多くの改善。

その他、細かな機能改善およびバグフィックスが含まれます。
すべての変更点は :doc:`changelogs` を参照してください。

互換性に関して
--------------
本リリースでは過去バージョンとの互換性に関する特別な情報はありません。

過去バージョンからのマイグレーション情報については、
以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.6.0
=============
Feb 17, 2014

`Asakusa Framework 0.6.0 documentation`_

..  _`Asakusa Framework 0.6.0 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.6.0/release/ja/html/index.html

.. contents::
   :local:
   :depth: 2
   :backlinks: none

新機能と主な変更点
------------------

標準のビルドシステムをGradleに移行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
バッチアプリケーションの開発で使用する標準のビルドツールを
従来のバージョンで使用していたMavenからGradleに移行しました。

バージョン ``0.5.2`` から試験的に提供していた
:doc:`Asakusa Gradle Plugin <application/gradle-plugin>` に対して
多くの改善とバグフィックスを行い、これを標準機能に昇格しました。
また、Asakusa Frameworkのドキュメント全体を
Gradleを利用した説明に変更しています。

Gradleを使ったアプリケーション開発の詳細や、
Mavenを利用しているアプリケーションプロジェクトを
Gradleを利用したプロジェクトに移行する方法などについては
以下のドキュメントを参照してください。

* :doc:`application/gradle-plugin`

Mavenの利用について
^^^^^^^^^^^^^^^^^^^
本バージョン、およびAsakusa Framework ``0.6`` 系では
Mavenを使ったアプリケーションの開発もサポートしています。

Asakusa Framework ``0.7`` 系以降の将来のバージョンで、
Mavenによるアプリケーション開発を非推奨とすることを検討しています。

Shafu - Gradleプロジェクト用Eclipse Plugin
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
標準のビルドシステムをGradleに移行したことにあわせて、
Gradleを利用するアプリケーションプロジェクトの開発をサポートするEclipseプラグイン
「Shafu (車夫)」を公開しました。

* :jinrikisha:`Shafu - Asakusa Gradle Plug-in Helper for Eclipse - <shafu.html>`

Shafu はバッチアプリケーション開発にGradleを利用する際に、
Eclipseから透過的にビルドツール上の操作を行えます。
Shafu を使うことで、ターミナル上でのビルドツールの操作が不要となり、
Eclipse上でアプリケーション開発に必要なほとんどの作業を行うことができるようになります。

テストドライバにJavaオブジェクトによるテストデータ指定を追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
テストドライバに指定可能なテストデータの形式を
従来のExcelとJson形式に加え、
Javaオブジェクトの指定が可能になりました。

詳しくは、 :doc:`testing/user-guide` の
「入力データと期待データをJavaで記述する」を
参照してください。

アプリケーションビルド時のログを改善
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
DMDLのコンパイルやAsakusa DSLのコンパイル、
テストドライバの実行時に出力されるログなどの
出力内容を改善しました。

試験的機能(Sandbox)
--------------------

アプリケーションテスト用のエミュレーションモード
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
試験的機能として、アプリケーションテスト用のエミュレーションモードを公開しました。

エミュレーションモードでAsakusa DSLのテストを実行すると、
Asakusa Frameworkが提供するラッパー機構を利用してHadoopの処理を実行します。

通常のテスト実行とは異なり、テストを実行しているプロセス内でほとんどの処理が行われるため、
デバッグモードのブレークポイントなどを利用できるようになります。
また、カバレッジツールと連携して演算子メソッドのテストカバレッジを確認しやすくなります。

また、エミュレーションモードと連携したインテグレーションテスト用のツールとして
バッチテストランナーAPIを追加しました。

エミュレーションモードの詳細や利用方法などについては、
以下のドキュメントを参照してください。

* :sandbox:`エミュレーションモードによるアプリケーションテスト <testing/emulation-mode.html>`

入力データサイズに応じて自動的にローカルモードでジョブを実行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
試験的機能として、入力データサイズに応じて自動的に
ローカルモードでHadoopジョブを実行する
実行時プラグインを公開しました。

このプラグインを利用することで
バッチの実行にかかるHadoopのオーバーヘッドが適切に調整され、
バッチ実行時間が改善する可能性があります。

現時点でこのプラグインは基本的な動作確認のみを行なっており、
動作検証プラットフォームは Apache Hadoop 1.2.1 のみです。

利用方法は以下のREADMEを参照してください。

* https://github.com/asakusafw/asakusafw-sandbox/blob/0.6.0/asakusa-runtime-ext/README.md

互換性に関して
--------------
本リリースでは過去バージョンとの互換性に関する特別な情報はありません。

過去バージョンからのマイグレーション情報については、
以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

リンク
======
* :doc:`previous-release-notes`
* :doc:`changelogs`

