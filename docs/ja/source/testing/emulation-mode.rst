==================================================
エミュレーションモードによるアプリケーションテスト
==================================================

この文書では、エミュレーションモードを利用したアプリケーションのテストについて説明します。

エミュレーションモード
======================
エミュレーションモードを有効にしてAsakusa DSLのテストを実行すると、Asakusa Frameworkが提供するラッパー機構を利用してHadoopの処理を実行します。

エミュレーションモードではテストを実行しているプロセス内でほとんどの処理が行われ、デバッグモードのブレークポイントなどを利用できるようになります。

さらに、Asakusa Framework バージョン ``0.7.1`` から追加されたスモールジョブ実行エンジンを併用することで、Hadoopジョブの実行を高速化し、テスト実行に要する時間を短縮できます。

..  attention::
    本機能はアプリケーションプロジェクトのHadoopライブラリを利用するため、標準で設定されたものと異なるHadoopディストリビューションやバージョンを利用する際に、正しく動かない可能性があります。

..  note::
    エミュレーションモードを有効にしていない場合、テスト実行時にコマンドラインインターフェースを利用して、別プロセスで様々な処理を行います。
    
    別プロセスで動作させたほうが Java VM 上の問題は起こりにくくなるため、
    エミュレーションモード利用時に正しく動かない場合には、本機能を無効化して動作を確認してみることを推奨します。

フローDSL/バッチDSLのテスト
---------------------------
エミュレーションモードを有効にして :ref:`データフローのテスト <testing-userguide-dataflow-test>` を行うと、IDEからブレークポイントを指定してデータフロー内の演算子メソッドの動作を確認したり、カバレッジツールと連携して演算子メソッドのテストカバレッジを確認しやすくなります。

インテグレーションテスト
------------------------
:ref:`バッチテストランナー <testing-userguide-integration-test>` とエミュレーションモードを併用すると、インテグレーションテスト時のデバッグ作業がやりやすくなります。

エミュレーションモードの利用方法
================================

エミュレーションモードで利用するモジュール
------------------------------------------
エミュレーションモード用のモジュールはAsakusa FrameworkのMavenリポジトリに以下の内容で登録されています。

..  list-table:: エミュレーションモードで使用するMavenアーティファクト
    :widths: 2 4 5
    :header-rows: 1

    * - グループID
      - アーティファクトID
      - 説明
    * - ``com.asakusafw``
      - ``asakusa-test-inprocess``
      - テストドライバ実行をエミュレーションモードに変更
    * - ``com.asakusafw``
      - ``asakusa-test-inprocess-ext``
      - スモールジョブ実行エンジンを利用したエミュレーションモードで実行 (Experimental)
    * - ``com.asakusafw``
      - ``asakusa-windgate-test-inprocess``
      - :doc:`WindGate <../windgate/index>` をエミュレーションモードで実行

アプリケーションプロジェクトの設定
----------------------------------

エミュレーションモードの有効化
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アプリケーションプロジェクトでエミュレーションモードを使用する場合は
``build.gradle`` の ``dependencies`` ブロック内に
``asakusa-test-inprocess`` を利用する依存定義を追加します。

..  code-block:: groovy

    dependencies {
        ...
        testRuntime group: 'com.asakusafw', name: 'asakusa-test-inprocess', version: asakusafw.asakusafwVersion

スモールジョブ実行エンジンを利用したエミュレーションモードの有効化
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
..  attention::
    Asakusa Framework バージョン |version| では、スモールジョブ実行エンジンを利用したエミュレーションモード実行は試験的機能として提供しています。

Gradleプロジェクトでスモールジョブ実行エンジンを利用したエミュレーションモードを使用する場合は
``build.gradle`` の ``dependencies`` ブロック内に
``asakusa-test-inprocess-ext`` を利用する依存定義を追加します [#]_ 。

..  code-block:: groovy

    dependencies {
        ...
        testRuntime group: 'com.asakusafw', name: 'asakusa-test-inprocess-ext', version: asakusafw.asakusafwVersion

..  [#] ``asakusa-test-inprocess-ext`` を利用する場合、 ``asakusa-test-inprocess`` の定義は不要です。

WindGateのエミュレーションモードの有効化
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
WindGateをエミュレーションモードで実行する場合は
``build.gradle`` の ``dependencies`` ブロック内に
``asakusa-windgate-test-inprocess`` を利用する依存定義を追加します。

..  code-block:: groovy

    dependencies {
        ...
        testRuntime group: 'com.asakusafw', name: 'asakusa-windgate-test-inprocess', version: asakusafw.asakusafwVersion

..  attention::
    WindGate-JDBCを利用するジョブフローをテストする場合は、
    利用するJDBCドライバをテスト実行時のクラスパスに追加する必要があります。
     
    上記の設定を行わない場合、テスト実行時にクラスロードに関する問題が発生する可能性があります。
    一例として、テストを連続で実行した場合にOutOfMemoryErrorが発生する可能性があります。

Gradle上でのテストドライバ実行
------------------------------
`アプリケーションプロジェクトの設定`_ を行った状態で Gradleの ``test`` タスクを実行すると、
テストドライバがエミュレーションモードで実行されます。

..  tip::
    エミュレーションモードを有効にして Gradle上でテストを実行すると、
    Gradleが提供する `JaCoCo Plugin <http://www.gradle.org/docs/current/userguide/jacoco_plugin.html>`_ などの
    ソースコードカバレッジ取得機能との連携が可能になります。

Eclipse上でのテストドライバ実行
-------------------------------
`アプリケーションプロジェクトの設定`_ を行った状態で Gradleの ``eclipse`` タスクを実行すると、
Eclipse上でアプリケーションプロジェクトに対してエミュレーションモードが有効になります。

この状態でEclipseからテストドライバを利用するテストクラスや、バッチテストランナーを実行すると、
テストドライバがエミュレーションモードで実行されます。

..  tip::
    エミュレーションモードを有効にすると、
    テストドライバを使ったテストクラスのデバッグ実行時に
    Eclipseのブレークポイント機能などを利用できます。
