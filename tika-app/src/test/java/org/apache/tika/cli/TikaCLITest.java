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
package org.apache.tika.cli;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.io.TikaInputStreamTest;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.DelegatingParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.ParserDecorator;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Tests the Tika's cli
 */
public class TikaCLITest extends TestCase{

    /* Test members */
    private File profile = null;
    private ByteArrayOutputStream outContent = null;
    private PrintStream stdout = null;
    private URI testDataURI = new File("src/test/resources/test-data/").toURI();
    private String resorcePrefix = testDataURI.toString();

    public void setUp() throws Exception {
        profile = new File("welsh.ngp");
        outContent = new ByteArrayOutputStream();
        stdout = System.out;
        System.setOut(new PrintStream(outContent));
    }

    public static void main(String[] args) throws Exception {
        Tika tika = new Tika();
        File file = new File("c:\\Users\\jukka\\Desktop\\enron_mail_20110402.tgz");

        final AtomicLong count = new AtomicLong();
        ParseContext context = new ParseContext();
        context.set(Parser.class, new ParserDecorator(tika.getParser()) {
            @Override
            public void parse(
                    InputStream stream, ContentHandler handler,
                    Metadata metadata, ParseContext context)
                    throws IOException, SAXException, TikaException {
                try {
                    long n = count.incrementAndGet();
                    if (n % 100 == 0) {
                        System.out.println(n);
                    }
                    super.parse(stream, handler, metadata, context);
                } catch (Exception e) {
                    System.out.println(metadata.get(Metadata.RESOURCE_NAME_KEY));
                    e.printStackTrace();
                }
            }
        });

        Metadata metadata = new Metadata();
        InputStream input = TikaInputStream.get(file, metadata);
        try {
            tika.getParser().parse(
                    input, new DefaultHandler(), metadata, context);
        } finally {
            input.close();
        }
    }

    /**
     * Creates a welsh language profile
     * 
     * @throws Exception
     */
    public void testCreateProfile() throws Exception {
        String[] params = {"--create-profile=welsh", "-eUTF-8", resorcePrefix + "welsh_corpus.txt"};
        TikaCLI.main(params);
        Assert.assertTrue(profile.exists());
    }

    /**
     * Tests --list-parser-detail option of the cli
     * 
     * @throws Exception
     */
    public void testListParserDetail() throws Exception{
        String[] params = {"--list-parser-detail"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("application/vnd.oasis.opendocument.text-web"));
    }

    /**
     * Tests --list-parser option of the cli
     * 
     * @throws Exception
     */
    public void testListParsers() throws Exception{
        String[] params = {"--list-parser"};
        TikaCLI.main(params);
        //Assert was commented temporarily for finding the problem
        //		Assert.assertTrue(outContent != null && outContent.toString().contains("org.apache.tika.parser.iwork.IWorkPackageParser"));
    }

    /**
     * Tests -x option of the cli
     * 
     * @throws Exception
     */
    public void testXMLOutput() throws Exception{
        String[] params = {"-x", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("?xml version=\"1.0\" encoding=\"UTF-8\"?"));
    }

    /**
     * Tests a -h option of the cli
     * 
     * @throws Exception
     */
    public void testHTMLOutput() throws Exception{
        String[] params = {"-h", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("html xmlns=\"http://www.w3.org/1999/xhtml"));
    }

    /**
     * Tests -t option of the cli
     * 
     * @throws Exception
     */
    public void testTextOutput() throws Exception{
        String[] params = {"-t", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("finished off the cake"));
    }

    /**
     * Tests -m option of the cli
     * @throws Exception
     */
    public void testMetadataOutput() throws Exception{
        String[] params = {"-m", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("text/plain"));
    }

    /**
     * Tests -l option of the cli
     * 
     * @throws Exception
     */
    public void testLanguageOutput() throws Exception{
        String[] params = {"-l", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("en"));
    }

    /**
     * Tests -d option of the cli
     * 
     * @throws Exception
     */
    public void testDetectOutput() throws Exception{
        String[] params = {"-d", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("text/plain"));
    }

    /**
     * Tests --list-met-models option of the cli
     * 
     * @throws Exception
     */
    public void testListMetModels() throws Exception{
        String[] params = {"--list-met-models", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("text/plain"));
    }

    /**
     * Tests --list-supported-types option of the cli
     * 
     * @throws Exception
     */
    public void testListSupportedTypes() throws Exception{
        String[] params = {"--list-supported-types", resorcePrefix + "alice.cli.test"};
        TikaCLI.main(params);
        Assert.assertTrue(outContent.toString().contains("supertype: application/octet-stream"));
    }

    /**
     * Tears down the test. Returns the System.out
     */
    public void tearDown() throws Exception {
        if(profile != null && profile.exists())
            profile.delete();
        System.setOut(stdout);
    }

}
