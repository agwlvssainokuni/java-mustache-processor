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
package cherry.mustache.cli;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArgumentParserTest {

    private final ArgumentParser parser = new ArgumentParser();

    @Test
    void parsesSingleTemplateWithDefaults() {
        CliArguments args = parser.parse(new String[]{"template.mustache"});
        assertEquals(List.of("template.mustache"), args.templateArgs());
        assertNull(args.dataArg());
        assertNull(args.format());
        assertNull(args.outputPath());
        assertNull(args.partialDir());
        assertFalse(args.helpRequested());
    }

    @Test
    void parsesAllOptions() {
        CliArguments args = parser.parse(new String[]{
                "--data", "data.json",
                "--format", "json",
                "--output", "out.txt",
                "--partial-dir", "partials",
                "t1.mustache", "t2.mustache"
        });
        assertEquals(List.of("t1.mustache", "t2.mustache"), args.templateArgs());
        assertEquals("data.json", args.dataArg());
        assertEquals("json", args.format());
        assertEquals(Path.of("out.txt"), args.outputPath());
        assertEquals(Path.of("partials"), args.partialDir());
    }

    @Test
    void missingTemplateThrowsArgumentException() {
        assertThrows(ArgumentException.class, () -> parser.parse(new String[]{"--data", "data.json"}));
    }

    @Test
    void unknownOptionThrowsArgumentException() {
        assertThrows(ArgumentException.class, () -> parser.parse(new String[]{"--bogus", "template.mustache"}));
    }

    @Test
    void missingOptionValueThrowsArgumentException() {
        assertThrows(ArgumentException.class, () -> parser.parse(new String[]{"template.mustache", "--data"}));
    }

    @Test
    void invalidFormatThrowsArgumentException() {
        assertThrows(ArgumentException.class,
                () -> parser.parse(new String[]{"--format", "xml", "template.mustache"}));
    }

    @Test
    void helpFlagShortCircuitsOtherValidation() {
        CliArguments args = parser.parse(new String[]{"--help"});
        assertTrue(args.helpRequested());
    }

    @Test
    void helpShortOptionShortCircuitsOtherValidation() {
        CliArguments args = parser.parse(new String[]{"-h", "--bogus"});
        assertTrue(args.helpRequested());
    }

    @Test
    void stdinConflictBetweenTemplateAndDefaultDataThrows() {
        assertThrows(ArgumentException.class, () -> parser.parse(new String[]{"-"}));
    }

    @Test
    void stdinConflictBetweenTemplateAndExplicitDataMarkerThrows() {
        assertThrows(ArgumentException.class,
                () -> parser.parse(new String[]{"--data", "-", "-"}));
    }

    @Test
    void stdinConflictBetweenMultipleTemplatesThrows() {
        assertThrows(ArgumentException.class,
                () -> parser.parse(new String[]{"--data", "data.json", "-", "-"}));
    }

    @Test
    void singleStdinConsumerDoesNotThrow() {
        CliArguments args = parser.parse(new String[]{"--data", "data.json", "-"});
        assertEquals(List.of("-"), args.templateArgs());
    }
}
