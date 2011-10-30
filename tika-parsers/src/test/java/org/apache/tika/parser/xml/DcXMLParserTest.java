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
package org.apache.tika.parser.xml;

import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

public class DcXMLParserTest extends TestCase {

    public void testXMLParserAsciiChars() throws Exception {
        InputStream input = DcXMLParserTest.class.getResourceAsStream(
                "/test-documents/testXML.xml");
        try {
            Metadata metadata = new Metadata();
            ContentHandler handler = new BodyContentHandler();
            new DcXMLParser().parse(input, handler, metadata);

            assertEquals("application/xml", metadata.getFormat());
            assertEquals("Tika test document", metadata.get(Metadata.TITLE));
            assertEquals("Rida Benjelloun", metadata.get(Metadata.CREATOR));
            assertEquals(
                    "Java, XML, XSLT, JDOM, Indexation",
                    metadata.get(Metadata.SUBJECT));
            assertEquals(
                    "Framework d\'indexation des documents XML, HTML, PDF etc..",
                    metadata.get(Metadata.DESCRIPTION));
            assertEquals(
                    "http://www.apache.org",
                    metadata.get(Metadata.IDENTIFIER));
            assertEquals("test", metadata.get(Metadata.TYPE));
            assertEquals("application/msword", metadata.get(Metadata.FORMAT));
            assertEquals("Fr", metadata.get(Metadata.LANGUAGE));
            assertTrue(metadata.get(Metadata.RIGHTS).contains("testing chars"));

            String content = handler.toString();
            assertTrue(content.contains("Tika test document"));
            
            assertEquals("2000-12-01T00:00:00.000Z", metadata.get(Metadata.DATE));
        } finally {
            input.close();
        }
    }
    
    public void testXMLParserNonAsciiChars() throws Exception {
        InputStream input = DcXMLParserTest.class.getResourceAsStream("/test-documents/testXML.xml");
        try {
            Metadata metadata = new Metadata();
            new DcXMLParser().parse(input, new DefaultHandler(), metadata);
            
            final String expected = "Archim\u00E8de et Lius \u00E0 Ch\u00E2teauneuf testing chars en \u00E9t\u00E9";
            assertEquals(expected,metadata.get(Metadata.RIGHTS));
        } finally {
            input.close();
        }
    }

}
