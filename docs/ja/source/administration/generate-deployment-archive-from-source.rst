============================================
デプロイメントアーカイブをソースから生成する
============================================

本書では、Asakusa FrameworkのデプロイメントアーカイブをAsakusa Frameworkのソースから生成する方法について説明します。

Asakusa Frameworkのソースアーカイブを取得
-----------------------------------------

Asakusa FrameworkのGitHubリポジトリから、Asakusa Frameworkのソースアーカイブを取得します。

GitHubからソースをダウンロードする方法はGitHubのドキュメントなどを参照してください。
例として、GitHubの以下のURL [#]_ をブラウザなどで開き、zipファイルのリンクからソースアーカイブをダウンロードすることができます。

..  [#] https://github.com/asakusafw/asakusafw/tags

Asakusa Frameworkのビルド
-------------------------

ダウンロードしたソースアーカイブからAsakusa Frameworkをビルドします。

ビルドするマシンには JDK, 及びMavenが使用できる必要があります。
Java, Mavenのインストールについては、 各ツールのドキュメントを参照してください。

ダウンロードしたソースアーカイブを展開し、解凍したディレクトリ直下に含まれる :file:`pom.xml` に対して :program:`install` フェーズを実行し、Asakusa Frameworkの全モジュールをビルドします。

以下手順例です。

..  code-block:: sh

    unzip ~/Downloads/asakusafw-asakusafw-*.zip
    cd asakusafw-asakusafw-*
    mvn clean install -DskipTests

..  attention::
    リリースビルドのソースに対して改変を行った場合、Asakusa Frameworkの :file:`pom.xml` に指定されているつバージョンも合わせて変更する必要があります。

