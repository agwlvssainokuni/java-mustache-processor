# Requirements Verification Questions (Round 2)

要件定義書（requirements.md）のレビュー中に見つかった追加確認事項です。パーシャル解決方法・CLIインターフェース・文字エンコーディング・配布方法について確認させてください。
各質問について、選択肢の記号を [Answer]: の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

## Question 1: パーシャルの解決方法（ライブラリAPI）
ライブラリAPIとして、パーシャル（部分テンプレート）をどのように解決しますか？

A) 呼び出し側がパーシャル名→テンプレート文字列を解決する関数/インターフェース（例: `Function<String, String>` やリゾルバーインターフェース）を渡す方式

B) 呼び出し側が事前にすべてのパーシャルを `Map<String, String>` として一括で渡す方式

C) 両方サポートする（Mapを実装したデフォルトリゾルバーを提供しつつ、カスタムリゾルバーも指定可能にする）

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 2: パーシャルの解決方法（CLI）
CLIとして、パーシャルをどのように解決しますか？

A) テンプレートファイルと同一ディレクトリ内から、パーシャル名＋拡張子（例: `.mustache`）でファイルを探す

B) コマンドライン引数でパーシャル用ディレクトリを明示的に指定する

C) 両方サポート（明示指定が無ければテンプレートと同じディレクトリを既定値とする）

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 3: CLIの引数指定方法
CLIの引数はどのような形式にしますか？

A) 位置引数（例: `mustache-cli <template> <data> [output]`）

B) 名前付きオプション（例: `--template`, `--data`, `--output`, `--format`）

C) 両方（位置引数を基本としつつ、オプションで上書き可能にする）

X) Other (please describe after [Answer]: tag below)

[Answer]: X — テンプレートは位置引数で複数指定可能とし、各テンプレートを個別にレンダリングした結果を連結して出力する。データは `--data` 等の名前付きオプションで指定する。標準的なUNIXコマンドと同様に標準入出力にも対応し、標準入力は既定でデータとして扱うが、オプション指定によりテンプレートを標準入力から読み込むことも可能とする。

**補足**: テンプレートを複数指定した場合、1つのデータに対して各テンプレートを順にレンダリングし、その結果を連結して1つの出力とする（テンプレートごとに個別ファイル出力はしない）。

## Question 4: CLIのデータ形式判定
入力データがJSONかYAMLかをどう判定しますか？

A) ファイル拡張子から自動判定する（`.json` / `.yaml`, `.yml`）

B) 明示的なオプション指定を必須とする（例: `--format json`）

C) 自動判定を基本としつつ、オプションで上書きも可能にする

X) Other (please describe after [Answer]: tag below)

[Answer]: C

**補足**: `--data` でファイルパスを指定する場合は拡張子から自動判定する。標準入力からデータを読む場合（Question 3参照）はファイル名が存在しないため自動判定できず、`--format` オプションでの明示指定を必須とする。

## Question 5: CLIの出力先
レンダリング結果の出力先はどうしますか？

A) 標準出力のみ

B) 標準出力を既定としつつ、オプションでファイル出力も可能にする

C) ファイル出力を必須とする

X) Other (please describe after [Answer]: tag below)

[Answer]: B

## Question 6: CLIのエラー時終了コード
エラー発生時の終了コードはどの粒度にしますか？

A) 成功=0、エラー=1のみのシンプルな区別

B) エラー種別ごとに異なる終了コード（例: 引数エラー、パースエラー、レンダリングエラー、I/Oエラーで別々のコード）を用意する

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 7: 文字エンコーディング
テンプレート・データファイルの読み込みエンコーディングはどうしますか？

A) UTF-8固定（他エンコーディングは非対応）

B) UTF-8を既定としつつ、CLIではオプションで他エンコーディングも指定可能にする

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 8: 配布方法
ライブラリの配布方法はどうしますか？

A) Maven Central（またはGitHub Packages等）への公開を前提とする

B) 当面は公開せず、ソースからのビルド・利用のみを想定する

C) 未定（後続フェーズで判断する）

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 9: CLIの配布形態
CLIはどのような形態で配布しますか？

A) 実行可能fat jar（依存関係を含む単一jarファイル）として配布する

B) GraalVMによるネイティブイメージ化まで対応する

C) 当面はfat jarのみとし、ネイティブイメージ化は将来検討とする

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 10: 依存脆弱性スキャンツールの方向性
Security Baseline(SECURITY-10)で必須となる依存脆弱性スキャンについて、方向性はどうしますか？

A) OWASP Dependency-Check（Gradleプラグイン）を使用する

B) GitHub Dependabotに任せる（追加ツール導入は不要）

C) 具体的なツール選定はNFR Requirementsステージで決定する（今は「スキャンを実施する」という方針のみ確認）

X) Other (please describe after [Answer]: tag below)

[Answer]: 
