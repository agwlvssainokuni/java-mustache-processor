# NFR Design Plan - cli (Unit 2: CLI Tool)

## Step 1: NFR Requirements成果物の分析
- [x] `nfr-requirements.md`（Security: OWASP Dependency-Check適用・Jacksonデシリアライズ安全性・ファイルパス検証タイミング, Reliability: CLI固有ロジックのテスト方針）を確認
- [x] `tech-stack-decisions.md`（Jackson, Shadow plugin, OWASP Dependency-Check, 手書きArgumentParser）を確認

## Step 2: カテゴリ別の適用判定と質問

- **Resilience Patterns（耐障害性）**: 例外種別→`ExitCode`マッピングの実装パターン、Jackson例外の扱い、出力ファイル書き込みエラー時の扱いが未決定。→ **質問あり（Q1, Q2, Q4）**
- **Scalability Patterns（拡張性）**: `nfr-requirements.md`で確定済みの通りN/A（CLIは単発実行プロセスで水平スケーリングの概念が無い）。**質問なし**
- **Performance Patterns（性能）**: CLIは1回の実行で各テンプレートを1回ずつコンパイル・レンダリングするのみで、繰り返し実行や再利用の概念が無い（NFR-1のキャッシュ方針は`core`側で対応済み）。**N/A（質問なし）**
- **Security Patterns（セキュリティ実装方式）**: パーシャル解決時のパストラバーサル対策の実装分担（cli/core間の責務分界）が未決定。→ **質問あり（Q3）**
- **Logical Components（インフラ的コンポーネント）**: キュー・キャッシュ・サーキットブレーカー等の外部インフラコンポーネントは存在しない。`ArgumentParser`/`DataLoader`/`OutputWriter`等のロジカルコンポーネントは既にFunctional Design（`domain-entities.md`）で設計済み。**N/A（質問なし）**

## Step 3: 質問

各質問について、選択肢の記号を `[Answer]:` の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

### Question 1: 例外→ExitCodeマッピングの実装パターン（Resilience/SECURITY-09）
`business-logic-model.md` 4.1節で確定済みの対応表（例外種別→`ExitCode`）を、コード上どう実装するか？

A) `CliRunner.run()`内の単一のtry-catchチェーン（具体的な例外型から汎用的な型へ、上から順にcatch節を並べる）で一元的に例外を捕捉し`ExitCode`へ変換する。`ArgumentParser`/`DataLoader`/`Mustache`/`OutputWriter`等の各コンポーネントは素直に例外を送出するだけで、`ExitCode`を意識しない

B) 各コンポーネントがそれぞれの層で例外を捕捉し、`ExitCode`を含む結果オブジェクト（Result型）を返す設計とする（例外を使わないスタイル）

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2: Jackson例外の扱い（Resilience）
JacksonのJSON/YAML構文エラー時の例外（`JsonProcessingException`とそのサブクラス）は`java.io.IOException`のサブタイプであるため、ファイルI/Oエラー用のcatch節でそのまま捕捉すると`IO_ERROR`に誤分類されてしまう。

A) `DataLoader`はJacksonの例外を追加の独自例外クラスにはラップせずそのまま伝播させる。`CliRunner`のcatchチェーン（Q1）で、`JsonProcessingException`（および そのサブクラス）を通常の`IOException`より**先に**catchする節を設け、`PARSE_ERROR`へ正しくマッピングする（cli固有の新しい例外クラスを追加しない。Application Designで確定済みの例外構成（`ArgumentException`のみ）を変更しない）

B) `DataLoader`内でJacksonの例外を捕捉し、cli固有の新しい非検査例外クラス（例: `DataFormatException`）にラップして送出する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3: パーシャル解決のパストラバーサル対策の責務分担（Security）
`cli`は`FilePartialResolver`のインスタンス化（基準ディレクトリの決定、BR-5）のみを担当する。パストラバーサル対策自体の実装分担は？

A) パストラバーサル対策は`core`の`FilePartialResolver`が既に実装済み（`nfr-design-patterns.md`のNFR-SEC-1参照）であるため、`cli`側では重複した検証ロジックを一切実装しない。`cli`が独自に受け取る`--partial-dir`の値自体の存在確認も、Q3（NFR Requirements）で確定した「実行時に検出」方針に従い、事前検証は行わない

B) `cli`側でも`--partial-dir`に対する追加のパス検証（正規化・許可リスト等）を独自に実装する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 4: 出力ファイル書き込みエラー時の扱い（Resilience）
`OutputWriter`がファイル書き込み中に`IOException`（ディスク容量不足、権限エラー等）を検出した場合の扱いは？

A) `IOException`をそのまま`CliRunner`まで伝播させ`IO_ERROR`としてマッピングする。部分的に書き込まれた不完全な出力ファイルが残る可能性があるが、一時ファイル経由のアトミックな書き込み等の追加保証は行わない（シンプルさを優先。単一開発者・低頻度実行のCLIツールとして許容できるリスクと判断）

B) 一時ファイルに書き込んでから成功時にのみ最終ファイルへリネームする（アトミックな書き込みを保証する）

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Step 4: 曖昧な回答の解析
- [x] 全4問がA回答、曖昧な回答なし。追加確認質問は不要と判断

## Step 5: NFR Design成果物の生成
- [x] `aidlc-docs/construction/cli/nfr-design/nfr-design-patterns.md`
- [x] `aidlc-docs/construction/cli/nfr-design/logical-components.md`
