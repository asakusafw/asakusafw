=======================
Asakusa Maven Archetype
=======================

この文書では、Asakusa Frameworkが提供しているMaven Archetypeを使ってバッチアプリケーション開発用プロジェクトを作成する手順、およびAsakusa Frameworkが提供するモデル生成ツールやバッチコンパイラをプロジェクト上から使用する方法、およびそのカスタマイズ方法について解説します。

アプリケーション開発プロジェクトの作成
======================================

Maven Archetypeからアプリケーション開発用プロジェクトを作成します。Asakusa Frameworkでは、アプリケーションを開発するために２つの方法を提供しています。

1. Asakusa Frameworkが提供するバッチアプリケーション作成用スクリプトを使用する方法
2. Mavenコマンドを使用して、段階的にプロジェクトを構築する方法

[1]の方法は各Mavenコマンドの実行をラップしたユーティリティスクリプトを使用します。コマンド１回の実行でアプリケーション開発プロジェクトの作成とAsakusa Frameworkのインストールが行われるため便利です。

バッチアプリケーション作成用スクリプトの使い方
----------------------------------------------

バッチアプリケーション作成用スクリプト setup_batchapp_project.sh はGitHub上のasakusa-contribリポジトリに置かれています。以下の手順でスクリプトを取得します。

..  code-block:: sh

    wget https://raw.github.com/asakusafw/asakusafw-contrib/master/development-utilities/scripts/setup_batchapp_project.sh
    chmod +x setup_batchapp_project.sh
    
