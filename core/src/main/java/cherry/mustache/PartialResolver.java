package cherry.mustache;

/**
 * パーシャル（部分テンプレート）名からテンプレート文字列を解決するためのインターフェース。
 */
public interface PartialResolver {

    /**
     * @param partialName パーシャル名
     * @return パーシャルのテンプレート文字列。解決できない場合は{@code null}
     */
    String resolve(String partialName);
}
