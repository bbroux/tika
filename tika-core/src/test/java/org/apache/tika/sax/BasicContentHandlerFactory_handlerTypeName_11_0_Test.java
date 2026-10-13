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
package org.apache.tika.sax;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BasicContentHandlerFactory.HANDLER_TYPE;

@ExtendWith(MockitoExtension.class)
public class BasicContentHandlerFactory_handlerTypeName_11_0_Test {

    @Mock
    private ParseContext parseContext;

    private BasicContentHandlerFactory factory;

    @BeforeEach
    public void setUp() {
        factory = new BasicContentHandlerFactory();
    }

    @Test
    public void testHandlerTypeNameDefault() {
        assertEquals("MARKDOWN", factory.handlerTypeName());
    }

    @Test
    public void testHandlerTypeNameCustom() {
        factory.setType(HANDLER_TYPE.XML);
        assertEquals("XML", factory.handlerTypeName());
    }
}
