=========================================
Amazon EMR上でAsakusa Frameworkを利用する
=========================================

* 対象バージョン: Asakusa Framework ``0.6.0`` 以降

この文書は、 `Amazon Web Services`_ (AWS) が提供する
クラウド環境上のHadoopサービス `Amazon Elastic MapReduce`_ (Amazon EMR) 上で
Asakusa Frameworkを利用する方法について説明します。

以降では、 `Amazon Web Services`_ を「AWS」、
`Amazon Elastic MapReduce`_ を「EMR」と表記します。

また、本書ではAsakusa Frameworkのデプロイやアプリケーションの実行時に、
AWSが提供するストレージサービスである `Amazon Simple Storage Service`_ （Amazon S3）
を利用します。以降では、 `Amazon Simple Storage Service`_ を「S3」と表記します。

..  _`Amazon Web Services`: http://aws.amazon.com/jp/
..  _`Amazon Elastic MapReduce`: http://aws.amazon.com/jp/elasticmapreduce/
..  _`Amazon Simple Storage Service`: http://aws.amazon.com/jp/s3/


はじめに
========
この文書では、
Direct I/Oを利用したサンプルアプリケーションを
EMR上で実行するまでの手順を説明します。

本書では、
:asakusafw:`Asakusa Framework スタートガイド <introduction/start-guide.html>` の説明で使用した
サンプルアプリケーションプロジェクトを利用します。
Asakusa Frameworkの開発環境上でサンプルアプリケーションを
ビルド及び実行するまでの手順については、
:asakusafw:`Asakusa Framework スタートガイド <introduction/start-guide.html>` を参照してください。

また、Direct I/O を使ったアプリケーションの開発方法については、
:asakusafw:`データの直接入出力 - Direct I/O <directio/index.html>` などを参照して下さい。

AWS利用環境の準備
=================
EMRやS3を利用するための環境を準備します。

本書で示す手順に従ってEMR上でAsakusa Frameworkを利用するためには、大きく以下3つの準備を行う必要があります。

1. AWSへのサインアップ
2. EMR操作用クライアントマシンの環境構築
3. Amazon S3 バケットの作成

これらの準備が整っていない場合、以下に示すドキュメントなどを参考にして、EMRを利用するための環境を準備してください。

* AWSへのサインアップ
   * `アカウント作成の流れ | アマゾン ウェブ サービス（AWS 日本語）`_
* EMR操作用クライアントマシンの環境構築とAmazon S3 バケットの作成
   * `Amazon Elastic MapReduce 開発者ガイド`_ の `Amazon EMR コマンドラインインターフェイスのインストール`_

この文書では、以下の環境が用意されたものとして以降の説明を進めます。

* Asakusa Framework開発環境（Linuxマシン）上にEMR操作用クライアントマシンの環境構築を行った。
* サンプルアプリケーションを実行するための各種データ入出力用のS3バケットとして ``[sample-bucket]`` を作成した [#]_ 。

また、以降の手順では開発環境からS3にファイルを配置、およびS3からファイルを取得する手順がいくつか出てきますが、この文書では `s3cmd`_ というツールを使ってS3上のファイルを扱う例を示します [#]_ 。

..  [#] この文書を元に環境構築を行う際には、バケット名は実際に使用するバケット名に置き換えてください。
..  [#] Ubuntu Linuxにs3cmdをインストールする手順は以下を参照してください。 
    http://s3tools.org/repositories#note-deb

..  _`アカウント作成の流れ | アマゾン ウェブ サービス（AWS 日本語）`: http://aws.amazon.com/jp/register-flow/
..  _`Amazon Elastic MapReduce 開発者ガイド`: http://docs.aws.amazon.com/ja_jp/ElasticMapReduce/latest/DeveloperGuide/
..  _`Amazon EMR コマンドラインインターフェイスのインストール`: http://docs.aws.amazon.com/ja_jp/ElasticMapReduce/latest/DeveloperGuide/emr-cli-install.html
..  _`s3cmd`: http://s3tools.org/s3cmd

Asakusa Framework実行環境のデプロイ
===================================
EMR環境に対してAsakusa Framework実行環境一式をデプロイする準備を行います。

EMR環境に対してAsakusa Framework実行環境一式をデプロイし、
バッチアプリケーションを実行するには様々な方法がありますが、
ここでは以下の方針でデプロイと実行を行うものとします。

* Asakusa FrameworkとバッチアプリケーションをあらかじめS3に配置しておく。
* EMRの起動と停止はEMR操作用クライアントからコマンドで指定する。
* EMRの起動時に、S3に配置したAsakusa FrameworkとバッチアプリケーションをEMRのHadoopクラスタにインストールする。
   * インストールはEMRのブートストラップアクションを利用して自動的に行う。
* バッチアプリケーションは、EMRの起動後にコマンドを実行して起動する
   * EMRの起動/停止とバッチアプリケーションの実行は連動させない。
* バッチアプリケーションはS3上に配置した入力データ(CSVファイル)に対して処理を行い、S3上に処理結果を出力する。
   * Direct I/Oに対してS3に対してデータの入出力を行うよう設定する。

この方針では、フレームワーク、アプリケーション、その他必要なスクリプト等のすべてをS3上に配置します。
具体的には、以下の作業が必要です。

1. Direct I/Oの設定ファイルを編集する
2. Asakusa Frameworkの実行環境一式をS3に配置する
3. ブートストラップアクション用スクリプトをS3に配置する
4. ステップ用スクリプトをS3に配置する

以下、これらの手順ごとの作業について説明します。
以降の手順は、Asakusa Frameworkの開発環境（EMR操作用クライアントマシン）で行うものとします。

また、Asakusa Frameworkのバージョンは ``0.6.2`` を用いて説明します。

..  hint::
    バッチアプリケーションの実行とEMRの起動/停止を連動させる使い方も可能です。この場合、EMR起動時のコマンド実行パラメータに指定したバッチアプリケーションの実行が完了するとEMRが自動的に停止します。

Direct I/Oの設定ファイルを編集する
----------------------------------
:asakusafw:`Asakusa Framework スタートガイド <introduction/start-guide.html>` 
で説明したアプリケーションプロジェクトに対して、
EMR上でS3に対してデータの入出力を行うための設定を行います。

本書で説明するAsakusa Frameworkの構成では、実行するアプリケーションはDirect I/Oを使って
S3バケットから入力ファイルを読み込み、処理の結果もS3バケットに出力ファイルを生成します。
ここでは、Direct I/Oが指定したS3バケット上のパスに対してデータの読み書きを行うようにするため
設定ファイル ``asakusa-resources.xml`` に記述します。

以下は、 ``asakusa-resources.xml`` の設定例です。

* :download:`asakusa-resources.xml <attachment/asakusa-resources.xml>`

..  literalinclude:: attachment/asakusa-resources.xml
    :language: xml

..  attention::
    上記例を参考に設定ファイルを作成する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

上記の設定例では、プロパティ ``com.asakusafw.directio.root.fs.path`` に対して、S3のバケット ``s3://[sample-bucket]/app-data`` の配下をアプリケーションの入出力ディレクトリとして使用するよう設定しています。

編集した設定ファイルは、後述の `Asakusa Frameworkの実行環境一式をS3に配置する`_ の手順で
Asakusa Frameworkのデプロイメントアーカイブに含めるため、
アプリケーションプロジェクト配下のディレクトリ ``src/dist/emr`` に
``$ASAKUSA_HOME`` と同じディレクトリ構成で配置します。

例えば ``asakusa-resources.xml`` は ``$ASAKUSA_HOME/core/conf`` に配置するため、
ここでは ``src/dist/emr/core/conf`` 配下に ``asakusa-resources.xml`` を配置します。

以下にAsakusa Frameworkの設定ファイルをプロジェクトディレクトリに配置する例を示します。
設定ファイルは事前に本ドキュメントのリンクからダウンロードして ``$HOME/Downloads`` に配置し、
バケット名などを適切に設定したものとします。

..  code-block:: sh

    cd <プロジェクトのパス>
    mkdir -p src/dist/emr/core/conf
    cp $HOME/Downloads/asakusa-resources.xml src/dist/emr/core/conf

Asakusa Frameworkの実行環境一式をS3に配置する
---------------------------------------------
Asakusa Frameworkの実行環境一式をS3に配置します。
Asakusa Frameworkの実行環境一式とは、以下のファイルを含みます。

* Asakusa Framework本体
* バッチアプリケーション
* Asakusa Frameworkの設定ファイル

ここでは、上記ファイルをすべて含むAsakusa Frameworkのデプロイメントアーカイブを作成します。
以下はアプリケーションプロジェクトで
Asakusa Frameworkのデプロイメントアーカイブを作成する例を示します

..  code-block:: sh

    cd <プロジェクトのパス>
    ./gradlew clean compileBatchapp attachBatchapps attachConfEMR assembleAsakusafw

..  note::
    上記例はいくつかのGradleタスクを組み合わせて実行することで、
    Asakusa Framework本体、バッチアプリケーション、設定ファイルをすべて同梱した
    Asakusa Frameworkのデプロイメントアーカイブを作成しています。
    
    各タスクの詳細については、 :asakusafw:`Asakusa Gradle Plugin利用ガイド <application/gradle-plugin.html>` の 
    :asakusafw:`Asakusa Frameworkのデプロイメントアーカイブ生成<application/gradle-plugin.html#deployment-archive-gradle-plugin>` を参照してください。

上記のGradleコマンドを実行すると、プロジェクトの ``build`` ディレクトリ配下に ``asakusafw-0.6.2.tar.gz`` というアーカイブファイルが作成されます。このアーカイブファイルをS3バケット上の任意のディレクトリに配置します。ここでは ``s3://[sample-bucket]/asakusafw/`` 配下に配置するものとします。

以下にAsakusa FrameworkのデプロイメントアーカイブをS3に配置する例を示します。

..  code-block:: sh

    s3cmd --rr put build/asakusafw-*.tar.gz s3://[sample-bucket]/asakusafw/

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

ブートストラップアクション用スクリプトをS3に配置する
----------------------------------------------------
ブートストラップアクションとは、EMRの起動直後に設定やデプロイなどの処理を実行する仕組みです。
ブートストラップアクション用のシェルスクリプトを作成してS3に配置し、
EMRの起動時に指定することでそのシェルスクリプトを自動的に実行します。

このスクリプトで、上述までの手順でS3に配置したAsakusa Framework実行環境ファイル一式をEMRのマスターノードに配置します。

以下は、 ブートストラップアクションでAsakusa Framework環境一式をデプロイするスクリプトの記述例です。

* :download:`bootstrap-deploy-asakusa.sh <attachment/bootstrap-deploy-asakusa.sh>`

..  literalinclude:: attachment/bootstrap-deploy-asakusa.sh
    :language: sh

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

以下にブートストラップアクション用スクリプトファイルをS3に配置する例を示します。スクリプトファイルは事前に本ドキュメントのリンクからダウンロードして ``$HOME/Downloads`` に配置し、バケット名などを適切に設定したものとします。

..  code-block:: sh

    s3cmd --rr put ~/Downloads/bootstrap-deploy-asakusa.sh s3://[sample-bucket]/bootstrap-actions/

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

ステップ用スクリプトをS3に配置する
----------------------------------
EMRではHadoopクラスターで実行する一連の処理群を「ジョブフロー」と呼び、またジョブフローに含まれる個々の処理を「ステップ」と呼びます。

EMRのインスタンス起動後に、EMR操作用クライアントマシンからのコマンド実行のたびに処理を実行することを実現したい場合、EMR起動後に「ステップを追加する」ことをEMRに指示します。ステップを追加すると、ただちにEMRはこのステップに対応付けられた処理を実行します。

また、Asakusa Frameworkでバッチアプリケーションを実行するには YAESS が提供するコマンドを使ってバッチを実行します。このため、EMRのステップ実行時にYAESSのコマンドが実行されるよう設定することで、EMRのステップ実行とAsakusa Frameworkのバッチ実行を対応付けることが出来ます。

EMRではステップの実行に紐づく実際の処理をいくつかの方法で指定することが出来ますが、ここではステップ実行時に指定したシェルスクリプトを実行し、このシェルスクリプトからYAESSを実行するようにします。

以下は、 EMRのステップでYAESSを実行するスクリプトの記述例です。

* :download:`step-yaess-batch.sh <attachment/step-yaess-batch.sh>` 

..  literalinclude:: attachment/step-yaess-batch.sh
    :language: sh
    
このスクリプトをS3バケット上の任意のディレクトリに配置します。ここでは ``[sample-bucket]/steps`` 配下に配置するものとします。

以下にステップ用スクリプトファイルをS3に配置する例を示します。スクリプトファイルは事前に本ドキュメントのリンクからダウンロードして ``$HOME/Downloads`` に配置し、バケット名などを適切に設定したものとします。

..  code-block:: sh

    s3cmd --rr put ~/Downloads/step-yaess-batch.sh s3://[sample-bucket]/steps/

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

EMRの起動と確認
===============
EMRの起動と起動確認の方法を説明します。これらはEMR操作用クライアントマシンにインストールしたAmazon EMR CLIを使用します。

EMRの起動
---------
EMRの起動は ``elastic-mapreduce`` を ``--create`` オプション付きで実行します。また、いくつかの起動オプションを合わせて指定します。

以下は、上述のデプロイ手順に対応した環境で使用するための、EMRの起動コマンド例です。

..  code-block:: sh

    elastic-mapreduce --create --alive \
     --name asakusa-batch \
     --ami-version 2.4.2 \
     --enable-debugging \
     --log-uri s3://[sample-bucket]/emr-logs \
     --master-instance-type m1.large \
     --slave-instance-type m1.large \
     --num-instances 2 \
     --bootstrap-action s3://elasticmapreduce/bootstrap-actions/run-if \
     --args "instance.isMaster=true,s3://[sample-bucket]/bootstrap-actions/bootstrap-deploy-asakusa.sh"

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

``elastic-mapreduce`` コマンドのオプション詳細についてはEMRのドキュメントなどを参照してください。ここでは本ドキュメントの説明で重要となるパラメータに絞って説明します。

``--ami-version``
  EMRクラスターのマシンイメージのバージョンです。利用可能なバージョンについては EMR Developer Guide の `Choose a Machine Image`_ [#]_ などを参照してください。

..  attention::
    AMIバージョンの ``3.0.0`` 以降はEMRクラスターに使用されるHadoopのバージョンはHadoop2系が利用されます。Asakusa FrameworkをHadoop2系で利用するには、Hadoop2系向けのAsakusa Frameworkバージョンを使用する必要があります。詳しくは、 :asakusafw:`Hadoop2系の運用環境でAsakusa Frameworkを利用する <administration/deployment-hadoop2.html>` を参照してください。

``--enable-debugging`` , ``--log-uri``
  EMRでは、各ノードのEMRやHadoopのログを非同期で収集してS3にコピーする機能が提供されています。 ``--enable-debugging`` オプションを指定するとのログ収集機能が有効になり、 ``--log-uri`` で指定したS3バケットのキー配下にログが出力されるようになります。
  
``--master-instance-type``, ``--slave-instance-type``
  EMRクラスターのEC2インスタンスタイプです。  

``--num-instances``
  EMRクラスターのインスタンス数を指定します。

..  hint::
    初期構築時のデプロイの確認など、試しに実行する段階ではEC2インスタンスタイプは低コストで利用できるインスタンスタイプを使用して、ノード数も最小構成( ノード数 ``2`` )で確認するのがよいでしょう。

..  attention::
    AMIバージョンの ``3.0.0`` 以降ではインスタンスタイプに ``m1.small`` を利用することができないようです。 EMR Developer Guideの `Hadoop 2.2.0 New Features`_ にこの制約に関する記述があります。

``--bootstrap-action`` , ``--args``
  EMRクラスターの起動時に実行するブートストラップアクションを指定します。上記の例では、マスターインスタンスに対してAsakusa Frameworkの実行環境一式をセットアップするブートストラップアクション用スクリプトを指定しています。

..  _`Choose a Machine Image`: http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/emr-plan-ami.html
..  _`Hadoop 2.2.0 New Features`: http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/emr-hadoop-2.2.0-features.html

..  [#] 日本語版のEMR Developer Guideは最新のバージョン情報が反映されていないことがあるようなので、ここでは英語版へのリンクを記載しています。

ジョブフローID
--------------
``elastic-mapreduce --create`` を使ってEMRを実行すると、コマンド実行後に以下のようにジョブフローIDが出力されます。

..  code-block:: sh

    > Created job flow j-XXXXXXXXXXX

EMR起動後に起動したEMRインスタンスに対してなにかしらの処理を行う場合、このジョブフローIDを指定して各種コマンドを実行します。ジョブフローIDは `AWS Management Console`_ からも確認することが出来ます。

..  _`AWS Management Console`: http://aws.amazon.com/jp/console/


ステータスの確認
----------------
EMRのジョブフローやステップの状態を確認するには、 ``elastic-mapreduce --list`` を実行します。

..  code-block:: sh

    elastic-mapreduce --list

過去数日間で起動、停止したEMRジョブフローと、ジョブフローが持つステップの状態を確認することが出来ます [#]_ 。

現在起動しているEMRジョブフローのみを表示するには、 ``--active`` オプションを付けてコマンドを実行します。

..  code-block:: sh

    elastic-mapreduce --list --active

..  [#] EMRが管理するステータスの意味やその種類については、EMRの以下のドキュメントなどを参照してください。
    
    http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/ProcessingCycle.html

バッチアプリケーションの実行
============================
EMR環境にデプロイしたAsakusa Frameworkのバッチアプリケーションを実行します。

バッチアプリケーションの実行の流れは、以下のようになります。

1. 入力データをS3に配置
2. ステップの実行
3. ステップの実行ステータス確認
4. 出力データをS3から取得

以下、これらの手順について説明します。

入力データをS3に配置
--------------------
入力データをS3に配置します。ここでは、Direct I/O を使って任意のS3バケットから入力データとなるCSVファイルを読み込むサンプルアプリケーションを例に説明します。

サンプルアプリケーションは、先述の `Direct I/Oの設定ファイルを編集する`_ で設定した
S3バケット上のパスから :asakusafw:`Direct I/O スタートガイド <directio/start-guide.html>` の表「サンプルアプリケーションが利用するパス」で記述する
仕様に基づいてCSVファイルを配置します。

例えば、  ``asakusa-resources.xml`` の ``com.asakusafw.directio.root.fs.path`` の値が ``s3://[sample-bucket]/app-data`` であった場合は、 入力データは ``s3://[sample-bucket]/app-data/master/item_info.csv`` や ``s3://[sample-bucket]/app-data/sales/2011-04-01.csv`` といったようなパス名で配置することができます。

以下は、サンプルアプリケーションに付属しているサンプルデータをS3に配置する例です。

..  code-block:: sh

    cd <プロジェクトのパス>
    s3cmd -r --rr put src/test/example-dataset/master/ s3://[sample-bucket]/app-data/master/
    s3cmd -r --rr put src/test/example-dataset/sales/ s3://[sample-bucket]/app-data/sales/

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

ステップの実行
--------------
先述の `ステップ用スクリプトをS3に配置する`_ でデプロイした YAESS を実行するスクリプトを実行するEMRのステップを追加します。

以下、ステップの実行例です。

..  code-block:: sh

    elastic-mapreduce \
     --jar s3://elasticmapreduce/libs/script-runner/script-runner.jar \
     --args "s3://[sample-bucket]/steps/step-yaess-batch.sh,example.summarizeSales,-A,date=2011-04-01" \
     --step-name "test-step" \
     --jobflow j-XXXXXXXXXXX

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。また、ジョブフローIDをEMR起動時に出力されたジョブフローIDに置き換えてください。

オプションの詳細についてはEMRのドキュメントなどを参照してください。ここでは本ドキュメントの説明で重要となるパラメータに絞って説明します。

``--jar``, ``--args``
  このステップで実行するjarファイルを指定します。ここではEMRが提供するScript Runnerと呼ばれる、マスターインスタンス上のシェルスクリプトをステップとして実行するためのjarファイルを指定し、引数にYAESSを実行するためのシェルスクリプトとYAESSバッチ実行コマンドの引数を指定しています。 
  
  ``--args`` で指定する ``step-yaess-batch.sh`` 以降の文字列は、 ``yaess-batch.sh`` に与える引数をカンマ区切りで指定します。 ここでは、 ``example.summarizeSales`` がバッチID、 ``-A,date=2011-04-01`` の部分がバッチ引数になります。

``--step-name``
  実行するステップに任意の名前を指定することが出来ます。これは後述する `ステップの実行ステータス確認`_ で説明する手順でステップを絞り込むためのパラメータとして利用することが出来ます。

``--jobflow``
  ステップを実行するジョブフローを特定するジョブフローIDを指定します。 `EMRの起動`_ 時に出力されたジョブフローIDを指定してください。

ステップの実行ステータス確認
----------------------------
`ステップの実行`_ で実行したステップの実行ステータスを確認するには、 `ステータスの確認`_ で説明した ``elastic-mapreduce --list`` コマンドを使用します。このコマンドは、ジョブフローに紐づくステップの一覧とそのステータスを表示します [#]_ 。

..  code-block:: sh

    elastic-mapreduce --list --active

ステータスの状態を外部ツールやプログラムで取得し、その結果に応じて処理を行わせたい場合は、EMRのSDKが提供するAPIを使用するなどいくつかの方法がありますが、ここではシンプルな例としてEMR CLI の出力を解析する例を示します。

..  code-block:: sh

    elastic-mapreduce --list --active | grep "<ステップ名>" | cut -d " " -f 4

先述の `ステップの実行`_ で一意となるステップ名を指定することで、 ``grep`` で行を特定できるようにします。抽出した行のステータス部分の文字列のみを抜き出し、この値に応じて以降の処理を決定します。

..  [#] EMRが取り得るステータスの意味やその種類については、EMRの以下のドキュメントなどを参照してください。
    
    http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/ProcessingCycle.html

出力データをS3から取得
----------------------
出力データをS3から取得します。ここでは、 `入力データをS3に配置`_ と同様にサンプルアプリケーションを例に説明します。

サンプルアプリケーションは、先述の `Direct I/Oの設定ファイルを編集する`_ で設定した
S3バケット上のパスから :asakusafw:`Direct I/O スタートガイド <directio/start-guide.html>` の表「サンプルアプリケーションが利用するパス」で記述する
仕様に基づいてCSVファイルを生成します。

例えば、 ``asakusa-resources.xml`` の ``com.asakusafw.directio.root.fs.path`` の値が ``s3://[sample-bucket]/app-data`` であった場合は、 出力データは ``s3://[sample-bucket]/app-data/result/category/result.csv`` といったようなパスに出力されます。

以下は、S3に出力された処理結果のCSVファイルをローカルの ``/tmp`` ディレクトリに配置して、CSVファイルの内容を確認する例です。

..  code-block:: sh

    s3cmd get s3://[sample-bucket]/app-data/result/category/result.csv /tmp
    cat /tmp/result.csv

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

EMRの停止
=========
EMRの停止は、 ``elastic-mapreduce --terminate`` を実行します。EMR起動時に出力されたジョブフローIDを指定します。

..  code-block:: sh

    elastic-mapreduce --terminate --jobflow j-XXXXXXXXXXX

..  warning::
    EMRインスタンスを稼働し続けると、その分課金が発生し続けます。
    不要になったEMRインスタンスは忘れずに停止してください。

EMRに関するTips
===============
上記で説明した内容のほか、Asakusa FrameworkをEMR上で使用する際に有用なトピックについて説明します。

マスターノードにSSHでログインする
---------------------------------
EMRのマスターノードにSSHでログインするには、EMR操作用クライアントマシンから ``elastic-mapreduce --ssh`` を実行します [#]_ 。

..  code-block:: sh

    elastic-mapreduce --ssh --jobflow j-XXXXXXXXXXX

..  [#] 類似のオプションとして、 ``--scp`` などのオプションなどもあります。詳しくは ``elastic-mapreduce`` コマンドのヘルプなどを参照して下さい。

YAESSのログを確認する
---------------------
バッチアプリケーションが異常終了した場合は、まず YAESS のログを確認すべきです。

最後に実行したバッチ実行（EMRのステップ）のYAESSのログを確認したい場合、EMR操作用クライアントマシンから ``elastic-mapreduce --logs`` を実行します。

..  code-block:: sh

    elastic-mapreduce --logs --jobflow j-XXXXXXXXXXX

また、本書で示す手順に沿った場合、YAESS のログはマスターノードの ``$HOME/asakusa/job-step.log`` にリダイレクトされます。過去のバッチ実行時のログも含めて確認したい場合、このファイルを参照するとよいでしょう。ログのパスは `ステップ用スクリプトをS3に配置する`_ で説明したステップ実行用スクリプト内で指定しているので、ログの出力パスを変更したい場合は、ステップ用スクリプトを修正してください。

また、後述の `EMRやHadoopのログを確認する`_ で説明する各ノードのログを非同期でS3にコピーする機能によって、YAESSのログはステップごとにS3上の ``<ログの基底パス>/<ジョブフローID>/steps/<ステップ番号>/stdout`` にもコピーされます。

EMRやHadoopのログを確認する
---------------------------
EMRやHadoopの各種ログは、EMRの各ノードの ``/mnt/var/log`` 配下に出力されます。

また、EMRでは、各ノードのEMRやHadoopのログを非同期で収集してS3にコピーする機能が提供されています。詳しくは、EMRの以下のドキュメントなどを参照して下さい。

http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/emr-plan-debugging.html

タイムゾーンを変更する
----------------------
EMRインスタンスでデフォルトで指定されるタイムゾーンはJST（日本標準時）以外のものが使われる可能性があります。EMRインスタンスに対してタイムゾーンを指定したい場合、ブートストラップアクションでタイムゾーンの設定を行うとよいでしょう。

以下は、 以下はタイムゾーンを指定するブートストラップアクションの記述例です。

* :download:`set-timezone.sh <attachment/set-timezone.sh>` 

..  literalinclude:: attachment/set-timezone.sh
    :language: sh

このスクリプトのデプロイ方法は `ブートストラップアクション用スクリプトをS3に配置する`_ などを参考にしてください。EMR起動時には以下のように指定します。

..  code-block:: sh

    elastic-mapreduce --create --alive \
     ...
     --bootstrap-action s3://[sample-bucket]/bootstrap-actions/set-timezone.sh \
     ...

..  attention::
    上記例を参考にコマンドを入力する際は、必ずバケット名 ``[sample-bucket]`` を実際に使用するバケット名に置き換えてください。

HadoopのWebUIを参照する
------------------------
Apache Hadoopやその他のHadoopディストリビューションと同様に、EMRでもHadoopのWebUIを参照してMapReduceジョブやHDFSの内容を確認することが出来ます。しかし、デフォルトの設定では、EMRのマスターグループに対するセキュリティグループ設定ではHadoopのWebUI用のポートが閉じているため、直接ブラウザでアクセスしてWeb画面を参照することはできません。

HadoopのWebUIを参照するには、ブラウザに対してSSHポートフォワードの設定を行いSSHのポートを経由する方法や、セキュリティグループの設定を変更する [#]_ などの方法があります。詳しくは、EMRの以下のドキュメントなどを参照してください。

http://docs.aws.amazon.com/ElasticMapReduce/latest/DeveloperGuide/UsingtheHadoopUserInterface.html

..  [#] プロダクション環境など機密性が高い環境では、セキュリティグループを変更する方法は望ましくないでしょう。

ステップ数の上限
----------------
EMRのジョブフローに含めることが出来るステップ数は ``256`` までという制限があります。EMRのジョブフローを長く起動し続け、ステップを大量に実行するという使い方をした場合にこの上限を超えてステップが実行出来なくなる可能性があります。

このような場合、ステップの上限を超える前に異なるEMRインスタンス（ジョブフロー）を起動させ、ジョブフローIDを切り替えてアプリケーションを実行するなどの対応が必要です。

