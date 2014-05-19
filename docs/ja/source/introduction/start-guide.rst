================================
Asakusa Framework スタートガイド
================================
この文書では、Asakusa Frameworkをはじめて利用するユーザ向けに、Asakusa Frameworkの開発環境を作成し、その環境でサンプルアプリケーションを動かすまでの手順を説明します。

なお、この文書では開発環境の構築に必要となる各種ソフトウェアのバージョンは明記していません。Asakusa Frameworkが動作検証を行っている各種ソフトウェアのバージョンについては、 :doc:`../product/target-platform` を確認してください。

.. _startguide-development-environment:

開発環境の構築
==============
Asakusa FrameworkはLinux OS上に開発環境を構築して利用します。WindowsPC上で開発を行う場合、Windows上でLinuxの仮想マシンを実行し、ここで開発を行うと便利です。

このスタートガイドでは仮想マシンの実行ソフトウェアとして `VMWare Player`_ 、仮想マシンに使用するOSとして `Ubuntu 12.04 Desktop (日本語 Remix CD x86用)`_ を使用し、この環境に必要なソフトウェアをセットアップする手順を説明します。

..  _`VMWare Player`: http://www.vmware.com/jp/products/player/
..  _`Ubuntu 12.04 Desktop (日本語 Remix CD x86用)`: http://www.ubuntulinux.jp/download/ja-remix 

..  tip::
    開発環境の構築については、ここで説明するセットアップ手順を実施するほか、Asakusa Frmameworkの開発環境を手軽に構築するインストーラパッケージである Jinrikisha を利用する方法もあります。
    
    * :jinrikisha:`Jinrikisha (人力車) - Asakusa Framework Starter Package - <index.html>`
     
    Jinrikisha を使ってインストールする場合、本書の :ref:`install-ubuntu` までの手順を実施し、その後は Jinrikisha のドキュメントに従って開発環境を構築することができます。
    
    なお Jinrikisha ではインストール環境にJava(JDK)がインストールされていない場合、OpenJDKを簡易にインストールする機能が備わっていますが、試用目的以外でAsakusa Frameworkを使用する場合は 本書の :ref:`install-java` の手順を参考にしてOracleJDKをインストールした後に Jinrikisha のドキュメント に従って開発環境を構築することを推奨します。

VMWare Playerのインストール
---------------------------
VMWare Playerをダウンロードし、インストールを行います。

VMWare Playerのダウンロードサイト (http://www.vmware.com/go/get-player-jp) からVMWare Player (Windows用) をダウンロードします。

ダウンロードしたインストーラを実行し、インストール画面の指示に従ってVMWare Playerをインストールします。

.. _install-ubuntu:

Ubuntu Desktop のインストール
-----------------------------
Ubuntu Desktopをダウンロードし、インストールを行います。

Ubuntu Desktop 日本語 Remix CDのダウンロードサイト (http://www.ubuntulinux.jp/download/ja-remix) からisoファイル(CDイメージ)をダウンロードします。

ダウンロードが完了したらVMWare Playerを起動し、以下の手順に従ってUbuntu Desktopをインストールします。

1. メニューから「新規仮想マシンの作成」を選択します、
2. インストール元の選択画面では「後でOSをインストール」を選択し [#]_ 、次へ進みます。
3. ゲストOSの選択で「Linux」を選択し、バージョンに「Ubuntu」を選択して次へ進みます。
4. 仮想マシン名の入力では、任意の仮想マシン名と保存場所を指定して、次へ進みます。
5. ディスク容量の指定は任意です。デフォルトの「20GB」はAsakusa Frameworkの開発を試すには十分な容量です。お使いの環境に合わせて設定し、次へ進みます。
6. 仮想マシン作成準備画面で「ハードウェアをカスタマイズ」を選択します。デバイス一覧から「新規 CD/DVD(IDE)」を選択後、画面右側の「ISOイメージファイルを使用する」を選択し、参照ボタンを押下してダウンロードしたUbuntu Desktopのisoファイルを選択します。その他の設定は環境に合わせて設定してください。設定が完了したら画面下の閉じるボタンを押します。
7. 完了ボタンを押して仮想マシンを作成後、仮想マシンを起動すると、Ubuntu Desktopのインストールが開始します。インストール画面の指示に従ってUbuntu Desktopをインストールします。

Ubuntu Desktopが起動したら、同梱のブラウザなどを使用してUbuntuからインターネットにできることを確認してください。以後の手順ではインターネットに接続できることを前提とします。

また、以降の手順で使用するホームフォルダ直下のダウンロードディレクトリを日本語名から英語に変更するため、ターミナルを開いて [#]_ 以下のコマンドを実行します。

..  code-block:: sh

    LANG=C xdg-user-dirs-gtk-update

ダイアログが開いたら ``Don't ask me this again``  (次回からチェックしない) 」にチェックを入れ、 ``Update Names`` ボタンを押下します。

そのほか、必須の手順ではないですがここでVMWare Toolsをインストールしておくとよいでしょう [#]_ 。

..  [#] ここで「インストーラ ディスク イメージ ファイル」を選択し、isoファイルを選択するとOSの「簡易インストール」が行われますが、簡易インストールでは日本語環境がインストールされないほか、いくつかの設定が適切に行われないため、簡易インストールの使用は推奨しません。
..  [#] ターミナルを開くにはデスクトップ上で ``[ctrl]`` + ``[alt]`` + ``[t]`` キーを押すか、画面左上のDashメニューを選択し、検索ボックスに ``terminal`` と入力後、画面下に表示される ``端末`` を選択します。
..  [#] VMWare Toolsのインストールについては VMWare Playerのドキュメントなどを参照してください。

.. _install-java:

Java(JDK)のインストール
-----------------------
Hadoop、及びAsakusa Frameworkの実行に使用するJavaをインストールします。

ブラウザを開き、Javaのダウンロードサイト (http://www.oracle.com/technetwork/java/javase/downloads/index.html) から、JDK 7 の インストールアーカイブ ``jdk-7uXX-linux-i586.tar.gz`` ( ``XX`` はUpdate番号) をダウンロードします [#]_ 。この文書では、ブラウザ標準のダウンロードディレクトリ  ``~/Downloads`` にダウンロードしたものとして説明を進めます。

ダウンロードが完了したら、以下の例を参考にしてJDKをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf jdk-7u*-linux-i586.tar.gz
    sudo chown -R root:root jdk1.7.0_*/
    sudo mkdir /usr/lib/jvm
    sudo mv jdk1.7.0_*/ /usr/lib/jvm
    sudo ln -s /usr/lib/jvm/jdk1.7.0_* /usr/lib/jvm/java-7-oracle

..  [#] 本スタートガイドの環境に従う場合は、x64版用のファイル( ``jdk-7uXX-linux-x64.tar.gz`` )や、RPM版のファイル( ``jdk-7uXX-linux-i586.rpm`` ) をダウンロードしないよう注意してください。

Hadoopのインストール
--------------------
`Apache Hadoop`_ をインストールします。

Apache Hadoopのインストール方法はOS毎に提供されているインストールパッケージを使う方法や、tarballを展開する方法などがありますが、ここではtarballを展開する方法でインストールします。

Apache Hadoopのダウンロードサイト (http://www.apache.org/dyn/closer.cgi/hadoop/common/) から Hadoop本体のコンポーネントのtarball ``hadoop-1.2.X.tar.gz`` ( ``X`` はバージョン番号 )  をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてApache Hadoopをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf hadoop-*.tar.gz
    sudo chown -R root:root hadoop-*/
    sudo mv hadoop-*/ /usr/lib
    sudo ln -s /usr/lib/hadoop-* /usr/lib/hadoop

..  _`Apache Hadoop`: http://hadoop.apache.org/

環境変数の設定
--------------
Asakusa Frameworkの利用に必要となる環境変数を設定します。

``~/.profile`` をエディタで開き、最下行に以下の定義を追加します。

..  code-block:: sh

    export JAVA_HOME=/usr/lib/jvm/java-7-oracle
    export ASAKUSA_HOME=$HOME/asakusa
    export PATH=$JAVA_HOME/bin:$PATH:/usr/lib/hadoop/bin

``~/.profile`` を保存した後、設定した環境変数をターミナル上のシェルに反映させるため、以下のコマンドを実行します。

..  code-block:: sh

    . ~/.profile

開発用Asakusa Frameworkのインストール
-------------------------------------
Asakusa Frameworkをインストールします。

Asakusa Frameworkを開発環境にインストールするには、
まずAsakusa Frameworkアプリケーション用プロジェクトテンプレートをダウンロードし、
これに含まれるビルドツール `Gradle`_ のAsakusa Frameworkインストール用タスクを実行します。

この文書では基本的なプロジェクトレイアウトのみを持つプロジェクトテンプレートに
サンプルアプリケーションを同梱したサンプルアプリケーションプロジェクトを利用します。
サンプルアプリケーションプロジェクトは以下からダウンロードします。

* `asakusa-example-project-0.6.2.tar.gz <http://www.asakusafw.com/download/gradle-plugin/asakusa-example-project-0.6.2.tar.gz>`_ 

ダウンロードが完了したら、サンプルアプリケーションプロジェクトを任意のディレクトリに配置します。

ここでは ``$HOME/workspace`` 配下に配置するため、まずこのディレクトリを作成します。

..  code-block:: sh
    
    mkdir ~/workspace

``$HOME/workspace`` 配下に ``example-app`` というディレクトリ名でサンプルアプリケーションを配置します。

..  code-block:: sh
    
    cd ~/Downloads 
    tar xf asakusa-example-project-*.tar.gz
    mv asakusa-example-project ~/workspace/example-app

配下したサンプルアプリケーションプロジェクト上で、
以下の例を参考にしてAsakusa FrameworkをインストールするGradleタスクを実行します。
インストールが成功すると、 ``$ASAKUSA_HOME`` 配下に Asakusa Frameworkがインストールされます。

..  code-block:: sh
     
    cd ~/workspace/example-app
    ./gradlew installAsakusafw

インストールに成功した場合、画面に以下のように ``BUILD SUCCESSFUL`` と表示されます。

..  code-block:: sh

    ...
    Asakusa Framework has been installed on ASAKUSA_HOME: /home/asakusa/asakusa

    BUILD SUCCESSFUL

    Total time: XX.XXX secs

..  note::
    以降の手順についても、Gradleのコマンド実行後に処理が成功したかを確認するには ``BUILD SUCCESSFUL`` が表示されていることを確認してください。

..  _`Gradle`: http://gradle.org

インストールソフトウェアの動作確認
----------------------------------
これまでの手順でインストールしたソフトウェアの動作確認を行います。

以下の例を参考にして、ターミナルからコマンドを実行し、例の通りの出力が行われることを確認してください。
コマンドが見つからないと表示された場合には、それぞれのインストール手順や `環境変数の設定`_ を見直してください。

Javaの動作確認
~~~~~~~~~~~~~~

..  code-block:: sh

    java -version

    java version "1.7.0_45"
    ...

Java SDKの動作確認
~~~~~~~~~~~~~~~~~~

..  code-block:: sh

    javac -version

    javac 1.7.0_45

Hadoopの動作確認
~~~~~~~~~~~~~~~~

..  code-block:: sh

    hadoop version

    Hadoop 1.2.1
    ...

..  attention::
    Hadoopのみバージョンを確認するためのコマンドが ``hadoop version`` となっていて、 ``version`` の前にハイフンが不要です。

Asakusa Frameworkのインストール確認
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

..  code-block:: sh
    
    cat $ASAKUSA_HOME/VERSION
    
    asakusafw.version=0.6.2
    
    asakusafw.build.timestamp=...
    asakusafw.build.java.version=1.6.0_...


Eclipseの環境構築
-----------------
Asakusa Frameworkのアプリケーション実装・テストに使用する統合開発環境(IDE)として、Eclipseの環境を構築します。

..  note::
    Asakusa Frameworkを使う上でEclipseの使用は必須ではありませんが、Asakusa FrameworkではEclipse上での開発をサポートするいくつかの機能を提供しています。ここではサンプルアプリケーションのソースを確認するなどの用途を想定して、Eclipseの環境構築手順を説明します。

Eclipseのインストール
~~~~~~~~~~~~~~~~~~~~~
Eclipseのダウンロードサイト (http://www.eclipse.org/downloads/) から Eclipse IDE for Java Developers - Linux 32 Bit ``eclipse-java-XX-linux-gtk.tar.gz`` ( ``XX`` はバージョンを表すコード名 )  をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてEclipseをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf eclipse-java-*-linux-gtk.tar.gz
    mv eclipse ~/eclipse

Eclipseを起動するには、 ``$HOME/eclipse/eclipse`` を実行します。以下はターミナルから起動する例です。

..  code-block:: sh

    $HOME/eclipse/eclipse &

..  attention::
    デスクトップ上のファイラーなどからEclipseを起動する場合は、デスクトップ環境に対して ``~/.profile`` で定義した環境変数が反映されている必要がるため、Eclipseを起動する前に一度デスクトップ環境からログアウトし、再ログインする必要があります。

Eclipse起動時にワークスペースを指定するダイアログが表示されるので、デフォルトの ``$HOME/workspace`` をそのまま指定します。

Eclipseへアプリケーションプロジェクトをインポート
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
アプリケーションプロジェクトをEclipseへインポートして、Eclipse上でアプリケーションの開発を行えるようにします。

インポートするプロジェクトのディレクトリに移動し、Gradleの以下のコマンドを実行してEclipse用の定義ファイルを作成します。

..  code-block:: sh

    cd ~/workspace/example-app
    ./gradlew eclipse

これでEclipseからプロジェクトをインポート出来る状態になりました。Eclipseのメニューから ``[File]`` -> ``[Import]`` -> ``[General]`` -> ``[Existing Projects into Workspace]`` を選択し、プロジェクトディレクトリを指定してEclipseにインポートします。

.. _startguide-running-example:

サンプルアプリケーションの実行
==============================
開発環境上で Asakusa Framework のサンプルアプリケーションを実行してみます。

サンプルアプリケーションの概要
------------------------------
サンプルアプリケーションプロジェクトには、サンプルアプリケーション「カテゴリー別売上金額集計バッチ」のソースファイルが含まれています。

カテゴリー別売上金額集計バッチは、売上トランザクションデータと、商品マスタ、店舗マスタを入力として、エラーチェックを行った後、売上データを商品マスタのカテゴリ毎に集計するアプリケーションです。

バッチアプリケーションの入力データ取得と出力データ生成には、Asakusa Frameworkの「Direct I/O」と呼ばれるコンポーネントを利用しています。Direct I/Oを利用して、Hadoopファイルシステム上のCSVファイルに対して入出力を行います。


サンプルアプリケーションのビルド
--------------------------------
アプリケーションのソースファイルをAsakusa Framework上で実行可能な形式にビルドします。

アプリケーションのビルドを実行するには、Gradleの ``build`` タスクを実行します（初回の実行時のみ、Gradleがリモートからライブラリをダウンロードするため、実行に時間がかかります）。

..  code-block:: sh

    cd ~/workspace/example-app
    ./gradlew build

このコマンドの実行によって、アプリケーションのプロジェクトに対して以下の処理が実行されます。

1. データモデル定義DSL(DMDL)から、データモデルクラスを生成
2. Asakusa DSLとデータモデル定義DSLから、実行可能なプログラム群（HadoopのMapReduceジョブなど)を生成
3. 実行可能なプログラム群に対するテストを実行
4. アプリケーションを実行環境に配置するためのデプロイメントアーカイブファイルを生成

ビルドが成功すると、プロジェクトの ``build`` ディレクトリ配下にいくつかのファイルが作成されますが、この中の ``example-app-batchapps.jar`` というファイルがサンプルアプリケーションが含まれるデプロイメントアーカイブファイルです。

..  note::
    このアーカイブファイルの名前は、プロジェクトディレクトリ名やビルドスクリプト上に設定したバージョンなどから決定されます。本ドキュメントの例以外のプロジェクト名やバージョンを指定した場合は、それに合わせて読み替えてください。
    
.. _introduction-start-guide-deploy-app:

サンプルアプリケーションのデプロイ
----------------------------------
サンプルアプリケーションを実行するために、先ほどビルドしたサンプルアプリケーションを実行環境にデプロイします。

実行環境は、通常はHadoopクラスターが構築されている運用環境となりますが、ここでは開発環境（ローカル）上のHadoopとAsakusa Framework上でサンプルアプリケーションを実行するため、ローカルに対するデプロイを行います。

アプリケーションのデプロイは、Asakusa Frameworkがインストールされているマシン上の ``$ASAKUSA_HOME/batchapps`` ディレクトリに アプリケーションが含まれるjarファイルの中身を展開して配置します。以下はアプリケーションプロジェクトで生成したアーカイブファイルをローカルのAsakusa Frameworkにデプロイする例です。

..  code-block:: sh

    cd ~/workspace/example-app
    cp build/*batchapps*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar xf *batchapps*.jar


サンプルデータの配置
--------------------
サンプルアプリケーションプロジェクトには、プロジェクトディレクトリ配下の ``src/test/example-dataset`` ディレクトリ以下にテスト用の入力データが用意されています。これらのファイルをHadoopファイルシステム上のDirect I/Oの入出力ディレクトリ(デフォルトの設定では ``target/testing/directio`` 配下にコピーします。

..  warning::
    Direct I/Oの入出力ディレクトリはテスト実行時に削除されます。特にスタンドアロンモードのHadoopを利用時にデフォルトの設定のような相対パスを指定した場合、 ホームディレクトリを起点としたパスと解釈されるため注意が必要です。
    
    例えばホームディレクトリが ``/home/asakusa`` であった場合でデフォルト設定の相対パスを利用する場合、 テスト実行の都度 ``/home/asakusa/target/testing/directio`` ディレクトリ以下が削除されることになります。このパスに重要なデータがないことを実行前に確認してください。

以下はサンプルデータの配置の実行例です。

..  code-block:: sh
    
    # スタンドアロンモードに対応するため、ホームディレクトリに移動しておく
    cd ~
    # ファイルシステムパス上のデータをクリアしておく
    hadoop fs -rmr target/testing/directio
    # サンプルデータを配置する
    hadoop fs -put ~/workspace/example-app/src/test/example-dataset/master target/testing/directio/master
    hadoop fs -put ~/workspace/example-app/src/test/example-dataset/sales target/testing/directio/sales
    
.. _introduction-start-guide-run-app:

サンプルアプリケーションの実行
------------------------------
ローカルにデプロイしたサンプルアプリケーションを実行します。

Asakusa Frameworkでは、バッチアプリケーションを実行するためのコマンドプログラムとして「YAESS」というツールが提供されています。
バッチアプリケーションを実行するには、 ``$ASAKUSA_HOME/yaess/bin/yaess-batch.sh`` に実行するバッチの
バッチIDとバッチ引数を指定します。

サンプルアプリケーション「カテゴリー別売上金額集計バッチ」は「 ``example.summarizeSales`` 」というバッチIDを持っています。
また、このバッチは引数に処理対象の売上日時( ``date`` )を指定し、この値に基づいて処理対象CSVファイルを特定します。

バッチIDとバッチ引数を指定して、以下のようにバッチアプリケーションを実行します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

バッチの実行が成功すると、コマンドの標準出力の最終行に ``Finished: SUCCESS`` と出力されます。

..  code-block:: sh

    ...
    2013/04/22 13:50:35 INFO  [YS-CORE-I01999] Finishing batch "example.summarizeSales": batchId=example.summarizeSales, elapsed=12,712ms
    2013/04/22 13:50:35 INFO  [YS-BOOTSTRAP-I00999] Exiting YAESS: code=0, elapsed=12,798ms
    Finished: SUCCESS

サンプルアプリケーション実行結果の確認
--------------------------------------
Asakusa FrameworkはDirect I/Oの入出力ディレクトリやファイルの一覧をリストアップするコマンド ``$ASAKUSA_HOME/directio/bin/list-file.sh`` を提供しています。このコマンドを利用して、サンプルアプリケーションの出力結果を確認します。

ここでは、Direct I/Oの入出力ディレクトリにサンプルアプリケーションが出力データを配置したパス ``result`` 以下のすべてのファイルを、サブディレクトリ含めてリストするようコマンドを実行してみます。

..  code-block:: sh

    $ASAKUSA_HOME/directio/bin/list-file.sh result "**/*"
.. ***

上記のコマンドを実行すると、以下のような結果が表示されます。

..  code-block:: sh
     
    Starting List Direct I/O Files:
     Hadoop Command: /usr/lib/hadoop/bin/hadoop
              Class: com.asakusafw.directio.tools.DirectIoList
          Libraries: /home/asakusa/asakusa/directio/lib/asakusa-directio-tools-X.X.X.jar,...
          Arguments: result **/*
    file:/home/asakusa/target/testing/directio/result/category
    file:/home/asakusa/target/testing/directio/result/error
    file:/home/asakusa/target/testing/directio/result/error/20110401.csv
    file:/home/asakusa/target/testing/directio/result/category/result.csv

出力ファイルの一覧に対して、
``hadoop fs -text`` コマンドを利用してファイル内容を確認します。
以下は ``result`` 配下に生成された売上データの集計ファイル ``category/result.csv`` を表示する例です。

..  code-block:: sh
    
    hadoop fs -text file:/home/asakusa/target/testing/directio/result/category/result.csv

指定したファイルの内容が表示されます。
売上データが商品マスタのカテゴリコード単位で集計され、売上合計の降順で整列されたCSVが出力されています。

..  code-block:: sh
    
    カテゴリコード,販売数量,売上合計
    1600,28,5400
    1300,12,1596
    1401,15,1470

Next Step:アプリケーションの開発を行う
======================================
これまでの手順で、Asakusa Framework上でバッチアプリケーションの開発を行う準備が整いました。

次に、アプリケーションの開発を行うために、Asakusa Frameworkを使ったアプリケーション開発の流れを見てみましょう。 >> :doc:`next-step`

