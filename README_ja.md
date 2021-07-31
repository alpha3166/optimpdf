# OptimPDF

携帯端末向けScanSnap PDF最適化ツール

## OptimPDFとは

OptimPDFは、PDF内部のJPEGの幅と高さをiPadやKindleなどの携帯端末に合わせて最適化し、PDFファイルのサイズを縮小するシンプルで小さなツールです。私は富士通のドキュメントスキャナScanSnapを使用しているので、このツールはScanSnap Managerで作成されたPDF専用であり、そのほかのPDFは扱えないと思います (他のものを試していないのでよくわかりません)。

## クイックスタート

1. Java (11以降) とImageMagick (6以降) をインストールします。

2. [Releaseページ](https://github.com/alpha3166/optimpdf/releases)から`optimpdf-1.0.2-jar-with-dependencies.jar`をダウンロードします。

3. コマンドラインから、対象となるPDFを引数に指定して、JARを実行します。

       java -jar optimpdf-1.0.2-jar-with-dependencies.jar some.pdf

   上のサンプルでは、元PDFと同じディレクトリに縮小版の`some_r.pdf`が作成されます。引数にディレクトリを指定すると、そのディレクトリ以下のすべての`*.pdf`を処理します。

## コマンドラインオプション

    使い方: java -jar OPTIMPDF_JAR [OPTION]... PDF_OR_DIR...
    携帯端末用にPDFを最適化
     -b <arg>   指定したページを漂白します (例: 1,3-5 または all)
     -d <arg>   出力ディレクトリ
     -f         出力ファイルを上書きします
     -h         このヘルプを表示して終了します
     -l         処理するPDFの一覧を表示して終了します
     -n         テスト実行 (新しいPDFを保存しません)
     -o <arg>   出力ファイル名 (入力が1ファイルの場合のみ)。-dを無効にします
     -p <arg>   指定されたページのみを処理します (例: 1,3-5)
     -Q <arg>   JPEGの品質 (デフォルト: 50)
     -q         各ページの情報を表示しません
     -s <arg>   出力ファイルの接尾辞 (デフォルト: _r)
     -t <arg>   使用するスレッドの数 (デフォルト: CPUコア数)
     -u         元のPDFが出力先のPDFよりも新しい場合、または出力先のPDFが無い場合に
                のみ処理します。-fを有効にします
     -w <arg>   見開きサイズの閾値。JPEGがこれ以下の場合、出力画面サイズを半分にしま
                す (デフォルト: 2539)
     -x <arg>   出力画面サイズ (デフォルト: 1536x2048)

## OptimPDFのビルド方法

Git、Java (11以降)、Maven、ImageMagick (6以降) をインストールし、クローン&ビルドします。

    git clone https://github.com/alpha3166/optimpdf
    cd optimpdf
    mvn package
