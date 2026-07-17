# Functional Design Plan - cli (Unit 2: CLI Tool)

## 対象範囲
`unit-of-work.md`のUnit 2定義、`components.md`/`component-methods.md`のcliサブプロジェクト定義、および`requirements.md`のFR-2・NFR-2・NFR-3・NFR-4に基づき、CLIツールの詳細な業務ロジック（引数解析規則、データ読込規則、レンダリング連結規則、出力規則、終了コードマッピング）を設計する。

## 実行ステップ

- [ ] Step 1: ビジネスロジックモデリング（CliRunnerのオーケストレーションフロー詳細化）
- [ ] Step 2: ドメインモデル（CliArguments・ExitCode）の詳細化
- [ ] Step 3: 業務ルール（引数解析規則、データ形式判定、連結規則、パーシャル解決規則、終了コード対応）の確定
- [ ] Step 4: エラーハンドリング方針の確定
- [ ] Step 5: 成果物生成
  - [ ] `aidlc-docs/construction/cli/functional-design/business-logic-model.md`
  - [ ] `aidlc-docs/construction/cli/functional-design/business-rules.md`
  - [ ] `aidlc-docs/construction/cli/functional-design/domain-entities.md`

## 対象外と判断した観点（理由付き）
- **Data Flow（永続化）**: ファイル入出力は行うが、DB等の永続化ストアは持たないため対象外
- **Frontend Components**: UIを持たないCLIのため対象外

## 質問

各質問について、選択肢の記号を `[Answer]:` の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

### Question 1: JSON/YAMLパースライブラリの選定
`DataLoader`がJSON/YAMLデータをパースするために使用するライブラリを決定します（SECURITY-13: デシリアライズ安全性、SECURITY-10: サプライチェーンにも関わる）。

A) Jackson（`jackson-databind` + `jackson-dataformat-yaml`）で統一する。JSON/YAML双方を同一の`ObjectMapper`ベースAPIで`Map<String, Object>`に変換でき、実装・保守コストが低い。`jackson-dataformat-yaml`は内部的にSnakeYAMLを利用するがデフォルトで安全側の設定になっている

B) JSONは`org.json`、YAMLはSnakeYAML（既に`core`のtestImplementationで導入済み）を個別に使用する

C) 外部ライブラリを追加せず、最小限の手書きJSONパーサー＋SnakeYAML（YAML用）とする

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 2: コマンドライン引数解析方式
`ArgumentParser`の実装方式です（`component-methods.md`では独自クラスとして定義済み）。

A) 手書きの`ArgumentParser`（外部ライブラリなし）。オプション数が少なく（`--data`/`--format`/`--output`/パーシャルディレクトリ/`--help`程度）、依存関係を増やさずシンプルに実装できる

B) picocli等のCLIフレームワークを導入し、アノテーションベースで引数解析・ヘルプ生成を行う

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 3: 複数テンプレート連結時の区切り
FR-2「複数テンプレートを順にレンダリングし結果を連結」について、各テンプレートの出力の間に区切り文字を挿入しますか？

A) 区切り文字なし（各レンダリング結果をそのまま連結する。改行が必要ならテンプレート側に含める）

B) 各テンプレート出力の間に改行を1つ自動挿入する

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 4: `--output`指定時の既存ファイル上書き挙動
出力先ファイルが既に存在する場合の挙動です。

A) 常に無条件で上書きする（一般的なCLIツールの慣習に従う）

B) 既存ファイルがあればエラー（`IO_ERROR`）で終了する

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 5: 複数テンプレート時のパーシャル解決ディレクトリ
FR-2「テンプレートと同一ディレクトリ内からパーシャルを既定で探索」について、複数テンプレートを指定した場合の基準ディレクトリはどうしますか？

A) 各テンプレートごとに、そのテンプレート自身が置かれているディレクトリを既定のパーシャル探索基準とする（テンプレートごとに異なる`FilePartialResolver`を都度構築）。明示的なパーシャルディレクトリ指定オプションがあれば、それを全テンプレート共通で優先する

B) 最初に指定されたテンプレートのディレクトリのみを全体で共通のパーシャル探索基準とする

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 6: 標準入力からのテンプレート読み込み指定方法
FR-2「オプション指定によりテンプレートを標準入力から読み込むことも可能」の具体的な指定方法です。

A) テンプレート位置引数に`-`を渡すと、そのテンプレートは標準入力から読み込む（UNIXコマンドの慣習）。複数テンプレート中の1つとして`-`を混在指定することも可能とする

B) 専用の真偽値オプション（例: `--template-stdin`）を用意し、指定時はテンプレート位置引数を1つも取らずに標準入力全体を単一テンプレートとして扱う

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 7: `--help`オプションの提供
A) `--help`（および`-h`）オプションを実装し、使用方法を標準出力に表示して正常終了（終了コード`SUCCESS`）する

B) `--help`は実装しない（引数エラー時にのみ簡易的な使用方法をstderrへ表示する）

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 8: 終了コードの具体的な値割当
`ExitCode`（`SUCCESS`, `ARGUMENT_ERROR`, `PARSE_ERROR`, `RENDER_ERROR`, `IO_ERROR`）に割り当てる具体的な数値です。

A) シンプルな連番: `SUCCESS=0`, `ARGUMENT_ERROR=1`, `PARSE_ERROR=2`, `RENDER_ERROR=3`, `IO_ERROR=4`

B) UNIX慣習（`sysexits.h`）に寄せた値（例: `EX_USAGE=64`等）を採用する

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 9: `--data`未指定時の挙動
FR-2「標準入力は既定でデータとして扱う」の解釈確認です。

A) `--data`が省略された場合は常に標準入力からデータを読み込む（要件文言通りの既定動作）

B) `--data`は必須オプションとし、省略時は`ArgumentException`（引数エラー）とする

X) Other (please describe after [Answer]: tag below)

[Answer]:
