package cherry.mustache.ast;

/**
 * BR-1で確定した4文字（{@code & < > "}）のみを対象とするHTMLエスケープ処理。
 */
final class HtmlEscaper {

    private HtmlEscaper() {
    }

    static String escape(String value) {
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }
}
