# Requirements Verification Questions

リポジトリ名（java-mustache-processor）から、Java向けのMustacheテンプレート処理系の開発が想定されますが、詳細を確認させてください。
各質問について、選択肢の記号（A, B, C...）を [Answer]: の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

## Question 1: プロジェクトの目的
このプロジェクトで何を実装しますか？

A) Javaで動作するMustacheテンプレートエンジン（テンプレートをレンダリングするコア処理系）の新規実装

B) 既存のJava製Mustacheライブラリ（mustache.java等）のラッパー・拡張

C) Mustacheテンプレートを別の形式（HTML以外の出力等）に変換するプロセッサ/トランスパイラ

X) Other (please describe after [Answer]: tag below)

[Answer]: A

## Question 2: 利用形態
このソフトウェアはどのような形で利用されますか？

A) 他のJavaアプリケーションに組み込むライブラリ（JARとして配布）

B) スタンドアロンのコマンドラインツール（CLI）

C) Webアプリケーション/APIサービスとして提供

D) 上記の組み合わせ（ライブラリ＋CLI等）

X) Other (please describe after [Answer]: tag below)

[Answer]: D

## Question 3: サポートするMustache仕様の範囲
どの範囲のMustache仕様をサポートしますか？

A) コア機能のみ（変数展開、セクション、否定セクション、コメント、パーシャル）

B) 公式仕様(mustache/spec)のフルサポート（デリミタ変更、ラムダ、ドット表記、HTMLエスケープ制御等を含む）

C) 独自に定めた簡易サブセットのみ

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 4: 入力データの形式
テンプレートに渡すデータはどのような形式を想定しますか？

A) Java の Map / POJO（プログラム内から直接渡す）

B) JSON文字列（外部から読み込んでレンダリングする）

C) 両方に対応する

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 5: パフォーマンス要件
パフォーマンスに関する要件はありますか？

A) 特になし（一般的な用途で十分な性能であればよい）

B) 高頻度・大量データのレンダリングを想定し、テンプレートのコンパイル結果のキャッシュ等の高速化が必要

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 6: ビルドツール・Javaバージョン
使用するビルドツールとJavaバージョンを教えてください。

A) Maven / Java 17

B) Maven / Java 21

C) Gradle / Java 17

D) Gradle / Java 21

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question 7: テスト要件
テストについてどこまでの範囲を求めますか？

A) JUnit 5による標準的な単体テストのみ

B) 単体テストに加え、Mustache公式のspecテストスイート（仕様準拠テスト）への対応も行う

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question: Security Extensions
Should security extension rules be enforced for this project?

A) Yes — enforce all SECURITY rules as blocking constraints (recommended for production-grade applications)

B) No — skip all SECURITY rules (suitable for PoCs, prototypes, and experimental projects)

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question: Resiliency Extensions
Should the resiliency baseline be applied to this project?

**What this extension is.** Enabling it applies a set of **directional, design-time best practices** for building resilient systems, derived from the **AWS Well-Architected Framework (Reliability Pillar)** and resilience-review guidance. It steers requirements, design, and code toward fault tolerance, high availability, observability, and recoverability.

**What this extension is NOT.** Enabling it does **not** make your workload production-ready, nor does it certify or guarantee any availability, RTO, or RPO target. It is a **starting point**, not a substitute for a formal AWS Well-Architected Review.

A) Yes — apply the resiliency baseline as directional best practices and design-time guidance (recommended for business-critical workloads)

B) No — skip the resiliency baseline (suitable for PoCs, prototypes, and experimental projects, and for a library like a template processor where this typically does not apply)

X) Other (please describe after [Answer]: tag below)

[Answer]: 

## Question: Property-Based Testing Extension
Should property-based testing (PBT) rules be enforced for this project?

A) Yes — enforce all PBT rules as blocking constraints (recommended for projects with business logic, data transformations, serialization, or stateful components — a template rendering engine typically qualifies)

B) Partial — enforce PBT rules only for pure functions and serialization round-trips

C) No — skip all PBT rules

X) Other (please describe after [Answer]: tag below)

[Answer]: 
