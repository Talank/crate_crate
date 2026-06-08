/*
 * Licensed to Crate.io GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate.protocols.postgres.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

import io.netty.buffer.Unpooled;

public class CharTypeTest extends BasePGTypeTest<String> {

    public CharTypeTest() {
        super(CharType.INSTANCE);
    }

    @Test
    public void test_write_binary_value() {
        assertBytesWritten("A", new byte[]{0, 0, 0, 1, 65});
    }

    @Test
    public void test_read_binary_value() {
        assertBytesReadBinary(new byte[]{65}, "A");
    }

    @Test
    public void test_read_text_value() {
        assertThat(pgType.encodeAsUTF8Text("c")).isEqualTo("c".getBytes(StandardCharsets.UTF_8));
        assertBytesReadText("c".getBytes(StandardCharsets.UTF_8), "c", 1);
    }

    @Test
    public void test_write_multi_byte_value_throws() {
        var buffer = Unpooled.buffer();
        try {
            assertThatThrownBy(() -> pgType.writeAsBinary(buffer, "ab"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("\"char\" value must be a single byte: ab");
            assertThatThrownBy(() -> pgType.writeAsBinary(buffer, "ä"))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage("\"char\" value must be a single byte: ä");
        } finally {
            buffer.release();
        }
    }
}
