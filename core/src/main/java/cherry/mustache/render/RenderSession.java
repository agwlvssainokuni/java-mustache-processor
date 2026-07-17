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
package cherry.mustache.render;

import cherry.mustache.PartialResolver;
import cherry.mustache.ast.Node;
import cherry.mustache.ast.Reparser;

import java.util.HashSet;
import java.util.Set;

/**
 * 1回の{@code render()}呼び出しに紐づくレンダリング状態。
 * パーシャル解決・Lambda出力の再パース・パーシャル循環参照検出（BR-9）をまとめて提供する。
 * インスタンスは{@code render()}呼び出しごとに生成され、呼び出しスタックローカルに保持される
 * （スレッドローカルではない。nfr-design-patterns.md参照）。
 */
public final class RenderSession {

    private final PartialResolver partialResolver;
    private final Reparser reparser;
    private final Set<String> resolvingPartials = new HashSet<>();

    public RenderSession(PartialResolver partialResolver, Reparser reparser) {
        this.partialResolver = partialResolver;
        this.reparser = reparser;
    }

    public PartialResolver partialResolver() {
        return partialResolver;
    }

    public Node reparse(String template, String openDelimiter, String closeDelimiter) {
        return reparser.reparse(template, openDelimiter, closeDelimiter);
    }

    /**
     * @param partialName 解決を開始するパーシャル名
     * @return 循環参照ではなく解決を開始できた場合は{@code true}
     */
    public boolean beginPartial(String partialName) {
        return resolvingPartials.add(partialName);
    }

    public void endPartial(String partialName) {
        resolvingPartials.remove(partialName);
    }
}
