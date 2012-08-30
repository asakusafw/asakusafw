============================================
デプロイメントアーカイブをソースから生成する
============================================
本書では、Asakusa FrameworkのデプロイメントアーカイブをAsakusa Frameworkのソースから生成する方法について説明します。

Asakusa Frameworkのデプロイメント用アーカイブはAsakusa Frameworkが提供するアーキタイプ( :doc:`../application/maven-archetype` を参照) から作成したプロジェクト上でMavenのassemblyプラグインを実行する ( ``mvn assembly:single`` ) ことで生成しますが、Asakusa Framework本体のソースから生成することによって、手元で改変を加えたAsakusa Frameworkからデプロイメントアーカイブを生成したり、開発用ブランチのコミットからデプロイメントアーカイブを生成することが出来ます。

Asakusa Frameworkのソースアーカイブを取得
-----------------------------------------
Asakusa FrameworkのGitHubリポジトリから、Asakusa Frameworkのソースアーカイブを取得します。

GitHubからソースをダウンロードする方法はGitHubのドキュメントなどを参照してください。例として、GitHubの以下のURL [#]_  をブラウザなどで開き、zipファイルのリンクからソースアーカイブをダウンロードすることができます。

..  [#] https://github.com/asakusafw/asakusafw/tags

Asakusa Frameworkのビルド
-------------------------
ダウンロードしたソースアーカイブからAsakusa Frameworkをビルドします [#]_ 。

ダウンロードしたソースアーカイブを展開し、解凍したディレクトリ直下に含まれる ``pom.xml`` に対して ``install`` フェーズを実行し、Asakusa Frameworkの全モジュールをビルドします。

以下手順例です。

..  code-block:: sh

    unzip ~/Downloads/asakusafw-asakusafw-*.zip
    cd asakusafw-asakusafw-*
    mvn clean install -Dmaven.test.skip=true

..  [#] ビルドするマシンには Java, 及び Maven が使用できる必要があります。Java, Mavenのインストールについては、 :doc:`../introduction/start-guide` などを参照してください。


Asakusa Frameworkのデプロイメントアーカイブ生成
-----------------------------------------------
アーカイブに含まれるプロジェクト ``asakusa-distribution`` の ``pom.xml`` に対してMavenの ``assembly:single`` ゴールを実行し、Asakusa Frameworkのデプロイメントアーカイブファイルを作成します。

以下手順例です。

..  code-block:: sh

    cd distribution-project/asakusa-distribution/
    mvn clean assembly:single

ビルドが成功すると、 ``target`` ディレクトリ配下に各種のデプロイメントアーカイブが作成されます。デプロイメントアーカイブについては、以下のドキュメントなどを参照してください。

* :doc:`../application/maven-archetype`
* :doc:`../administration/deployment-with-windgate`
* :doc:`../administration/deployment-with-thundergate`
* :doc:`../administration/deployment-with-directio`
* :doc:`../administration/deployment-extension-module`

