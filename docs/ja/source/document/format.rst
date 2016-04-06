==========================
ドキュメンテーションの構成
==========================

プロジェクトの構成
==================

ドキュメンテーションのプロジェクトを :file:`<root>/docs` に配置する。

ルートディレクトリの構成
------------------------

プロジェクトのルートディレクトリは、以下のような構成とする

..  list-table:: プロジェクトディレクトリの構成
    :widths: 1 5
    :header-rows: 1

    * - パス 
      - 内容 
    * - :file:`source/`
      - ドキュメントのソースファイルを格納するディレクトリ
    * - :file:`build/`
      - ビルド結果を格納するディレクトリ (自動生成)
    * - :file:`README`
      - プロジェクトについての解説
    * - :file:`LICENSE`
      - ライセンス情報
    * - :file:`make.bat`
      - Windows環境でドキュメントをビルドするバッチ
    * - :file:`Makefile`
      - Unix系環境でドキュメントをビルドするmakeファイル

ソースディレクトリの構成
------------------------

プロジェクトのソースディレクトリは、以下のような構成とする。

..  list-table:: ソースディレクトリの構成
    :widths: 2 5
    :header-rows: 1

    * - パス 
      - 内容 
    * - :file:`_static`
      - 静的ファイルを配置するディレクトリ
    * - :file:`conf.py`
      - ドキュメントの構成設定情報
    * - :file:`index.rst`
      - プロジェクトのマスタードキュメント
    * - :file:`<component>/index.rst`
      - コンポーネントごとのマスタードキュメント

コンポーネント
==============

ソースディレクトリ以下にコンポーネントごとにディレクトリを作成し、関連するドキュメントを配置する。

..  list-table:: コンポーネントの例
    :widths: 1 4 10
    :header-rows: 1

    * - パス
      - コンポーネント
      - 内容
    * - :file:`./`
      - フレームワーク
      - インデックスやリリースノート、サイトマップ等
    * - :file:`intruduction/`
      - フレームワークの紹介
      - フレームワークの概要説明、入門ドキュメント、開発の流れ
    * - :file:`application/`
      - アプリケーション開発環境の整備
      - 開発環境構築手順やバッチアプリケーションのビルド手順等
    * - :file:`dmdl/`
      - Asakusa Data Model
      - DMDLおよびDMDLコンパイラ
    * - :file:`dsl/`
      - Asakusa DSL
      - 各種DSLおよびコンパイラ
    * - :file:`testing/`
      - TestDriver
      - テストドライバー
    * - :file:`directio/`
      - Direct I/O
      - Direct I/O
    * - :file:`windgate/`
      - WindGate
      - WindGate
    * - :file:`thundergate/`
      - ThunderGate
      - ThunderGate
    * - :file:`yaess/`
      - YAESS
      - YAESS
    * - :file:`administration/`
      - 運用環境の整備
      - 運用環境へのデプロイメント手順等
    * - :file:`product/`
      - プロダクトについて
      - ライセンス条項やFAQ、対応プラットフォーム等
    * - :file:`sandbox/`
      - Sandbox
      - Sandboxプロジェクト
    * - :file:`documentation/`
      - Documentation
      - ドキュメントの書き方等 (内部向け)

ドキュメントの形式
==================

ドキュメントは Sphinx_ でビルド可能な reStructuredText_ 形式で記述し、拡張子は :file:`*.rst` とする。

..  _Sphinx : http://sphinx.pocoo.org/
..  _reStructuredText : http://docutils.sourceforge.net/rst.html

日本語
------

基本的には「ですます」で記述し、仕様書等は「だである」で記述する。

ドキュメントのターゲット
------------------------

以下のうち誰を対象とするかを想定すること。

* User (U): Asakusa Frameworkを利用してバッチアプリケーションを開発する人
* Administrator (A): Asakusa Frameworkを利用して開発されたバッチアプリケーションを運用する人
* Manager (M): Asakusa Frameworkを利用してバッチアプリケーションを開発させる人
* Developer (D): Asakusa Frameworkそのものを読んだり、拡張ポイントを利用して拡張したりする人
* Insider (I): Asakusa Frameworkそのものを開発する人

ドキュメントファイルの命名規則
------------------------------

ファイル名の規則は以下のとおり。

* ドキュメント名は小文字アルファベット、数字、ハイフンのみから構成
* 同じ内容で言語の異なるドキュメント名は一致させる

標準的なドキュメント名
----------------------

ありえそうなドキュメントの例。
下記に該当するドキュメントは、可能な限り名前をそろえる。下記に該当しないドキュメントは、命名規則の範囲で自由に名前をつけてよい。

..  list-table:: 標準的なドキュメント名
    :widths: 3 2 10
    :header-rows: 1

    * - ドキュメント名
      - 想定対象
      - 内容
    * - index
      - 全員
      - モジュールの概要や他のドキュメントの参照
    * - faq
      - 全員
      - FAQ的なもの
    * - start-guide
      - U, M
      - 最も簡素な方法でモジュールを利用する手順
    * - user-guide
      - U
      - バッチアプリケーション開発時に必要なモジュールの公開機能を可能な限り網羅したマニュアル
    * - developer-guide
      - D
      - Asakusa Frameworkの拡張ポイントを利用した拡張ガイド
    * - administrator-guide
      - U, A
      - 運用に関するマニュアル
    * - log-table
      - U, A
      - ログ一覧 (コンポーネントに関してまとめて出す)
    * - with-X
      - U, Aなど
      - 他のコンポーネントXとの連携方法
    * - [X-]specification
      - I
      - モジュール[のX (language, extensionなど)]に関連する設計書または仕様書

