========================
拡張モジュールのデプロイ
========================
この文書では、 拡張モジュールのデプロイ手順について説明します。

拡張モジュールについて
======================
Asakusa Frameworkでは、標準のデプロイメントアーカイブ [#]_ に含まれない追加機能やレガシーモジュール [#]_ を拡張モジュールとして提供しています。

拡張モジュールを実行環境にデプロイすることで、Asakusa Frameworkの拡張機能やレガシーモジュールを利用することが出来ます。

..  [#] Asakusa Frameworkのデプロイメントアーカイブの作成や設定は、
        使用している開発環境に応じた以下のドキュメントを参照してください。
        
        * Gradleを使った開発環境
           * :doc:`../application/gradle-plugin` - :ref:`deployment-archive-gradle-plugin`
        * Mavenを使った開発環境
           * :doc:`framework-organizer` - :ref:`deployment-archive-maven-archetype`

..  [#] レガシーモジュールについては、 :doc:`../application/legacy-module-guide` を参照してください。

.. _extention-module-list:

拡張モジュール一覧
==================
バージョン |version| でAsakusa Frameworkが提供する拡張モジュールは以下の通りです。


..  list-table:: Asakusa Framework 拡張モジュール一覧
    :widths: 35 15 50
    :header-rows: 1
    
    * - 拡張モジュールID
      - 導入バージョン
      - 説明
    * - ``ext-windgate-retryable-plugin``
      - 0.4.0
      - :ref:`windgate-userguide-retryable-plugin` を利用するためのWindGateプラグイン
    * - ``ext-yaess-jobqueue-plugin``
      - 0.4.0
      - :doc:`../yaess/jobqueue` 用のクライアントプラグイン
    * - ``ext-experimental-shell-script``
      - 0.4.0
      - レガシーモジュール Experimental shell script を使用するための拡張モジュール
    * - ``ext-cleaner``
      - 0.4.0
      - レガシーモジュール Asakusa Cleaner を使用するための拡張モジュール

拡張モジュールの利用方法
========================

Gradleプロジェクトの拡張モジュール利用方法
------------------------------------------
:doc:`../introduction/start-guide` や :doc:`../application/gradle-plugin` を利用したアプリケーションプロジェクトについては、
Asakusa Frameworkのデプロイメントアーカイブの作成時に
拡張モジュールを同梱することができます。
詳しくは :ref:`include-extention-modules-gradle-plugin` を参照してください。

また、後述する `Mavenプロジェクトの拡張モジュール利用方法`_ で説明する方法で
拡張モジュール用のデプロイメントアーカイブを作成し、
これを個別に実行環境にデプロイすることも可能です。

Mavenプロジェクトの拡張モジュール利用方法
-----------------------------------------
:doc:`../application/maven-archetype` の手順に従って作成したアプリケーションプロジェクトについては、
以下の手順で拡張モジュールを利用します。

1. 拡張モジュール用のデプロイメントアーカイブを生成する。
2. 拡張モジュール用のデプロイメントアーカイブを実行環境にデプロイする。

以下、それぞれの手順について説明します。

拡張モジュール用のデプロイメントアーカイブを生成する
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
Framework Organizer [#]_ 上で、拡張モジュール用のデプロイメントアーカイブを生成します。

まずFramework Organizerの ``pom.xml`` に対して、 ``maven-assembly-plugin`` のプラグインの定義にデプロイメントアーカイブ生成用の設定を追加します。先述の :ref:`extention-module-list` から利用する拡張モジュールの拡張モジュールIDを確認し、その拡張モジュールIDを ``maven-assembly-plugin`` の設定 ``plugin/executions/execution/configuration/descriptorRefs/descriptorRef`` 要素の値として設定します。

以下はWindGate用のアーキタイプ ``asakusa-archetype-windgate`` に拡張モジュール ``ext-yaess-jobqueue-plugin`` を追加する ``pom.xml`` の設定例です。

..  code-block:: xml

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-assembly-plugin</artifactId>
                <version>${plugin.assembly.version}</version>
                <dependencies>
                    <dependency>
                        <groupId>com.asakusafw</groupId>
                        <artifactId>asakusa-distribution</artifactId>
                        <version>${asakusafw.version}</version>
                    </dependency>
                </dependencies>
                <executions>
                    <execution>
                        <id>default-cli</id>
                        <goals>
                            <goal>single</goal>
                        </goals>
                        <configuration>
                            <descriptorRefs>
                                <descriptorRef>dev</descriptorRef>
                                <descriptorRef>prod-windgate</descriptorRef>
                                <!-- 拡張モジュール ext-yaess-jobqueue-plugin を追加 -->
                                <descriptorRef>ext-yaess-jobqueue-plugin</descriptorRef>
                            </descriptorRefs>
                            <finalName>asakusafw-${asakusafw.version}</finalName>
                        </configuration>
                    </execution>
                </executions>
            </plugin>


上記の設定を追加後、Framework Organizerに対して ``mvn package`` を実行します。Framework Organizerの ``target`` ディレクトリ配下に標準で生成されるデプロイアーカイブに加えて拡張モジュール用のデプロイアーカイブが生成されます。

上記の例では、 ``target`` ディレクトリ配下に拡張モジュール ``ext-yaess-jobqueue-plugin`` に対応するデプロイメントアーカイブ ``asakusafw-{asakusafw.version}-ext-yaess-jobqueue-plugin.tar.gz`` [#]_ が生成されます。

..  [#] Framework Organizerについては、 :doc:`framework-organizer` を参照してください。
..  [#] ファイル名の ``${asakusafw.version}`` 部分は実際には使用しているAsakusa Frameworkのバージョンに置き換えます。例えばバージョン |version| を使用している場合は、 asakusafw-|version|-ext-yaess-jobqueue-plugin.tar.gz になります。

拡張モジュール用のデプロイメントアーカイブを実行環境にデプロイする
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
実行環境に拡張モジュールをデプロイするには、基本的には以下の手順を実施します。

1. 実行環境で拡張モジュールのデプロイメントアーカイブを展開し、展開したファイルのシェルスクリプトに実行権限を付与する。
2. 拡張モジュール固有のデプロイメント手順を実施する。例えば設定ファイルを利用環境に合わせて編集するなど。

ここでは上記1.のデプロイメントアーカイブの展開手順のみ説明します。2. については各拡張モジュールのドキュメントを参照してください。

拡張モジュール用のデプロイメントアーカイブの展開
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
実行環境の ``$ASAKUSA_HOME`` 配下に拡張モジュールのデプロイメントアーカイブを展開します。展開後、 ``$ASAKUSA_HOME`` 配下の ``*.sh`` に実行権限を追加します。

以下は、 拡張モジュール ``ext-yaess-jobqueue-plugin`` のデプロイメントアーカイブの展開例です。

..  code-block:: sh

    mv asakusafw-*-ext-yaess-jobqueue-plugin.tar.gz $ASAKUSA_HOME
    cd $ASAKUSA_HOME
    tar -xzf asakusafw-*-ext-yaess-jobqueue-plugin.tar.gz
    find $ASAKUSA_HOME -name "*.sh" | xargs chmod u+x
..

デプロイメントアーカイブの展開の後、2.の拡張モジュール固有のデプロイメント手順を実施します。

