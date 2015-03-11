====================================
アプリケーションの実装 - Asakusa DSL
====================================

Asakusa Frameworkでアプリケーションを作成するには、
Asakusa DSLで処理の流れや処理の本体を記述します。

DSLの構成
=========

Asakusa DSLは3種類のDSLで構成されています。

Operator DSL
------------

Operator DSLは「演算子」と呼ばれるデータフロー処理の最小単位を記述するDSLです。
それぞれの演算子では単一のデータを表す「レコード」や、それらをグループ化した「グループ」に対する処理をJavaのプログラムとして記述できます。

Flow DSL
--------

Flow DSLは演算子を組み合わせてデータフローの構造を記述するDSLです。
このDSLではデータフローの構造を非循環有向グラフ (Directed Acyclic Graph: DAG)を構造の通りにそのまま記述できます。

Batch DSL
---------

Batch DSLはデータフローを組み合わせて複雑なバッチ処理の流れを記述するDSLです。
それぞれのデータフローを処理する順序を、依存関係のグラフ構造で記述できます。

関連するドキュメント
====================

..  toctree::
    :maxdepth: 1

    start-guide
    user-guide
    operators
    generic-dataflow
    developer-guide
