===================
Asakusa DSLの可視化
===================
この文書では、Asakusa DSLの分析を行うにあたって、Asakusa Frameworkが提供するDSLの可視化の仕組みについて解説します。

Asakusa DSLの分析用ファイル
===========================
Asakusa DSLをバッチコンパイルして生成したバッチアプリケーションアーカイブファイル (詳しくは :ref:`maven-archetype-batch-compile` を参照)には、Asakusa DSLの分析用ファイルが含まれています。この分析用ファイルは、バッチアプリケーションの構造を把握したり、アプリケーション実行時に発生した問題箇所の特定を行うことを補助します。

分析用ファイルは、バッチアプリケーションアーカイブファイル内の ``<バッチID>/opt/dsl-analysis`` ディレクトリ配下に配置されています [#]_ 。各分析用ファイルの説明を以下に示します。なお、以下に示すファイルのうち、dotファイルについては後述の `Graphvizによるグラフの生成`_ を参照してグラフ形式として出力することも可能です。

..  list-table:: dotスクリプトファイル一覧
    :widths: 4 6
    :header-rows: 1

    * - ファイル名
      - 説明
    * - batch/compiled-structure.txt
      - バッチコンパイルした結果のバッチ構造を示すテキストファイル
    * - batch/compiled-structure.dot
      - バッチコンパイルした結果のバッチ構造を示すdotファイル
    * - batch/compiled-merged-structure.dot
      - バッチコンパイルした結果のバッチ構造を示すdotファイル。同一の演算子を単一のノードとして表現する。
    * - batch/original-structure.txt
      - バッチコンパイル前のバッチ構造を示すテキストファイル
    * - batch/original-structure.dot
      - バッチコンパイル前のバッチ構造を示すdotファイル
    * - original-merged-structure.dot
      - バッチコンパイル前のバッチ構造を示すテキストファイル。同一の演算子を単一のノードとして表現する。
    * - jobflow/<フローID>/flowgraph.dot
      - フローの入出力を示すdotファイル
    * - jobflow/<フローID>/stagegraph.dot
      - ステージ全体の構造を示すdotファイル
    * - jobflow/<フローID>/stageblock-XX.dot
      - 各ステージ単位の構造を示すdotファイル


..  [#] バッチアプリケーションアーカイブファイルを分析用ファイルを取り出すには、jarコマンド等を使用してアーカイブファイルから分析用ファイルを抽出してください。

.. _create-graph-with-graphviz:

Graphvizによるグラフの生成
==========================
グラフ生成ツールである Graphviz [#]_ を使用して、dotファイルからグラフファイルを生成する方法を説明します。

..  [#] http://www.graphviz.org/

Graphvizのインストール
----------------------
開発環境にGraphvizをインストールします。UbuntuにGraphvizをインストールする例を以下に示します。

..  code-block:: sh
    
    sudo apt-get install graphviz

Graphvizの詳細やインストール方法は上記のGraphvizのサイト等を参照して下さい。

dotスクリプトからグラフファイルを作成する
=========================================
Graphvizを使って、dotファイルからPDF形式でグラフを作成する例を以下に示します。

..  code-block:: sh

    # バッチアプリケーションアーカイブファイルを解凍する
    jar -xf example-app-batchapps-1.0-SNAPSHOT.jar
    # バッチアプリケーションアーカイブに含まれるdotファイルをPDFに変換する
    cd example.summarizeSales/opt/dsl-analysis/batch
    dot -Tpdf -o compiled-structure.pdf compiled-structure.dot 

