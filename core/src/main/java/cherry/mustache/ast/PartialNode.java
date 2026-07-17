package cherry.mustache.ast;

import cherry.mustache.MustacheRenderException;
import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * パーシャル参照（{@code {{&gt;partialName}}}）を表すノード。
 * パーシャル内容はコンパイル時には解決せず、レンダリング時に都度{@link cherry.mustache.PartialResolver}で解決する（1.4節）。
 */
public final class PartialNode extends Node {

    private static final String DEFAULT_OPEN = "{{";
    private static final String DEFAULT_CLOSE = "}}";

    private final String partialName;
    private final String indent;

    public PartialNode(String partialName, String indent) {
        this.partialName = partialName;
        this.indent = indent;
    }

    @Override
    public void render(Context context, RenderSession session, Writer out) throws IOException {
        String partialTemplate = session.partialResolver().resolve(partialName);
        if (partialTemplate == null) {
            return;
        }
        if (!session.beginPartial(partialName)) {
            throw new MustacheRenderException("Circular partial reference detected: " + partialName, partialName);
        }
        try {
            Node partialRoot = session.reparse(partialTemplate, DEFAULT_OPEN, DEFAULT_CLOSE);
            if (indent.isEmpty()) {
                partialRoot.render(context, session, out);
            } else {
                StringWriter buffer = new StringWriter();
                partialRoot.render(context, session, buffer);
                out.write(applyIndent(buffer.toString(), indent));
            }
        } finally {
            session.endPartial(partialName);
        }
    }

    private static String applyIndent(String rendered, String indent) {
        if (rendered.isEmpty()) {
            return rendered;
        }
        StringBuilder sb = new StringBuilder();
        String[] lines = rendered.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            boolean isLast = i == lines.length - 1;
            if (!(isLast && lines[i].isEmpty())) {
                sb.append(indent).append(lines[i]);
            }
            if (!isLast) {
                sb.append('\n');
            }
        }
        return sb.toString();
    }
}
