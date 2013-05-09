============================================
デプロイメントアーカイブをソースから生成する
============================================
本書では、Asakusa FrameworkのデプロイメントアーカイブをAsakusa Frameworkのソースから生成する方法について説明します。

Asakusa Frameworkのデプロイメント用アーカイブは通常 Framework Organizer [#]_ から生成しますが、Asakusa Framework本体のソースから生成することによって、手元で改変を加えたAsakusa Frameworkからデプロイメントアーカイブを生成したり、開発用ブランチのコミットからデプロイメントアーカイブを生成することが出来ます。

..  [#] 詳しくは、 :doc:`framework-organizer` を参照してください。

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
    mvn clean install -DskipTests

..  [#] ビルドするマシンには Java, 及び Maven が使用できる必要があります。Java, Mavenのインストールについては、 :doc:`../introduction/start-guide` などを参照してください。

..  attention::
    リリースビルドのソースに対して改変を行った場合、Asakusa Frameworkのpom.xmlが持つバージョンも合わせて変更する必要があります。

Asakusa Frameworkのデプロイメントアーカイブ生成
-----------------------------------------------
Asakusa Frameworkのデプロイメントアーカイブを生成する方法は、 :doc:`framework-organizer` を参照してください。

