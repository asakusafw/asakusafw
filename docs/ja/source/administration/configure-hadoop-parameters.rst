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
    :widths: 20 10 30
    :header-rows: 1

    * - 設定名
      - 既定値
      - 概要
    * - ``com.asakusafw.input.combine.max``
      - ``Integer.MAX_VALUE``
      - Mapperごとの最大スプリット数
    * - ``com.asakusafw.input.combine.tiny.limit``
      - ``-1``
      - 「小さな入力」として扱う最大バイト数 ( ``0`` 未満の場合は無効)

上記は `Mapperごとの最大スプリット数` の設定です。ジョブの中で複数のMapperが利用される場合、最大で ``Mapper数 * Mapperごとの最大スプリット数`` のMapタスクが実行されます。また、Mapperごとの最大スプリット数が上記以下の場合、そのMapperへの入力に関するスプリットの結合は行われません。

ただし、特定のMapperへの入力データサイズが `「小さな入力」として扱う最大バイト数` よりも小さな場合、そのMapperに対するスプリットはすべて1つにまとめられます。

..  hint::
    通常の場合、 `Mapperごとの最大スプリット数` はMapタスクの合計スロット数の1~2倍程度が妥当でしょう。標準では ``Integer.MAX_VALUE`` に設定されているため、この機能は無効化されています。

..  attention::
    `「小さな入力」として扱う最大バイト数` に大きな数を指定した場合、 Map タスクが分散処理を行えなくなる場合があります。

    ここには非常に小さな値を指定するか、または本機能を無効化しておくことを推奨します（標準では無効化されています）。

..  note::
    スタンドアロンモードのHadoopでは、 `Mapperごとの最大スプリット数` は自動的に ``1`` が設定されます。

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

Reduceタスクの調整
------------------

..  attention::
    Asakusa Framework バージョン |version| では、 Reduceタスクの調整機能は試験的機能として提供されています。 

MapReduceジョブを実行する際に、入力データの特性に応じてReduceタスクを調整する機能を提供しています。

..  list-table:: Reduceタスクの調整
    :widths: 20 10 30
    :header-rows: 1

    * - 設定名
      - 既定値
      - 概要
    * - ``com.asakusafw.reducer.tiny.limit``
      - ``-1``
      - 「小さなジョブ」として扱う最大バイト数 ( ``0`` 未満の場合は無効)

あるジョブの入力データサイズが、「小さなジョブ」として扱う最大バイト数以下の場合に、そのジョブのReduceタスク数を ``1`` に再設定します（Reduceタスクを利用しない場合を除く）。

..  hint::
    Reduceタスク数を減らすことで計算リソースの無駄遣いを抑制したり、タスク起動のオーバーヘッドを削減したりできます。

ただし、入力データが小さくても計算に時間が掛かる処理や、Mapタスク内でデータを大量に増幅させる処理などが存在する場合、余計に処理時間を要する可能性があります。

ここには非常に小さな値（数MB程度）を指定するか、または本機能を無効化しておくことを推奨します（標準では無効化されています）。

