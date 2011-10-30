/**
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
package org.apache.tika.parser.microsoft;

import java.io.InputStream;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import junit.framework.TestCase;

public class PublisherParserTest extends TestCase {

    public void testPublisherParser() throws Exception {
        InputStream input = PublisherParserTest.class.getResourceAsStream(
                "/test-documents/testPUBLISHER.pub");
        try {
            Metadata metadata = new Metadata();
            ContentHandler handler = new BodyContentHandler();
            new OfficeParser().parse(input, handler, metadata, new ParseContext());

            assertEquals("application/x-mspublisher", metadata.getFormat());
            assertEquals(null, metadata.get(Metadata.TITLE));
            assertEquals("Nick Burch", metadata.get(Metadata.AUTHOR));
            String content = handler.toString();
            assertTrue(content.contains("0123456789"));
            assertTrue(content.contains("abcdef"));
        } finally {
            input.close();
        }
    }

}
