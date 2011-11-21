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
package org.apache.tika.detect;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

/**
 * Junit test class for {@link ContainerAwareDetector}
 */
public class TestContainerAwareDetector extends TestCase {

    private final Detector detector = new DefaultDetector();

    private void assertTypeByData(String file, String type) throws Exception {
       assertTypeByNameAndData(file, null, type);
    }
    private void assertTypeByNameAndData(String file, String type) throws Exception {
       assertTypeByNameAndData(file, file, type);
    }
    private void assertTypeByNameAndData(String dataFile, String name, String type) throws Exception {
       TikaInputStream stream = TikaInputStream.get(
               TestContainerAwareDetector.class.getResource(
                       "/test-documents/" + dataFile));
       try {
           Metadata m = new Metadata();
           if (name != null)
              m.add(Metadata.RESOURCE_NAME_KEY, name);
           
           assertEquals(
                   MediaType.parse(type),
                   detector.detect(stream, m));
       } finally {
           stream.close();
       }
    }

    public void testDetectOLE2() throws Exception {
        // Microsoft office types known by POI
        assertTypeByData("testEXCEL.xls", "application/vnd.ms-excel");
        assertTypeByData("testWORD.doc", "application/msword");
        assertTypeByData("testPPT.ppt", "application/vnd.ms-powerpoint");

        // Try some ones that POI doesn't handle, that are still OLE2 based
        assertTypeByData("testWORKS.wps", "application/vnd.ms-works");
        assertTypeByData("testWORKS2000.wps", "application/vnd.ms-works");
        assertTypeByData("testCOREL.shw", "application/x-corelpresentations");
        assertTypeByData("testQUATTRO.qpw", "application/x-quattro-pro");
        assertTypeByData("testQUATTRO.wb3", "application/x-quattro-pro");
        
        // With the filename and data
        assertTypeByNameAndData("testEXCEL.xls", "application/vnd.ms-excel");
        assertTypeByNameAndData("testWORD.doc", "application/msword");
        assertTypeByNameAndData("testPPT.ppt", "application/vnd.ms-powerpoint");

        // With the wrong filename supplied, data will trump filename
        assertTypeByNameAndData("testEXCEL.xls", "notWord.doc",  "application/vnd.ms-excel");
        assertTypeByNameAndData("testWORD.doc",  "notExcel.xls", "application/msword");
        assertTypeByNameAndData("testPPT.ppt",   "notWord.doc",  "application/vnd.ms-powerpoint");

        // With a filename of a totally different type, data will trump filename
        assertTypeByNameAndData("testEXCEL.xls", "notPDF.pdf",  "application/vnd.ms-excel");
        assertTypeByNameAndData("testEXCEL.xls", "notPNG.png",  "application/vnd.ms-excel");
    }

    public void testOpenContainer() throws Exception {
        TikaInputStream stream = TikaInputStream.get(
                TestContainerAwareDetector.class.getResource(
                        "/test-documents/testPPT.ppt"));
        try {
            assertNull(stream.getOpenContainer());
            assertEquals(
                    MediaType.parse("application/vnd.ms-powerpoint"),
                    detector.detect(stream, new Metadata()));
            assertTrue(stream.getOpenContainer() instanceof NPOIFSFileSystem);
        } finally {
            stream.close();
        }
    }

    public void testDetectODF() throws Exception {
        assertTypeByData("testODFwithOOo3.odt", "application/vnd.oasis.opendocument.text");
        assertTypeByData("testOpenOffice2.odf", "application/vnd.oasis.opendocument.formula");
    }

    public void testDetectOOXML() throws Exception {
        assertTypeByData("testEXCEL.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertTypeByData("testWORD.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertTypeByData("testPPT.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        // Check some of the less common OOXML types
        assertTypeByData("testPPT.pptm", "application/vnd.ms-powerpoint.presentation.macroenabled.12");
        assertTypeByData("testPPT.ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        assertTypeByData("testPPT.ppsm", "application/vnd.ms-powerpoint.slideshow.macroEnabled.12");
        
        // .xlsb is an OOXML file containing the binary parts, and not
        //  an OLE2 file as you might initially expect!
        assertTypeByData("testEXCEL.xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");

        // With the filename and data
        assertTypeByNameAndData("testEXCEL.xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertTypeByNameAndData("testWORD.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertTypeByNameAndData("testPPT.pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        // With the wrong filename supplied, data will trump filename
        assertTypeByNameAndData("testEXCEL.xlsx", "notWord.docx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        assertTypeByNameAndData("testWORD.docx",  "notExcel.xlsx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        assertTypeByNameAndData("testPPT.pptx",   "notWord.docx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");

        // With an incorrect filename of a different container type, data trumps filename
        assertTypeByNameAndData("testEXCEL.xlsx", "notOldExcel.xls", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    /**
     * Check that temporary files created by Tika are removed after
     * closing TikaInputStream.
     */
    public void testRemovalTempfiles() throws Exception {
        assertRemovalTempfiles("testWORD.docx");
        assertRemovalTempfiles("test-documents.zip");
    }

    private int countTemporaryFiles() {
        return new File(System.getProperty("java.io.tmpdir")).listFiles(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.startsWith("apache-tika-");
                    }
                }).length;
    }

    private void assertRemovalTempfiles(String fileName) throws Exception {
        int numberOfTempFiles = countTemporaryFiles();

        TikaInputStream stream = TikaInputStream.get(
                TestContainerAwareDetector.class.getResource(
                        "/test-documents/" + fileName));
        try {
            detector.detect(stream, new Metadata());
        } finally {
            stream.close();
        }

        assertEquals(numberOfTempFiles, countTemporaryFiles());
    }

    public void testDetectIWork() throws Exception {
        assertTypeByData("testKeynote.key", "application/vnd.apple.keynote");
        assertTypeByData("testNumbers.numbers", "application/vnd.apple.numbers");
        assertTypeByData("testPages.pages", "application/vnd.apple.pages");
    }

    public void testDetectZip() throws Exception {
        assertTypeByData("test-documents.zip", "application/zip");
        assertTypeByData("test-zip-of-zip.zip", "application/zip");
        assertTypeByData("testJAR.jar", "application/java-archive");
    }

    private TikaInputStream getTruncatedFile(String name, int n)
            throws IOException {
        InputStream input =
            TestContainerAwareDetector.class.getResourceAsStream(
                    "/test-documents/" + name);
        try {
            byte[] bytes = new byte[n];
            int m = 0;
            while (m < bytes.length) {
                int i = input.read(bytes, m, bytes.length - m);
                if (i != -1) {
                    m += i;
                } else {
                    throw new IOException("Unexpected end of stream");
                }
            }
            return TikaInputStream.get(bytes);
        } finally {
            input.close();
        }
    }

    public void testTruncatedFiles() throws Exception {
        // First up a truncated OOXML (zip) file
       
        // With only the data supplied, the best we can do is the container
        TikaInputStream xlsx = getTruncatedFile("testEXCEL.xlsx", 300);
        Metadata m = new Metadata();
        try {
            assertEquals(
                    MediaType.application("x-tika-ooxml"),
                    detector.detect(xlsx, m));
        } finally {
            xlsx.close();
        }
        
        // With truncated data + filename, we can use the filename to specialise
        xlsx = getTruncatedFile("testEXCEL.xlsx", 300);
        m = new Metadata();
        m.add(Metadata.RESOURCE_NAME_KEY, "testEXCEL.xlsx");
        try {
            assertEquals(
                    MediaType.application("vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
                    detector.detect(xlsx, m));
        } finally {
            xlsx.close();
        }
        

        // Now a truncated OLE2 file 
        TikaInputStream xls = getTruncatedFile("testEXCEL.xls", 400);
        m = new Metadata();
        try {
            assertEquals(
                    MediaType.application("x-tika-msoffice"),
                    detector.detect(xls, m));
        } finally {
            xls.close();
        }
        
        // Finally a truncated OLE2 file, with a filename available
        xls = getTruncatedFile("testEXCEL.xls", 400);
        m = new Metadata();
        m.add(Metadata.RESOURCE_NAME_KEY, "testEXCEL.xls");
        try {
            assertEquals(
                    MediaType.application("vnd.ms-excel"),
                    detector.detect(xls, m));
        } finally {
            xls.close();
        }
   }

}
