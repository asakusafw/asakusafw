================================
Asakusa Framework スタートガイド
================================
この文書では、Asakusa Frameworkをはじめて利用するユーザ向けに、Asakusa Frameworkの開発環境を作成し、その環境でサンプルアプリケーションを動かすまでの手順を説明します。

開発環境の構築
==============
Asakusa FrameworkはLinux OS上に開発環境を構築して利用します。WindowsPC上で開発を行う場合、Windows上でLinuxの仮想マシンを実行し、ここで開発を行うと便利です。

このスタートガイドでは仮想マシンの実行ソフトウェアとして `VMWare Player`_ 、仮想マシンに使用するOSとして `Ubuntu 11.10 Desktop (日本語 Remix CD x86用)`_ を使用し、この環境に必要なソフトウェアをセットアップする手順を説明します。

..  _`VMWare Player`: http://www.vmware.com/jp/products/desktop_virtualization/player/overview 
..  _`Ubuntu 11.10 Desktop (日本語 Remix CD x86用)`: http://www.ubuntulinux.jp/News/ubuntu1110-desktop-ja-remix

VMWare Playerのインストール
---------------------------
VMWare Playerをダウンロードし、インストールを行います。

VMWare Playerのダウンロードサイト (http://www.vmware.com/go/get-player-jp) からVMWare Player (Windows用) をダウンロードします。

ダウンロードしたインストーラを実行し、インストール画面の指示に従ってVMWare Playerをインストールします。

Ubuntu Desktop のインストール
-----------------------------
Ubuntu Desktopをダウンロードし、インストールを行います。

Ubuntu Desktop 日本語 Remix CDのダウンロードサイト (http://www.ubuntulinux.jp/products/JA-Localized/download) からisoファイル(CDイメージ)をダウンロードします。

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

ダイアログが開いたら「次回からチェックしない」にチェックを入れ、「Update Names」を選択します。

そのほか、必須の手順ではないですがここでVMWare Toolsをインストールしておくとよいでしょう。

..  [#] ここで「インストーラ ディスク イメージ ファイル」を選択し、isoファイルを選択するとOSの「簡易インストール」が行われますが、簡易インストールでは日本語環境がインストールされないほか、いくつかの設定が適切に行われないため、簡易インストールの使用は推奨しません。
..  [#] [ctrl] + [alt] + [t] キーでターミナルが起動します。


Java(JDK)のインストール
-----------------------
Hadoop、及びAsakusa Frameworkの実行に使用するJavaをインストールします。

Javaのダウンロードサイト (http://www.oracle.com/technetwork/java/javase/downloads/index.html) から、Java SE 6 の JDK をダウンロードします [#]_ 。

ダウンロードが完了したら、以下の例を参考にしてJavaをインストールします
(標準の ``~/Downloads`` にダウンロードした場合の例です)。

..  code-block:: sh

    cd ~/Downloads
    chmod +x jdk-*
    ./jdk-*

    ...
    ※ライセンス条項が表示されるため確認し、同意します。　　
    
    sudo mkdir /usr/lib/jvm
    sudo chown -R root:root jdk1.6.0_*/
    sudo mv jdk1.6.0_*/ /usr/lib/jvm

    cd /usr/lib/jvm
    sudo ln -s jdk1.6.0_* jdk-6

..  [#] ダウンロードするファイルは「jdk-6uXX-linux-i586.bin」(XXはUpdate番号) です。本スタートガイドの環境に従う場合は、x64版(xx-ia64.bin)や、RPM版のファイル(xx-rpm.bin)をダウンロードしないよう注意してください。

このほかに環境変数の設定が必要ですが、本手順では後ほどまとめて設定するため、このまま次に進みます。

Mavenのインストール
-------------------
Asakusa Frameworkの開発環境に必要なビルドツールであるMavenをインストールします。

Mavenのダウンロードサイト (http://maven.apache.org/download.html) から Maven3 のtarball (apache-maven-3.X.X-bin.tar.gz) をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてMavenをインストールします
(標準の ``~/Downloads`` にダウンロードした場合の例です)。

..  code-block:: sh

    cd ~/Downloads
    tar xf apache-maven-*-bin.tar.gz
    sudo chown -R root:root apache-maven-*/
    sudo mv apache-maven-*/ /usr/local/lib
    sudo ln -s /usr/local/lib/apache-maven-*/bin/mvn /usr/local/bin/mvn

..  note:: 
    インターネットへの接続にプロキシサーバを経由する必要がある環境については、Mavenに対してプロキシの設定を行う必要があります。Mavenのプロキシ設定については、Mavenの次のサイト等を確認してください。

    http://maven.apache.org/guides/mini/guide-proxies.html

Hadoopのインストール
--------------------
Clouderaから提供されているHadoopのディストリビューションである `Cloudera's Distribution including Apache Hadoop Version 3 (CDH3)`_ をインストールします。

CDH3のインストール方法はOS毎に提供されているインストールパッケージを使う方法と、tarballを展開する方法がありますが、ここではtarballを展開する方法でインストールします。

CDH3のtarballのダウンロードサイト (https://ccp.cloudera.com/display/SUPPORT/CDH3+Downloadable+Tarballs) から CDH3 のHadoopのコンポーネント(Hadoop 0.20.2+XXX) (hadoop-0.20.2-cdh3uX.tar.gz) をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてCDH3をインストールします
(標準の ``~/Downloads`` にダウンロードした場合の例です)。

..  code-block:: sh

    cd ~/Downloads
    tar xf hadoop-0.20.2-*.tar.gz
    sudo chown -R root:root hadoop-0.20.2-*/
    sudo mv hadoop-0.20.2-*/ /usr/lib
    sudo ln -s /usr/lib/hadoop-0.20.2-* /usr/lib/hadoop

..  _`Cloudera's Distribution including Apache Hadoop Version 3 (CDH3)`: https://ccp.cloudera.com/display/CDHDOC/CDH3+Documentation

環境変数の設定
--------------
Asakusa Frameworkの利用に必要となる環境変数を設定します。

``~/.profile`` をエディタで開き、最下行に以下の定義を追加します。

..  code-block:: sh

    export JAVA_HOME=/usr/lib/jvm/jdk-6
    export HADOOP_HOME=/usr/lib/hadoop
    export ASAKUSA_HOME=$HOME/asakusa
    export PATH=$JAVA_HOME/bin:$HADOOP_HOME/bin:$PATH

環境変数をデスクトップ環境に反映させるため、一度デスクトップ環境からログアウトし、再ログインします。

インストールソフトウェアの動作確認
----------------------------------
これまでの手順でインストールしたソフトウェアの動作確認を行います。

以下の例を参考にして、ターミナルからコマンドを実行し、例の通りの出力が行われることを確認してください。
コマンドが見つからないと表示された場合には、それぞれのインストール手順や `環境変数の設定`_ を見直してください。

Javaの動作確認
~~~~~~~~~~~~~~

..  code-block:: sh

    java -version

    java version "1.6.0_29"
    Java(TM) SE Runtime Environment (build 1.6.0_29-b11)
    Java HotSpot(TM) Client VM (build 20.4-b02, mixed mode, sharing)

Java SDKの動作確認
~~~~~~~~~~~~~~~~~~

..  code-block:: sh

    javac -version

    javac 1.6.0_29

Mavenの動作確認
~~~~~~~~~~~~~~~

..  code-block:: sh

    mvn -version

    Apache Maven 3.0.3 (r1075438; 2011-02-28 09:31:09-0800)
    Maven home: /usr/local/lib/apache-maven-3.0.3
    Java version: 1.6.0_29, vendor: Sun Microsystems Inc.
    Java home: /usr/lib/jvm/jdk1.6.0_29/jre
    Default locale: en_US, platform encoding: UTF-8
    OS name: "linux", version: "3.0.0-14-generic", arch: "i386", family: "unix"

Hadoopの動作確認
~~~~~~~~~~~~~~~~

..  code-block:: sh

    hadoop version

    Hadoop 0.20.2-cdh3u2
    Subversion ...
    Compiled by jenkins on Fri Oct 14 01:36:05 PDT 2011
    From source with checksum 644e5db6c59d45bca96cec7f220dda51

..  attention::
    Hadoopのみバージョンを確認するためのコマンドが ``hadoop version`` となっていて、 ``version`` の前にハイフンが不要です。

Eclipseのインストール
---------------------
アプリケーションの実装・テストに使用する統合開発環境(IDE)として、Eclipseをインストールします。

..  note:: Asakusa Frameworkを使う上でEclipseの使用は必須ではありません。サンプルアプリケーションのソースを確認する場合などでEclipseがあると便利であると思われるため、ここでEclipseのインストールを説明していますが、スタートガイドの手順のみを実行するのであれば、Eclipseのインストールは不要です。

Eclipseのダウンロードサイト (http://www.eclipse.org/downloads/) から Eclipse IDE for Java Developers (Linux 32 Bit) (eclipse-java-XX-linux-gtk.tar.gz) をダウンロードします。

ダウンロードが完了したら、以下の例を参考にしてEclipseをインストールします。

..  code-block:: sh

    cd ~/Downloads
    tar xf eclipse-java-*-linux-gtk.tar.gz
    mv eclipse ~/eclipse

Eclipseを起動するには、ファイラーから $HOME/eclipse/eclipse を実行します。ワークスペースはデフォルトの$HOME/workspace をそのまま指定します。

..  attention::
    Eclipse 3.6以前のEclipse IDE for Java Developersを使用している場合は、Eclipseを起動する前にクラスパス変数M2_REPOを設定する必要があります。詳しくは :doc:`../application/maven-archetype` の :ref:`eclipse-configuration` を参照して下さい。

Asakusa Frameworkのインストールとサンプルアプリケーションの実行
===============================================================
開発環境にAsakusa Frameworkをインストールして、Asakusa Frameworkのサンプルアプリケーションを実行してみます。

アプリケーション開発プロジェクトの作成
--------------------------------------
まず、Asakusa Frameworkのバッチアプリケーションを開発、及び管理する単位となる「プロジェクト」を作成します。

Asakusa Frameworkでは、プロジェクトのテンプレートを提供しており、このテンプレートにサンプルアプリケーションも含まれています。また、このテンプレートに含まれるスクリプトを使ってAsakusa Frameworkを開発環境にインストールすることができます。

プロジェクトのテンプレートはMavenのアーキタイプという仕組みで提供されています。Mavenのアーキタイプからプロジェクトを作成するには、以下のコマンドを実行します（Mavenがライブラリをダウンロードするため、実行に時間がかかります)。

..  code-block:: sh

    mkdir -p ~/workspace
    cd ~/workspace
    mvn archetype:generate -DarchetypeCatalog=http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml

コマンドを実行すると、Asakusa Frameworkが提供するプロジェクトテンプレートのうち、どれを使用するかを選択する画面が表示されます。ここでは、3 (asakusa-archetype-windgate) のWindGateと連携するアプリケーション用のテンプレートを選択します。

..  code-block:: sh

    1: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> com.asakusafw:asakusa-archetype-batchapp (-)
    2: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> com.asakusafw:asakusa-archetype-thundergate (-)
    3: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> com.asakusafw:asakusa-archetype-windgate (-)
    4: http://asakusafw.s3.amazonaws.com/maven/archetype-catalog.xml -> com.asakusafw:asakusa-archetype-directio (-)
    Choose a number or apply filter (format: [groupId:]artifactId, case sensitive contains): : 3 (<-3を入力)

次に、Asakusa Frameworkのバージョンを選択します。ここでは 5 (0.2.5) を選択します。

..  code-block:: sh

    Choose com.asakusafw:asakusa-archetype-windgate version: 
    1: 0.2-SNAPSHOT
    2: 0.2.2
    3: 0.2.3
    4: 0.2.4
    5: 0.2.5
    Choose a number: 5: 5 (<-5を入力)

この後、アプリケーションプロジェクトに関するいくつかの定義を入力します。いずれも任意の値を入力することが出来ます。ここでは、グループIDに「com.example」、アーティファクトID（アプリケーションプロジェクト名）に「example-app」を指定します。後の項目はそのままEnterキーを入力します。最後に確認をうながされるので、そのままEnterキーを入力します。

..  code-block:: sh

    Define value for property 'groupId': : com.example    [<-アプリケーションのグループ名を入力。]
    Define value for property 'artifactId': : example-app [<-アプリケーションのプロジェクト名を入力。]
    Define value for property 'version':  1.0-SNAPSHOT: : [<-ここではそのままEnterキーを入力 (バージョン名)。]
    Define value for property 'package':  com.example: :  [<-ここではそのままEnterキーを入力 (パッケージ名)。]

    Confirm properties configuration:
    groupId: com.example
    artifactId: example-app
    version: 1.0-SNAPSHOT
    package: com.example
    Y: : [<-そのままEnterキーを入力]

入力が終わるとプロジェクトの作成が始まります。成功した場合、画面に以下のように「BUILD SUCCESS」と表示されます。

..  code-block:: sh

    ...
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 20.245s
    ...

..  note::
    以降の手順についても、Mavenのコマンド実行後に処理が成功したかを確認するには「BUILD SUCCESS」が表示されていることを確認してください。

これでアプリケーションプロジェクトが作成されました。

Asakusa Frameworkのインストール
-------------------------------
Asakusa Frameworkを開発環境にインストールします。

先ほど作成したアプリケーションプロジェクトから、Mavenの以下のコマンドを使ってAsakusa Frameworkをローカルにインストールすることができます（Mavenがライブラリをダウンロードするため、実行に時間がかかります)。

..  code-block:: sh

    cd ~/workspace/example-app
    mvn assembly:single antrun:run

成功すると、 ``$ASAKUSA_HOME`` (このスタートガイドでは ``$HOME/asakusa`` ) にAsakusa Frameworkがインストールされます。

サンプルアプリケーションのビルド
--------------------------------
アプリケーションのテンプレートには、あらかじめサンプルアプリケーション（カテゴリー別売上金額集計バッチ) のソースファイルが含まれています。このサンプルアプリケーションのソースファイルをAsakusa Framework上で実行可能な形式にビルドします。

アプリケーションのビルドを実行するには、Mavenの以下のコマンドを実行します（初回の実行時のみ、Mavenがライブラリをダウンロードするため、実行に時間がかかります）。

..  code-block:: sh

    cd ~/workspace/example-app
    mvn clean package

このコマンドの実行によって、サンプルアプリケーションに対して以下の処理が実行されます。

1. データモデル定義DSL(DMDL)から、データモデルクラスを生成
2. Asakusa DSLとデータモデル定義DSLから、実行可能なプログラム群（HadoopのMapReduceジョブやWindGate用の実行定義ファイルなど)を生成
3. 実行可能なプログラム群に対するテストを実行
4. サンプルアプリケーションを運用環境に配置するためのアーカイブファイルを生成

ビルドが成功すると、プロジェクトのtargetディレクトリ配下にいくつかのファイルが作成されますが、この中の 「 ``example-app-batchapps-1.0-SNAPSHOT.jar`` 」 というファイルがサンプルアプリケーションが含まれるアーカイブファイルです。

..  note::
    このアーカイブファイルの名前は、実際には ``${artifactId}-batchapp-${version}.jar`` という命名ルールに従って作成されます。プロジェクト作成時に本ドキュメントの例以外のプロジェクト名やバージョンを指定した場合は、それに合わせて読み替えてください。
    
..  warning::
    targetディレクトリの配下に似た名前のファイルとして ``${artifactId}-${version}.jar`` というファイル(「batchapp」が付いていないjarファイル)が同時に作成されますが、これは実行可能なアーカイブファイルではないので注意してください。

サンプルアプリケーションのデプロイ
----------------------------------
サンプルアプリケーションを実行するために、先ほどビルドしたサンプルアプリケーションを実行環境にデプロイします。

実行環境は、通常はHadoopクラスターが構築されている運用環境となりますが、ここでは開発環境（ローカル）上のHadoopとAsakusa Framework上でサンプルアプリケーションを実行するため、ローカルに対するデプロイを行います。

アプリケーションのデプロイは、Asakusa Frameworkがインストールされているマシン上の ``$ASAKUSA_HOME/batchapps`` ディレクトリに アプリケーションが含まれるjarファイルの中身を展開して配置します。以下はアプリケーションプロジェクトで生成したアーカイブファイルをローカルのAsakusa Frameworkにデプロイする例です。

..  code-block:: sh

    cd ~/workspace/example-app
    cp target/*batchapps*.jar $ASAKUSA_HOME/batchapps
    cd $ASAKUSA_HOME/batchapps
    jar xf *batchapps*.jar

..  attention::
    上記のコマンドを正しく動作させるには、あらかじめ `サンプルアプリケーションのビルド`_ を実行しておく必要があります。

サンプルデータの作成と配置
--------------------------
カテゴリー別売上金額集計バッチは、売上トランザクションデータと、商品マスタ、店舗マスタを入力として、エラーチェックを行った後、商品マスタのカテゴリ毎に集計するアプリケーションです。入力データの取得と出力データの生成はそれぞれCSVファイルに対して行うようになっています。

このバッチは入力データを /tmp/windgate-$USER ($USERはOSユーザ名に置き換え) ディレクトリから取得するようになっています。プロジェクトにはあらかじめ ``src/test/example-dataset`` ディレクトリ以下にテストデータが用意されているので、これらのファイルを  ``/tmp/windgate-$USER`` 配下にコピーします。

..  code-block:: sh

    mkdir -p /tmp/windgate-$USER
    rm /tmp/windgate-$USER/* -rf
    cd ~/workspace/example-app
    cp -a src/test/example-dataset/* /tmp/windgate-$USER/

サンプルアプリケーションの実行
------------------------------
ローカルにデプロイしたサンプルアプリケーションを実行します。

Asakusa Frameworkでは、バッチアプリケーションを実行するためのコマンドプログラムとして「YAESS」というツールが提供されています。
バッチアプリケーションを実行するには、 ``$ASAKUSA_HOME/yaess/bin/yaess-batch.sh`` に実行するバッチのバッチIDを指定します。

サンプルアプリケーション「カテゴリー別売上金額集計バッチ」のバッチは「 ``example.summarizeSales`` 」というIDを持っています。
また、このバッチは引数に処理対象の売上日時( ``date`` )を指定し、この値に基づいて処理対象CSVファイルを特定します [#]_ 。

バッチIDとバッチ引数を指定して、以下のようにバッチアプリケーションを実行します。

..  code-block:: sh

    $ASAKUSA_HOME/yaess/bin/yaess-batch.sh example.summarizeSales -A date=2011-04-01

バッチの実行が成功すると、コマンドの標準出力の最終行に「Finished: SUCCESS」と出力されます。

..  code-block:: sh

    ...
    2011/12/08 16:54:38 INFO  [JobflowExecutor-example.summarizeSales] END PHASE - example.summarizeSales|byCategory|CLEANUP@cc5c8cfd-604b-4652-a387-b2ea4d463943
    2011/12/08 16:54:38 DEBUG [JobflowExecutor-example.summarizeSales] Completing jobflow "byCategory": example.summarizeSales
    Finished: SUCCESS

カテゴリー別売上金額集計バッチはバッチの実行結果として、ディレクトリ /tmp/windgate-$USER/result に集計データがCSVファイルとして出力されます。CSVファイルの中身を確認すると、売上データがカテゴリー毎に集計されている状態で出力されています。
下記は結果の例です (結果の順序は実行のたびに変わるかもしれません)。

..  code-block:: sh

    cat /tmp/windgate-$USER/result/category-2011-04-01.csv

    カテゴリコード,販売数量,売上合計
    1300,12,1596
    1401,15,1470
    1600,28,5400

..  [#] より詳しく言えば、このバッチでは ``/tmp/windgate-$USER/sales/<売上日時>.csv`` という名前のCSVファイルを読み出し、
    ``/tmp/windgate-$USER/result/category-<売上日時>.csv`` という名前のCSVファイルを作成します。
    なお、サンプルのデータセットには ``sales/2011-04-01.csv`` が含まれています。

Eclipseへアプリケーションプロジェクトをインポート
-------------------------------------------------
アプリケーションプロジェクトをEclipseへインポートして、Eclipse上でアプリケーションの開発を行えるようにします。

インポートするプロジェクトのディレクトリに移動し、Mavenの以下のコマンドを実行してEclipse用の定義ファイルを作成します。
この作業には多少時間がかかるかもしれません。

..  code-block:: sh

    cd ~/workspace/example-app
    mvn eclipse:eclipse

これでEclipseからプロジェクトをImport出来る状態になりました。Eclipseのメニューから [File] -> [Import] -> [General] -> [Existing Projects into Workspace] を選択し、プロジェクトディレクトリを指定してEclipseにインポートします。

Next Step:アプリケーションの開発を行う
======================================
本スタートガイドの手順を実行し、Asakusa Framework上でバッチアプリケーションの開発を行う準備が出来ました。

次に、アプリケーションの開発を行うために、Asakusa Frameworkを使ったアプリケーション開発の流れを見てみましょう。 >> :doc:`next-step`

