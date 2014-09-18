================================
Asakusa Gradle Plugin:非推奨機能
================================

この文書では、 :doc:`gradle-plugin` で提供する機能のうち、
Asakusa Framework バージョン |version| 時点で
非推奨となっっている機能の説明をまとめています。

ここで紹介する機能の多くは
バージョン |version| では正常に動作しますが、
将来のバージョンでこれらの機能が使用できなくなる可能性があります。

また、いくつかの機能はすでに使用できなくなっています。
そのような機能については個別に注意書きで説明しています。

基本的なプラグインの使用方法
============================

Asakusa Frameworkのデプロイメントアーカイブ生成
-----------------------------------------------

..  attention::
    :doc:`gradle-plugin` では ``assembleAsakusafw`` の代わりに ``assemble`` タスクの利用を紹介しています。
    バージョン |version| では 標準設定の状態で ``assemble`` タスクを実行すると、
    デプロイメントアーカイブが生成され、バッチアプリケーションが同梱されるようになっています。
    
    ``assembleAsakusafw`` タスクは
    バージョン |version| でも以前のバージョンと同様に使用することができます。

Asakusa Frameworkを運用環境にデプロイするためのデプロイメントアーカイブを生成します。

運用環境向けの標準的な構成を持つデプロイメントアーカイブを生成するには、 ``assembleAsakusafw`` タスクを実行します。

..  code-block:: sh

    ./gradlew assembleAsakusafw
    
``assembleAsakusafw`` タスクを実行すると、 ``build`` 配下に  ``asakusafw-${asakusafwVersion}.tar.gz`` という名前でデプロイメントアーカイブが作成されます。このアーカイブには Direct I/O , WindGateを含むAsakusa Framework実行環境一式が含まれます。

このデプロイメントアーカイブは運用環境上の$ASAKUSA_HOME配下に展開してデプロイします。より詳しくは、 :doc:`../administration/index` のデプロイメントガイドなどを参照してください。

..  attention::
    以降で説明する、デプロイメントアーカイブ構築のカスタマイズ方法について、
    バージョン 0.7.0 以降では、 ``attach`` から始まる各タスクを組み合わせる方式から
    ``build.gradle`` 内の ``asaksuafwOrganizer`` ブロックで
    デプロイメント構成を定義する方法を推奨するよう変更しています。
    
    詳しくは、 :doc:`../administration/deployment-guide` を参照してください。

バッチアプリケーションの同梱
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

``attachBatchapps`` タスクは、 ``build/batchc`` 配下に存在するバッチアプリケーションをデプロイメントアーカイブに含めます。以下は、バッチアプリケーションの生成してこれを含むデプロイメントアーカイブを生成する例です。

..  code-block:: sh

    ./gradlew clean compileBatchapp attachBatchapps assembleAsakusafw

このようにタスクを実行すると、バッチコンパイルを実行後に ``build`` 配下に  ``asakusafw-${asakusafwVersion}.tar.gz`` が生成され、このアーカイブの   ``batchapps`` 配下には ``compileBatchapp`` タスクによって ``build/batchc`` 配下に生成されたバッチアプリケーションの実行ファイル一式が含まれます。

設定ファイル/アプリケーションライブラリの同梱
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

デプロイメントアーカイブに、特定の運用環境向けの設定ファイルやアプリケーション用の追加ライブラリを含めることもできます。

この機能を使用するには、まずプロジェクトディレクトリの ``src/dist`` 配下に特定環境を示す名前（以下この名前を「ディストリビューション名」と呼びます）を持つディレクトリを生成します。このディレクトリは英数小文字のみ使用できます。

ディストリビューション名のディレクトリ配下に、 ``$ASAKUSA_HOME`` のディレクトリ構造と同じ形式で追加したい設定ファイルやライブラリファイルを配置します。このディレクトリ構成がデプロイメントアーカイブにそのまま追加されます。

以下は、 ``src/dist`` 配下に ``myenv`` というディストリビューション名を持つのディレクトリを作成し、これに アプリケーション向けの追加ライブラリとYAESS 向けの設定ファイル を配置した例です。

..  code-block:: sh

    src/dist
    └── myenv
        ├── ext
        │   └── lib
        │       └── joda-time-2.2.jar
        └── yaess
            └── conf
                └── yaess.properties

``src/dist`` 配下のディレクトリ構成をデプロイメントアーカイブに含めるには、  ``attachConf<ディストリビューション名>`` というタスクを実行します。タスク名の ``<ディストビューション名>`` 部分は ``src/dist`` 配下のディストリビューション名に対応し、これにマッチしたディレクトリ配下のファイル [#]_ をデプロイメントアーカイブに含めます。

タスク名の  ``<ディストリビューション名>``  部分は大文字/小文字の違いを無視します。例えば ``src/dist/myenv`` に対応するタスクは ``attachConfMyEnv`` にも対応します。

以下は、 ``src/dist/myenv`` 配下のファイルを含むデプロイメントアーカイブを生成する例です。

..  code-block:: sh

    ./gradlew attachConfMyEnv assembleAsakusafw
    

このようにタスクを実行すると、バッチコンパイルを実行後に ``build`` 配下に   ``asakusafw-${asakusafwVersion}.tar.gz`` が生成され、このアーカイブには  ``src/dist/myenv`` 以下のディレクトリ構造を含むファイル一式が含まれます。

..  [#]  ``.``  (ドット)から始まる名前を持つファイルやディレクトリは無視され、アーカイブに含まれません。

.. _include-extention-modules-gradle-plugin:

拡張モジュールの同梱
~~~~~~~~~~~~~~~~~~~~

Asakusa Frameworkでは、標準のデプロイメントアーカイブに含まれない追加機能を拡張モジュール [#]_ として提供しています。

拡張モジュールは Asakusa Framworkの標準的なデプロイ構成にプラグインライブラリを追加することで利用することができます。Asakusa Gradle Plugin ではデプロイメントアーカイブの生成時に拡張モジュール取得用のタスクを合わせて実行することで、デプロイメントアーカイブに拡張モジュールを含めることができます。

以下は、拡張モジュール ``asakusa-windgate-retryable`` をデプロイメントアーカイブに含める例です。

..  code-block:: sh

    ./gradlew attachExtensionWindGateRetryable assembleAsakusafw

このようにタスクを実行すると、 ``build`` 配下に  ``asakusafw-${asakusafwVersion}.tar.gz`` が生成され、このアーカイブには拡張モジュールが含まれた状態となります。今回の例では、アーカイブ内の  ``windgate/plugin`` 配下に ``asakusa-windgate-retryable`` 用のjarファイルが追加されています。

..  [#] 拡張モジュールについて、詳しくは  :doc:`../administration/deployment-extension-module` を参照してください。

組み合わせの例
~~~~~~~~~~~~~~

これまで説明した内容を組み合わせて利用すると、特定環境向けのリリース用デプロイメントアーカイブをビルド時に作成することができます。

以下は、リリースビルドを想定したデプロイメントアーカイブ生成の実行例です。

..  code-block:: sh

    ./gradlew clean build attachBatchapps attachConfMyEnv attachExtensionWindGateRetryable assembleAsakusafw
    
このようにタスクを実行すると、テスト済のバッチアプリケーションと設定ファイル、追加ライブラリ、拡張モジュールを含むデプロイメントアーカイブを生成します。

Asakusa Gradle Plugin リファレンス
==================================

Framework Organizer Plugin
--------------------------

タスク
~~~~~~

Framework Organizer Plugin は、以下のタスクを定義します。

..  attention::
    バージョン 0.7.0 以降では、 ``attach`` から始まる各タスクの使用は非推奨となりました。
    ``attach`` から始まるタスクはFramework Organizer Pluginが内部で生成し利用します。
    
    デプロイメントアーカイブ構築のカスタマイズ方法について、
    バージョン 0.7.0 以降では、 ``attach`` から始まる各タスクを組み合わせる方式から
    ``build.gradle`` 内の ``asaksuafwOrganizer`` ブロックで
    デプロイメント構成を定義する方法を推奨するよう変更しています。
    
    詳しくは、 :doc:`../administration/deployment-guide` や :ref:`asakusa-gradle-plugin-reference` を参照してください。

..  warning::
    バージョン 0.7.0 以降では、以下のタスクは削除されました。
     
    * ``attachAssemble``
    * ``attachAssembleDev``
    * ``assembleCustomAsakusafw``
    * ``assembleDevAsakusafw``
    
    複数のデプロイメントアーカイブ構成を管理する機能として、
    バージョン 0.7.0 以降ではプロファイル定義による構成機能を提供しています。
    
    詳しくは、 :doc:`../administration/deployment-guide` や :ref:`asakusa-gradle-plugin-reference` を参照してください。

..  attention::
    以下の表ではバージョン 0.7.0 以降で非推奨となったタスク、及び削除されたタスクをあげています。

..  list-table:: Framework Organizer Plugin - タスク
    :widths: 152 121 48 131
    :header-rows: 1

    * - タスク名
      - 依存先
      - 型
      - 説明
    * -  ``attachBatchapps`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にバッチアプリケーションを追加する [#]_
    * -  ``attachComponentCore`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にランタイムコアモジュールを追加する
    * -  ``attachComponentDirectIo`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にDirect I/Oを追加する
    * -  ``attachComponentYaess`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にYAESSを追加する
    * -  ``attachComponentWindGate`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にWindGateを追加する
    * -  ``attachComponentThunderGate`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にThunderGateを追加する
    * -  ``attachComponentDevelopment`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成に開発ツールを追加する
    * -  ``attachComponentOperation`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成に運用ツールを追加する
    * -  ``attachExtensionYaessJobQueue`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にYAESS JobQueue Pluginを追加する
    * -  ``attachExtensionWindGateRetryable`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にWindGate Retryable Pluginを追加する
    * -  ``attachConf<``  ``DistributionName``  ``>`` 
      -  ``-`` 
      - ``Task``
      - デプロイメント構成にディストリビューション名に対応するディレクトリを追加する [#]_
    * -  ``attachAssembleDev`` 
      -  ``attachBatchapps,`` 
         ``attachComponentCore,`` 
         ``attachComponentDirectIo,`` 
         ``attachComponentYaess,`` 
         ``attachComponentWindGate,`` 
         ``attachComponentDevelopment,`` 
         ``attachComponentOperation`` 
      - ``Task``
      - 開発環境向けのデプロイメント構成を構築する
    * -  ``attachAssemble`` 
      -  ``attachComponentCore,`` 
         ``attachComponentDirectIo,`` 
         ``attachComponentYaess,`` 
         ``attachComponentWindGate,`` 
         ``attachComponentOperation`` 
      - ``Task``
      - 運用環境向けのデプロイメント構成を構築する
    * -  ``assembleCustomAsakusafw`` 
      -  ``-`` 
      - ``Task``
      - 任意のデプロイメント構成を持つデプロイメントアーカイブを生成する
    * -  ``assembleDevAsakusafw`` 
      -  ``attachAssembleDev`` 
      - ``Task``
      - 開発環境向けのデプロイメント構成を持つデプロイメントアーカイブを生成する

..  [#]  ``attachBatchapps`` タスクを利用するには本プラグインをアプリケーションプロジェクト上で利用する必要があります。
..  [#]  ``attachConf<DistributionName>`` タスクを利用するには本プラグインをアプリケーションプロジェクト上で利用する必要があります。

