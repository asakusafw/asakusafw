==========================
実行時プラグインのデプロイ
==========================
この文書では、Asakusa Frameworkの実行時プラグインのデプロイ方法について説明します。

実行時プラグインについて
========================
実行時プラグインとは、Asakusa FrameworkのフレームワークAPIを拡張するための内部機構です。バッチアプリケーションがAsakusa Frameworkが標準で提供していない、個別の拡張フレームワークAPIを使用している場合は、以下に説明する手順に従って、実行時プラグインのデプロイを行ってください。

実行時プラグイン用ライブラリの配置
----------------------------------
実行時プラグイン用ライブラリはjarファイルとして提供されます。実行時プラグイン用のjarファイルは $ASAKUSA_HOME/ext/lib ディレクトリに配置してください。

実行時プラグインの設定
----------------------
実行時プラグインの設定ファイルを編集します。

実行時プラグインの設定は、$ASAKUSA_HOME/core/conf/asakusa-resources.xml を編集します。以下のように、１つの設定項目に対して <property>エレメントを作成し、設定名を<name>要素に、設定値を<value>要素にそれぞれ設定します。

..  code-block:: sh

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

