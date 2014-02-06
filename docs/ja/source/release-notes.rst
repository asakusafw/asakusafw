==============
リリースノート
==============

Release 0.6.0
=============

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

Shafu(車夫) - Gradleプロジェクト用Eclipse Plugin
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
標準のビルドシステムをGradleに移行したことにあわせて、
Gradleを利用するアプリケーションプロジェクトの開発をサポートするEclipseプラグイン
`Shafu`_ (車夫) を公開しました。

* `Shafu`_ - Asakusa Gradle Plug-in Helper for Eclipse -

Shafu はバッチアプリケーション開発にGradleを利用する際に、
Eclipseから透過的にビルドツール上の操作を行えます。
Shafu を使うことで、ターミナル上でのビルドツールの操作が不要となり、
Eclipse上でアプリケーション開発に必要なほとんどの作業を行うことができるようになります。

..  _`Shafu`: http://asakusafw.s3.amazonaws.com/documents/jinrikisha/ja/html/shafu.html

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

* `エミュレーションモードによるアプリケーションテスト`_

..  _`エミュレーションモードによるアプリケーションテスト`: http://asakusafw.s3.amazonaws.com/documents/sandbox/ja/html/testing/emulation-mode.html

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

Changes
-------

Enhancements
~~~~~~~~~~~~
* [ :issue:`341` ] - Creates directories for generated sources on eclipse task [Gradle Plugin]
* [ :issue:`347` ] - Adds wrapper task with settings for batchapp to template project [Gradle Plugin]
* [ :issue:`353` ] - Enables to modify compilerArgs via build script and changes default value [Gradle Plugin]
* [ :issue:`354` ] - Adds extention point for configuring jobs to StageClient
* [ :issue:`355` ] - Adjusts application build log
* [ :issue:`358` ] - Add pluggable job executors for test driver
* [ :issue:`361` ] - Add TestDriver API for preparing and verifying test data with model object collection.
* [ :issue:`364` ] - Add pluggable testing environment configurator for test driver

Bug fixes
~~~~~~~~~
* [ :issue:`339` ] - Fix a closing tag name
* [ :issue:`343` ] - Incorrect hadoopWorkDirectory set on Gradle Plugin
* [ :issue:`344` ] - generateTestbook task should set headless option [Gradle Plugin]
* [ :issue:`350` ] - Fix a wrong Javadoc parameter explanation 
* [ :issue:`351` ] - Generates resources.prefs file in configuration phase [Gradle Plugin]
* [ :issue:`352` ] - Maven archetype has broken example script file
* [ :issue:`356` ] - Task inputs/outputs property does not evaluate correctly when changing that [Gradle Plugin] 
* [ :issue:`357` ] - TestDriver cannot accept an empty file as a JSON data input 
* [ :issue:`359` ] - Direct I/O does not detect data source correctly when using base path with valuables
* [ :issue:`360` ] - TestDriver fails on project with blank space path
* [ :issue:`362` ] - TestDriverBase#setFrameworkHomePath does not work
* [ :issue:`365` ] - Log message is not clear when ConfigurationProvider failed to find hadoop conf.

Others
~~~~~~
* [ :issue:`340` ] - Changes standard build system on documents to Gradle-based 
* [ :issue:`342` ] - Refactoring Gradle Plugin
* [ :issue:`345` ] - Prepare for 0.6.0 release
* [ :issue:`346` ] - 0.6.0 Documents
* [ :issue:`348` ] - Refactoring Gradle Template Project [Gradle Plugin] 
* [ :issue:`349` ] - Adds eclipse.preferences.version to asakusafw project prefs [Gradle Plugin]
* [ :issue:`363` ] - 0.6.0 Refactoring



リンク
------
* `Asakusa Framework 0.6.0 documentation`_
* :doc:`previous-release-notes`
* :doc:`changelogs`

..  _`Asakusa Framework 0.6.0 documentation`: http://asakusafw.s3.amazonaws.com/documents/0.6.0/release/ja/html/index.html
