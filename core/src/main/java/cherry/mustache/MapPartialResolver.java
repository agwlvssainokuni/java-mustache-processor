package cherry.mustache;

import java.util.Map;

/**
 * {@code Map<String, String>}をパーシャル名からテンプレート文字列への対応表として使う{@link PartialResolver}実装。
 */
public final class MapPartialResolver implements PartialResolver {

    private final Map<String, String> partials;

    /**
     * @param partials パーシャル名からテンプレート文字列への対応表
     */
    public MapPartialResolver(Map<String, String> partials) {
        this.partials = partials;
    }

    @Override
    public String resolve(String partialName) {
        return partials.get(partialName);
    }
}
