============================
レガシーモジュール利用ガイド
============================
この文書では、Asakusa Framework の標準構成に含まれなくなった機能であるレガシーモジュールについて説明します。

レガシーモジュールについて
==========================
Asakusa Frameworkのバージョンアップに伴う各機能の機能整理により、Asakusa Framework のバージョンアップにより標準の構成に含まれなくなった機能を *レガシーモジュール* と呼びます。これらの機能は主に、それらの機能のリリース後により洗練された新機能がリリースされたために、新機能を利用することを推奨するものです。

レガシーモジュールは今後のメンテナンスは基本的に行われず、将来のバージョンで利用出来なくなる可能性があるため、後述する新機能へのマイグレーション方針に従ってマイグレーションを行うことを推奨します。

レガシーモジュール一覧
======================
Asakusa Frameworkのバージョン |version| でレガシーモジュールとして位置づけられている機能(DSL, API, 及びツール群)は以下の通りです。

Asakusa DSL
-----------
* ``FileImporterDescription`` [#]_
* ``FileExporterDescription`` [#]_

..  [#] :javadoc:`com.asakusafw.vocabulary.external.FileImporterDescription`
..  [#] :javadoc:`com.asakusafw.vocabulary.external.FileExporterDescription`

TestDriver
----------
* ``BatchTestDriver`` [#]_
* ``JobFlowTestDriver`` [#]_
* ``FlowPartTestDriver`` [#]_

..  [#] :javadoc:`com.asakusafw.testdriver.BatchTestDriver`
..  [#] :javadoc:`com.asakusafw.testdriver.JobFlowTestDriver`
..  [#] :javadoc:`com.asakusafw.testdriver.FlowPartTestDriver`

外部システム連携
----------------
* :doc:`ThunderGate <../thundergate/index>`

ワークフロー定義
----------------
* Experimental shell script


運用ツール
----------
* Asakusa Cleaner [#]_

..  [#] :doc:`../administration/deployment-cleaner`

レガシーモジュールのマイグレーションパス
========================================
レガシーモジュールのマイグレーションパスについて説明します。

Asakusa DSL
-----------
``FileImporterDescription``, 及び ``FileExporterDescription`` はローカルファイルに対するインポート/エクスポートを行うためのFlow DSLです。

これらのFlow DSLは非公開インターフェースとして存在しており、アプリケーション開発者が利用することを想定していないものですが、旧バージョンではローカルファイルに対するインポート/エクスポートする唯一の手段として利用されることがありました。

ローカルファイルに対して直接インポート/エクスポートする機能は :doc:`../directio/index` によって提供されています。Direct I/O はバージョン0.2では実験的機能として提供していましたが、バージョン0.4.0からは正式な機能として提供されました。バージョン0.4以降は Direct I/O を利用することを強く推奨します。

TestDriver
----------
``BatchTestDriver``, ``JobFlowTestDriver``, 及び ``FlowPartTestDriver`` はAsakusa DSLのテストを記述するためのTestDriver APIです。

これらのTestDriver APIはThunderGateと組み合わせて利用することのみを想定しており、 WindGate や Direct I/O と組み合わせて利用することが出来ません。

バージョン0.2.0から :doc:`../testing/index` で説明されている新TestDriver APIが提供されました。新TestDriver APIはAsakusa Frameworkのすべての外部連携モジュールに対応しており、また複数のテストデータの入出力方式への対応や入出力データ方法のカスタマイズ、テストデータの検証方法のカスタマイズが可能であったりと、より柔軟で豊富な機能を提供しています。バージョン0.4以降は新TestDriver APIを利用することを強く推奨します。

外部システム連携
----------------
ThunderGateは「オンラインシステムのRDBMSとHadoopの連携」を念頭に置いたデータ転送ツールです。詳細は、 :doc:`../thundergate/index` を参照してください。

ThunderGateは対応プラットフォームが限定的であることや、利用するための前提条件や制約が非常に厳しいため、通常の場合、外部システム連携を行う機能として :doc:`../directio/index` や :doc:`../windgate/index` の利用を検討してください。

ワークフロー定義
----------------
Experimental shell script は Asakusa DSLをバッチコンパイルすることによって生成される、バッチアプリケーション実行用スクリプトです。

Experimental shell script は 簡易的にアプリケーションをテストするためのスクリプトとして、非常にシンプルな機能を提供していましたが、環境に依存したスクリプトが生成されるため運用環境のシステム構成に柔軟に対応できないなど、運用環境で利用する点では多くの問題がありました。

バージョン0.2.3から、環境に合わせて実行方法を柔軟にカスタマイズできるバッチ実行ツール :doc:`../yaess/index` が提供されました。YAESSは設定ベースで実行環境に合わせたバッチアプリケーションの実行を可能にします。バージョン0.4以降は YAESS を利用することを強く推奨します。

運用ツール
----------
Asakusa Cleaner はローカルファイル、及びHadoopクラスター上の分散ファイルシステム(HDFS等)上のファイルをクリーニングするためのコマンドツールです。

Asakusa Cleaner はファイルパスの解決方法に問題があるため、Hadoopファイルシステムを扱うために追加のライブラリが必要になるHadoopディストリビューション [#]_ に対応できていないなどの問題があります。

バージョン0.4.0から :doc:`../administration/utility-tool-user-guide` で説明されている ``hadoop-fs-clean.sh`` が提供されました。このコマンドはローカルにインストールされているHadoopクラスターの設定を参照し、その設定に基づいてHadoopファイルシステムに対するファイルをクリーニングする機能を提供しています。バージョン0.4以降は ``hadoop-fs-clean.sh`` を利用することを強く推奨します。

..  [#] バージョン |version| 時点では、MapRで提供されるMapRFS上で正常に動作しないことが確認されています。

レガシーモジュールの利用方法
============================
レガシーモジュールを利用する方法について説明します。

Asakusa DSL
-----------
``FileImporterDescription`` , 及び ``FileExporterDescription`` を利用する場合は、 アプリケーションプロジェクトの ``pom.xml`` に対して、以下のdependencyを追加してください。

..  code-block:: xml

        <dependency>
            <groupId>com.asakusafw</groupId>
            <artifactId>asakusa-fileio-vocabulary</artifactId>
            <version>${asakusafw.version}</version>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.asakusafw</groupId>
            <artifactId>asakusa-fileio-plugin</artifactId>
            <version>${asakusafw.version}</version>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>com.asakusafw</groupId>
            <artifactId>asakusa-fileio-test-moderator</artifactId>
            <version>${asakusafw.version}</version>
            <scope>test</scope>
        </dependency>

Eclipse で開発を行っている場合は、 ``pom.xml`` を編集後、  ``mvn eclipse:eclipse`` を実行し、上記のモジュールをEclipseのクラスパス定義ファイルに追加します。

TestDriver
----------
``BatchTestDriver``, ``JobFlowTestDriver``, 及び ``FlowPartTestDriver`` を利用する場合は、 アプリケーションプロジェクトの ``pom.xml`` に対して、以下のdependencyを追加してください。

..  code-block:: xml

        <dependency>
            <groupId>com.asakusafw</groupId>
            <artifactId>asakusa-legacy-test-driver</artifactId>
            <version>${asakusafw.version}</version>
            <scope>test</scope>
        </dependency>

Eclipse で開発を行っている場合は、 ``pom.xml`` を編集後、  ``mvn eclipse:eclipse`` を実行し、上記のモジュールをEclipseのクラスパス定義ファイルに追加します。

外部システム連携
----------------
ThunderGateを利用するための方法は、 :doc:`../thundergate/index` を参照してください。

また、 ThunderGateをGradleから利用するための設定については、 :doc:`../application/gradle-plugin-deprecated` を参照してください。

ワークフロー定義
----------------
Experimental shell script を利用する場合は、 以下の2つの対応を行います。

1. アプリケーションプロジェクトの ``pom.xml`` に  Experimental shell script を生成するコンパイラプラグインを追加する
2. 実行環境(開発環境、及び運用環境)に Experimental shell script 用の拡張モジュールをデプロイする

Experimental shell script 生成用のコンパイラプラグインを追加する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アプリケーションプロジェクトの ``pom.xml`` に対して、以下のdependencyを追加してください。

..  code-block:: xml

        <dependency>
            <groupId>com.asakusafw</groupId>
            <artifactId>asakusa-legacy-workflow-plugin</artifactId>
            <version>${asakusafw.version}</version>
            <optional>true</optional>
        </dependency>


上記の定義を追加した状態でアプリケーションのバッチコンパイルを行うと、バッチアプリケーション用アーカイブを展開したディレクトリの ``<バッチID>/bin`` 配下に ``experimental.sh`` が生成されます。

実行環境に Experimental shell script 用の拡張モジュールをデプロイする
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
実行環境に Experimental shell script 用の拡張モジュール ``ext-experimental-shell-script`` をデプロイします。

拡張モジュールのデプロイ方法については、 :doc:`../administration/deployment-extension-module` を参照してください。


運用ツール
----------
Asakusa Cleaner はバージョン0.2までは アプリケーションプロジェクトに対して ``mvn assembly:single`` を実行すると Asakusa Cleaner用のデプロイアーカイブが作成されましたが、バージョン0.4からは標準ではこのデプロイアーカイブは作成されません。

Asakusa Cleaner を利用する場合は、 :doc:`../administration/deployment-cleaner` の手順に従ってデプロイを行なってください。

