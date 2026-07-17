package cherry.mustache.ast;

/**
 * Lambdaの戻り値やパーシャル本文を、指定したデリミタでASTに再パースするためのコールバック。
 * {@code ast}パッケージが{@code parser}パッケージへ直接依存しないための抽象化。
 */
@FunctionalInterface
public interface Reparser {

    /**
     * @param template       再パース対象のテンプレート文字列
     * @param openDelimiter  再パース開始時点で有効な開始デリミタ
     * @param closeDelimiter 再パース開始時点で有効な終了デリミタ
     * @return 再パース結果のASTルートノード
     */
    Node reparse(String template, String openDelimiter, String closeDelimiter);
}
