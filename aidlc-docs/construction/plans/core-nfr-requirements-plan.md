# NFR Requirements Plan - core (Unit 1: Core Template Engine)

## Step 1: Functional Design成果物の分析
- [x] `business-logic-model.md` / `business-rules.md` / `domain-entities.md` を確認
- [x] Testable Properties（PBT-01）を確認
- 特にNFR的な影響を持つポイント: `FilePartialResolver`によるファイルシステムアクセス、Contextのイミュータブル設計（スレッドセーフ性）、例外階層（メッセージに含める情報の範囲）

## Step 2: NFR観点の評価

以下の観点を評価した（requirements.mdのNFR-1〜NFR-6を前提とする）。

- **Scalability（拡張性）**: NFR-1で「特別な性能要件は設けない」と既に確定。coreはインメモリ処理のみで水平スケーリングの概念が無い。**N/A**（ライブラリであり、呼び出し側のスケーリング戦略に依存するため）
- **Performance（性能）**: NFR-1で「パース結果を再利用可能な内部表現として保持する」設計方針は確定済み。追加のベンチマーク要件は無し。**質問なし**
- **Availability（可用性）**: NFR-5でResiliency Baseline対象外と確定済み。coreはプロセス内ライブラリであり可用性の概念が無い。**N/A**
- **Security（セキュリティ）**: Security Baseline有効。`FilePartialResolver`のパストラバーサル対策（SECURITY-05/09）、依存関係脆弱性スキャン（SECURITY-10）、例外メッセージのハードニング（SECURITY-09）が論点。→ **質問あり（Q1, Q2, Q3）**
- **Tech Stack Selection（技術選定）**: PBT-09によりPBTフレームワークの最終確定が必要（要件でjqwikを想定済み）。→ **質問あり（Q4）**
- **Reliability（信頼性）**: Contextのイミュータブル設計によるスレッドセーフ性（BR-10）をどう検証するか。→ **質問あり（Q5）**
- **Maintainability（保守性）**: 公開APIのJavadoc要否。→ **質問あり（Q6）**
- **Usability（使いやすさ）**: coreはCLIを持たないライブラリAPIのみ。UI/UXの概念は無い。**N/A**

## Step 3: 質問

### Question 1: FilePartialResolverのパストラバーサル対策（SECURITY-05/09）
`baseDir`配下のファイルをパーシャル名から解決する`FilePartialResolver`において、パーシャル名に`../`等が含まれることで`baseDir`外のファイルを読み取られるリスクがある。どう対策するか？

- A) 解決後の絶対パスを正規化し、`baseDir`の配下に収まっていることを検証する。範囲外の場合は例外を投げずに「未解決（`null`）」として扱う（他の未解決パーシャルと同じ扱いに統一し、情報漏えいを防ぐ）
- B) パーシャル名に`.`や`/`等の区切り文字・親ディレクトリ参照が含まれる場合、`MustacheRenderException`を送出する（明示的なエラーとして扱う）
- C) 特に制限を設けない（呼び出し側が信頼できる`baseDir`とパーシャル名のみを渡す前提とする）
- X) Other（自由記述）

[Answer]: A

### Question 2: 依存関係の脆弱性スキャン（SECURITY-10）
- A) OWASP Dependency-Check Gradleプラグインを導入し、ビルド時に既知脆弱性をスキャンする（推奨。無料・広く使われるOSS）
- B) Gradleの依存関係ロック機能（`dependencies.lock`）のみを導入し、専用スキャンツールは今回は見送る
- C) スキャンは行わない（依存関係が少ないため対象外と判断）
- X) Other（自由記述）

[Answer]: A

### Question 3: 例外メッセージのハードニング方針（SECURITY-09）
`MustacheParseException`/`MustacheRenderException`のメッセージに含めてよい情報の範囲は？

- A) 行番号・列番号・タグ名・パーシャル名など、テンプレート開発者がデバッグに必要な仕様上の情報は含めてよいが、内部実装のスタックトレース文字列やJVM内部のクラス名等を独自に埋め込むことはしない（`cause`として保持しログ出力は呼び出し側に委ねる）
- B) 可能な限り詳細な内部情報（スタックトレース全体を文字列化したもの等）をメッセージに含める
- X) Other（自由記述）

[Answer]: A

### Question 4: PBTフレームワークのバージョン確定（PBT-09）
requirements.mdでjqwikを想定済み。バージョン方針は？

- A) `build.gradle.kts`にjqwikの最新安定版（JUnit 5 Platform対応）を明記し、以後の依存バージョンはコード生成時に確定する
- B) 特定バージョンをこの場で固定指定する
- X) Other（自由記述）

[Answer]: A

### Question 5: スレッドセーフ性の検証方法（Reliability / BR-10関連）
Contextはイミュータブル設計とし、同一`Template`インスタンスへの並行`render()`呼び出しを想定している（BR-10）。この性質をどう検証するか？

- A) 複数スレッドから同一`Template`インスタンスに対して同時に`render()`を呼び出し、各スレッドの出力が期待値と一致することを検証する専用のマルチスレッドテストを用意する
- B) 設計がイミュータブルであることの静的な説明（Javadoc等）のみとし、専用の並行テストは設けない
- X) Other（自由記述）

[Answer]: A

### Question 6: 公開APIのJavadoc要否（Maintainability）
- A) `core`パッケージの公開クラス・メソッド（`Mustache`, `Template`, `PartialResolver`, `Lambda`, 例外クラス等）にはJavadocを必須とする
- B) Javadocは任意とし、必須要件にはしない
- X) Other（自由記述）

[Answer]: A

## Step 4: 曖昧な回答の解析
（回答収集後に実施）

## Step 5: NFR Requirements成果物の生成
（承認後に実施: `nfr-requirements.md`, `tech-stack-decisions.md`）
