====================
過去のリリースノート
====================

Asakusa Frameworkの過去バージョンのリリースノートです。

すべての変更点一覧は :doc:`changelogs` も参照してください。

Release 0.6.2
=============

May 22, 2014

`Asakusa Framework 0.6.2 documentation`_

..  _`Asakusa Framework 0.6.2 documentation`: http://docs.asakusafw.com/0.6.2/release/ja/html/index.html

新機能と主な変更点
------------------

「小さなジョブ」の実行に関する最適化オプションの追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Asakusa Frameworkのアプリケーション実行時における最適化設定として、以下のオプションを追加しました。

* Mapperごとにジョブの入力データサイズを判定し、データが小さい場合にMapperに対する入力スプリットを1つにまとめる: ``com.asakusafw.input.combine.tiny.limit``
* ジョブの入力データサイズを判定し、データが小さい場合に起動するReduceタスクを ``1`` に再設定する: ``com.asakusafw.reducer.tiny.limit``

実行するアプリケーションの特性に応じてこれらのオプションを有効にすることで、計算リソースの無駄遣いを抑制したり、タスク起動のオーバーヘッドを削減したりすることでアプリケーション実行時のパフォーマンスが向上する可能性があります。

詳しくは、 :doc:`administration/configure-hadoop-parameters` の上記設定項目の説明を参照してください。

対応プラットフォームのアップデート
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

動作検証プラットフォームのHadoopディストリビューションに CDH5 [#]_ を追加しました。
また、Amazon EMR [#]_ など一部のHadoopディストリビューションの動作検証バージョンをアップデートしました。

Hadoop2系での動作については、MRv2(YARN)上でアプリケーションを実行した際に不適切な最適化が適用されることによる性能上の問題や、MRv1上でアプリケーションが正常に実行されないことがある不具合などを修正し、安定性を向上させています。

アプリケーション開発環境については、Ubuntu Desktop 14.04 [#]_  や Gradle 1.12 [#]_ など動作検証プラットフォームのアップデートを行いました。

対応プラットフォームの一覧は、 :doc:`product/target-platform` を参照してください。

..  attention::
    本バージョンでは、Hadoop2系の対応は試験的機能として提供されます。
    Hadoop2系の利用について詳しくは :doc:`administration/deployment-hadoop2` を参照してください。

..  [#] http://www.cloudera.co.jp/products-services/cdh/cdh.html
..  [#] http://aws.amazon.com/jp/elasticmapreduce/
..  [#] http://www.ubuntu.com/desktop
..  [#] http://www.gradle.org/

YAESSログの可視化
~~~~~~~~~~~~~~~~~

試験的機能として、YAESSの実行時ログからCSV形式のレポートファイルを生成するYAESS Log Analyzerツール を追加しました。
アプリケーションの実行時間の分析などに有用です。

詳しくは、 :doc:`application/yaess-log-visualization` を参照してください。

互換性に関して
--------------

本リリースでは過去バージョンとの互換性に関する特別な情報はありません。

過去バージョンからのマイグレーション情報については、以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.6.1
=============

Mar 19, 2014

`Asakusa Framework 0.6.1 documentation`_

..  _`Asakusa Framework 0.6.1 documentation`: http://docs.asakusafw.com/0.6.1/release/ja/html/index.html

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

過去バージョンからのマイグレーション情報については、以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.6.0
=============

Feb 17, 2014

`Asakusa Framework 0.6.0 documentation`_

..  _`Asakusa Framework 0.6.0 documentation`: http://docs.asakusafw.com/0.6.0/release/ja/html/index.html

.. contents::
   :local:
   :depth: 2
   :backlinks: none

新機能と主な変更点
------------------

標準のビルドシステムをGradleに移行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

バッチアプリケーションの開発で使用する標準のビルドツールを従来のバージョンで使用していたMavenからGradleに移行しました。

バージョン ``0.5.2`` から試験的に提供していた :doc:`Asakusa Gradle Plugin <application/gradle-plugin>` に対して多くの改善とバグフィックスを行い、これを標準機能に昇格しました。
また、Asakusa Frameworkのドキュメント全体をGradleを利用した説明に変更しています。

Gradleを使ったアプリケーション開発の詳細や、Mavenを利用しているアプリケーションプロジェクトをGradleを利用したプロジェクトに移行する方法などについては以下のドキュメントを参照してください。

* :doc:`application/gradle-plugin`

Mavenの利用について
^^^^^^^^^^^^^^^^^^^

本バージョン、およびAsakusa Framework ``0.6`` 系ではMavenを使ったアプリケーションの開発もサポートしています。

Asakusa Framework ``0.7`` 系以降の将来のバージョンで、Mavenによるアプリケーション開発を非推奨とすることを検討しています。

Shafu - Gradleプロジェクト用Eclipse Plugin
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

標準のビルドシステムをGradleに移行したことにあわせて、Gradleを利用するアプリケーションプロジェクトの開発をサポートするEclipseプラグイン「Shafu (車夫)」を公開しました。

* :jinrikisha:`Shafu - Asakusa Gradle Plug-in Helper for Eclipse - <shafu.html>`

Shafu はバッチアプリケーション開発にGradleを利用する際に、Eclipseから透過的にビルドツール上の操作を行えます。
Shafu を使うことで、ターミナル上でのビルドツールの操作が不要となり、Eclipse上でアプリケーション開発に必要なほとんどの作業を行うことができるようになります。

テストドライバにJavaオブジェクトによるテストデータ指定を追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

テストドライバに指定可能なテストデータの形式を従来のExcelとJson形式に加え、Javaオブジェクトの指定が可能になりました。

詳しくは、 :doc:`testing/user-guide` の「入力データと期待データをJavaで記述する」を参照してください。

アプリケーションビルド時のログを改善
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

DMDLのコンパイルやAsakusa DSLのコンパイル、テストドライバの実行時に出力されるログなどの出力内容を改善しました。

試験的機能(Sandbox)
--------------------

アプリケーションテスト用のエミュレーションモード
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

試験的機能として、アプリケーションテスト用のエミュレーションモードを公開しました。

エミュレーションモードでAsakusa DSLのテストを実行すると、Asakusa Frameworkが提供するラッパー機構を利用してHadoopの処理を実行します。

通常のテスト実行とは異なり、テストを実行しているプロセス内でほとんどの処理が行われるため、デバッグモードのブレークポイントなどを利用できるようになります。
また、カバレッジツールと連携して演算子メソッドのテストカバレッジを確認しやすくなります。

また、エミュレーションモードと連携したインテグレーションテスト用のツールとしてバッチテストランナーAPIを追加しました。

エミュレーションモードの詳細や利用方法などについては、以下のドキュメントを参照してください。

* :doc:`testing/emulation-mode`

入力データサイズに応じて自動的にローカルモードでジョブを実行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

試験的機能として、入力データサイズに応じて自動的にローカルモードでHadoopジョブを実行する実行時プラグインを公開しました。

このプラグインを利用することでバッチの実行にかかるHadoopのオーバーヘッドが適切に調整され、バッチ実行時間が改善する可能性があります。

現時点でこのプラグインは基本的な動作確認のみを行なっており、動作検証プラットフォームは Apache Hadoop 1.2.1 のみです。

利用方法は以下のREADMEを参照してください。

* https://github.com/asakusafw/asakusafw-sandbox/blob/0.6.0/asakusa-runtime-ext/README.md

互換性に関して
--------------

本リリースでは過去バージョンとの互換性に関する特別な情報はありません。

過去バージョンからのマイグレーション情報については、以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.5.3
=============

Dec 24, 2013

`Asakusa Framework 0.5.3 documentation`_

..  _`Asakusa Framework 0.5.3 documentation`: http://docs.asakusafw.com/0.5.3/release/ja/html/index.html

本リリースはAsakusa Frameworkの開発版リリースです。
主な変更内容は以下の通りです。

* Apache Hadoop 2.2.0 に試験的に対応

 * Hadoop2系の利用については、 :doc:`administration/deployment-hadoop2` を参照してください。

* JDK 7に対応

 * 開発環境におけるJDK 7の利用については、 :doc:`application/develop-with-jdk7` を参照してください。

* Hadoopディストリビューション、アプリケーション開発環境の動作検証プラットフォームをアップデート

 * :doc:`product/target-platform` を参照してください。

* DMDLコンパイラ, DSLコンパイラ, Direct I/O実行時のエラーメッセージを改善
* TestDriverのExcel 2007形式によるテストデータ定義に試験的に対応
* その他、多くの細かな機能改善、およびバグフィックス

Release 0.5.2
=============

Nov 20, 2013

`Asakusa Framework 0.5.2 documentation`_

..  _`Asakusa Framework 0.5.2 documentation`: http://docs.asakusafw.com/0.5.2/release/ja/html/index.html

本リリースはAsakusa Frameworkの開発版リリースです。
主な変更内容は以下の通りです。

* 試験的機能として、Gradleベースの新ビルドシステムを提供
   * 詳しくは、 :doc:`application/gradle-plugin` を参照してください。
* Direct I/O CSV, Direct I/O TSV(Sandbox) に入出力データの圧縮/解凍機能を追加
* その他、多くの細かな機能改善、およびバグフィックス。

Release 0.5.1
=============

Jul 26, 2013

`Asakusa Framework 0.5.1 documentation`_

..  _`Asakusa Framework 0.5.1 documentation`: http://docs.asakusafw.com/0.5.1/release/ja/html/index.html

本リリースはAsakusa Frameworkの開発版リリースです。
主な変更内容は以下の通りです。

* テストドライバに演算子のトレースログを出力する機構を追加。
* アプリケーション依存ライブラリの管理方法を改善。
* DMDLコンパイラの日本語メッセージリソースを追加。
* その他、多くの細かな機能改善、およびバグフィックス。

Release 0.5.0
=============

May 9, 2013

`Asakusa Framework 0.5.0 documentation`_

..  _`Asakusa Framework 0.5.0 documentation`: http://docs.asakusafw.com/0.5.0/release/ja/html/index.html

本リリースはAsakusa Frameworkの開発版リリースです。
主な変更内容は以下の通りです。

* 試験的にCDH4に対応 [#]_ 。またいくつかの動作検証プラットフォームの追加。
* フレームワーク本体とバッチアプリケーションの構成情報を分離し、バッチアプリケーションの構成定義をシンプル化。
* 今後のAsakusa Frameworkの拡張のベースとなるFramework本体に対する多くのリファインメント。
* その他、多くの細かな機能改善、およびバグフィックス。

..  [#] CDH4上でAsakusa Frameworkを利用するためのドキュメントを、 Sandboxプロジェクトに公開しています。

* `Asakusa Framework Sandbox - CDH4上でAsakusa Frameworkを利用する`_

..  _`Asakusa Framework Sandbox - CDH4上でAsakusa Frameworkを利用する`: http://docs.asakusafw.com/sandbox/ja/html/administration/asakusa-on-cdh4.html

Release 0.4.0
=============

Aug 30, 2012

`Asakusa Framework 0.4.0 documentation`_

..  _`Asakusa Framework 0.4.0 documentation`: http://docs.asakusafw.com/0.4.0/release/ja/html/index.html

本リリースはAsakusa Frameworkの安定版リリースです。
主な変更内容は以下の通りです。

* [Direct I/O] ワイルドカード指定の出力機能などを追加し、試験的機能から正式機能として昇格。
* [Asakusa DSL] コンパイラ最適化のチューニングおよびバグフィックス。
* [YAESS] シミュレーションモードの実行やデプロイモジュールのバージョン検証機能などを追加。
* 動作検証プラットフォームの追加。
* 広範囲にわたるドキュメントの拡充と改善、および多くのドキュメントバグのフィックス。
* その他、多くの細かな機能改善、およびバグフィックス。

Release 0.2.6
=============

May 31, 2012

`Asakusa Framework 0.2.6 documentation`_

..  _`Asakusa Framework 0.2.6 documentation`: http://docs.asakusafw.com/0.2/release/ja/html/index.html

本リリースではYAESS マルチディスパッチ機能が追加されました。

これによりバッチやジョブフローなどを異なる複数のHadoopクラスタに振り分けて実行したり、それぞれ異なる設定で起動したりできるようになります。

* :doc:`yaess/multi-dispatch`

その他、多数の機能改善やバグフィックスが行われています。 

Release 0.2.5
=============

Jan 31, 2012

本リリースでは試験的な機能として「Direct I/O」が追加されました。
これは、Hadoopクラスターから直接バッチの入出力データを読み書きするための機構です。

* :doc:`directio/index`

また、本バージョンでは対応プラットフォームの拡張として、従来のバージョンで対応していたHadoopディストリビューションであるCDH3に加えて、Apache Hadoop 0.20.203.0での動作検証が行われ、この環境で動作するための変更が行われています。

その他、細かな機能改善やバグフィックスが行われています。

Release 0.2.4
=============

Dec 19, 2011

本リリースからWindGateがGA (Generally Available) となりました。
WindGateにはローカルのCSVに対するデータ入出力を行う機能が追加となっています。

また、本リリースではドキュメントの構成を見直し、内容を大幅に拡充しました。
特に「Asakusa Framework入門」の追加、およびWindGateやYAESSに関する記述が多く追加されています。

* :doc:`introduction/index`

旧バージョンを使っている開発環境を0.2.4に移行するにはマイグレーション作業が必要となります。
詳しくは以下のマイグレーションガイドを参照してください。

* :doc:`application/migration-guide`

その他、細かな機能改善やバグフィックスが行われています。

Release 0.2.3
=============

Nov 16, 2011

本リリースでは、様々な環境に合わせて実行方法をカスタマイズすることが可能なバッチ実行ツール「YAESS」とThunderGateの差分インポート機能を実現する「ThunderGateキャッシュ」機能が追加されました。

* :doc:`yaess/index`
* :doc:`thundergate/cache`

今回のリリースでは、旧バージョンを使っている開発環境を0.2.3に移行するためにマイグレーション作業が必要となります。
詳しくは以下のマイグレーションガイドを参照してください。

* :doc:`application/migration-guide`

その他、細かな機能改善やバグフィックスが行われています。

Release 0.2.2
=============

Sep 29, 2011

本リリースではExperimental Featureとして「WindGate」が追加されました。

WindGateはThunderGateと同様にバッチに対するデータの外部入出力を行うモジュールですが、様々なプラットフォームに対応するよう設計され、ThunderGateに対してポータビリティが高いことが特徴です。

* :doc:`windgate/index`

その他、バグフィックスや細かい機能改善が行われています。

Release 0.2.1
=============

Jul 27, 2011

* Extract演算子の追加
* Restructure演算子の追加
* ThunderGateのCLOBサポート
* その他バグフィックス

Release 0.2.0
=============

Jun 29, 2011

* DMDLの導入
* テストドライバの大幅な改善
* その他多くのバグフィックス

Release 0.1.0
=============
Mar 30, 2011

* 初版リリース

