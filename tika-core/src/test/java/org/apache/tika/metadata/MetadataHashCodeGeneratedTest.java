/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tika.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import org.apache.tika.metadata.Property;

public class MetadataHashCodeGeneratedTest {

    @Test
    public void testEmptyMetadataHashCode() {
        Metadata metadata = new Metadata();

        assertEquals(0, metadata.hashCode());
    }

    @Test
    public void testHashCodeChangesWhenValueChanges() {
        Metadata metadata = new Metadata();

        metadata.set("key", "value1");
        int firstHash = metadata.hashCode();

        metadata.set("key", "value2");
        int secondHash = metadata.hashCode();

        assertNotEquals(firstHash, secondHash);
    }

    @Test
    public void testEqualMetadataHaveSameHashCode() {
        Metadata first = new Metadata();
        Metadata second = new Metadata();

        first.set("key", "value");

        second.set("key", "value");

        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
public void testHashCodeUsesXorAndAddition() {
    Metadata metadata = new Metadata();
    metadata.set("author", "Alice");
    metadata.set("title", "Document");

    int expected =
            ("author".hashCode() ^ java.util.Arrays.hashCode(new String[]{"Alice"}))
            + ("title".hashCode() ^ java.util.Arrays.hashCode(new String[]{"Document"}));

    assertEquals(expected, metadata.hashCode());
}
@Test
public void testCompositePropertySetsPrimaryProperty() {
    Metadata metadata = new Metadata();

    Property primary = Property.internalText("customPrimary");
    Property secondary = Property.internalText("customSecondary");

    Property composite = Property.composite(
            primary,
            new Property[]{secondary}
    );

    assertNull(metadata.get(primary));

    metadata.set(composite, "expectedValue");

    assertEquals("expectedValue", metadata.get(primary));
    assertEquals("expectedValue", metadata.get(secondary));
}
}
