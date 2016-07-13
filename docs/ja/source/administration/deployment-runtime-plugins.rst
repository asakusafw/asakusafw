==========================
実行時プラグインのデプロイ
==========================

この文書では、Asakusa Frameworkの実行時プラグインのデプロイ方法について説明します。

実行時プラグインについて
========================

実行時プラグインとは、Asakusa FrameworkのフレームワークAPIを拡張するための内部機構です。
バッチアプリケーションがAsakusa Frameworkが標準で提供していない、個別の拡張フレームワークAPIを使用している場合は、以下に説明する手順に従って、実行時プラグインのデプロイを行ってください。

実行時プラグイン用ライブラリの配置
----------------------------------

実行時プラグイン用ライブラリはjarファイルとして提供されます。
実行時プラグイン用のjarファイルは :file:`$ASAKUSA_HOME/ext/lib` ディレクトリに配置してください。

デプロイメントアーカイブの生成時に実行時プラグイン用のjarファイルを同梱することもできます。
:doc:`deployment-guide` - :ref:`deployment-extention-libraries-example` の例などを参考にしてください。

実行時プラグインの設定
----------------------

実行時プラグインの設定ファイルを編集します。

実行時プラグインの設定は、 :file:`$ASAKUSA_HOME/core/conf/asakusa-resources.xml` を編集します。
以下のように、それぞれの設定項目に対して ``<property>`` 要素を作成し、設定名を ``<name>`` 要素に、設定値を ``<value>`` 要素にそれぞれ設定します。

..  code-block:: xml
    :caption: asakusa-resources.xml
    :name: asakusa-resources.xml-deployment-runtime-plugins-1

    <configuration>
        <property>
            <name>com.asakusafw.runtime.core.Report.Delegate</name>
            <value>com.asakusafw.runtime.core.Report$Default</value>
        </property>
        <property>
            <name>com.asakusafw.runtime.extention.HogeRuntimePlugin.Delegate</name>
            <value>com.asakusafw.runtime.extention.HogeRuntimePlugin$Default</value>
        </property>
    </configuration>

..  hint::
    上記設定ファイルの記法はHadoop本体の設定ファイル ( :file:`core-site.xml` 等 ) と同様です。

レポートAPIに関するプラグイン
=============================

レポートAPIは実行時プラグインの仕組みを利用して実装されており、レポートAPIの実装を自由に差し替えることが可能です。
レポートAPIは ``Report.Delegate`` [#]_ インターフェースを実装したクラスを利用して自由に実装を変更できます。

レポートAPIの実装クラスは、実行時プラグインの設定に対し、 ``com.asakusafw.runtime.core.Report.Delegate`` という設定名で設定値に実装クラスの完全限定名を指定します。

Asakusa Frameworkは以下の実装クラスを組み込みで提供しています。

..  list-table:: レポートAPIの組み込み実装クラス
    :widths: 10 10
    :header-rows: 1

    * - クラス名
      - 概要
    * - ``com.asakusafw.runtime.core.Report$Default`` [#]_
      - 標準出力にレポートを出力する実装。
    * - ``com.asakusafw.runtime.report.CommonsLoggingReport`` [#]_
      - Commons Loggingを経由してレポートを出力する実装。
        Hadoop上で実行する場合、Hadoopのログの設定を変更することで出力先等を設定可能。

レポートAPIの利用方法については :doc:`../dsl/user-guide` を参照してください。

..  [#] :javadoc:`com.asakusafw.runtime.core.Report.Delegate`
..  [#] :javadoc:`com.asakusafw.runtime.core.Report.Default`
..  [#] :javadoc:`com.asakusafw.runtime.report.CommonsLoggingReport`


