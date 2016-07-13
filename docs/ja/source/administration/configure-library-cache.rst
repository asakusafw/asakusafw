==========================
ライブラリキャッシュの設定
==========================

この文書では、Hadoopパラメータとして設定可能なライブラリキャッシュの設定方法について説明します。

Hadoopパラメータの設定方法については、 :doc:`configure-hadoop-parameters` を参照してください。

概要
====

Asakusa Frameworkを利用してアプリケーションを実行する場合、フレームワークやバッチアプリケーションのライブラリをHadoop上にアップロードして利用します。

この処理はバッチに含まれるジョブ(ステージ)ごとに行うため、多くのジョブが含まれるバッチを実行すると、アップロードやスレーブノードへの展開に時間がかかるため、処理全体が遅くなってしまいます。

アプリケーションライブラリのキャッシュを利用すると、過去にアップロードしたライブラリをキャッシュして再利用するようになり、実行時ライブラリの配布を高速化します。

キャッシュパスの設定
====================

ライブラリキャッシュを有効にするには、Hadoopパラメータ ``com.asakusafw.launcher.cache.path`` にキャッシュファイルを保持するHadoopファイルシステムのパスを指定します。

以下はライブラリキャッシュの設定例です。

..  code-block:: xml
    :caption: asakusa-resources.xml
    :name: asakusa-resources.xml-configure-library-cache-1

    <property>
        <name>com.asakusafw.launcher.cache.path</name>
        <value>target/libcache</value>
    </property>

ライブラリキャッシュの設定を追加した状態でYAESSからアプリケーションを実行した後にキャッシュパスの内容を参照すると、アプリケーション実行時のライブラリファイルがキャッシュされていることが確認できます。

..  code-block:: sh

    $ hadoop fs -ls target/libcache/*
    -rw-------   3 asakusa asakusa     507053 2014-06-23 13:11 /user/asakusa/target/libcache/1b1eded1/asakusa-runtime-all.jar
    -rw-------   3 asakusa asakusa          8 2014-06-23 13:11 /user/asakusa/target/libcache/1b1eded1/asakusa-runtime-all.jar.acrc
    -rw-------   3 asakusa asakusa     129107 2014-06-23 13:11 /user/asakusa/target/libcache/b27b4dc4/jobflow-byCategory.jar
    -rw-------   3 asakusa asakusa          8 2014-06-23 13:11 /user/asakusa/target/libcache/b27b4dc4/jobflow-byCategory.jar.acrc

..  attention::
    運用環境のHadoopクラスターを複数のAsakusa Frameworkの構成から利用するような場合、その構成間でキャッシュパスが重複しているとキャッシュファイルの競合が起こりキャッシュが不正な状態となる可能性があります。
    このような場合、複数の構成それぞれに異なるキャッシュパスを設定するのがよいでしょう。

その他の設定
============

ライブラリキャッシュの設定により、キャッシュの動作を詳細に設定することができます。

ライブラリキャッシュの設定項目の一覧を以下に示します。

..  list-table:: ライブラリキャッシュの設定
    :widths: 20 10 30
    :header-rows: 1

    * - 設定名
      - 既定値
      - 概要
    * - ``com.asakusafw.launcher.cache.path``
      - ``未指定``
      - キャッシュファイルを保持するHadoopファイルシステムのパス
    * - ``com.asakusafw.launcher.cache.retry.max``
      - ``50``
      - キャッシュ操作に対する最大リトライ回数 [#]_
    * - ``com.asakusafw.launcher.cache.retry.interval``
      - ``100``
      - キャッシュ操作に対するリトライインターバル(ミリ秒)
    * - ``com.asakusafw.launcher.cache.local``
      - JVMのテンポラリディレクトリ [#]_
      - キャッシュ処理が利用するローカルテンポラリディレクトリ
    * - ``com.asakusafw.launcher.cache.threads``
      - ``4``
      - キャッシュの検証や作成に利用するスレッド数
    * - ``com.asakusafw.launcher.cache.jobjar``
      - ``true``
      - アプリケーションJarをHadoopに登録するかどうか [#]_

..  [#] キャッシュ操作に失敗した場合は、キャッシュを利用しない状態でアプリケーションが実行されます。

..  [#] JVMのシステムプロパティ ``java.io.tmp`` で設定されるディレクトリ配下に  :file:`asakusa-launcher-cache-<ユーザー名>` というディレクトリを作成し、これを利用します。

..  [#] Hadoopディストリビューションによっては、この設定項目が ``true`` の状態ではアプリケーションが実行できません。
        ライブラリキャッシュの設定後アプリケーション実行がエラー終了する場合は、この設定項目を変更してみてください。

キャッシュされたライブラリのクリーンアップ
==========================================

不要になったキャッシュファイルを削除する場合、アプリケーションが実行していない状態でキャッシュパス上のファイル、またはキャッシュパス全体を削除します。

..  hint::
    ライブラリファイルをキャッシュする際に、ローカルファイルシステムの同じパス上のファイルは、常にHadoopでも同一のパス上に配置されます。
    ほとんどの場合、通常の利用方法ではキャッシュされたライブラリが占めるHadoop上のディスク容量を気にする必要はありません

