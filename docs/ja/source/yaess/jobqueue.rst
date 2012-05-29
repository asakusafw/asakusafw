==============
YAESS JobQueue
==============
この文書では、シンプルなHadoopジョブの実行を制御するJobQueueのサーバーおよび、それをYAESSと連携して利用するクライアントプラグイン ``yaess-jobqueue`` の利用方法について解説します。

YAESSがリモートのHadoopと連携する場合に、ネットワークの瞬断に強いなどの利点があります。
そのため、Hadoopのジョブ実行が長時間に渡る場合や、通信路が不安定な場合などに、組み込みで提供されているSSH経由での通信よりも安定した動作が見込めます。

JobQueue
========
JobQueueは主に2つのコンポーネントで構成されています。

JobQueueサーバー
    Hadoopのジョブを実行するサービスです。
    HTTP経由でHadoopジョブ実行リクエストを受け取り、Hadoopコマンドを利用してジョブを起動します。
    また、ジョブ実行リクエストを内部のキューに一度蓄えて、同時に実行可能なジョブ数を制御できます。
    
    このコンポーネントは、Hadoopサービス群と通信するHadoopクライアントマシン上に配置します。

JobQueueクライアントプラグイン
    JobQueueサーバーにHadoopジョブ実行リクエストを送るクライアントで、 :doc:`YAESS <index>` のHadoopジョブハンドラプラグインとして提供されます。
    JobQueueサーバーを経由してYAESSからHadoopジョブを実行できます。
    
    このコンポーネントは、YAESSを実行するマシン上にYAESSのプラグインとして配置します。

JobQueueは以下のような機能を持っています。

同時実行数制御
    JobQueueサーバーから同時に実行可能なHadoopジョブの個数を制限できます。
標準出力の保存
    JobQueueサーバーからジョブを実行した際の標準出力および標準エラー出力を、ジョブごとに保存できます。
HTTPS通信
    HTTPだけでなくHTTPSによる通信も可能です。
ベーシック認証
    JobQueueサーバーとの通信時にHTTPのベーシック認証を利用可能です。
ラウンドロビン処理
    複数のJobQueueサーバーを登録すると、ラウンドロビン方式でバッチ内のHadoopジョブを振り分けて実行します。
    一部のサーバーを利用不可能になった場合、そのサーバーをブラックリストに追加して同一バッチ内で利用しないようにします。

JobQueueを利用する際の構成例
============================
以下はJobQueueを利用する際の構成例です。

単一構成
--------
JobQueueサーバーを一台のマシンにインストールする場合、次のような構成をとります。

..  figure:: jobqueue-single.png

Hadoopコマンドを利用できるマシンでJobQueueサーバーを実行し、そこに対してYAESSからJobQueueクライアントプラグインを利用して通信します。

冗長構成
--------
`単一構成`_ ではJobQueueサーバーが単一障害点になるという問題があるため、JobQueueサーバーを複数用意する構成も検討してください。

..  figure:: jobqueue-multiple.png

単一構成と異なるのはJobQueueサーバーの台数で、上図では2台の異なるマシン上でJobQueueサーバーを実行しています。
JobQueueクライアントプラグインに複数のJobQueueサーバーを登録すると、ラウンドロビン方式でそれぞれのJobQueueサーバーに対して交互にジョブ実行のリクエストを発行します。

JobQueueクライアントプラグインが、いずれかのJobQueueサーバーのダウンを検出すると、該当のサーバーをブラックリストに登録し、同一のバッチ内でそのサーバーを利用しないようにします。
JobQueueサーバーがダウンから復旧したら、次回以降のYAESSの実行ではそのサーバーを再度利用するようになります。

JobQueueサーバーの利用方法
==========================

JobQueueクライアントプラグインの利用方法
========================================

プラグインの登録
----------------
このプラグインを利用するには、 ``asakusa-yaess-jobqueue`` というプラグインライブラリをYAESSに登録します。
これは以下のURLからダウンロードできます。

* http://asakusafw.s3.amazonaws.com/maven/releases/com/asakusafw/asakusa-yaess-jobqueue/0.2.6/asakusa-yaess-jobqueue-0.2.6.jar

また、依存ライブラリとして以下のライブラリも必要です。

* `HttpComponents Core <http://hc.apache.org/index.html>`_ ( ``Ver.4.1.4`` で動作確認 )
* `HttpComponents Client <http://hc.apache.org/index.html>`_ ( ``Ver.4.1.3`` で動作確認 )
* `Gson <http://code.google.com/p/google-gson/>`_ ( ``Ver.1.7.1`` で動作確認 )

プラグインライブラリの登録方法は、 :asakusafw:`YAESS本体のマニュアル <yaess/user-guide.html>` を参照してください。

JobQueueを利用したHadoopジョブの実行
------------------------------------
JobQueueを利用してHadoopジョブを実行する場合、構成ファイルの ``hadoop`` セクションに以下の内容を設定します。

..  list-table:: JobQueueを利用する設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``hadoop``
      - ``com.asakusafw.yaess.jobqueue.QueueHadoopScriptHandler``
    * - ``hadoop.1.url``
      - JobQueueサーバーのURL
    * - ``hadoop.1.user``
      - JobQueueサーバーの認証ユーザー名
    * - ``hadoop.1.password``
      - JobQueueサーバーの認証パスワード
    * - ``hadoop.timeout``
      - ジョブ登録時のタイムアウト (ミリ秒)
    * - ``hadoop.pollingInterval``
      - ジョブ状態の問い合わせ間隔 (ミリ秒)

``hadoop.1.url`` には、対象のJobQueueサーバーが動作しているコンテキストパスのルートまでを指定します。
現在のところ、プロトコルにはHTTPとHTTPSを利用可能で、URLに認証情報を含めることはできません。

``hadoop.1.user`` と ``hadoop.1.password`` はそれぞれ上記URLに対する認証情報です。
認証を行わない場合、これらの認証情報は省略可能です。

``hadoop.time`` と ``hadoop.pollingInterval`` はいずれも省略可能です。
省略した場合、タイムアウトは ``10000`` 、問い合わせ間隔は ``1000`` をそれぞれ既定値として利用します。

ラウンドロビン方式でのHadoopジョブの実行
----------------------------------------
複数のJobQueueサーバーを利用してラウンドロビン方式でHadoopジョブを実行する場合、
`JobQueueを利用したHadoopジョブの実行`_ に加えて以下の設定を追加します。

..  list-table:: ラウンドロビン方式を利用する設定
    :widths: 10 15
    :header-rows: 1

    * - 名前
      - 値
    * - ``hadoop.<n>.url``
      - JobQueueサーバーのURL
    * - ``hadoop.<n>.user``
      - JobQueueサーバーの認証ユーザー名
    * - ``hadoop.<n>.password``
      - JobQueueサーバーの認証パスワード

上記の ``<n>`` の部分には ``2`` 以上の整数を指定し、それらに対してURL、ユーザー名、パスワードをそれぞれ指定します。
ただし、認証を必要としないJobQueueに対しては、ユーザー名とパスワードを省略可能です。

この ``<n>`` の箇所を ``2`` , ``3`` , ... と次々増やしていくことで、より多くのJobQueueサーバーを登録できます。
これらはバッチ実行の際に、ラウンドロビン方式で順番に利用され、サーバーが動作していない際にはブラックリストに入れられます。

..  attention::
    サーバーが動作していない場合にはラウンドロビンから外されますが、
    ジョブの実行中にサーバーがダウンした場合にはその場でジョブの実行が失敗します。
