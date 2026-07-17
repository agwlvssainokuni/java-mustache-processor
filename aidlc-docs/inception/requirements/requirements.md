# Requirements Document

## Intent Analysis

- **User Request**: 「ソフトウェアの開発を開始します。」（初期リクエストは目的未確定。リポジトリ名 `java-mustache-processor` とその後の質疑応答を通じて具体化した）
- **Request Type**: New Project（Greenfield）
- **Scope Estimate**: Multiple Components（テンプレートエンジン本体（パーサー／レンダラー）、ライブラリAPI、CLIラッパーの複数コンポーネントから構成）
- **Complexity Estimate**: Moderate（公式Mustache仕様のフルサポート、ラムダ対応、複数データ形式対応を含むため、単純なCRUD等と比べて相応の複雑度がある）

## Functional Requirements

### FR-1: Mustacheテンプレートエンジン（ライブラリ）
- Java向けのMustacheテンプレートエンジンを新規実装する
- 公式仕様（[mustache/spec](https://github.com/mustache/spec)）をフルサポートする:
  - 変数展開（エスケープ有り/無し）
  - セクション（真偽値・リスト・ラムダによる真偽判定含む）
  - 否定セクション
  - コメント
  - パーシャル（部分テンプレート）
  - デリミタ変更（Set Delimiter）
  - ラムダ（セクション内テキストを処理する関数をコンテキスト値として渡せる）
  - ドット表記によるネストしたプロパティ参照
- ライブラリAPIは、テンプレートに渡すデータとして Java の `Map`／POJO を受け取れること
- ラムダはJavaの関数型インターフェースとしてコンテキストに渡せること

### FR-2: CLIツール
- コマンドラインからMustacheテンプレートエンジンを利用できるCLIを提供する
- テンプレートファイルと入力データファイルを指定してレンダリング結果を標準出力（またはファイル）に出力できること
- 入力データ形式として **JSON** と **YAML** の両方をサポートすること
- **制約**: CLIは静的なデータファイル（JSON/YAML）のみを入力とするため、ラムダ（実行可能なコード）をコンテキストに渡す手段がない。したがって、CLIモードではラムダを含むテンプレートを正しくレンダリングすることはサポート対象外とし、その旨をドキュメントに明記する。ラムダが必要な場合はライブラリAPIを直接利用することを案内する。CLI向けの組み込みラムダ機構（名前付きラムダをデータ側から参照する等の独自拡張）は設けない。

## Non-Functional Requirements

### NFR-1: パフォーマンス
- 特別な性能要件は設けない（高頻度・大量データを想定した高速化は今回のスコープ外）
- ただし、テンプレートを都度文字列解析するのではなく、パース結果をコンパイル済みの内部表現として保持し再利用できる、一般的に妥当な設計とする

### NFR-2: ビルド環境
- ビルドツール: Gradle（Gradle Wrapperを使用し、開発者のGradleインストール状況に依存しないようにする）
- Java バージョン: Java 25

### NFR-3: テスト
- JUnit 5による標準的な単体テストを実施する
- Mustache公式のspecテストスイート（YAML形式で提供される仕様準拠テストケース）に対応し、公式仕様への準拠を検証する

### NFR-4: セキュリティ（Security Baseline拡張: 有効）
- Security Baseline拡張のルール（SECURITY-01〜SECURITY-15）を適用する
- 本プロジェクトはネットワークサービス・認証機能・永続データストアを持たないライブラリ／CLIであるため、インフラ系・認証系のルール（SECURITY-01, 02, 04, 06, 07, 08, 11, 12, 14 等）の多くは設計・実装段階で **N/A** と評価される見込みだが、各構築ステージで個別に判定する
- 特に該当する可能性が高いルール:
  - SECURITY-05（入力検証）: CLIが受け取るJSON/YAMLの妥当性検証
  - SECURITY-09（堅牢化・エラーハンドリング）: 内部情報を露出しないエラーメッセージ
  - SECURITY-10（ソフトウェアサプライチェーン）: 依存関係のロックファイル管理・脆弱性スキャン
  - SECURITY-13（デシリアライズ安全性）: JSON/YAMLデシリアライズ時の安全性（特にYAMLは不用意なデシリアライザ実装が任意コード実行につながることがあるため要注意）
  - SECURITY-15（例外処理・フェイルセーフ）: パースエラー・レンダリングエラー時の安全な失敗

### NFR-5: レジリエンシー（Resiliency Baseline拡張: 無効）
- 本プロジェクトはホスティングされるサービスではなくライブラリ／CLIであり、可用性・DR・運用監視などのAWS Well-Architected信頼性の観点は対象外と判断し、Resiliency Baseline拡張は適用しない

### NFR-6: テスト手法（Property-Based Testing拡張: 有効・フル適用）
- Property-Based Testing拡張のルール（PBT-01〜PBT-10）をフル適用する
- 特に該当する性質:
  - Round-trip（PBT-02）: パース→レンダリングやシリアライズ／デシリアライズの往復性
  - Invariant（PBT-03）: レンダリング結果の構造的な不変性（例: エスケープ処理の一貫性）
  - Oracle（PBT-05）: 公式specテストスイートとの突合
- 使用フレームワーク: jqwik（JUnit 5との統合、Javaにおける標準的な選択）を想定（構築フェーズのNFR Requirementsステージで最終確定）

## Extension Configuration Summary

| Extension | Enabled | 適用範囲 |
|---|---|---|
| Security Baseline | Yes | 全SECURITY-01〜15ルールを適用（該当しないものはN/A評価） |
| Resiliency Baseline | No | 適用しない |
| Property-Based Testing | Yes | フル適用（PBT-01〜10すべてブロッキング） |

## Summary

Java 25 / Gradle（Gradle Wrapper）を用いて、公式Mustache仕様（mustache/spec）にフル準拠したテンプレートエンジンをライブラリとして新規実装し、あわせてJSON/YAMLデータを入力とするCLIツールを提供する。CLIはラムダ非対応という明確な制約を持つ一方、ライブラリAPIはラムダを含むフル仕様をサポートする。テストはJUnit 5による単体テストと公式specテストスイート準拠テストに加え、Property-Based Testing（jqwik想定）による性質ベースのテストを実施する。Security Baselineは適用するがResiliency Baselineは対象外とする。
