# NFR Requirements Plan - cli (Unit 2: CLI Tool)

## Step 1: Functional Design成果物の分析
- [x] `business-logic-model.md` / `business-rules.md` / `domain-entities.md` を確認
- 特にNFR的な影響を持つポイント: Jacksonによる外部データ（JSON/YAML）のデシリアライズ、fat jar配布形態（NFR-2）、`CliRunner.run(args, in, out, err)`のテスト容易性を意識したシグネチャ、依存関係にJacksonが新規追加される点

## Step 2: NFR観点の評価

以下の観点を評価した（requirements.mdのNFR-1〜NFR-6、およびFunctional Designで確定済みのBR-1〜BR-9を前提とする）。

- **Scalability（拡張性）**: NFR-1で「特別な性能要件は設けない」と確定済み。CLIは単発実行のプロセスであり水平スケーリングの概念が無い。**N/A**
- **Performance（性能）**: NFR-1と同様、追加のベンチマーク要件は無し。**N/A**
- **Availability（可用性）**: NFR-5でResiliency Baseline対象外と確定済み。CLIは単発実行プロセスであり可用性の概念が無い。**N/A**
- **Security（セキュリティ）**: Security Baseline有効。fat jarに含まれるJackson等cli固有の依存関係の脆弱性スキャン（SECURITY-10）、JSON/YAMLデシリアライズの安全性設定（SECURITY-13）、コマンドライン引数（ファイルパス）の検証タイミング（SECURITY-05）が論点。→ **質問あり（Q1, Q2, Q3）**
- **Tech Stack Selection（技術選定）**: fat jar構築方式が未確定（NFR-2「実行可能fat jarとして配布」の具体的な実現手段）。→ **質問あり（Q4）**
- **Reliability（信頼性）**: `CliRunner`のオーケストレーションと終了コードマッピングはFunctional Designで確定済み。検証方法（テスト方針）が論点。→ **質問あり（Q5）**
- **Maintainability（保守性）**: `cli`はライブラリとして再利用されるAPIを公開しない（`Main`がエントリポイント）ため、`core`のような公開APIへのJavadoc必須化は該当しない。**N/A**（内部クラスへのJavadocは任意）
- **Usability（使いやすさ）**: 引数の長短形式については`business-rules.md` BR-2/BR-7で`--help`/`-h`以外は長形式オプションのみとする方針が既に確定済み。追加の質問は不要。**質問なし**

## Step 3: 質問

各質問について、選択肢の記号を `[Answer]:` の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

### Question 1: cliモジュールへのOWASP Dependency-Check適用（SECURITY-10）
`core/build.gradle.kts`にはOWASP Dependency-Checkプラグインを導入済み。`cli`モジュールにも新規追加されるJackson等の依存関係があるため、同様の対応が必要か？

A) `cli/build.gradle.kts`にも同じプラグインを適用し、`core`と同様に`cli`固有の依存関係（Jackson等）も個別にスキャン対象とする（既存の「サブプロジェクトごとに設定」という構成方針と一貫する）

B) ルートの`build.gradle.kts`に一括で適用し、全サブプロジェクトの依存関係をまとめてスキャンする（設定の重複を避ける）

C) `cli`はスキャン対象外とする

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 2: Jacksonによるデシリアライズの安全性設定（SECURITY-13）
JSON/YAMLデータの読み込みに`ObjectMapper`を使う際の安全性方針です。

A) `ObjectMapper`は既定設定のまま使用し、多相型ハンドリング（`enableDefaultTyping`等の任意クラスへのデシリアライズを許す機能）は一切有効化しない。読込先は常に`Map<String, Object>`型（`readValue(source, Map.class)`相当）に固定し、任意のJavaクラスへのバインディングを行わない設計とする（既定で安全側であり追加対策は不要）

B) 上記に加え、最大ネスト深さ制限等の追加のハードニング設定を明示的に施す

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 3: コマンドライン引数のファイルパス検証タイミング（SECURITY-05）
テンプレート/データ/出力/パーシャルディレクトリの各パスについて、存在確認や読み書き可否の検証をいつ行うか？

A) `ArgumentParser`では値の構文検証（空文字列でない等）のみを行い、実際のファイルアクセス可否は各処理の実行時（ファイルオープン時）に自然に検出し`IOException`→`IO_ERROR`として扱う（事前の存在チェックは二重コスト・TOCTOU問題を避けるため行わない。coreの`FilePartialResolver`と同じ「実行時に検出」の考え方に統一）

B) `ArgumentParser`内で全パスの存在確認を事前にまとめて行い、検証結果をまとめて返す

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 4: fat jar構築方式の選定（NFR-2、Tech Stack Selection）
A) Gradle Shadow plugin（`com.gradleup.shadow`、旧`com.github.johnrengelman.shadow`の後継）を導入し、依存関係を含む単一の実行可能fat jarを構築する（デファクトスタンダードで設定も比較的シンプル）

B) `application`プラグインの`distZip`/`distTar`（fat jarではなく、実行スクリプト＋依存jar群を同梱した配布アーカイブ形式）を使用する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

### Question 5: CLI固有ロジックのテスト方針（NFR-3・Reliability）
`CliRunner.run(args, in, out, err)`は標準入出力を引数として受け取る設計（テスト容易性のため）です。

A) JUnit 5単体テストで標準入出力を`ByteArrayInputStream`/`ByteArrayOutputStream`に差し替え、代表的な引数パターン・データ形式・エラーケースごとに`ExitCode`と出力内容（stdout/stderr）を検証する。実際に`java -jar`でfat jarをプロセス起動するE2Eテストは行わない

B) 上記に加え、fat jarをビルドして実際に`java -jar`でプロセス起動するE2Eスモークテストも用意し、Shadow pluginによる同梱・Manifest設定が正しく機能することまで検証する

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Step 4: 曖昧な回答の解析
- [x] 全5問がA回答、曖昧な回答なし。追加確認質問は不要と判断

## Step 5: NFR Requirements成果物の生成
- [x] `aidlc-docs/construction/cli/nfr-requirements/nfr-requirements.md`
- [x] `aidlc-docs/construction/cli/nfr-requirements/tech-stack-decisions.md`
