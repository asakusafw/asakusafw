======================
Hadoopパラメータの設定
======================

この文書では、HadoopのパラメータのうちAsakusa Framework特有のものについて説明します。

設定ファイル
============
Asakusa Frameworkに関するHadoopのパラメータは、 ``$ASAKUSA_HOME/core/conf/asakusa-resources.xml`` 内 [#]_ に記載します。
以下のように、１つの設定項目に対して ``<property>`` 要素を作成し、設定名を ``<name>`` 要素に、設定値を ``<value>`` 要素にそれぞれ設定します。

..  code-block:: sh

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
    ただし、 :doc:`ThunderGateのキャッシュビルド <../thundergate/cache>` 等、YAESSから設定を行えないものについては通常の設定ファイル内に記載してください。

設定項目
========

入力スプリットの結合
--------------------
Asakusa Frameworkでは、Map Reduceジョブを実行する際に複数の入力データのスプリットを結合してMapタスク数を減らす機能を提供しています。


..  list-table:: 入力スプリットの結合
    :widths: 20 10 20
    :header-rows: 1

    * - 設定名
      - 既定値
      - 概要
    * - ``com.asakusafw.input.combine.max``
      - ``Integer.MAX_VALUE``
      - Mapperごとの最大スプリット数


上記は `Mapperごとの最大スプリット数` の設定です。ジョブの中で複数のMapperが利用される場合、最大で ``Mapper数 * Mapperごとの最大スプリット数`` のMapタスクが実行されます。また、Mapperごとの最大スプリット数が上記以下の場合、そのMapperへの入力に関するスプリットの結合は行われません。

..  hint::
    通常の場合、Mapタスクのスロット数の2倍程度が妥当でしょう。

..  note::
    スタンドアロンモードのHadoopでは、上記は自動的に ``1`` が設定されます。

なお、入力スプリットの結合には遺伝的アルゴリズムを利用します。遺伝的アルゴリズムのパラメータは以下の設定が可能です。


..  list-table:: 入力スプリットの結合
    :widths: 5 2 3
    :header-rows: 1

    * - 設定名
      - 既定値
      - 概要
    * - ``com.asakusafw.input.combine.ga.generation``
      - ``50``
      - 世代数
    * - ``com.asakusafw.input.combine.ga.population``
      - ``20``
      - 世代ごとの個体数
    * - ``com.asakusafw.input.combine.ga.mutation``
      - ``0.001``
      - 突然変異率

..  note::
    基本的に、上記パラメータの変更は不要です。
    例外的に、遺伝的アルゴリズムによる計算にかかる時間が気になる場合、世代数や個体数を減らすことで計算時間を削減できます。
