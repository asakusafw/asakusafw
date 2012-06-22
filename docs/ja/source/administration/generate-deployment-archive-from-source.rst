============================================
デプロイメントアーカイブをソースから生成する
============================================
本書では、Asakusa FrameworkのデプロイメントアーカイブをAsakusa Frameworkのソースから生成する方法について説明します。

..  todo:: Need to modify for changing repository structure and assembly descriptor.

Asakusa Framework本体のデプロイメント用アーカイブはバッチアプリケーション用プロジェクトからMavenのassemblyプラグインを実行する (mvn assembly:single) ことによって生成しますが、Asakusa Framework本体のソースから生成することによって、手元で改変を加えたAsakusa Frameworkからデプロイメントアーカイブを生成したり、スナップショットビルドに対して個別のコミットからデプロイメントアーカイブを生成することが出来ます。

Asakusa Frameworkのソースアーカイブを取得
-----------------------------------------
Asakusa FrameworkのGitHubリポジトリ [#]_ から、Asakusa Frameworkのソースアーカイブを取得します。

..  [#] https://github.com/asakusafw/asakusafw

以下はwgetを使ってAsakusa Framework ver 0.4.0 を取得する例です。

..  code-block:: sh

    wget https://github.com/asakusafw/asakusafw/zipball/0.4.0

Asakusa Frameworkのビルド
-------------------------
アーカイブを展開し、アーカイブに含まれるプロジェクト「asakusa-aggregator」のpom.xmlに対してinstallフェーズを実行し、Asakusa Frameworkの全モジュールをビルドします。

以下の例に沿ってビルドを実施して下さい。「BUILD SUCCESS」が出力されることを確認してください。

..  code-block:: sh

    unzip asakusafw-asakusafw-*.zip
    cd asakusafw-asakusafw-*/asakusa-aggregator
    mvn clean install -Dmaven.test.skip=true

Asakusa Frameworkのデプロイアーカイブ生成
-----------------------------------------
アーカイブに含まれるプロジェクト「asakusa-distribution」のpom.xmlに対してassebmbly:singleゴールを実行し、Hadoopクラスターにデプロイするアーカイブファイルを作成します。

以下の例に沿ってビルドを実施して下さい。「BUILD SUCCESS」が出力されることを確認してください。

..  code-block:: sh

    cd ../asakusa-distribution
    mvn clean assembly:single

デプロイアーカイブの確認
------------------------
「asakusa-distribution」のtagetディレクトリ配下に、以下のファイルが作成されていることを確認します。

  asakusafw-${asakusafw-version}-dev.tar.gz
    Asakusa Frameworkを開発環境に展開するためのアーカイブ。
  asakusafw-${asakusafw-version}-windgate.tar.gz
    Asakusa FrameworkをWindGateと使用する場合における、Asakusa Frameworkを運用環境に展開するためのアーカイブ。
  asakusafw-${asakusafw-version}-prod-thundergate-hc.tar.gz
    Asakusa FrameworkをThunderGateと使用する場合における、HadoopクラスターのHadoopクライアントノードに展開するためのアーカイブ。
  asakusafw-${asakusafw-version}-prod-thundergate-db.tar.gz
    Asakusa FrameworkをThunderGateと使用する場合における、データベースノードに展開するためのアーカイブ。
  asakusafw-${asakusafw-version}-directio.tar.gz
    Asakusa FrameworkをDirect I/Oと使用する場合における、Asakusa Frameworkを運用環境に展開するためのアーカイブ。
  asakusafw-${asakusafw.version}-prod-cleaner.tar.gz
    Asakusa Frameworkが提供するクリーニングツールのデプロイに使用するアーカイブ

