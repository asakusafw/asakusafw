=======================
Asakusa Maven Archetype
=======================

この文書では、Asakusa Frameworkが提供しているMaven Archetypeを使ってバッチアプリケーション開発用プロジェクトを作成する手順、およびAsakusa Frameworkが提供するモデル生成ツールやバッチコンパイラをプロジェクト上から使用する方法、およびそのカスタマイズ方法について解説します。

アプリケーション開発プロジェクトの作成
======================================
Maven Archetypeからアプリケーション開発用プロジェクトを作成します。Asakusa Frameworkでは、アプリケーションを開発するために２つの方法を提供しています。

1. Asakusa Frameworkが提供するバッチアプリケーション作成用スクリプトを使用する方法
2. Mavenコマンドを使用して、段階的にプロジェクトを構築する方法

バッチアプリケーション作成用スクリプト
--------------------------------------
バッチアプリケーション作成用スクリプトはアプリケーション開発環境構築用のMavenコマンドのラップスクリプトで、本スクリプトを使用すると１回のコマンド実行でアプリケーション開発用プロジェクトの作成とAsakusa Frameworkのインストールが行われるため便利です。このコマンドは $HOME/worspace 配下にプロジェクトディレクトリを作成します。

バッチアプリケーション作成用スクリプト setup_batchapp_project.sh はGitHub上のasakusa-contribリポジトリに置かれています。以下の手順でスクリプトを取得します。

..  code-block:: sh

    wget https://raw.github.com/asakusafw/asakusafw-contrib/master/development-utilities/scripts/setup_batchapp_project.sh
    chmod +x setup_batchapp_project.sh

setup_batchapp_project.shは以下の引数を指定して実行します。

..  list-table:: バッチアプリケーション作成用スクリプトの引数
    :widths: 1 9
    :header-rows: 1
    
    * - no
      - 説明
    * - 1
      - グループID (パッケージ名)
    * - 2
      - アーティファクトID (プロジェクト名)
    * - 3
      - Asakusa Frameworkのpom.xml上のVersion [#]_ 
      
..  [#] 指定可能なVersionは次のアーキタイプカタログを参照:https://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml
    
例えばAsakusa Framework 0.2.0(SNAPSHOT)を使ったアプリケーションプロジェクトを作成する場合は以下のように実行します。この例では $HOME/workspace 配下に batchapp-sample プロジェクトディレクトリが作成されます。

..  code-block:: sh

    ./setup_batchapp_project.sh com.example batchapp-sample 0.2.0-SNAPSHOT

プロジェクトの作成用Mavenコマンド
---------------------------------
バッチアプリケーション作成用スクリプトを使わず、Mavenコマンドを使用して段階的にプロジェクトを構築する場合は以下の手順で実行します。

archetype:generate
~~~~~~~~~~~~~~~~~~
Asakusa Frameworkが公開しているMavenアーキタイプカタログからAsakusa Frameworkアプリケーション開発用のプロジェクトを作成します。

アーキタイプカタログのURL:http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml

以下にプロジェクト作成例を示します。

..  code-block:: sh

    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml
    ...
    Choose archetype:
    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> asakusa-archetype-batchapp (-)
    Choose a number: : ※1を入力
    ...
    Choose version: 
    1: 0.1.0
    2: 0.1.1-SNAPSHOT
    3: 0.2.0-SNAPSHOT

    Choose a number: 3: ※3を入力
    ...
    Define value for property 'groupId': : com.example ※任意の値を入力
    Define value for property 'artifactId': : batchapp-sample ※任意の値を入力
    Define value for property 'version':  1.0-SNAPSHOT ※任意の値を入力
    Define value for property 'package':  com.example ※任意の値を入力
    ...
    Y: : Yを入力

assembly:single
~~~~~~~~~~~~~~~
アーキタイプから作成したpom.xmlのassembly:singleゴールを実行すると、Asakusa Framework本体のインストール用アーカイブがtarget配下に作成されます。後述するantrun:runゴールと組み合わせて、Asakusa Frameworkをローカル環境にインストールするために使用します。

antrun:run
~~~~~~~~~~
assembly:singleゴールで作成したAsakusa Framework本体のインストール用アーカイブを使用して、$ASAKUSA_HOME配下にAsakusa Frameworkをインストールします。

assembly:singleとantrun:runを組み合わせて、以下のようにAsakusa Frameworkをローカル環境にインストールします。

..  code-block:: sh

    cd batchapp-sample
    mvn assembly:single antrun:run

