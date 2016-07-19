===============================================
Asakusa Gradle Plugin バージョン 0.8系 の変更点
===============================================

ここでは、Asakusa Gradle Plugin バージョン 0.6系, 0.7系 から 0.8系 の変更点について説明します。

Asakusa Gradle Pluginを利用したアプリケーションプロジェクトのマイグレーションについては、 :doc:`gradle-plugin-migration-guide` も参照してください。

Gradleラッパーのアップデートに関する変更
========================================

アプリケーションプロジェクトに含まれるGradleラッパーをアップデートする手順が変更になりました。

過去のバージョンでは、 :program:`wrapper` タスクを実行する手順を推奨していましたが、
0.8系以降では、Asakusa Gradle Pluginが提供する :program:`asakusaUpgrade` タスクを利用してください。

:program:`asakusaUpgrade` タスクは、Asakusa Gradle Pluginの該当バージョンで推奨されるGradleラッパーのバージョンをアプリケーションプロジェクトに導入します。

..  code-block:: sh

    ./gradlew asakusaUpgrade

また、この変更に伴いビルドスクリプトの ``wrapper`` タスクに関する定義は不要となりました。
特別な理由がない限り、以下の例に示す ``wrapper`` タスクの定義はビルドスクリプトから削除することを推奨します。

..  code-block:: groovy
    :caption: build.gradle (以下の定義は不要)
    :name: build.gradle-gradle-plugin-v08-changes-1

    task wrapper(type: Wrapper) {
        distributionUrl 'http://services.gradle.org/distributions/gradle-2.8-bin.zip'
        jarFile file('.buildtools/gradlew.jar')
    }

ビルドスクリプトの設定に関する変更
==================================

プラグインの適用方法の変更
--------------------------

過去のバージョンでは、 ビルドスクリプトでAsakusa Gradle Pluginを適用する方法は、以下のように記述していました。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-gradle-plugin-v08-changes-2

    apply plugin: 'asakusafw'
    apply plugin: 'asakusafw-organizer'

0.8系以降では、以下のように記述します。

..  code-block:: groovy
    :caption: build.gradle
    :name: build.gradle-gradle-plugin-v08-changes-3

    apply plugin: 'asakusafw-sdk'
    apply plugin: 'asakusafw-organizer'
    apply plugin: 'asakusafw-mapreduce'

0.8系ではプラグイン構成が以下のように変更されています。

..  list-table:: Asakusa Gradle Plugin - プラグイン構成
    :widths: 3 7
    :header-rows: 1

    * - プラグインID
      - 説明
    * - ``asakusafw-sdk``
      - バッチアプリケーション開発向けの基底となる機能を導入する
    * - ``asakusafw-organizer``
      - デプロイメントなど構成管理向けの基底となる機能を導入する
    * - ``asakusafw-mapreduce``
      - MapReduce向けのバッチアプリケーションを生成、実行するための機能を導入する
    * - ``asakusafw-spark``
      - Spark向けのバッチアプリケーションを生成、実行するための機能を導入する [#]_
    * - ``asakusafw-legacy``
      - ThunderGateなどのレガシーモジュールを利用するための機能を導入する
    * - ``asakuafw``
      - 次のプラグイン機能を導入する: ``asakusafw-sdk`` , ``asakusafw-mapreduce``, ``asakusafw-legacy`` [#]_

..  [#] このプラグインは Asakusa on Spark Gradle Pluginが提供します。詳しくは :asakusa-on-spark:`Asakusa on Spark のドキュメント <index.html>` を参照してください。

..  [#] 現時点では過去バージョンとの互換性のために必要なプラグイン機能を適用しています。なお、今後のバージョンで導入される機能が変更される可能性があります。

.. _gradle-plugin-v08-specify-asakusafw-version:

Asakusa Frameworkバージョンの指定
---------------------------------

..  attention::
    バージョン 0.8.1 より、 Asakusa Frameworkバージョンの指定 は非推奨機能に変更されました。
    本項の説明の通り、``asakusafwVersion`` の定義をビルドスクリプトから削除することを強く推奨します。

0.8系以降ではHadoop1系が非対応となったことにより、Asakusa Framwork バージョン 0.7.0 から導入された「Hadoopバージョン」（``<version>-hadoop1``, ``<version>-hadoop2`` のように、利用するHadoopのバージョンを持つバージョン体系）が廃止になりました。

0.8系以降は、Asakusa Frameworkのバージョンは単一のバージョン体系 ( 例えば ``0.8.0`` ) を使用します。

また、0.8系ではビルドスクリプト上の ``asakusafwVersion`` の指定はオプションになりました。
``asakusafwVersion`` を指定しない場合、 Asakusa Gradle Pluginの該当バージョンが規定するAsakusa Frameworkバージョンを導入します。
また、 Asakusa on Spark Gradle Pluginを利用する場合も同様に、このプラグインの該当バージョンが規定するAsakusa Frameworkバージョンを導入します。

..  attention::
    通常、Asakusa Gradle Pluginはプラグインのバージョンと同一のAsakusa Frameworkバージョンを適用しますが、ホットフィックスリリースが行われた場合などにより異なるバージョンを適用する可能性があります。
    アプリケーションプロジェクトで利用される各コンポーネントのバージョンを確認する方法は、後述の `Asakusa Frameworkバージョンの確認`_ を参照してください。

なお、検証されていない組み合わせの各Gradle PluginとAsakusa Frameworkバージョンを利用することは非推奨です。

これらの理由により、特別な理由がない限り以下の例に示す ``asakusafwVersion`` の定義はビルドスクリプトから削除することを推奨します。

..  code-block:: groovy
    :caption: build.gradle (以下の定義は不要)
    :name: build.gradle-gradle-plugin-v08-changes-4

    asakusafw {
        asakusafwVersion '0.7.6-hadoop1'
    ...
    }

    asakusafwOrganizer {
        profiles.prod {
            asakusafwVersion asakusafw.asakusafwVersion
            ....
        }
        ....
    }

Hadoopライブラリの指定
----------------------

過去のバージョンでは、 バッチアプリケーションの開発時に使用するHadoopライブラリはビルドスクリプトの ``dependencies`` ブロックに設定する必要がありました。

0.8系以降では、Asakusa FrameworkのSDKアーティファクト ``asakusa-sdk-core`` [#]_ を経由して、Asakusa Frameworkの該当バージョンが規定するHadoopライブラリを導入します。

..  attention::
    アプリケーションプロジェクトで利用されるHadoopライブラリのバージョンを確認する方法は、後述の `Asakusa Frameworkバージョンの確認`_ を参照してください。

なお、ビルドスクリプトの設定でHadoopライブラリのバージョンを変更することは可能ですが、検証されていないHadoopライブラリのバージョンを利用することは非推奨です。

このため、特別な理由がない限り以下の例に示す ``dependencies`` ブロックのHadoopライブラリに関する定義はビルドスクリプトから削除することを推奨します。

..  code-block:: groovy
    :caption: build.gradle (以下の定義は不要)
    :name: build.gradle-gradle-plugin-v08-changes-5

    dependencies {
        ...
        provided (group: 'org.apache.hadoop', name: 'hadoop-client', version: '1.2.1') {
            exclude module: 'junit'
            exclude module: 'mockito-all'
            exclude module: 'slf4j-log4j12'
        }

..  [#] SDKアーティファクトについて詳しくは、 :doc:`sdk-artifact` を参照してください。

バッチアプリケーションのコンパイルに関する変更
==============================================

コンパイルに使用するタスクの動作に関する変更
--------------------------------------------

過去のバージョンでバッチアプリケーションのコンパイルを行うには、MapReduce向けのコンパイルは :program:`compileBatchapp` タスクを利用し、 Spark向けのコンパイルは :program:`sparkCompileBatchapps` タスクを利用していました。

また、 :program:`assemble` タスクによってデプロイメントアーカイブを作成する際には常に :program:`compileBatchapp` タスクが実行され、 Asakusa on Spark Gradle Pluginを適用している場合は常に :program:`sparkCompileBatchapps` が実行されていました。

0.8系以降は、タスクの構成と動作が以下のように変更されています。

:program:`compileBatchapp` タスク
  ビルドスクリプトのプラグイン設定に従って利用可能なDSLコンパイラを全て実行します。

  例えば、ビルドスクリプトにMapReduce向けのプラグイン ``asakusafw-mapreduce`` と Spark向けのプラグイン ``asakusafw-spark`` が適用されている場合、
  :program:`compileBatchapp` タスクを実行すると MapReduce向けのコンパイルとSpark向けのコンパイルをそれぞれ実行します。

  ..  attention::
      0.8系から導入されたビルドスクリプトに対するプラグインの適用方法については先述の `プラグインの適用方法の変更`_ を参照してください。

:program:`mapreduceCompileBatchapps` タスク
  MapReduceコンパイラによるバッチアプリケーションのコンパイルを実行します。

  このタスクはビルドスクリプトにプラグイン ``asakusafw-mapreduce`` を適用することで利用可能になります。
  このプラグインを適用した状態で :program:`compileBatchapp` タスクを実行すると、 :program:`mapreduceCompileBatchapps` タスクが実行されます。

  このタスクはバージョン0.8.0で新規で追加されました。

:program:`sparkeCompileBatchapps` タスク
  Sparkコンパイラによるバッチアプリケーションのコンパイルを実行します。

  このタスクはビルドスクリプトにプラグイン ``asakusafw-spark`` を適用することで利用可能になります。
  このプラグインを適用した状態で :program:`compileBatchapp` タスクを実行すると、 :program:`sparkCompileBatchapps` タスクが実行されます。

:program:`assemble` タスク
  処理の過程で :program:`compileBatchapp` タスクを実行するため、 :program:`compileBatchapp` タスクの動作変更の影響を受けることに注意してください。

  なお、バージョン 0.8.0から追加になった機能として、デプロイメント構成ごとに各DSLコンパイラの生成物を含めるかどうかの設定が可能になりました。

  詳しくは、 :doc:`gradle-plugin` - :ref:`gradle-plugin-dslcompile-disable` を参照してください。

バッチアプリケーションのフィルタリングに関する変更
--------------------------------------------------

Asakusa Frameworkのバージョン 0.7.5 で :program:`compileBatchapp` タスクに対してコンパイル対象をフィルタリングするための ``--update`` オプションが追加されましたが、
バージョン 0.8.0 ではこのオプションは :program:`compileBatchapp` タスクでは利用できなくなりました。
代わりに、 :program:`mapreduceCompileBatchapps` タスクで利用可能です。

:program:`sparkeCompileBatchapps` ついては過去のバージョンと同様に、 ``--update`` オプションを利用することができます。

また、バージョン 0.8.0 ではMapReduceのコンパイルに関するフィルター設定をビルドスクリプト上に記述することができるようになりました。

詳しくは、 :doc:`gradle-plugin` - :ref:`gradle-plugin-dslcompile-filter` を参照してください。

DSLコンパイラプロパティに関する変更
-----------------------------------

バージョン 0.8.0 より、 ``asakusafw`` ブロック内の参照名 ``compiler`` ブロックによるDSLコンパイラプロパティの指定は非推奨となりました。
MapReduceコンパイラに対する設定は、バージョン 0.8.0 から追加された ``mapreduce`` ブロックによるMapReduceプロパティの指定を使用してください。

なお、現時点では ``compiler`` ブロックによる設定も有効ですが、将来のバージョンでは使用できなくなる可能性があります。

MapReduceプロパティの指定や設定項目については、 :doc:`gradle-plugin-reference` を参照してください。

Asakusa Frameworkバージョンの確認
=================================

`Asakusa Frameworkバージョンの指定`_ で説明した通り、0.8系ではAsakusa Gradle Plugin や Asakuas on Spark Gradle Plugin の定義によって自動的にAsakusa Frameworkバージョンが設定されることがあります。

そのため、0.8系からアプリケーションプロジェクトで利用されるAsakusa Frameworkバージョンやその他のコンポーネントバージョンを確認するための :program:`asakusaVersion` タスクが追加されました。

..  code-block:: sh

    ./gradlew asakusaVersion

:program:`asakusaVersion` タスクはビルドスクリプトの設定を解析し、以下のようにプロジェクトで利用するコンポーネントのバージョンが表示します。

..  code-block:: none

    :asakusaVersions
    Asakusa Gradle Plug-ins: 0.8.0
    Asakusa on Spark: 0.3.0
    Asakusa SDK: 0.8.0
    JVM: 1.7
    Spark: 1.6.0
    Hadoop: 2.7.2
