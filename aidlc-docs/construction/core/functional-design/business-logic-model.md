# Business Logic Model - core (Unit 1: Core Template Engine)

## 全体フロー

```
[コンパイル]  テンプレート文字列 --(Parser)--> AST（Nodeツリー）
[レンダリング] AST + データ + PartialResolver --(Renderer)--> 出力文字列
```

## 1. パース処理（Parser）

Parserは1回のパースパスで以下を同時に行う（実装は内部的に「タグのスキャン」と「ツリー構築」の2責務に分けてもよいが、公開コンポーネントとしては`Parser`単体）。

### 1.1 タグスキャン
- 現在有効なデリミタ（初期値 `{{` / `}}`）を使って、テキスト中のタグ開始・終了位置を走査する
- タグの種別はタグ内の先頭記号で判定する:
  - `{{{ ... }}}` または `{{& ... }}` → Unescaped Variable
  - `{{# ... }}` → Section開始
  - `{{^ ... }}` → Inverted Section開始
  - `{{/ ... }}` → Section/Inverted Section終了
  - `{{! ... }}` → Comment
  - `{{> ... }}` → Partial
  - `{{= ... =}}` → Set Delimiter
  - それ以外 → (Escaped) Variable
- `{{= newopen newclose =}}` を検出した時点で、以降のタグスキャンに使うデリミタを即座に切り替える（デリミタ変更の効力は、それを含むセクションの終わりまで、またはファイル末尾まで。パーシャル内でのデリミタ変更は呼び出し元テンプレートに影響しない）

### 1.2 スタンドアロン行の判定と空白除去
対象タグ種別: Section開始/終了、Inverted Section、Comment、Partial、Set Delimiter（Variable/Unescaped Variableは対象外）

判定条件: そのタグが行内で唯一のコンテンツであり、かつ行頭からタグ開始までが空白文字のみ、タグ終了から行末（改行含む）までが空白文字のみである場合、そのタグは「スタンドアロン」と判定する。

スタンドアロン判定時の処理:
- Section開始/終了、Inverted Section、Comment、Set Delimiter: その行全体（前後の空白と改行）を出力から除去する
- Partial: 前後の空白と改行を除去した上で、行頭の字下げ（インデント文字列）を記録し、パーシャル本文の各行にそのインデントを付与してレンダリングする（1.4節参照）

### 1.3 ツリー構築
- テキスト片は`TextNode`として、タグは対応する`Node`サブタイプとして、出現順に親ノードの子リストへ追加する
- `{{#name}}` / `{{^name}}` の出現時、対応する`SectionNode`/`InvertedSectionNode`をスタックにpushし、以降の子ノードはそのスタックトップの子として追加する
- `{{/name}}` の出現時、スタックトップのノード名と一致することを検証し、一致すればpopしてツリー構築を1階層戻す。不一致・対応の無い開始/終了タグは構文エラーとして`MustacheParseException`を送出する
- パース終了時にスタックが空でなければ（閉じられていないセクションがあれば）`MustacheParseException`を送出する

### 1.4 パーシャルの扱い（パース時点）
- パーシャル自体の内容はコンパイル時には解決しない（`PartialResolver`は`Template`が保持し、レンダリング時に都度解決する。これによりパーシャル内容の実行時差し替えが可能になる）
- `PartialNode`は「パーシャル名」と「スタンドアロン判定時に記録したインデント文字列（無ければ空文字列）」のみを保持する

## 2. レンダリング処理（Renderer + Context）

### 2.1 走査の起点
`Template.render(data, ...)`は、ルートの`Context`（渡された`data`を最上位スコープとする）を生成し、ASTルートの各`Node`に対して順に`render(context, partialResolver, out)`を呼び出す（ポリモーフィズム、Application Design Q7）。

### 2.2 ノード種別ごとの処理
- **TextNode**: 保持する文字列をそのまま`out`に書き込む
- **VariableNode（エスケープあり）**: `context.resolve(key)`で値を解決し、`String.valueOf(value)`相当で文字列化した後、HTMLエスケープ（business-rules.md参照）を適用して書き込む。値が`Lambda`の場合は2.4節の手順に従う
- **UnescapedVariableNode**: 同様に解決・文字列化するが、エスケープは適用しない
- **SectionNode**: `context.resolve(key)`の値に応じて分岐する（真偽判定の詳細はbusiness-rules.md）
  - 値が`Lambda`: 2.4節の手順に従う
  - 値が真かつリスト: リストの各要素について、その要素を新しいスコープとして`context.push(element)`し、セクション内の子ノード群をレンダリングする
  - 値が真かつリスト以外（オブジェクト/真偽値true等）: その値を新しいスコープとして`context.push(value)`し（値がMap/POJOでない場合は現在のコンテキストをそのままpushする、単純に「1回だけ描画する」ためのスコープとして扱う）、子ノード群を1回レンダリングする
  - 値が偽（falsy）: 何も出力しない
- **InvertedSectionNode**: 値が偽（falsy）の場合のみ、現在のコンテキストのまま（pushしない）子ノード群をレンダリングする。値が真の場合は何も出力しない
- **CommentNode**: 何も出力しない（コンパイル時点で内容は保持するが、レンダリング時には無視する）
- **PartialNode**:
  1. `partialResolver.resolve(partialName)`でテンプレート文字列を取得する。`null`が返された場合は空文字列として扱い、何も出力しない
  2. 取得したテンプレート文字列を、パーシャル解決時点の`PartialResolver`と共に`Parser`で都度パースする（パーシャルは呼び出しごとに動的に解決されるため、事前コンパイルはしない）
  3. `PartialNode`が保持するインデント文字列が空でない場合、パース結果のレンダリング出力の各行（最終行の末尾改行を除く）の先頭にそのインデントを付与する
  4. 現在の`Context`（pushせず、そのまま）を使ってレンダリングし、`out`に書き込む
  5. 循環参照検出（2.5節）を適用する

### 2.3 Contextでの値解決
- `context.resolve(key)`はキーがドット表記（`a.b.c`）を含む場合、最初のセグメントのみ「現在のスコープから親方向へのスタック探索」を行い、見つかった値に対して以降のセグメントを「その値の内部のみ」で解決する（親方向へは探索しない）
- 各セグメントの解決は、値がMapなら`Map.get(segment)`、POJO/Recordならbusiness-rules.md「POJOプロパティ解決順序」に従う
- 途中のセグメントが解決できない（missing、またはMap/POJOではないプリミティブに対して更なるセグメント解決を試みた）場合、そのタグは「未解決」として扱い、変数展開は空文字列、セクションは偽として扱う（Broken Chain）

### 2.4 ラムダの扱い
- **VariableNode/UnescapedVariableNodeの値がLambda**: `Lambda.execute("")`相当（変数タグの場合、渡す生テキストは無い）を呼び出し、返却された文字列を**現在のデリミタで**テンプレートとして再パース・再レンダリング（現在の`Context`を使用）した結果を、タグ種別に応じてエスケープ有無を適用して出力する
- **SectionNodeの値がLambda**: そのセクションの開始タグと終了タグに挟まれた「生のテンプレート文字列（未パース、未解釈）」を`Lambda.execute(rawText)`に渡し、返却された文字列を現在のデリミタ・現在の`Context`で再パース・再レンダリングした結果を出力する（Section由来のためエスケープは適用しない、公式spec準拠）
- **InvertedSectionNodeの値がLambda**: Lambdaは常に真とみなし、Inverted Sectionとしては何も出力しない（公式spec準拠）

### 2.5 パーシャル循環参照の検出
- レンダリング呼び出し単位（`Template.render()`の1回の呼び出し）ごとに、現在解決中のパーシャル名の集合（呼び出しスタック）を追跡する
- `PartialNode`のレンダリング開始時に、そのパーシャル名が既に追跡中の集合に含まれていれば、循環参照と判定し`MustacheRenderException`を送出する。含まれていなければ集合に追加してからレンダリングし、完了後に集合から除去する

## 3. エラー発生ポイントの整理
- **パースエラー**（`MustacheParseException`）: タグの対応不整合、不正なデリミタ指定、未終了のタグ等、Parserが構文的に処理できない場合
- **レンダリングエラー**（`MustacheRenderException`）: パーシャル循環参照、POJOアクセス時の例外（getter/Recordアクセサ内で例外発生）
- 上記以外（未解決キー、未解決パーシャル）はエラーとせず、空文字列/falsy扱いとしてレンダリングを継続する（Broken Chain / Missing Partial は公式spec上も正常系）
