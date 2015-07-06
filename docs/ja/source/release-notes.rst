==============
リリースノート
==============

Release 0.7.3
=============

Apr 22, 2015

`Asakusa Framework 0.7.3 documentation`_

..  _`Asakusa Framework 0.7.3 documentation`: http://docs.asakusafw.com/0.7.3/release/ja/html/index.html

新機能と主な変更点
------------------

Direct I/O 入力フィルター
~~~~~~~~~~~~~~~~~~~~~~~~~

Direct I/O を利用してファイルからデータを読み出す際に、ファイル単位やレコード単位で読み出すデータを制限する機能を追加。

入力フィルターを使うことで、従来のDirect I/Oの機能では記述しきれないような複雑な絞り込みを行えるようになります。
バッチの実行パラメータと組み合わせることで、処理対象のデータを動的に制限することも可能です。
また、フィルターによるデータの絞り込みをDSLコンパイラの最適化設定と組み合わせることで、アプリケーションの高速化が望めます。

Direct I/O 入力フィルターが提供する機能には以下のようなものがあります。

パスフィルターメソッド
  入力の候補となるファイルパスに対して、個別に処理を行うかを決定するフィルタールールを記述します。

データフィルターメソッド
  入力の候補となるデータモデルの内容に基づいて、個別に処理を行うかを決定するフィルタールールを記述します。

Direct I/O 入力フィルターについての詳細は、以下のドキュメントを参照してください。

* :doc:`directio/user-guide` - :ref:`directio-input-filter`

スモールジョブ実行エンジンの正式対応
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Asakusa Framework バージョン 0.7.1 ( `Release 0.7.1`_ ) で試験的機能として追加されたスモールジョブ実行エンジンに正式に対応しました。

正式対応に伴い、 :doc:`testing/emulation-mode` で利用するための設定方法が変更になっています。
従来の設定で利用している環境も当面は引き続き利用可能ですが、できるだけ新しい設定方法を利用するようにしてください。

サポートプラットフォームの更新
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

いくつかのプラットフォームの対応バージョンを更新しています。

* Apache Hadoop 2.6.0
* Apache Hive 1.1.0
* MapR 4.1.0

詳しくは、 :doc:`product/target-platform` を参照してください。

その他の変更点
~~~~~~~~~~~~~~

* WindGate/JDBCでTRUNCATE時のクエリーをジョブフロー単位で指定可能になりました。
* Direct I/O及びWindGateでCSVファイルの入力時にヘッダーの検証をスキップするオプションを追加しました。
* テストドライバを利用したインテグレーションテスト用のAPIを追加しました。
* 多相データフロー向けのコア演算子用APIを拡張しました。
* ドキュメントの構成を改善しました。

その他、細かな機能改善およびバグフィックスが含まれます。
すべての変更点は :doc:`changelogs` を参照してください。

互換性に関して
--------------

Java SE Development Kit (JDK)
  本バージョンからJDK6の利用は非推奨となりました。
  開発環境、運用環境共にJDK7を利用してください。

  Asakusa Frameworkが動作検証を行なっているJavaのバージョンについては、 :doc:`product/target-platform` を参照してください。
  また、開発環境で利用するJavaについての詳細は、 :doc:`application/using-jdk` を参照してください。

  なお、将来のバージョンではJDK6の利用は非対応とすることを計画しています。
  
過去バージョンからのマイグレーション情報については、以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.7.2
=============

Jan 05, 2015

`Asakusa Framework 0.7.2 documentation`_

..  _`Asakusa Framework 0.7.2 documentation`: http://docs.asakusafw.com/0.7.2/release/ja/html/index.html

新機能と主な変更点
------------------

Windows上でのアプリケーション開発に対応
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

アプリケーション開発環境の対応プラットフォームとしてWindowsを追加しました。

:doc:`introduction/start-guide-windows` ではWindows上にアプリケーション開発環境を構築し、サンプルアプリケーションを例に開発環境を利用する方法を紹介しています。

Windowsでは運用機能に制限がありますが、アプリケーションの開発、テスト、ビルド機能のほぼすべてを利用することができます。

Hive 0.14に対応
~~~~~~~~~~~~~~~

:doc:`Direct I/O Hive <directio/using-hive>` がHive 0.14.0に対応しました。

Direct I/O Hiveが出力するParquetフォーマットで ``TIMESTAMP`` や ``DECIMAL`` 型などHive 0.14で新たに対応したデータタイプを利用することができるようになりました。

その他の変更点
~~~~~~~~~~~~~~

その他、細かな機能改善およびバグフィックスが含まれます。
すべての変更点は :doc:`changelogs` を参照してください。

互換性に関して
--------------

本リリースでは過去バージョンとの互換性に関する特別な情報はありません。

過去バージョンからのマイグレーション情報については、以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.7.1
=============

Nov 20, 2014

`Asakusa Framework 0.7.1 documentation`_

..  _`Asakusa Framework 0.7.1 documentation`: http://docs.asakusafw.com/0.7.1/release/ja/html/index.html

新機能と主な変更点
------------------

小さなデータの処理性能を改善 (Experimental)
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

10MB程度の非常に小さなデータを処理するステージについて、実行性能を向上させる「スモールジョブ実行エンジン」を追加しました。

バッチアプリケーション内に小さなデータを処理するステージが多数含まれる場合、この機能を有効にすると性能が改善する場合があります。

また、常に小さなデータを利用する開発環境上のテスト実行では、この機能と :doc:`エミュレーションモード <testing/emulation-mode>` を組み合わせて利用することで、テストの実行時間を大幅に短縮できます。

運用環境で本機能を有効にするには、以下を参照してください。

* :doc:`administration/configure-task-optimization`

開発環境で本機能を有効にするには、以下を参照してください。

* :doc:`testing/emulation-mode`

サポートプラットフォームを追加
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

対応プラットフォームにHortonworks Data Platform 2.1を追加しました。

その他、いくつかのプラットフォームの対応バージョンを更新しています。

詳しくは、 :doc:`product/target-platform` を参照してください。

その他の変更点
~~~~~~~~~~~~~~

その他、細かな機能改善およびバグフィックスが含まれます。
すべての変更点は :doc:`changelogs` を参照してください。

互換性に関して
--------------

本リリースでは過去バージョンとの互換性に関する特別な情報はありません。

過去バージョンからのマイグレーション情報については、以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

Release 0.7.0
=============

Sep 25, 2014

`Asakusa Framework 0.7.0 documentation`_

..  _`Asakusa Framework 0.7.0 documentation`: http://docs.asakusafw.com/0.7.0/release/ja/html/index.html

新機能と主な変更点
------------------

Direct I/O Hive
~~~~~~~~~~~~~~~

`Apache Hive <https://hive.apache.org/>`_ で利用されるいくつかのファイルフォーマットをDirect I/Oで直接取り扱えるようになりました。
これにより、Apache Hiveのテーブルデータをアプリケーションから直接作成できるようになります。

本フィーチャーには主に以下の改善が含まれています。

Parquet / ORCFile フォーマット
  さまざまなクエリーエンジンがサポートしている、ParquetとORCFileフォーマットをDirect I/Oから読み書きできるようになりました。
DMDL上での各種フォーマットのサポート
  DMDLから各種Hive対応フォーマット向けのDataFormatクラスを自動生成できるようになりました。

  また、上記の方法で作成したデータモデルから、Hive Metastore向けにDDLスクリプトを自動生成できるようになりました。

Direct I/O Hiveについて詳しくは、以下のドキュメントを参照してください。

* :doc:`directio/using-hive`

Hadoop2系に正式対応
~~~~~~~~~~~~~~~~~~~

従来のHadoop1系に加え、以前のバージョンから試験的機能として対応していたHadoop2系に本バージョンから正式に対応しました。
これにより、最新のHadoopディストリビューション上でAsakusa Frameworkのアプリケーションを安全に実行できます。

なお、正式にサポートするHadoopのバージョンラインが複数になったことにより、Asakusa Frameworkのバージョン体系もそれに合わせて変化しています。詳しくは以下のドキュメントを参照してください。

* :doc:`application/migration-guide` - :ref:`versioning-sysytem-changing`

本バージョンより、Gradleを利用したビルドシステムにおいて、開発環境や様々な運用環境で異なるHadoopのバージョンラインを使い分けられるようになりました。
利用方法については以下のドキュメントを参照してください。

* :doc:`application/gradle-plugin` - :ref:`gradle-plugin-oraganizer-profile`
* :doc:`administration/deployment-guide`

テストドライバの改善
~~~~~~~~~~~~~~~~~~~~

テストドライバに以下の改善が加えられています。

Excelの数式をサポート
  Excelによるテストデータ定義において、セルに数式を指定できるようになりました。これにより、より柔軟な方法でテストデータの定義を行えるようになります。
いくつかの比較形式を追加
  Excelによるテストデータ定義において、誤差を許す比較や、大小比較をサポートしました。

  本機能を利用する場合、新しいバージョンのテストデータテンプレートが必要になります。Excelのテストデータテンプレートを再生成してください。
テストデータの事前検証
  テストデータやテスト条件に形式的な問題がある場合、Asakusa DSLのコンパイルやHadoop上での実行に先立ってエラーが報告されるようになりました。

Excelによるテストデータ定義に関して詳しくは、以下のドキュメントを参照してください。

* :doc:`testing/using-excel`

実行時パフォーマンスの改善
~~~~~~~~~~~~~~~~~~~~~~~~~~

以下の機能により、全体的なパフォーマンス改善が加えられています。

ライブラリファイルのキャッシュ
  フレームワークやアプリケーションのライブラリファイル群をHadoop上にキャッシュして再利用できるようになりました。
ステージ間の新しい中間データ形式
  中間データに独自の形式を利用するようになりました。また、中間データの入出力をマルチコアプロセッサー向けに改善しました。
Mapタスクのスケジューリングを改善
  Mapタスクの結合を行う遺伝的アルゴリズムを見直し、よりデータローカリティを重視するようになりました。

これらの機能に関する設定など詳しくは、以下のドキュメントを参照してください。

* :doc:`administration/configure-library-cache`
* :doc:`administration/configure-task-optimization`

----

| その他、 :doc:`product/target-platform` のアップデートや細かな機能改善およびバグフィックスが含まれます。
| すべての変更点は :doc:`changelogs` を参照してください。

互換性に関して
--------------

本リリースには、過去のリリースに対していくつかの潜在的な非互換性が存在します。

Java SE Development Kit (JDK)
  アプリケーションプロジェクトの標準設定で利用するJavaのバージョンをJDK 6からJDK 7に変更しました。

  Java 7に対応していないHadoopディストリビューション上でアプリケーションを実行する場合、手動でJDK 6に戻す必要があります。
Gradle
  Gradle 2.1に対応しました。

  以前のAsakusa FrameworkはGradle 2.0以降に対応していません。プロジェクトのAsakusa Frameworkのバージョンをダウングレードする場合に注意が必要です。
Maven
  本バージョンより非推奨となりました。当面は引き続き利用可能ですが、できるだけGradleを利用するようにしてください。

  マイグレーション手順については :doc:`application/gradle-plugin` - :ref:`migrate-from-maven-to-gradle` を参照してください。
Framework Organizer Plugin (Gradle)
  新機能の追加に伴い、いくつかのタスクが非推奨/利用不可能になりました。

  詳しくは、 :doc:`application/gradle-plugin-deprecated` を参照してください。

過去バージョンからのマイグレーション情報については、以下のドキュメントを参照してください。

* :doc:`application/migration-guide`
* :doc:`administration/migration-guide`

..  warning::
    バージョン 0.7.0 は以前のバージョンからいくつかの重要な変更が行われました。 
    過去のバージョンからのマイグレーションを検討する際には必ず :doc:`application/migration-guide` の内容を確認してください。

リンク
======

* :doc:`previous-release-notes`
* :doc:`changelogs`

