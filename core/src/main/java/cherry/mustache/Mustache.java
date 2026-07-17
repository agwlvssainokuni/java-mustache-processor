package cherry.mustache;

import cherry.mustache.ast.Node;
import cherry.mustache.parser.Parser;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

/**
 * Mustacheテンプレートをコンパイルするためのstaticファクトリ。
 */
public final class Mustache {

    private Mustache() {
    }

    /**
     * @param template テンプレート文字列
     * @return コンパイル済みの{@link Template}（パーシャルリゾルバーは空のデフォルト実装）
     */
    public static Template compile(String template) {
        return compile(template, new MapPartialResolver(Map.of()));
    }

    /**
     * @param template        テンプレート文字列
     * @param partialResolver パーシャル解決に使う{@link PartialResolver}
     * @return コンパイル済みの{@link Template}
     */
    public static Template compile(String template, PartialResolver partialResolver) {
        Node root = new Parser().parse(template);
        return new Template(root, partialResolver);
    }

    /**
     * @param reader          テンプレート文字列を読み込む{@link Reader}
     * @param partialResolver パーシャル解決に使う{@link PartialResolver}
     * @return コンパイル済みの{@link Template}
     */
    public static Template compile(Reader reader, PartialResolver partialResolver) {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[4096];
        try {
            int read;
            while ((read = reader.read(buffer)) != -1) {
                sb.append(buffer, 0, read);
            }
        } catch (IOException e) {
            throw new MustacheException("Failed to read template", e);
        }
        return compile(sb.toString(), partialResolver);
    }
}
