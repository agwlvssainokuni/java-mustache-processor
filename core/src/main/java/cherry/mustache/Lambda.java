package cherry.mustache;

/**
 * セクション内の生テキストを処理する関数をコンテキスト値として渡すための関数型インターフェース。
 */
@FunctionalInterface
public interface Lambda {

    /**
     * @param text 変数タグ由来の呼び出しでは空文字列、セクションタグ由来の呼び出しではセクション内の生テンプレート文字列
     * @return 置換後の文字列（Mustacheテンプレートとして再解釈される）
     */
    String execute(String text);
}
