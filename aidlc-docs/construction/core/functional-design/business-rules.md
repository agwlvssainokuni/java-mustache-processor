# Business Rules - core (Unit 1: Core Template Engine)

決定事項（core-functional-design-plan.md 回答結果）に基づく確定ルールを示す。

## BR-1: HTMLエスケープ対象文字（Question 1 = A）
`{{variable}}`（エスケープあり展開）で以下の4文字のみをエスケープする。

| 文字 | 置換後 |
|---|---|
| `&` | `&amp;` |
| `<` | `&lt;` |
| `>` | `&gt;` |
| `"` | `&quot;` |

- 置換は`&`を最初に処理すること（他の置換で生成された`&`を二重エスケープしないよう、元文字列に対して一括で走査・置換する）
- `{{{variable}}}` および `{{&variable}}` はエスケープを一切行わない

## BR-2: 公式specテストスイートの対象範囲（Question 2 = A）
- 対象: `comments.yml`, `delimiters.yml`, `interpolation.yml`, `inverted.yml`, `partials.yml`, `sections.yml`（コア仕様）＋ `~lambdas.yml`
- 対象外: `~inheritance.yml`（テンプレート継承/ブロック）, `~dynamic-names.yml`（動的パーシャル名）— FR-1に明記が無いためスコープ外。実装もこれらの構文（`{{$name}}`, `{{<name}}`, `{{*name}}`等）はサポートしない
- NFR-3「公式specテストスイートの全項目合格」は、上記スコープ内のテストケースを指す

## BR-3: 真偽判定（Truthy/Falsy）
セクション（`{{#name}}`）およびInverted Section（`{{^name}}`）における値の真偽は以下の通り:

| 値 | 判定 |
|---|---|
| `null` | falsy |
| `Boolean.FALSE` | falsy |
| 空のList/配列 | falsy |
| 空文字列 `""` | **truthy**（公式spec準拠。空リストのみがfalsyであり、空文字列は「1回描画する非リスト値」として扱う） |
| `0`、`0.0`等の数値 | **truthy**（`false`/`null`/空リスト以外はすべてtruthy） |
| 非空のList/配列 | truthy（要素ごとに繰り返し描画） |
| 上記以外（Map, POJO, `Boolean.TRUE`, 非ゼロ数値等） | truthy（1回描画） |
| `Lambda`インスタンス | 常にtruthy（Section）／常にtruthy扱いで無出力（Inverted Section） |

## BR-4: スタンドアロン行の空白除去
- 対象タグ: Section開始/終了、Inverted Section、Comment、Partial、Set Delimiter
- 対象外タグ: Variable（エスケープ有無問わず）— 変数タグは常にインライン展開として扱い、周囲の空白除去は行わない
- 判定・処理の詳細は`business-logic-model.md` 1.2節を参照

## BR-5: パーシャルのインデント再適用（Code Generation Step12で是正）
- スタンドアロン行として検出された`{{>partial}}`タグの行頭インデント文字列を記録し、パーシャルの**生テンプレート文字列（パース前）**の各行（末尾の改行を除く各行の先頭）に付与してから、パース・レンダリングする
- スタンドアロンでない（インライン中に埋め込まれた）パーシャルタグはインデント再適用を行わない
- **是正記録**: 当初「レンダリング後の出力文字列」にインデントを適用する設計としていたが、公式spec `partials.yml`「Standalone Indentation」テスト（パーシャル内の変数タグが複数行にわたる値に展開されるケース）が示す通り、レンダリング後の出力にインデントを適用すると、埋め込まれたデータ値自身に含まれる改行にも誤ってインデントが付与されてしまう。正しくは、パーシャルの**テンプレート文字列自体（タグ展開前）**の各行にインデントを付与してからパース・レンダリングする必要があり、これにより変数タグの展開結果（データ由来の改行）はインデントの影響を受けない

## BR-6: ドット表記とコンテキストスタックの探索範囲
- キーの最初のセグメントのみ、現在のスコープから親方向へスタックを遡って探索する（Section/Inverted Sectionのpushによって作られたスコープチェーンを辿る）
- 2番目以降のセグメントは、直前のセグメントで解決された値の内部のみを探索し、親方向へは探索しない
- 途中経路が解決できない場合（Broken Chain）はエラーとせず、変数展開は空文字列、セクション/Inverted Sectionはfalsyとして扱う

## BR-7: POJOプロパティ解決順序（Question 4 = C）
キー`xxx`に対し、以下の順序で最初に見つかった手段を採用する:
1. JavaBean規約のgetter: `getXxx()` または（戻り値がbooleanの場合）`isXxx()`
2. Java Recordのアクセサ: `xxx()`（get接頭辞なし、フィールド名と同名のpublicメソッド）
3. publicフィールド: `xxx`

いずれも見つからない場合は「未解決（missing）」として扱う（BR-6のBroken Chainと同様、エラーにはしない）。

## BR-8: POJOアクセス時の例外処理（Question 6 = A）
- BR-7の解決手段（getter/Recordアクセサ）の呼び出し自体が例外を送出した場合、`MustacheRenderException`でラップして送出し、レンダリングを中断する
- 「プロパティが存在しない（未解決）」場合と「プロパティ取得中に例外が発生した」場合は明確に区別する（前者は正常系、後者は異常系）

## BR-9: パーシャル循環参照の検出（Question 5 = A、Code Generation Step12で是正）
- 同一`render()`呼び出し内で、パーシャル解決のネスト深さが上限（100）に達した場合、`MustacheRenderException`を送出する
- **是正記録**: 当初「解決中のパーシャル名の再出現」を検出基準としていたが、公式spec `partials.yml`「Recursion」テスト（`{{content}}<{{#nodes}}{{>node}}{{/nodes}}>`のようにデータ駆動で終端する自己再帰パーシャル）が示す通り、同名パーシャルの正当な再帰利用（木構造の描画等）は一般的なユースケースであり、名前の再出現だけでは循環参照と区別できないことが判明した。検出基準を「ネスト深さの上限」に変更し、真に無限ループするケース（`MustacheRenderException`で安全に失敗させ`StackOverflowError`を防ぐという当初の目的、SECURITY-15）と、正当な有限再帰の両方を正しく扱えるようにした

## BR-10: Contextの実装方針（Question 3 = A）
- `Context`はイミュータブルとし、`push(Object data)`は新しい`Context`インスタンス（親への参照を保持）を返す
- 同一の`Template`インスタンスを複数スレッドから同時に`render()`しても、`Context`の状態が競合しないことを保証する設計とする

## BR-11: デリミタ変更のスコープ
- `{{=newopen newclose=}}`の効力は、それが出現した位置以降、同じ階層（同じセクション内、またはトップレベル）の末尾まで有効
- パーシャル内でのデリミタ変更は、そのパーシャル内のみで完結し、呼び出し元テンプレートのデリミタには影響しない（パーシャルは常にデフォルトデリミタ`{{ }}`から解析を開始する）
