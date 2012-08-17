===============================================
フローDSLのコンパイル結果のグラフ化 (0.2.5以前)
===============================================
この文書では、Graphvizを使ってフローDSLのコンパイル結果をグラフ化する手順を説明します。

..  attention::
    Asakusa Frameworkのバージョン ``0.2.6`` 以降は、 :doc:`dsl-visualization` で説明している機能を使用することを推奨します。

Graphvizのインストール
======================
フローDSLのコンパイル結果からグラフPDFを生成するために、開発環境にGraphviz [#]_ をインストールします。Graphvizの詳細やインストール方法は以下のサイト等を参照して下さい。

..  [#] http://www.graphviz.org/


バッチコンパイル時に生成されるdotスクリプト
===========================================

Asakusa DSLコンパイラはフローDSLのコンパイル時に実行計画の内容を表すグラフ情報をGraphviz用のdotスクリプトファイルとして生成し、ジョブフロー単位に生成されるjarファイル（以下「ジョブフローjarファイル」）内に格納します。

ジョブフローjarファイル内には、以下のdorスクリプトが格納されます。

..  list-table:: dotスクリプトファイル一覧
    :widths: 5 5
    :header-rows: 1

    * - ファイル名
      - 説明
    * - META-INF/visualize/flowgraph.dot
      - フローの入出力を示すグラフ
    * - META-INF/visualize/stagegraph.dot
      - ステージ全体の構造を示すグラフ
    * - META-INF/visualize/stageblock-XX.dot
      - 各ステージ単位の構造を示すグラフ

dotスクリプトからグラフPDFを作成する
====================================

Graphvizを使って、上記に示すdotファイルからPDF形式でグラフを作成する方法を以下に示します。

1. バッチコンパイルを行い、ジョブフローjarファイルを生成します。
2. ジョブフローjarファイルからGrapviz用のdotファイルを取り出します。
3. dotコマンドを使用して、dotファイルをグラフPDFに変換します。

以下、example-businessプロジェクトのバッチコンパイル結果からステージ全体の構造を示すグラフPDFを生成する例を示します。

..  code-block:: sh

    # example-businessをバッチコンパイル
    cd workspace/example-business
    mvn package
    # バッチコンパイル結果の出力ディレクトリに移動
    cd target/batchc/business/lib
    # jarファイルからdotファイルを取りだす
    jar -xf jobflow-stock.jar META-INF/visualize
    # 取り出したdotファイルをPDFに変換
    dot -Tpdf -o flowgraph.pdf META-INF/visualize/flowgraph.dot
