# NFR Design Plan - core (Unit 1: Core Template Engine)

## Step 1: NFR Requirements成果物の分析
- [x] `nfr-requirements.md`（Security: パストラバーサル対策・依存関係スキャン・例外ハードニング, Reliability: スレッドセーフ性検証, Maintainability: Javadoc）を確認
- [x] `tech-stack-decisions.md`（JUnit5, jqwik, OWASP Dependency-Check）を確認

## Step 2: カテゴリ別の適用判定と質問

- **Resilience Patterns（耐障害性）**: `FilePartialResolver`のファイルI/Oが唯一の外部リソースアクセス。エラー処理パターンの決定が必要。→ **質問あり（Q1）**
- **Scalability Patterns（拡張性）**: `nfr-requirements.md`で確定済みの通りN/A（coreはインメモリ処理のみで水平スケーリングの概念が無い）。**質問なし**
- **Performance Patterns（性能）**: NFR-1の「パース結果の再利用」をどう実現するか（グローバルキャッシュの要否）が未決定。→ **質問あり（Q2）**
- **Security Patterns（セキュリティ実装方式）**: NFR-SEC-1（パストラバーサル対策）の具体的な実装パターンが未決定。→ **質問あり（Q3）**
- **Logical Components（インフラ的コンポーネント）**: キュー・キャッシュ・サーキットブレーカー等の外部インフラコンポーネントは存在しない（coreはプロセス内ライブラリ）。ASTノード群・Context等のロジカルコンポーネントは既にFunctional Design（`domain-entities.md`）で設計済み。**N/A（質問なし）**

## Step 3: 質問

### Question 1: FilePartialResolverのファイルI/Oエラー処理パターン（Resilience）
`baseDir`配下のファイル読み取り時に、権限エラーやI/O例外（`IOException`）が発生した場合の扱いは？

- A) `IOException`等のファイルI/Oエラーは`MustacheRenderException`でラップして送出する（BR-8のPOJOアクセス例外ラップと一貫した方針。「ファイルが存在しない（未解決）」と「I/Oエラーが発生した（異常系）」を明確に区別する）
- B) ファイルI/Oエラーもすべて「未解決（`null`）」として黙って握りつぶす
- X) Other（自由記述）

[Answer]: A

### Question 2: パース結果再利用のキャッシュパターン（Performance）
NFR-1「パース結果を再利用可能な内部表現として保持する」の実現方式は？

- A) `Template`インスタンス自体がコンパイル済みASTを保持し、呼び出し側が`Template`インスタンスを再利用することで再パースを回避する。ライブラリ内部にグローバルな自動キャッシュ（テンプレート文字列→ASTのstaticマップ等）は設けない（シンプルで、キャッシュの無制限肥大化・無効化・スレッド間共有の複雑さを避けられる）
- B) ライブラリ内部に、テンプレート文字列をキーとしたグローバルキャッシュ（例: 内部staticマップ）を実装し、同一テンプレート文字列であれば自動的にコンパイル結果を再利用する
- X) Other（自由記述）

[Answer]: A

### Question 3: パストラバーサル検証の実装パターン（Security）
NFR-SEC-1の「`baseDir`外は未解決扱い」をどう実装するか？

- A) `java.nio.file.Path`の`normalize()`と`startsWith()`を用いた検証ロジックを`FilePartialResolver.resolve()`内に直接実装する（単一箇所でのみ使用されるため、専用ユーティリティクラスへの切り出しは行わない）
- B) 再利用可能な独立したパス検証ユーティリティクラス（例: `PathValidator`）を新設し、`FilePartialResolver`から利用する
- X) Other（自由記述）

[Answer]: A

## Step 4: 曖昧な回答の解析
（回答収集後に実施）

## Step 5: NFR Design成果物の生成
（承認後に実施: `nfr-design-patterns.md`, `logical-components.md`）
