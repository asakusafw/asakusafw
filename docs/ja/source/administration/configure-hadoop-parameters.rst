======================
Hadoopパラメータの設定
======================

この文書では、HadoopのパラメータのうちAsakusa Framework特有のものについて説明します。

設定ファイル
============

Asakusa Frameworkに関するHadoopのパラメータは、 :file:`$ASAKUSA_HOME/core/conf/asakusa-resources.xml` [#]_ に記載します。
このファイルに設定した内容はバッチアプリケーションの実行時にHadoopジョブの設定として使用され、ジョブ実行時の動作に影響を与えます。

設定ファイルはHadoopの各設定ファイルのフォーマットと同様です。
以下のように、１つの設定項目に対して ``<property>`` 要素を作成し、設定名を ``<name>`` 要素に、設定値を ``<value>`` 要素にそれぞれ設定します。

..  code-block:: xml
    :caption: asakusa-resources.xml
    :name: asakusa-resources.xml-configure-hadoop-parameters-1

    <configuration>
        <property>
            <name>(設定名1)</name>
            <value>(設定値1)</value>
        </property>
        <property>
            <name>(設定名2)</name>
            <value>(設定値2)</value>
        </property>
        <!-- 以下、繰り返し -->
    </configuration>

..  [#] このファイルは、 :doc:`実行時プラグインの設定 <deployment-runtime-plugins>` と共有しています。

..  hint::
    上記の設定は、 :doc:`YAESSのHadoopのプロパティ設定 <../yaess/user-guide>` でも一部設定が可能です。

設定項目
========

Hadoopパラメータで設定可能な項目は、以下のドキュメントを参照してください。

* :doc:`configure-task-optimization`
* :doc:`configure-library-cache`
