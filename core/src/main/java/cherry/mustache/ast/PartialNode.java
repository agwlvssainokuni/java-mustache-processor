/*
 * Copyright 2026 agwlvssainokuni
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cherry.mustache.ast;

import cherry.mustache.MustacheRenderException;
import cherry.mustache.render.Context;
import cherry.mustache.render.RenderSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * パーシャル参照（{@code {{&gt;partialName}}}）を表すノード。
 * パーシャル内容はコンパイル時には解決せず、レンダリング時に都度{@link cherry.mustache.PartialResolver}で解決する（1.4節）。
 */
public final class PartialNode extends Node {

    private static final Logger log = LoggerFactory.getLogger(PartialNode.class);

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
            log.debug("Partial not resolved, skipping: {}", partialName);
            return;
        }
        if (!session.beginPartial(partialName)) {
            log.warn("Circular partial reference detected: {}", partialName);
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
