=============================
SDKアーティファクト利用ガイド
=============================

この文書では、アプリケーション開発プロジェクト用の依存性定義を行うためのSDKアーティファクトについて説明します。

SDKアーティファクト概要
=======================

Asakusa Frameworkではアプリケーションプロジェクトで使用するAsakusa Frameworkのライブラリをグループ化した「SDKアーティファクト」を提供しています。

SDkアーティファクトを利用することで、アプリケーションプロジェクトに対してライブラリの依存性定義をシンプルに過不足なく行うことを支援します。
またAsakusa Frameworkのバージョンアップなどに伴うマイグレーション作業を容易にします。

アプリケーションプロジェクトのビルド定義で指定するライブラリにはSDKアーティファクトを使用することを推奨します。

SDKアーティファクト一覧
=======================

SDKアーティファクトはAsakusa FrameworkのMavenリポジトリにグループID ``com.asakusafw.sdk`` を持つMavenアーティファクトとして登録されています。

Asakusa Frameworkが提供するSDKアーティファクトは以下のものがあります。

..  list-table:: Asakusa Framework SDKアーティファクト一覧
    :widths: 30 20 50
    :header-rows: 1

    * - アーティファクトID
      - 導入バージョン
      - 説明
    * - ``asakusa-sdk-core``
      - 0.5.0
      - すべてのアプリケーション開発プロジェクトで共通的に必要となるライブラリをまとめたアーティファクト
    * - ``asakusa-sdk-windgate``
      - 0.5.0
      - :doc:`Windgate <../windgate/index>` を使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト
    * - ``asakusa-sdk-thundergate``
      - 0.5.0
      - :doc:`Thundergate <../thundergate/index>` を使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト
    * - ``asakusa-sdk-directio``
      - 0.5.0
      - :doc:`Direct I/O <../directio/index>` を使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト
    * - ``asakusa-sdk-hive``
      - 0.7.0
      - :doc:`Direct I/O Hive <../directio/using-hive>` を使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト
    * - ``asakusa-sdk-test-emulation``
      - 0.7.2
      - :doc:`エミュレーションモード <../testing/emulation-mode>` を使用するアプリケーション開発プロジェクトで必要となるライブラリをまとめたアーティファクト

SDKアーティファクトの利用方法
=============================

:doc:`gradle-plugin` などの手順で構築したGradleプロジェクトでSDKアーティファクトを使用する場合は、ビルドスクリプトの ``dependencies`` ブロック内にSDKアーティファクトの定義を追加します。

以下、SDKアーティファクトを利用したビルドスクリプトの設定例です。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-sdk-artifact-1

    dependencies {
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-core', version: asakusafw.asakusafwVersion
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-directio', version: asakusafw.asakusafwVersion
        compile group: 'com.asakusafw.sdk', name: 'asakusa-sdk-windgate', version: asakusafw.asakusafwVersion
        testRuntime group: 'com.asakusafw.sdk', name: 'asakusa-sdk-test-emulation', version: asakusafw.asakusafwVersion
    }