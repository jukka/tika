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
package org.apache.tika.parser.rtf;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.tika.TikaTest;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.WriteOutContentHandler;
import org.apache.tika.utils.ParseUtils;

/**
 * Junit test class for the Tika {@link RTFParser}
 */
public class RTFParserTest extends TikaTest {
    private RTFParser parser;
    private static final TikaConfig defaultConfig = TikaConfig.getDefaultConfig();

    private static class Result {
        public final String text;
        public final Metadata metadata;

        public Result(String text, Metadata metadata) {
            this.text = text;
            this.metadata = metadata;
        }
    }

    public void setUp() throws Exception {
        parser = new RTFParser();
    }

    public void testBasicExtraction() throws Exception {
        File file = getResourceAsFile("/test-documents/testRTF.rtf");
        
        Metadata metadata = new Metadata();
        StringWriter writer = new StringWriter();
        parser.parse(
                     new FileInputStream(file),
                     new WriteOutContentHandler(writer),
                     metadata,
                     new ParseContext());
        String content = writer.toString();

        assertEquals("application/rtf", metadata.getFormat());
        assertContains("Test", content);
        assertContains("indexation Word", content);
    }

    public void testUmlautSpacesExtraction2() throws Exception {
        String content = getText("testRTFUmlautSpaces2.rtf");
        content = content.replaceAll("\\s+", "");
        assertEquals("\u00DCbersicht", content);
    }

    public void testUnicodeUCNControlWordCharacterDoublingExtraction() throws Exception {
        String content = getText("testRTFUnicodeUCNControlWordCharacterDoubling.rtf");

        assertContains("\u5E74", content);
        assertContains("\u5ff5", content);
        assertContains("0 ", content);
        assertContains("abc", content);
        assertFalse("Doubled character \u5E74", content.contains("\u5E74\u5E74"));
    }

    public void testHexEscapeInsideWord() throws Exception {
        String content = getText("testRTFHexEscapeInsideWord.rtf");
        assertContains("ESP\u00cdRITO", content);
    }

    public void testWindowsCodepage1250() throws Exception {
        String content = getText("testRTFWindowsCodepage1250.rtf");
        assertContains("za\u017c\u00f3\u0142\u0107 g\u0119\u015bl\u0105 ja\u017a\u0144", content);
        assertContains("ZA\u017b\u00d3\u0141\u0106 G\u0118\u015aL\u0104 JA\u0179\u0143", content);
    }

    public void testTableCellSeparation() throws Exception {
        File file = getResourceAsFile("/test-documents/testRTFTableCellSeparation.rtf");
        String s1 = ParseUtils.getStringContent(file, defaultConfig);
        String s2 = ParseUtils.getStringContent(file, defaultConfig, "application/rtf");
        assertEquals(s1, s2);
        String content = s1;
        content = content.replaceAll("\\s+"," ");
        assertTrue(content.contains("a b c d \u00E4 \u00EB \u00F6 \u00FC"));
        assertContains("a b c d \u00E4 \u00EB \u00F6 \u00FC", content);
    }
    
    public void testTableCellSeparation2() throws Exception {
        String content = getText("testRTFTableCellSeparation2.rtf");
        // TODO: why do we insert extra whitespace...?
        content = content.replaceAll("\\s+"," ");
        assertContains("Station Fax", content);
    }

    public void testWordPadCzechCharactersExtraction() throws Exception {
        File file = getResourceAsFile("/test-documents/testRTFWordPadCzechCharacters.rtf");
        String s1 = ParseUtils.getStringContent(file, defaultConfig);
        String s2 = ParseUtils.getStringContent(file, defaultConfig, "application/rtf");
        assertEquals(s1, s2);
        assertTrue(s1.contains("\u010Cl\u00E1nek t\u00FDdne"));
        assertTrue(s1.contains("starov\u011Bk\u00E9 \u017Eidovsk\u00E9 n\u00E1bo\u017Eensk\u00E9 texty"));
    }

    public void testWord2010CzechCharactersExtraction() throws Exception {
        File file = getResourceAsFile("/test-documents/testRTFWord2010CzechCharacters.rtf");
        String s1 = ParseUtils.getStringContent(file, defaultConfig);
        String s2 = ParseUtils.getStringContent(file, defaultConfig, "application/rtf");
        assertEquals(s1, s2);
        assertTrue(s1.contains("\u010Cl\u00E1nek t\u00FDdne"));
        assertTrue(s1.contains("starov\u011Bk\u00E9 \u017Eidovsk\u00E9 n\u00E1bo\u017Eensk\u00E9 texty"));
    }

    public void testExtraction() throws Exception {
        File file = getResourceAsFile("/test-documents/testRTF.rtf");
        String s1 = ParseUtils.getStringContent(file, defaultConfig);
        String s2 = ParseUtils.getStringContent(file, defaultConfig, "application/rtf");
        assertEquals(s1, s2);
    }

    public void testMS932Extraction() throws Exception {
        File file = getResourceAsFile("/test-documents/testRTF-ms932.rtf");
        String s1 = ParseUtils.getStringContent(file, defaultConfig);
        String s2 = ParseUtils.getStringContent(file, defaultConfig, "application/rtf");
        assertEquals(s1, s2);

        // Hello in Japanese
        assertTrue(s1.contains("\u3053\u3093\u306b\u3061\u306f"));

        // Verify title, since it was also encoded with MS932:
        Result r = getResult("testRTF-ms932.rtf");
        assertEquals("\u30bf\u30a4\u30c8\u30eb", r.metadata.get(Metadata.TITLE));
    }

    public void testUmlautSpacesExtraction() throws Exception {
        File file = getResourceAsFile("/test-documents/testRTFUmlautSpaces.rtf");
        String s1 = ParseUtils.getStringContent(file, defaultConfig);
        String s2 = ParseUtils.getStringContent(file, defaultConfig, "application/rtf");
        assertEquals(s1, s2);
        assertTrue(s1.contains("\u00DCbersicht"));
    }

    public void testGothic() throws Exception {
    	String content = getText("testRTFUnicodeGothic.rtf");
    	assertContains("\uD800\uDF32\uD800\uDF3f\uD800\uDF44\uD800\uDF39\uD800\uDF43\uD800\uDF3A", content);
    }
    
    public void testJapaneseText() throws Exception {
        Result r = getResult("testRTFJapanese.rtf");
        String content = r.text;

        // Verify title -- this title uses upr escape inside
        // title info field:
        assertEquals("\u30be\u30eb\u30b2\u3068\u5c3e\u5d0e\u3001\u6de1\u3005\u3068\u6700\u671f\u3000",
                     r.metadata.get(Metadata.TITLE));
        assertEquals("VMazel", r.metadata.get(Metadata.AUTHOR));
        assertEquals("StarWriter", r.metadata.get(Metadata.COMMENT));
        assertContains("1.", content);
        assertContains("4.", content);
       
        // Special version of (GHQ)
        assertContains("\uff08\uff27\uff28\uff31\uff09", content);
       
        // 6 other characters
        assertContains("\u6771\u4eac\u90fd\u4e09\u9df9\u5e02", content);
    }

    public void testTextWithCurlyBraces() throws Exception {
        String content = getText("testRTFWithCurlyBraces.rtf");
        assertContains("{ some text inside curly brackets }", content);
    }

    public void testControls() throws Exception {
        Result r = getResult("testRTFControls.rtf");
        String content = r.text;
        assertContains("Thiswordhasanem\u2014dash", content);
        assertContains("Thiswordhasanen\u2013dash", content);
        assertContains("Thiswordhasanon\u2011breakinghyphen", content);
        assertContains("Thiswordhasanonbreaking\u00a0space", content);
        assertContains("Thiswordhasanoptional\u00adhyphen", content);
        assertContains("\u2018Single quoted text\u2019", content);
        assertContains("\u201cDouble quoted text\u201d", content);
        assertContains("\u201cDouble quoted text again\u201d", content);
    }


    public void testInvalidUnicode() throws Exception {
        Result r = getResult("testRTFInvalidUnicode.rtf");
        String content = r.text;
        assertContains("Unpaired hi \ufffd here", content);
        assertContains("Unpaired lo \ufffd here", content);
        assertContains("Mismatched pair \ufffd\ufffd here", content);
    }

    public void testVarious() throws Exception {
        Result r = getResult("testRTFVarious.rtf");
        String content = r.text;
        assertContains("Footnote appears here", content);
        assertContains("This is a footnote.", content);
        assertContains("This is the header text.", content);
        assertContains("This is the footer text.", content);
        assertContains("Here is a text box", content);
        assertContains("Bold", content);
        assertContains("italic", content);
        assertContains("underline", content);
        assertContains("superscript", content);
        assertContains("subscript", content);
        assertContains("Here is a citation:", content);
        assertContains("Figure 1 This is a caption for Figure 1", content);
        assertContains("(Kramer)", content);

        // Table
        assertContains("Row 1 Col 1 Row 1 Col 2 Row 1 Col 3 Row 2 Col 1 Row 2 Col 2 Row 2 Col 3", content.replaceAll("\\s+"," "));

        // 2-columns
        assertContains("Row 1 column 1 Row 2 column 1 Row 1 column 2 Row 2 column 2", content.replaceAll("\\s+"," "));
        assertContains("This is a hyperlink", content);
        assertContains("Here is a list:", content);
        for(int row=1;row<=3;row++) {
            //assertContains("·\tBullet " + row, content);
            assertContains("\u00b7\tBullet " + row, content);
        }
        assertContains("Here is a numbered list:", content);
        for(int row=1;row<=3;row++) {
            assertContains(row + ")\tNumber bullet " + row, content);
        }

        for(int row=1;row<=2;row++) {
            for(int col=1;col<=3;col++) {
                assertContains("Row " + row + " Col " + col, content);
            }
        }

        assertContains("Keyword1 Keyword2", content);
        assertEquals("Keyword1 Keyword2",
                     r.metadata.get(Metadata.KEYWORDS));

        assertContains("Subject is here", content);
        assertEquals("Subject is here",
                     r.metadata.get(Metadata.SUBJECT));

        assertContains("Suddenly some Japanese text:", content);
        // Special version of (GHQ)
        assertContains("\uff08\uff27\uff28\uff31\uff09", content);
        // 6 other characters
        assertContains("\u30be\u30eb\u30b2\u3068\u5c3e\u5d0e\u3001\u6de1\u3005\u3068\u6700\u671f", content);

        assertContains("And then some Gothic text:", content);
    	assertContains("\uD800\uDF32\uD800\uDF3f\uD800\uDF44\uD800\uDF39\uD800\uDF43\uD800\uDF3A", content);
    }

    public void testVariousStyle() throws Exception {
        String content = getXML("testRTFVarious.rtf").xml;
        assertContains("<b>Bold</b>", content);
        assertContains("<i>italic</i>", content);
    }

    public void testBoldItalic() throws Exception {
        String content = getXML("testRTFBoldItalic.rtf").xml;
        assertContains("<b>bold</b>", content);
        assertContains("<b>bold </b><b><i>italic</i></b>", content);
        assertContains("<b><i>italic </i></b><b>bold</b>", content);
        assertContains("<i>italic</i>", content);
        assertContains("<b>bold then </b><b><i>italic then</i></b><i> not bold</i>", content);
        assertContains("<i>italic then </i><b><i>bold then</i></b><b> not italic</b>", content);
    }

    public void testHyperlink() throws Exception {
        String content = getXML("testRTFHyperlink.rtf").xml;
        assertContains("our most <a href=\"http://r.office.microsoft.com/r/rlidwelcomeFAQ?clid=1033\">frequently asked questions</a>", content);
    }

    public void testIgnoredControlWord() throws Exception {
        assertContains("<p>The quick brown fox jumps over the lazy dog</p>", getXML("testRTFIgnoredControlWord.rtf").xml);
    }

    private Result getResult(String filename) throws Exception {
        File file = getResourceAsFile("/test-documents/" + filename);
       
        Metadata metadata = new Metadata();
        StringWriter writer = new StringWriter();
        parser.parse(
                     new FileInputStream(file),
                     new WriteOutContentHandler(writer),
                     metadata,
                     new ParseContext());
        String content = writer.toString();
        return new Result(content, metadata);
    }

    private static class XMLResult {
        public final String xml;
        public final Metadata metadata;

        public XMLResult(String xml, Metadata metadata) {
            this.xml = xml;
            this.metadata = metadata;
      }
    }

    private XMLResult getXML(String filename) throws Exception {
        Metadata metadata = new Metadata();
        
        StringWriter sw = new StringWriter();
        SAXTransformerFactory factory = (SAXTransformerFactory)
                 SAXTransformerFactory.newInstance();
        TransformerHandler handler = factory.newTransformerHandler();
        handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "xml");
        handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "no");
        handler.setResult(new StreamResult(sw));

        // Try with a document containing various tables and formattings
        InputStream input = getResourceAsStream("/test-documents/" + filename);
        try {
            parser.parse(input, handler, metadata, new ParseContext());
            return new XMLResult(sw.toString(), metadata);
        } finally {
            input.close();
        }
    }

    private String getText(String filename) throws Exception {
        return getResult(filename).text;
    }
}
