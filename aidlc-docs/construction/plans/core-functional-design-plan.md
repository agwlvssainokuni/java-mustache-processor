# Functional Design Plan - core (Unit 1: Core Template Engine)

## 対象範囲
`unit-of-work.md`のUnit 1定義、および`requirements.md`のFR-1・NFR-1・NFR-3・NFR-4・NFR-6に基づき、Mustacheテンプレートエンジンの詳細な業務ロジック（パース規則、レンダリング規則、ドメインモデル、業務ルール、エラーハンドリング）を設計する。

## 実行ステップ

- [ ] Step 1: ビジネスロジックモデリング（パース・レンダリングアルゴリズムの詳細化）
- [ ] Step 2: ドメインモデル（AST・Context・デリミタ状態）の詳細化
- [ ] Step 3: 業務ルール（真偽判定、空白処理、エスケープ、ドット表記解決等）の確定
- [ ] Step 4: エラーハンドリング方針の確定
- [ ] Step 5: 成果物生成
  - [ ] `aidlc-docs/construction/core/functional-design/business-logic-model.md`
  - [ ] `aidlc-docs/construction/core/functional-design/business-rules.md`
  - [ ] `aidlc-docs/construction/core/functional-design/domain-entities.md`

## 対象外と判断した観点（理由付き）
- **Data Flow（永続化）**: 本Unitはファイル/DB等の永続化を一切行わない（純粋な文字列変換ロジック）ため対象外
- **Integration Points（外部システム連携）**: `PartialResolver`インターフェースによる抽象化のみで、具体的な外部システム（HTTP/DB等）との連携は持たない（Application Design済み）ため対象外
- **Frontend Components**: UIを持たないライブラリのため対象外

## 質問

各質問について、選択肢の記号を `[Answer]:` の後に記入してください。該当する選択肢がない場合は最後の「Other」を選び、内容を記述してください。

### Question 1: HTML エスケープ対象文字
`{{variable}}`（エスケープあり変数展開）でエスケープする文字セットは、公式Mustache仕様では厳密に規定されていません。どの文字集合を採用しますか？

A) `&` `<` `>` `"` の4文字（多くの実装で採用される最小セット）

B) `&` `<` `>` `"` `'` の5文字（アポストロフィも含める）

C) リファレンス実装（mustache.js）と同じマッピング（`&`→`&amp;`, `<`→`&lt;`, `>`→`&gt;`, `"`→`&quot;`, `'`→`&#39;`, `` ` ``→`&#x60;`, `=`→`&#x3D;`）

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 2: 公式specテストスイートの対象範囲
`mustache/spec`リポジトリには、コア仕様（`comments`, `delimiters`, `interpolation`, `inverted`, `partials`, `sections`）に加え、オプション扱いの拡張モジュール（`~lambdas`, `~inheritance`, `~dynamic-names`）があります。NFR-3「公式specテストスイートの全項目合格」の対象範囲はどこまでとしますか？

A) コア仕様＋`~lambdas`（FR-1でラムダ対応が明記されているため）のみを対象とする。`~inheritance`（テンプレート継承/ブロック）と`~dynamic-names`（動的パーシャル名）はFR-1に明記が無いためスコープ外とする

B) コア仕様のみを対象とし、`~lambdas`を含む全オプションモジュールをスコープ外とする

C) `~inheritance`・`~dynamic-names`を含む全モジュール（コア＋全オプション）を対象とする

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 3: Contextのスタック実装方式
Application Designの未確定事項として残っていた`Context`の実装方式です。

A) イミュータブル（`push()`は新しい`Context`インスタンスを返し、元の`Context`は変更しない。親への参照を保持してチェーンを辿る）

B) ミュータブル（内部にスタック構造を持ち、`push()`/`pop()`で同一インスタンスの状態を変更する）

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 4: POJOプロパティ解決の方式
ライブラリ利用者がデータとしてPOJOを渡した場合、プロパティはどう解決しますか？

A) JavaBean規約のgetter（`getXxx()`/`isXxx()`）のみをリフレクションで探索する

B) getterに加えて、getterが無い場合はpublicフィールドも探索する

C) Java 25のRecord型のアクセサ（`xxx()`、getプレフィックスなし）にも対応し、getter・Recordアクセサ・publicフィールドの順で探索する

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 5: パーシャルの再帰参照（循環参照）検出
パーシャルAがパーシャルBを含み、BがAを再び含むような循環参照がある場合の挙動はどうしますか？（Security Baseline: SECURITY-09堅牢化・SECURITY-15フェイルセーフの観点）

A) レンダリング中に再帰の深さ・出現済みパーシャル名を追跡し、循環を検出したら`MustacheRenderException`を送出する（`StackOverflowError`を防ぐ）

B) 特に対策しない（循環参照はテンプレート作者の責任とし、`StackOverflowError`が発生してもライブラリとしては関知しない）

X) Other (please describe after [Answer]: tag below)

[Answer]:

### Question 6: POJOアクセス時の例外処理
リフレクションによるgetter呼び出し中にPOJO側の実装が例外を送出した場合（例: getterの内部バグ）、どう扱いますか？

A) `MustacheRenderException`でラップして送出する（呼び出し元にエラーとして明示的に伝える）

B) キーが見つからなかった場合と同様に扱い、空文字列としてレンダリングを継続する（フェイルセーフ優先）

X) Other (please describe after [Answer]: tag below)

[Answer]:
