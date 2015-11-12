======================================
Asakusa Framework デプロイメントガイド
======================================

この文書は、運用環境(Hadoopクラスター)に対してAsakusa Frameworkの実行環境をセットアップする方法に関して説明します。

はじめに
========

まず運用環境構築の前提となる事項について説明します。

Hadoopバージョンについて
------------------------

`Apache Hadoop`_ のリリースは多くのバージョン体系が存在しますが、現在は以下の2つのバージョン系が主に利用されており、多くのHadoopディストリビューションはこの2つのどちらかのバージョン系がベースとなっています。

Hadoop1系
  `Apache Hadoop`_ バージョン ``1.x.x`` というバージョン名でリリース

Hadoop2系
  `Apache Hadoop`_ バージョン ``2.x.x`` というバージョン名でリリース

..  _`Apache Hadoop`: http://hadoop.apache.org/

..  attention::
    利用するHadoopディストリビューションがベースとしているHadoopバージョンについては、各Hadoopディストリビューションのドキュメントなどを参照してください。

Asakusa Frameworkバージョンについて
-----------------------------------

Hadoop1系とHadoop2系ではいくつかの非互換性を含む変更 [#]_ が含まれているため、Asakusa FrameworkではそれぞれのHadoopバージョン系向けに以下のAsakusa Frameworkバージョンをリリースしています。

Hadoop1系向けAsakusa Framework
  Asakusa Framework バージョン ``0.x.x-hadoop1`` というバージョン名でリリース

Hadoop2系向けAsakusa Framework
  Asakusa Framework バージョン ``0.x.x-hadoop2`` というバージョン名でリリース

運用環境を構築する際には、利用するHadoopバージョンに応じたAsakusa Frameworkの実行モジュールを運用環境にデプロイする必要があります。

..  warning::
    Asakusa Framework バージョン ``0.7.0`` からAsakusa Frameworkのバージョン体系が変更になりました。
    Asakusa Framework バージョン ``0.6.x`` 以前のバージョンからのマイグレーションを検討する場合、必ず :doc:`../application/migration-guide` - :ref:`versioning-sysytem-changing` の内容を確認してください。

..  attention::
    Asakusa FrameworkとHadoopで異なるHadoopバージョン系を指定した場合、バッチ実行時にバリデーションエラーとなります。

..  attention::
    `CDH`_ などのHadoop2系ディストリビューションでは ``MRv1`` と呼ばれる、YARNベースのサービスを使用せずHadoop1系と同様のサービスを利用したHadoopクラスターを構築することができますが、MRv1はHadoop2系のAPIを利用しています。
     
    このため、 ``MRv1`` を利用する場合は、 **Hadoop2系向けAsakusa Framework** を使用します。

..  [#] `Apache Hadoop`_ のリリースにおける非互換性を含む変更については、 `Apache Hadoop Documentation`_ に含まれる Release Notes や Change Log などを参照してください。

..  _`Apache Hadoop Documentation`: http://hadoop.apache.org/docs/current/
..  _`CDH`: http://www.cloudera.com/content/cloudera/en/products-and-services/cdh.html

Hadoopクラスターの構築について
------------------------------

以降の説明では、利用可能なHadoopクラスターが準備済みであることを前提とします。
Hadoopクラスターの具体的な構築手順は、利用するHadoopディストリビューションのドキュメント等を参考にして下さい。

Asakusa Frameworkのデプロイメント
=================================

ここでは、運用環境にAsakusa FrameworkをデプロイしてHadoopクラスター上でバッチアプリケーションの実行を行うまでの手順を、以下のトピックに分けて説明します。

* `デプロイメントアーカイブの作成`_
* `デプロイメントアーカイブの配置`_
* `環境構成の確認とバッチの実行`_

以降の説明では、Akakusa Frameworkの開発環境でアプリケーションプロジェクトからバッチアプリケーションが作成できることを前提とします。
開発環境の構築やアプリケーションプロジェクトについては、 :doc:`../introduction/start-guide` や :doc:`../application/gradle-plugin` などを参照してください。

デプロイメントアーカイブの作成
------------------------------

Asakusa Frameworkを運用環境で利用するには、Asakusa Frameworkの「実行モジュール」一式を運用環境に配置する必要があります。

実行モジュールには、たとえば以下のようなものが含まれます。

* Asakusa Framework本体の実行ライブラリ
* Asakusa Frameworkで生成したバッチアプリケーション
* Asakusa Frameworkのプラグインモジュール
* Asakusa Frameworkの設定ファイル

Asakusa Frameworkではこれらの実行モジュールを生成する様々な方法を提供していますが、ここでは運用環境への配置が必要な全てのファイルを「デプロイメントアーカイブ」と呼ばれる単一のアーカイブファイルに含める方法について、いくつかの構成例とともに説明します。

シンプルな例
~~~~~~~~~~~~

デプロイメントアーカイブの作成には、Gradleの :program:`assemble` タスクを実行します。

..  code-block:: sh
    
    ./gradlew assemble

アプリケーションプロジェクトの標準設定でGradleの :program:`assemble` タスクを実行すると、バッチアプリケーションのビルドが行われ、ビルドが成功した場合はデプロイメントアーカイブがプロジェクトの :file:`build` ディレクトリ配下に :file:`asakusafw-${asakusafwVersion}.tar.gz` というファイル名で生成されます。

このデプロイメントアーカイブには以下のモジュールが含まれます。

* Hadoop1系向けのAsakusa Framework本体の実行ライブラリ
* プロジェクトに含まれるすべてのバッチアプリケーション
* Asakusa Frameworkの標準設定の設定ファイル

Hadoop2系向けの構成
~~~~~~~~~~~~~~~~~~~

運用環境にHadoop2系がベースのHadoopディストリビューションを利用する場合は、Hadoop2系向けのAsakusa Framework本体の実行ライブラリをデプロイメントアーカイブに含むよう設定します。

デプロイメントアーカイブの構成を変更するには、アプリケーションプロジェクトのビルドスクリプト :file:`build.gradle` の ``asakusafwOrganizer`` ブロックを編集します。

Hadoop2系向けの構成に変更するには、 ``profile.prod`` ブロックに含まれる ``asakusafwVersion`` をHadoop2系向けのバージョンに変更します。

**build.gradle**

..  code-block:: groovy
    :emphasize-lines: 3
   
    asakusafwOrganizer {
        profiles.prod {
            asakusafwVersion '0.7.5-hadoop2'
        }
    }


この状態で :program:`assemble` タスクを実行すると、Hadoop2系向けのAsakusa Framework本体の実行ライブラリが含まれます。

..  hint::
    上記の構成では、開発環境のHadoopはHadoop1系を利用し、運用環境向けのHadoopはHadoop2系を利用する、という構成になります。
     
    Asakusa Framework バージョン |version| では、開発環境で利用するHadoopはHadoop1系を推奨しています。
    :doc:`../introduction/start-guide` の手順やJinrikishaの標準構成ではHadoop1系を利用するため、上記例のように設定すると開発環境の構成変更が不要です。
     
    開発環境で利用するHadoopについて詳しくは、 :doc:`../application/using-hadoop` を参照してください。

設定ファイルの同梱
~~~~~~~~~~~~~~~~~~

デプロイメントアーカイブに、特定の運用環境向けの設定ファイルを含めることもできます。

以下は、 プロジェクトディレクトリの :file:`src/dist/prod` 配下に配置した設定ファイルをデプロイメントアーカイブに含める例です。

**build.gradle**

..  code-block:: groovy
    :emphasize-lines: 4-6
   
    asakusafwOrganizer {
        profiles.prod {
            asakusafwVersion asakusafw.asakusafwVersion
            assembly.into('.') {
                put 'src/dist/prod'
            }
        }
    }

``assembly.into`` は引数に指定したパス上にファイルを含めることを意味します。
例では引数に ``(.)`` と記述しており、これはデプロイメントアーカイブのルートディレクトリに対してファイルを含める指定となります。

``assembly.into`` ブロック配下の ``put`` の引数で含める対象となるファイルパスを指定します。
相対パスで指定した場合はプロジェクトディレクトリが起点となります。
この例では、 :file:`src/dist/prod` 配下には ``$ASAKUSA_HOME`` のディレクトリ構造と同じ形式で追加したい設定ファイルを以下のように配置しています。

..  code-block:: sh
    
    src/dist/prod
     ├── core
     │   └── conf
     │       └── asakusa-resources.xml
     └── yaess
         └── conf
             └── yaess.properties

その他の例
~~~~~~~~~~

``asakusafwOrganizer`` ブロック上では上記の他にも様々な構成に関する設定が可能です。
いくつかの構成例を以下に紹介します。

``asakusafwOrganizer`` ブロックに関する設定やこれを提供するAsakusa Gradle Pluginの詳細な説明は、 :doc:`../application/gradle-plugin` を参照してください。

拡張モジュールの同梱
^^^^^^^^^^^^^^^^^^^^

Asakusa Frameworkが標準のデプロイメントアーカイブに含めていない拡張モジュールを同梱する例です。

以下は、Direct I/O Hive用の実行ライブラリとWindGateのリトライ実行用の拡張プラグイン ``asakusa-windgate-retryable`` をデプロイメントアーカイブに含める例です。

**build.gradle**

..  code-block:: groovy
    :emphasize-lines: 2-3
   
    asakusafwOrganizer {
        hive.enabled true
        windgate.retryableEnabled true
        profiles.prod {
            asakusafwVersion asakusafw.asakusafwVersion
        }
    }

この例では、設定を ( ``profiles.prod`` ブロックではなく ) ``asakusafwOrganizer`` ブロックの直下に追加しているため、この設定は開発環境用のインストール構成にも適用されます。

..  seealso::
    拡張モジュールの利用については、 :doc:`../application/gradle-plugin` を参照してください。

Hiveライブラリの指定
^^^^^^^^^^^^^^^^^^^^

開発環境と運用環境でDirect I/O Hive用の実行ライブラリを分ける例です。

以下は、運用環境のHadoopディストリビューションに `MapR`_ を利用する環境でDirect I/O Hiveを利用するための設定例です。

**build.gradle**

..  code-block:: groovy
    :emphasize-lines: 6,9
     
    repositories {
        maven { url 'http://repository.mapr.com/maven/' }
    }
     
    asakusafwOrganizer {
        hive.enabled true
        profiles.prod {
            asakusafwVersion asakusafw.asakusafwVersion
            hive.libraries = ['org.apache.hive:hive-exec:0.13.0-mapr-1501-protobuf250@jar']
        }
    }

Direct I/O Hiveを `MapR`_ 環境で利用する場合、Direct I/O HiveはMapRが提供するHiveライブラリを利用する必要があるため、運用環境用のデプロイメントアーカイブにHiveライブラリを指定しています。

..  hint::
    `MapR`_ 用のライブラリを指定する場合、MapRライブラリ配布用のMavenリポジトリを ``repositories`` ブロックで指定する必要があります。

..  seealso::
    Direct I/O Hiveについては、 :doc:`../directio/using-hive` を参照してください。

..  _`MapR`: https://www.mapr.com/

.. _deployment-extention-libraries-example:

外部ライブラリの配置
^^^^^^^^^^^^^^^^^^^^

外部ライブラリやAsakusa Frameworkが標準で同梱しない、特別な実行時プラグインを利用する場合、 :file:`$ASAKUSA_HOME/ext/lib` 配下にライブラリを配置すると利用可能になります [#]_ 。

これらのライブラリをデプロイメントアーカイブに同梱するには、以下のように書けます。

**build.gradle**

..  code-block:: groovy
    :emphasize-lines: 2
    
    asakusafwOrganizer {
        extension {
            libraries += ['joda-time:joda-time:2.5']
        }

..  attention::
    この機能では、指定したライブラリの推移的依存関係となるライブラリは含まれません。

..  hint::
    リポジトリ上に存在しないライブラリを同梱したい場合には、 `設定ファイルの同梱`_ と同様の方法などで、ライブラリファイルを配置するのがよいでしょう。

..  [#] 実行時プラグインの配置については、 :doc:`deployment-runtime-plugins` の内容も参照してください。

複数の運用環境向けのデプロイ管理
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

バッチアプリケーションを実行する運用環境が複数ある場合、環境ごとにデプロイ構成を変更したい場合があります。
このような場合、運用環境ごとにデプロイ構成用のプロファイルを作成すると便利です。

以下は、ステージング環境用のデプロイ構成を持つデプロイメントアーカイブを作成する例です。

**build.gradle**

..  code-block:: groovy
    :emphasize-lines: 2,12
     
    asakusafwOrganizer {
        profiles.prod {
            asakusafwVersion asakusafw.asakusafwVersion
            assembly.into('.') {
                put 'src/dist/prod'
            }
            assembly.into('.') {
                put 'src/dist/common'
                replace 'asakusa-resources.xml', inputCombineMax: '48'
            }
        }
        profiles.stage {
            asakusafwVersion asakusafw.asakusafwVersion
            assembly.into('.') {
                put 'src/dist/stage'
            }
            assembly.into('.') {
                put 'src/dist/common'
                replace 'asakusa-resources.xml', inputCombineMax: '24'
            }
        }
    }

標準で設定されているプロファイル ``profiles.prod`` に加えて、ステージング環境用のプロファイルとして ``profiles.stage`` を追加しています。

この設定でデプロイメントアーカイブの生成を行うと、 :file:`build` ディレクトリ配下に標準のデプロイメントアーカイブに加えて、 :file:`asakusafw-${asakusafwVersion}-stage.tar.gz` というファイル名で ``profiles.stage`` に対応したデプロイメントアーカイブが生成されます。

この例では、それぞれのプロファイル用に作成した設定ファイル用のディレクトリ( :file:`src/dist/prod`, :file:`src/dist/stage` )から設定ファイルを配置しています。

また、それぞれのプロファイルに共通の設定ファイルを管理するディレクトリ( :file:`src/dist/common` )からも設定ファイルを配置しています。
このとき、 ``replace`` 句を指定することで設定ファイルの内容を置換して、環境ごとに固有のパラメータを設定しています。

例えば、 :file:`src/dist/common` 配下に :file:`asakusa-resources-xml` を以下のような内容で配置します。

**asakusa-resources.xml**

..  code-block:: xml
    :emphasize-lines: 7
         
    <?xml version="1.0" encoding="UTF-8"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>
    <configuration>
        ...
        <property>
            <name>com.asakusafw.input.combine.max</name>
            <value>@inputCombineMax@</value>
        </property>
    </configuration>

:file:`build.gradle` では以下のように設定しているため、

* ``profile.prod`` ブロック: ``replace 'asakusa-resources.xml', inputCombineMax: '48'``
* ``profile.stage`` ブロック: ``replace 'asakusa-resources.xml', inputCombineMax: '24'``

それぞれのデプロイメントアーカイブの :file:`asakusa-resources.xml` にはこれらの設定値が置換された状態の設定ファイルが同梱されます。

..  seealso::
    :doc:`../sandbox/asakusa-on-emr` でも運用環境向けのデプロイ機能の利用例を紹介しています。デプロイ構成を柔軟に設定できるこれらの機能は、クラウド環境上で様々な運用環境を構築する場合などでも効果的でしょう。
    
デプロイメントアーカイブの配置
------------------------------

`デプロイメントアーカイブの作成`_ で作成したデプロイメントアーカイブを運用環境に配置します。

ここでは、運用環境上に構築したHadoopクラスターの各ノードうち、Asakusa Frameworkを配置してバッチアプリケーションの実行操作を行うノードを「Hadoopクライアントマシン」と呼びます。

環境変数の設定
~~~~~~~~~~~~~~

Hadoopクライアントマシン上でAsakusa Frameworkを配置しバッチアプリケーションの実行操作を行うOSユーザに対して、以下の環境変数を設定します。

* ``ASAKUSA_HOME``: Asakusa Frameworkのインストールパス
* ``JAVA_HOME``: YAESSが使用するJavaのインストールパス
* ``HADOOP_CMD``: YAESSが使用するHadoopコマンドのパス

:file:`~/.profile` をエディタで開き、最下行に以下の定義を追加します。

..  code-block:: sh
    
    export JAVA_HOME=/usr/lib/jvm/java-7-oracle
    export ASAKUSA_HOME=$HOME/asakusa
    export HADOOP_CMD=/usr/lib/hadoop/bin/hadoop

:file:`~/.profile` を保存した後、設定した環境変数をターミナル上のシェルに反映させるため、以下のコマンドを実行します。

..  code-block:: sh

    . ~/.profile

..  attention::
    実際に必要となる環境変数は利用するコンポーネントやHadoopの構成によって異なります。
    これらの詳細はAsakusa Frameworkの各コンポーネントのドキュメントや利用するHadoopディストリビューションのドキュメントを参照してください

デプロイメントアーカイブの展開
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Hadoopクライアントマシンにデプロイメントアーカイブファイル :file:`asakusafw-${asakusafwVersion}.tar.gz` を配置し、 ``$ASAKUSA_HOME`` 配下にデプロイメントアーカイブを展開します。
展開後、 ``$ASAKUSA_HOME`` 配下の :file:`*.sh` に実行権限を追加します。

..  code-block:: sh

    mkdir -p "$ASAKUSA_HOME"
    cd "$ASAKUSA_HOME"
    tar -xzf /path/to/asakusafw-*.tar.gz
    find "$ASAKUSA_HOME" -name "*.sh" | xargs chmod u+x
..  **

環境構成の確認とバッチの実行
----------------------------

運用環境にデプロイしたAsakusa Frameworkのバッチアプリケーションを以下の手順で実行して運用環境上の動作確認を行います。

* 動作確認用テストデータの配置
* バッチアプリケーションの実行
* バッチアプリケーションの実行結果の確認

ここでは、 :doc:`../introduction/start-guide` で紹介したDirect I/O をを使ったサンプルアプリケーションをAsakusa Frameworkの標準設定のままで実行する例を示します。

動作確認用テストデータの配置
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

バッチアプリケーションの実行に必要な入力データを配置します。

以下は、Hadoopクライアントマシン上に配置した入力データファイルを :program:`hadoop` コマンドでHadoopファイルシステムに登録する例です。

..  code-block:: sh

    hadoop fs -put /path/to/example-dataset/master target/testing/directio/master
    hadoop fs -put /path/to/example-dataset/sales target/testing/directio/sales

..  hint::
    実行するバッチアプリケーションが利用する外部システム連携機能によって、入力データの配置箇所は異なります。
    例えば、WindGate/JDBC を利用する場合はデータベースに対して入力データを配置します。

バッチアプリケーションの実行
~~~~~~~~~~~~~~~~~~~~~~~~~~~~

デプロイしたバッチアプリケーションをYAESSを使って実行します。

:program:`$ASAKUSA_HOME/yaess/bin/yaess-batch.sh` コマンドに実行するバッチIDとバッチ引数を指定してバッチを実行します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

バッチの実行が成功すると、コマンドの標準出力の最終行に ``Finished: SUCCESS`` と出力されます。

..  code-block:: sh

    2013/04/22 13:50:35 INFO  [YS-CORE-I01999] Finishing batch "example.summarizeSales": batchId=example.summarizeSales, elapsed=12,712ms
    2013/04/22 13:50:35 INFO  [YS-BOOTSTRAP-I00999] Exiting YAESS: code=0, elapsed=12,798ms
    Finished: SUCCESS

バッチの実行が失敗した場合はYAESSのログを確認します。

バッチアプリケーションのシミュレーションモード実行
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

YAESSでは実際のアプリケーションの実行は行わず、環境構成や設定の確認のみを行うシミュレーションモード実行を行うことができます。

この機能は、バッチの失敗が環境構成や設定の問題であるか、アプリケーション内の問題であるかを切り分けるために有効です。

バッチをシミュレーションモードで実行するには、 :program:`yaess-batch.sh` コマンドの引数の末尾に ``-D dryRun`` と指定します。

..  code-block:: sh

    .../yaess-batch.sh example.summarizeSales -A date=2011-04-01 -D dryRun

バッチアプリケーションの実行結果の確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

バッチアプリケーションが出力したデータの内容を確認します。

Direct I/O をバッチの出力に利用するアプリケーションについては、以下のツールなどを利用してHadoopファイルシステム上のファイル内容を確認することができます。

* :program:`$ASAKUSA_HOME/directio/bin/list-file.sh <base-path> <resource-pattern>`

  * Direct I/Oの入出力ディレクトリやファイルの一覧を表示
* :program:`hadoop fs -text <file-path>`

  * 指定したファイルパスの内容を表示

関連するトピック
================

運用環境の構築や設定に関する情報として、以下のドキュメントも参考にしてください。

システム構成の検討
------------------

外部システム連携モジュールを用いた場合のシステム構成に関して以下のドキュメントで紹介しています。

* :doc:`deployment-architecture`

Hadoopパラメータの設定
----------------------

以下のドキュメントでは、Hadoopジョブの実行に関してAkakusa Framework特有のチューニングパラメータなどを説明しています。

* :doc:`configure-hadoop-parameters`

各コンポーネントの設定
----------------------

Asakusa Frameworkの各コンポーネントの設定に関しては、各コンポーネントのユーザガイドなどを参照してください。

* :doc:`../directio/user-guide`
* :doc:`../windgate/user-guide`
* :doc:`../thundergate/user-guide`
* :doc:`../yaess/user-guide`

