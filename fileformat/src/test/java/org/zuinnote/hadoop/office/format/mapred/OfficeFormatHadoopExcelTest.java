/**
* Copyright 2016 ZuInnoTe (Jörn Franke) <zuinnote@gmail.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
**/


package org.zuinnote.hadoop.office.format.mapred;

import java.io.IOException;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.Files;
import java.nio.file.FileVisitResult;
import java.nio.file.SimpleFileVisitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;


import org.junit.Ignore;
import org.junit.Test;
import org.junit.Rule;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.After;
import org.junit.rules.ExpectedException;

import java.io.File;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.FileInputFormat;
import org.apache.hadoop.mapred.FileOutputFormat;
import org.apache.hadoop.mapred.FileSplit;
import org.apache.hadoop.mapred.InputSplit;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.JobConfigurable;
import org.apache.hadoop.mapred.JobContext;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;

import org.apache.hadoop.io.compress.BZip2Codec;
import org.apache.hadoop.io.compress.GzipCodec;
import org.apache.hadoop.io.compress.CompressionCodec;

import org.apache.hadoop.util.ReflectionUtils;

import org.apache.poi.EncryptedDocumentException;

import org.zuinnote.hadoop.office.format.common.dao.*;
import org.zuinnote.hadoop.office.format.common.parser.FormatNotUnderstoodException;
import org.zuinnote.hadoop.office.format.common.writer.OfficeWriterException;

public class OfficeFormatHadoopExcelTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

private static JobConf defaultConf = new JobConf();
private static FileSystem localFs = null; 
private static Reporter reporter = Reporter.NULL;
private static final String attempt = "attempt_201612311111_0001_m_000000_0";
private static final String tmpPrefix = "hadoopofficetest";
private static java.nio.file.Path tmpPath;

   @BeforeClass
    public static void oneTimeSetUp() throws IOException {
      // one-time initialization code   
      defaultConf.set("fs.defaultFS", "file:///");
      localFs = FileSystem.getLocal(defaultConf);
      // create temp directory
      tmpPath = Files.createTempDirectory(tmpPrefix);
      // create shutdown hook to remove temp files after shutdown, may need to rethink to avoid many threads are created
  	Runtime.getRuntime().addShutdownHook(new Thread(
    	new Runnable() {
      	@Override
      	public void run() {
        	try {
          		Files.walkFileTree(tmpPath, new SimpleFileVisitor<java.nio.file.Path>() {
	
            		@Override
            		public FileVisitResult visitFile(java.nio.file.Path file,BasicFileAttributes attrs)
                		throws IOException {
              			Files.delete(file);
             			return FileVisitResult.CONTINUE;
        			}

        		@Override
        		public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException e) throws IOException {
          			if (e == null) {
            				Files.delete(dir);
            				return FileVisitResult.CONTINUE;
          			}
          			throw e;
        	}
        	});
      	} catch (IOException e) {
        throw new RuntimeException("Error temporary files in following path could not be deleted "+tmpPath, e);
      }
    }}));
    }

    @AfterClass
    public static void oneTimeTearDown() {
        // one-time cleanup code
      }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {  
    }

    @Test
    public void checkTestExcel2003EmptySheetAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2003empty.xls";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013EmptySheetAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013empty.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2003SingleSheetAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2003test.xls";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013SingleSheetAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013test.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013MultiSheetAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013testmultisheet.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013CommentAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013comment.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013LinkedWorkbooksAvailable() {
	// depends on file excel2013test.xslx
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013linkedworkbooks.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013LinkedWorkbooksLink1Available() {
	// depends on file excel2013test.xslx
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013linkedworkbookslink1.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013LinkedWorkbooksLink2Available() {
	// depends on file excel2013test.xslx
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013linkedworkbookslink2.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013MultiSheetGzipAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013testmultisheet.xlsx.gz";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2003EmptyRows() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2003testemptyrows.xls";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

    @Test
    public void checkTestExcel2013EmptyRows() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013testemptyrows.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }


   @Test
    public void checkTestExcel2013MainWorkbook() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013linkedworkbooks.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

   @Test
    public void checkTestExcel2013LinkedWorkbook2() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013linkedworkbookslink1.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

   @Test
    public void checkTestExcel2013LinkedWorkbook1() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013linkedworkbookslink2.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

   @Test
    public void checkTestExcel2003MainWorkbook() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2003linkedworkbooks.xls";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

   @Test
    public void checkTestExcel2003LinkedWorkbook1() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2003linkedworkbookslink1.xls";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }

   @Test
    public void checkTestExcel2003LinkedWorkbook2() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2003linkedworkbookslink2.xls";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }
   
   @Test
   public void checkTestExcel2013TemplateAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="templatetest1.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
   }
   
   @Test
   public void checkTestExcel2013TemplateEncryptedAvailable() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="templatetest1encrypt.xlsx";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
   }

    @Test
    public void checkTestExcel2013MultiSheetBzip2Available() {
	ClassLoader classLoader = getClass().getClassLoader();
	String fileName="excel2013testmultisheet.xlsx.bz2";
	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
	assertNotNull("Test Data File \""+fileName+"\" is not null in resource path",fileNameSpreadSheet);
	File file = new File(fileNameSpreadSheet);
	assertTrue("Test Data File \""+fileName+"\" exists", file.exists());
	assertFalse("Test Data File \""+fileName+"\" is not a directory", file.isDirectory());
    }



    @Test
    public void readExcelInputFormatExcel2003Empty() throws IOException {
JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003empty.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 1 and is empty", 0,spreadSheetValue.get().length);	
	assertFalse("Input Split for Excel file contains no further row", reader.next(spreadSheetKey,spreadSheetValue));	
    }

   @Test
    public void readExcelInputFormatExcel2013Empty() throws IOException {
        JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013empty.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 1 and is empty", 0,spreadSheetValue.get().length);	
	assertFalse("Input Split for Excel file contains no further row", reader.next(spreadSheetKey,spreadSheetValue));		
    }

    @Test
    public void readExcelInputFormatExcel2003SingleSheet() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003test.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2003test.xls]Sheet1!A1\"", "[excel2003test.xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

    @Test
    public void readExcelInputFormatExcel2013SingleSheetEncryptedPositive() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013encrypt.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013encrypt.xlsx]Sheet1!A1\"", "[excel2013encrypt.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }

    @Test
    public void readExcelInputFormatExcel2013SingleSheetEncryptedNegative() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013encrypt.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test2");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);	
    	assertNull("Null record reader implies invalid password",reader);
    }

    @Test
    public void readExcelInputFormatExcel2003SingleSheetEncryptedPositive() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003encrypt.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2003encrypt.xls]Sheet1!A1\"", "[excel2003encrypt.xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }


    @Test
    public void readExcelInputFormatExcel2003SingleSheetEncryptedNegative() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003encrypt.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test2");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
    	assertNull("Null record reader implies invalid password",reader);
    }

    @Test
    public void readExcelInputFormatExcel2013EmptyRows() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013testemptyrows.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013testemptyrows.xlsx]Sheet1!A1\"", "[excel2013testemptyrows.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 0 columns", 0, spreadSheetValue.get().length);
	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
		assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 0 columns", 0, spreadSheetValue.get().length);
	
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }


    @Test
    public void readExcelInputFormatExcel2013SingleSheet() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013test.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013test.xlsx]Sheet1!A1\"", "[excel2013test.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }


    @Test
    public void readExcelInputFormatExcel2013MultiSheetAll() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013testmultisheet.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013testmultisheet.xlsx]Sheet1!A1\"", "[excel2013testmultisheet.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 7 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 7 with cell 1 == \"8\"", "8", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with cell 2 == \"99\"", "99", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with 2 columns", 2, spreadSheetValue.get().length);
	assertTrue("Input Split for Excel file contains row 8 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 8 with 1 column",1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 8 with cell 1 == \"test\"", "test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 9 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 9 with 3 columns", 3, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 9 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertNull("Input Split for Excel file contains row 9 with cell 2 == null", spreadSheetValue.get()[1]);	
	assertEquals("Input Split for Excel file contains row 9 with cell 3 == \"seven\"", "seven", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }

@Test
    public void readExcelInputFormatExcel2013MultiSheetSelectedSheet() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013testmultisheet.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// select the sheet	
	job.set("hadoopoffice.read.sheets","testsheet");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 7 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 7 with cell 1 == \"8\"", "8", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with cell 2 == \"99\"", "99", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with 2 columns", 2, spreadSheetValue.get().length);
	assertTrue("Input Split for Excel file contains row 8 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 8 with 1 column",1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 8 with cell 1 == \"test\"", "test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 9 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 9 with 3 columns", 3, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 9 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertNull("Input Split for Excel file contains row 9 with cell 2 == null", spreadSheetValue.get()[1]);	
	assertEquals("Input Split for Excel file contains row 9 with cell 3 == \"seven\"", "seven", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }


    @Test
    public void readExcelInputFormatExcel2013Comment() throws IOException {
JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013comment.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013comment.xlsx]CommentSheet!A1\"", "[excel2013comment.xlsx]CommentSheet!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 comment == \"First comment\"", "First comment", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getComment());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"CommentSheet\"", "CommentSheet", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 2 columns", 2, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 3 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertEquals("Input Split for Excel file contains row 2 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 2 with cell 2 comment == \"Second comment\"", "Second comment", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getComment());	
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 3 column", 3, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 4 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertNull("Input Split for Excel file contains row 4 with cell 2 == null", spreadSheetValue.get()[1]);	
	assertEquals("Input Split for Excel file contains row 4 with cell 3 == \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 4 with cell 3 comment == \"Third comment\"", "Third comment", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getComment());		
    }

    @Test
    public void readExcelInputFormatGzipCompressedExcel2013MultiSheetAll() throws IOException {
      JobConf job = new JobConf(defaultConf);
      CompressionCodec gzip = new GzipCodec();
      ReflectionUtils.setConf(gzip, job);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013testmultisheet.xlsx.gz";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013testmultisheet.xlsx.gz]Sheet1!A1\"", "[excel2013testmultisheet.xlsx.gz]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 7 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 7 with cell 1 == \"8\"", "8", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with cell 2 == \"99\"", "99", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with 2 columns", 2, spreadSheetValue.get().length);
	assertTrue("Input Split for Excel file contains row 8 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 8 with 1 column",1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 8 with cell 1 == \"test\"", "test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 9 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 9 with 3 columns", 3, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 9 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertNull("Input Split for Excel file contains row 9 with cell 2 == null", spreadSheetValue.get()[1]);	
	assertEquals("Input Split for Excel file contains row 9 with cell 3 == \"seven\"", "seven", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

    @Test
    public void readExcelInputFormatExcel2013LinkedWorkbook() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013linkedworkbooks.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// enable option to read linked workbooks
	job.setBoolean("hadoopoffice.read.linkedworkbooks",true);
	job.setBoolean("hadoopoffice.read.ignoremissinglinkedworkbooks",false);
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013linkedworkbooks.xlsx]Sheet1!A1\"", "[excel2013linkedworkbooks.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 1 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"3\" (this tests also if the cached value of 6 is ignored)", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());

    }

  @Test
    public void readExcelInputFormatExcel2003LinkedWorkbook() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003linkedworkbooks.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// enable option to read linked workbooks
	job.setBoolean("hadoopoffice.read.linkedworkbooks",true);
	job.setBoolean("hadoopoffice.read.ignoremissinglinkedworkbooks",false);
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2003linkedworkbooks.xls]Sheet1!A1\"", "[excel2003linkedworkbooks.xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 1 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"3\" (this tests also if the cached value of 6 is ignored)", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());

    }

    @Test
    public void readExcelInputFormatBzip2CompressedExcel2013MultiSheetAll() throws IOException {
       JobConf job = new JobConf(defaultConf);
       CompressionCodec bzip2 = new BZip2Codec();
       ReflectionUtils.setConf(bzip2, job);
	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013testmultisheet.xlsx.bz2";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013testmultisheet.xlsx.bz2]Sheet1!A1\"", "[excel2013testmultisheet.xlsx.bz2]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 7 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 7 with cell 1 == \"8\"", "8", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with cell 2 == \"99\"", "99", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with 2 columns", 2, spreadSheetValue.get().length);
	assertTrue("Input Split for Excel file contains row 8 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 8 with 1 column",1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 8 with cell 1 == \"test\"", "test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 9 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 9 with 3 columns", 3, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 9 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertNull("Input Split for Excel file contains row 9 with cell 2 == null", spreadSheetValue.get()[1]);	
	assertEquals("Input Split for Excel file contains row 9 with cell 3 == \"seven\"", "seven", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

    @Test
    public void writeExcelOutputFormatExcel2013SingleSheet() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheettestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 2 and is empty", 0,spreadSheetValue.get().length);	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 3 columns", 3,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 3 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 1 column", 1,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
    }

 @Test
    public void writeExcelOutputFormatExcel2013SingleSheetEncryptedPositive() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheettestoutencryptedpositive";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new excel format
	// security
	// for the new Excel format you need to decide on your own which algorithms are secure
	job.set("hadoopoffice.write.security.crypt.encrypt.mode","agile");
	job.set("hadoopoffice.write.security.crypt.encrypt.algorithm","aes256");
	job.set("hadoopoffice.write.security.crypt.chain.mode","cbc");
	job.set("hadoopoffice.write.security.crypt.hash.algorithm","sha512");
	job.set("hadoopoffice.write.security.crypt.password","test");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// you just need to provide the password to read encrypted data
	job.set("hadoopoffice.read.security.crypt.password","test");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 2 and is empty", 0,spreadSheetValue.get().length);	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 3 columns", 3,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 3 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 1 column", 1,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
    }

@Test
    public void writeExcelOutputFormatExcel2013SingleSheetEncryptedNegative() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheettestoutencryptednegative";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new excel format
	// security
	// for the new Excel format you need to decide on your own which algorithms are secure
	job.set("hadoopoffice.write.security.crypt.encrypt.mode","agile");
	job.set("hadoopoffice.write.security.crypt.encrypt.algorithm","aes256");
	job.set("hadoopoffice.write.security.crypt.chain.mode","cbc");
	job.set("hadoopoffice.write.security.crypt.hash.algorithm","sha512");
	job.set("hadoopoffice.write.security.crypt.password","test");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// you just need to provide the password to read encrypted data
	job.set("hadoopoffice.read.security.crypt.password","test2");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
    	assertNull("Null record reader implies invalid password",reader);
    }


 @Test
    public void writeExcelOutputFormatExcel2003SingleSheetEncryptedPositive() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2003singlesheettestoutencryptedpositive";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel"); // old excel format
	// security
	// for the old Excel format you simply need to define only a password
	job.set("hadoopoffice.write.security.crypt.password","test");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xls");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.read.security.crypt.password","test");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xls]Sheet1!A1\"", "["+fileName+".xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 2 and is empty", 0,spreadSheetValue.get().length);	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 3 columns", 3,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 3 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 1 column", 1,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
    }

 @Test
 public void writeExcelOutputFormatExcel2003SingleSheetEncryptedNegative() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
 	String fileName="excel2003singlesheettestoutencryptednegative";
 	String tmpDir=tmpPath.toString();	
 	Path outputPath = new Path(tmpDir);
 	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel"); // old excel format
	// security
	// for the old Excel format you simply need to define only a password
	job.set("hadoopoffice.write.security.crypt.password","test");
	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
 	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xls");
 	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.read.security.crypt.password","test2");
	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
 	inputFormat.configure(job);
 	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
 	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
 	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
 	assertNull("Null record reader implies invalid password",reader);
 }
 
 
  @Test
    public void writeExcelOutputFormatExcel2013SingleSheetMetaDataMatchAllPositive() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheetmetadatapositivetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
	// set all the meta data including to custom properties
	job.set("hadoopoffice.write.metadata.category","dummycategory");
	job.set("hadoopoffice.write.metadata.contentstatus","dummycontentstatus");
	job.set("hadoopoffice.write.metadata.contenttype","dummycontenttype");
	job.set("hadoopoffice.write.metadata.created","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.creator","dummycreator");
	job.set("hadoopoffice.write.metadata.description","dummydescription");	
	job.set("hadoopoffice.write.metadata.identifier","dummyidentifier");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.modified","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.revision","2");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty1","dummymycustomproperty1");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty2","dummymycustomproperty2");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","true");
	// following filter
   	job.set("hadoopoffice.read.filter.metadata.category","dummycategory");
	job.set("hadoopoffice.read.filter.metadata.contentstatus","dummycontentstatus");
	job.set("hadoopoffice.read.filter.metadata.contenttype","dummycontenttype");
	job.set("hadoopoffice.read.filter.metadata.created","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.creator","dummycreator");
	job.set("hadoopoffice.read.filter.metadata.description","dummydescription");	
	job.set("hadoopoffice.read.filter.metadata.identifier","dummyidentifier");
	job.set("hadoopoffice.read.filter.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.read.filter.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.modified","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.read.filter.metadata.revision","2");
	job.set("hadoopoffice.read.filter.metadata.subject","dummysubject");
	job.set("hadoopoffice.read.filter.metadata.title","dummytitle");
	job.set("hadoopoffice.read.filter.metadata.custom.mycustomproperty1","dummymycustomproperty1");
	job.set("hhadoopoffice.read.filter.metadata.custom.mycustomproperty2","dummymycustomproperty2");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion is not true that means the document has (wrongly) been filtered out
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

    @Test
    public void writeExcelOutputFormatExcel2003SingleSheetMetaDataMatchAllPositive() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2003singlesheetmetadatapositivetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel"); //old Excel format


	// set all the meta data 
	job.set("hadoopoffice.write.metadata.applicationname","dummyapplicationname");
	job.set("hadoopoffice.write.metadata.author","dummyauthor");
	job.set("hadoopoffice.write.metadata.charcount","1");
	job.set("hadoopoffice.write.metadata.comments","dummycomments");
	job.set("hadoopoffice.write.metadata.createdatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.edittime","0");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastauthor","dummylastauthor");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastsavedatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.pagecount","1");
	job.set("hadoopoffice.write.metadata.revnumber","1");
	job.set("hadoopoffice.write.metadata.security","0");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	//job.set("hadoopoffice.write.metadata.template","dummytemplate");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	//job.set("hadoopoffice.write.metadata.wordcount","1");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xls");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","true");
	// following filter
	job.set("hadoopoffice.read.filter.metadata.applicationname","dummyapplicationname");
	job.set("hadoopoffice.read.filter.metadata.metadata.author","dummyauthor");
	job.set("hadoopoffice.read.filter.metadata.metadata.charcount","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.comments","dummycomments");
	job.set("hadoopoffice.read.filter.metadata.metadata.createdatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.metadata.edittime","0");
	job.set("hadoopoffice.read.filter.metadata.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastauthor","dummylastauthor");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastsavedatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.metadata.pagecount","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.revnumber","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.security","0");
	job.set("hadoopoffice.read.filter.metadata.metadata.subject","dummysubject");
	job.set("hadoopoffice.read.filter.metadata.metadata.template","dummytemplate");
	job.set("hadoopoffice.read.filter.metadata.metadata.title","dummytitle");
	job.set("hadoopoffice.read.filter.metadata.metadata.wordcount","1");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion is not true that means the document has (wrongly) been filtered out
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

 @Test
    public void writeExcelOutputFormatExcel2013SingleSheetMetaDataMatchAllNegative() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheetmetadatanegativetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
	// set all the meta data including to custom properties
	job.set("hadoopoffice.write.metadata.category","dummycategory");
	job.set("hadoopoffice.write.metadata.contentstatus","dummycontentstatus");
	job.set("hadoopoffice.write.metadata.contenttype","dummycontenttype");
	job.set("hadoopoffice.write.metadata.created","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.creator","dummycreator");
	job.set("hadoopoffice.write.metadata.description","dummydescription");	
	job.set("hadoopoffice.write.metadata.identifier","dummyidentifier");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.modified","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.revision","2");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty1","dummymycustomproperty1");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty2","dummymycustomproperty2");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","true");
	// following filter
   	job.set("hadoopoffice.read.filter.metadata.category","no Category");
	job.set("hadoopoffice.read.filter.metadata.contentstatus","dummycontentstatus");
	job.set("hadoopoffice.read.filter.metadata.contenttype","dummycontenttype");
	job.set("hadoopoffice.read.filter.metadata.created","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.creator","dummycreator");
	job.set("hadoopoffice.read.filter.metadata.description","dummydescription");	
	job.set("hadoopoffice.read.filter.metadata.identifier","dummyidentifier");
	job.set("hadoopoffice.read.filter.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.read.filter.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.modified","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.read.filter.metadata.revision","2");
	job.set("hadoopoffice.read.filter.metadata.subject","dummysubject");
	job.set("hadoopoffice.read.filter.metadata.title","dummytitle");
	job.set("hadoopoffice.read.filter.metadata.custom.mycustomproperty1","dummymycustomproperty1");
	job.set("hhadoopoffice.read.filter.metadata.custom.mycustomproperty2","dummymycustomproperty2");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion is true that means the document has wrongly NOT been filtered out
	assertFalse("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
    }
    @Test
    public void writeExcelOutputFormatExcel2003SingleSheetMetaDataMatchAllNegative() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2003singlesheetmetadatanegativetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel"); //old Excel format


	// set all the meta data 
	job.set("hadoopoffice.write.metadata.applicationname","dummyapplicationname");
	job.set("hadoopoffice.write.metadata.author","dummyauthor");
	job.set("hadoopoffice.write.metadata.charcount","1");
	job.set("hadoopoffice.write.metadata.comments","dummycomments");
	job.set("hadoopoffice.write.metadata.createdatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.edittime","0");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastauthor","dummylastauthor");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastsavedatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.pagecount","1");
	job.set("hadoopoffice.write.metadata.revnumber","1");
	job.set("hadoopoffice.write.metadata.security","0");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	job.set("hadoopoffice.write.metadata.template","dummytemplate");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	job.set("hadoopoffice.write.metadata.wordcount","1");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xls");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","true");
	// following filter
	job.set("hadoopoffice.read.filter.metadata.applicationname","dummyapplicationname2");
	job.set("hadoopoffice.read.filter.metadata.metadata.author","dummyauthor");
	job.set("hadoopoffice.read.filter.metadata.metadata.charcount","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.comments","dummycomments");
	job.set("hadoopoffice.read.filter.metadata.metadata.createdatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.metadata.edittime","0");
	job.set("hadoopoffice.read.filter.metadata.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastauthor","dummylastauthor");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastsavedatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.read.filter.metadata.metadata.pagecount","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.revnumber","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.security","0");
	job.set("hadoopoffice.read.filter.metadata.metadata.subject","dummysubject");
	job.set("hadoopoffice.read.filter.metadata.metadata.template","dummytemplate");
	job.set("hadoopoffice.read.filter.metadata.metadata.title","dummytitle");
	job.set("hadoopoffice.read.filter.metadata.metadata.wordcount","1");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion not true that means the document has (wrongly) NOT been filtered out
	assertFalse("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
    }

@Test
    public void writeExcelOutputFormatExcel2013SingleSheetMetaDataMatchOncePositive() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheetmetadatapositiveoncetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
	// set all the meta data including to custom properties
	job.set("hadoopoffice.write.metadata.category","dummycategory");
	job.set("hadoopoffice.write.metadata.contentstatus","dummycontentstatus");
	job.set("hadoopoffice.write.metadata.contenttype","dummycontenttype");
	job.set("hadoopoffice.write.metadata.created","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.creator","dummycreator");
	job.set("hadoopoffice.write.metadata.description","dummydescription");	
	job.set("hadoopoffice.write.metadata.identifier","dummyidentifier");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.modified","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.revision","2");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty1","dummymycustomproperty1");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty2","dummymycustomproperty2");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","false");
	// following filter
   	job.set("hadoopoffice.read.filter.metadata.category","dummycategory");
	job.set("hadoopoffice.read.filter.metadata.contentstatus","dummycontentstatus2");
	job.set("hadoopoffice.read.filter.metadata.contenttype","dummycontenttype2");
	job.set("hadoopoffice.read.filter.metadata.created","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.creator","dummycreator2");
	job.set("hadoopoffice.read.filter.metadata.description","dummydescription2");	
	job.set("hadoopoffice.read.filter.metadata.identifier","dummyidentifier2");
	job.set("hadoopoffice.read.filter.metadata.keywords","dummykeywords2");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser2");
	job.set("hadoopoffice.read.filter.metadata.lastprinted","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.modified","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser2");
	job.set("hadoopoffice.read.filter.metadata.revision","3");
	job.set("hadoopoffice.read.filter.metadata.subject","dummysubject2");
	job.set("hadoopoffice.read.filter.metadata.title","dummytitle2");
	job.set("hadoopoffice.read.filter.metadata.custom.mycustomproperty1","dummymycustomproperty12");
	job.set("hhadoopoffice.read.filter.metadata.custom.mycustomproperty2","dummymycustomproperty22");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion is not true that means the document has (wrongly) been filtered out
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

    @Test
    public void writeExcelOutputFormatExcel2003SingleSheetMetaDataMatchOncePositive() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2003singlesheetmetadatapositiveoncetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel"); //old Excel format


	// set all the meta data 
	job.set("hadoopoffice.write.metadata.applicationname","dummyapplicationname");
	job.set("hadoopoffice.write.metadata.author","dummyauthor");
	job.set("hadoopoffice.write.metadata.charcount","1");
	job.set("hadoopoffice.write.metadata.comments","dummycomments");
	job.set("hadoopoffice.write.metadata.createdatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.edittime","0");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastauthor","dummylastauthor");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastsavedatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.pagecount","1");
	job.set("hadoopoffice.write.metadata.revnumber","1");
	job.set("hadoopoffice.write.metadata.security","0");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	job.set("hadoopoffice.write.metadata.template","dummytemplate");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	job.set("hadoopoffice.write.metadata.wordcount","1");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xls");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","false");
	// following filter
	job.set("hadoopoffice.read.filter.metadata.applicationname","dummyapplicationname");
	job.set("hadoopoffice.read.filter.metadata.metadata.author","dummyautho2r");
	job.set("hadoopoffice.read.filter.metadata.metadata.charcount","2");
	job.set("hadoopoffice.read.filter.metadata.metadata.comments","dummycomments2");
	job.set("hadoopoffice.read.filter.metadata.metadata.createdatetime","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.metadata.edittime","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.keywords","dummykeywords2");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastauthor","dummylastauthor2");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastprinted","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastsavedatetime","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.metadata.pagecount","2");
	job.set("hadoopoffice.read.filter.metadata.metadata.revnumber","2");
	job.set("hadoopoffice.read.filter.metadata.metadata.security","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.subject","dummysubject2");
	job.set("hadoopoffice.read.filter.metadata.metadata.template","dummytemplate2");
	job.set("hadoopoffice.read.filter.metadata.metadata.title","dummytitle2");
	job.set("hadoopoffice.read.filter.metadata.metadata.wordcount","2");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion is not true that means the document has (wrongly) been filtered out
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

@Test
    public void writeExcelOutputFormatExcel2013SingleSheetMetaDataMatchOnceNegative() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheetmetadatanativeoncetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
	// set all the meta data including to custom properties
	job.set("hadoopoffice.write.metadata.category","dummycategory");
	job.set("hadoopoffice.write.metadata.contentstatus","dummycontentstatus");
	job.set("hadoopoffice.write.metadata.contenttype","dummycontenttype");
	job.set("hadoopoffice.write.metadata.created","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.creator","dummycreator");
	job.set("hadoopoffice.write.metadata.description","dummydescription");	
	job.set("hadoopoffice.write.metadata.identifier","dummyidentifier");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.modified","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser");
	job.set("hadoopoffice.write.metadata.revision","2");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty1","dummymycustomproperty1");
	job.set("hadoopoffice.write.metadata.custom.mycustomproperty2","dummymycustomproperty2");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","false");
	// following filter
   	job.set("hadoopoffice.read.filter.metadata.category","dummycategory2");
	job.set("hadoopoffice.read.filter.metadata.contentstatus","dummycontentstatus2");
	job.set("hadoopoffice.read.filter.metadata.contenttype","dummycontenttype2");
	job.set("hadoopoffice.read.filter.metadata.created","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.creator","dummycreator2");
	job.set("hadoopoffice.read.filter.metadata.description","dummydescription2");	
	job.set("hadoopoffice.read.filter.metadata.identifier","dummyidentifier2");
	job.set("hadoopoffice.read.filter.metadata.keywords","dummykeywords2");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser2");
	job.set("hadoopoffice.read.filter.metadata.lastprinted","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.modified","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.lastmodifiedbyuser","dummylastmodifiedbyuser2");
	job.set("hadoopoffice.read.filter.metadata.revision","3");
	job.set("hadoopoffice.read.filter.metadata.subject","dummysubject2");
	job.set("hadoopoffice.read.filter.metadata.title","dummytitle2");
	job.set("hadoopoffice.read.filter.metadata.custom.mycustomproperty1","dummymycustomproperty12");
	job.set("hhadoopoffice.read.filter.metadata.custom.mycustomproperty2","dummymycustomproperty22");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion is true that means the document has (wrongly) NOT been filtered out
	assertFalse("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
    }

    @Test
    public void writeExcelOutputFormatExcel2003SingleSheetMetaDataMatchOnceNegative() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// the idea here is to have some content although we only evaluate metadata
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2003singlesheetmetadatanegativeoncetestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel"); //old Excel format


	// set all the meta data 
	job.set("hadoopoffice.write.metadata.applicationname","dummyapplicationname");
	job.set("hadoopoffice.write.metadata.author","dummyauthor");
	job.set("hadoopoffice.write.metadata.charcount","1");
	job.set("hadoopoffice.write.metadata.comments","dummycomments");
	job.set("hadoopoffice.write.metadata.createdatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.edittime","0");
	job.set("hadoopoffice.write.metadata.keywords","dummykeywords");
	job.set("hadoopoffice.write.metadata.lastauthor","dummylastauthor");
	job.set("hadoopoffice.write.metadata.lastprinted","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.lastsavedatetime","12:00:00 01.01.2016");
	job.set("hadoopoffice.write.metadata.pagecount","1");
	job.set("hadoopoffice.write.metadata.revnumber","1");
	job.set("hadoopoffice.write.metadata.security","0");
	job.set("hadoopoffice.write.metadata.subject","dummysubject");
	job.set("hadoopoffice.write.metadata.template","dummytemplate");
	job.set("hadoopoffice.write.metadata.title","dummytitle");
	job.set("hadoopoffice.write.metadata.wordcount","1");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xls");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// set metadata to match all
	job.set("hadoopoffice.read.filter.metadata.matchAll","false");
	// following filter
	job.set("hadoopoffice.read.filter.metadata.applicationname","dummyapplicationname2");
	job.set("hadoopoffice.read.filter.metadata.metadata.author","dummyautho2r");
	job.set("hadoopoffice.read.filter.metadata.metadata.charcount","2");
	job.set("hadoopoffice.read.filter.metadata.metadata.comments","dummycomments2");
	job.set("hadoopoffice.read.filter.metadata.metadata.createdatetime","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.metadata.edittime","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.keywords","dummykeywords2");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastauthor","dummylastauthor2");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastprinted","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.metadata.lastsavedatetime","12:00:00 01.01.2017");
	job.set("hadoopoffice.read.filter.metadata.metadata.pagecount","2");
	job.set("hadoopoffice.read.filter.metadata.metadata.revnumber","2");
	job.set("hadoopoffice.read.filter.metadata.metadata.security","1");
	job.set("hadoopoffice.read.filter.metadata.metadata.subject","dummysubject2");
	job.set("hadoopoffice.read.filter.metadata.metadata.template","dummytemplate2");
	job.set("hadoopoffice.read.filter.metadata.metadata.title","dummytitle2");
	job.set("hadoopoffice.read.filter.metadata.metadata.wordcount","2");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	// if following assertion is true that means the document has (wrongly) NOT been filtered out
	assertFalse("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
    }


 @Test
    public void writeExcelOutputFormatExcel2013SingleSheetGZipCompressed() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheetcompressedtestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	job.setBoolean("mapreduce.output.fileoutputformat.compress",true);
	job.set("mapreduce.output.fileoutputformat.compress.codec","org.apache.hadoop.io.compress.GzipCodec");
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx.gz");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx.gz]Sheet1!A1\"", "["+fileName+".xlsx.gz]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 2 and is empty", 0,spreadSheetValue.get().length);	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 3 columns", 3,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 3 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 1 column", 1,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
    }

   @Test
    public void writeExcelOutputFormatExcel2013SingleSheetComment() throws IOException {
	// 2nd cell with a comment
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","This is a test","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheetcommenttestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
        assertEquals("Input Split for Excel file contains row 1 with cell 2 comment == \"This is a test\"", "This is a test", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getComment());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	
    }

    @Test
    public void writeExcelOutputFormatExcel2013MultiSheet() throws IOException {
	// one sheet "Sheet1"
	// one row string and three columns ("test1","test2","test3")
	SpreadSheetCellDAO sheet1a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO sheet1b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO sheet1c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// one sheet "Sheet2"
	// one row string and three columns ("test4","test5","test6")
	SpreadSheetCellDAO sheet2a1 = new SpreadSheetCellDAO("test4","","","A1","Sheet2");
	SpreadSheetCellDAO sheet2b1 = new SpreadSheetCellDAO("test5","","","B1","Sheet2");
	SpreadSheetCellDAO sheet2c1 = new SpreadSheetCellDAO("test6","","","C1","Sheet2");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013multisheettestout";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,sheet1a1);
	writer.write(null,sheet1b1);
	writer.write(null,sheet1c1);
	writer.write(null,sheet2a1);
	writer.write(null,sheet2b1);
	writer.write(null,sheet2c1);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 Sheet1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns for Sheet1", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 1 Sheet2", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet2!A1\"", "["+fileName+".xlsx]Sheet2!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns for Sheet1", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test5\"", "test5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test6\"", "test6", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

    @Test
    public void writeExcelOutputFormatExcel2003SingleSheetOneLinkedWorkbook() throws IOException {
	// write linkedworkbook1
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO wb1a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO wb1b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO wb1c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String linkedWB1FileName="excel2003linkedwb1";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, linkedWB1FileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,wb1a1);
	writer.write(null,wb1b1);
	writer.write(null,wb1c1);
	writer.close(reporter);
	// write mainworkbook
	String linkedWorkbookFilename="["+tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+linkedWB1FileName+".xls]";
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test4","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("","","["+linkedWB1FileName+".xls]Sheet1!B1","B1","Sheet1"); // should be test2 in the end
	// write
	job = new JobConf(defaultConf);
    	String mainWBfileName="excel2003singlesheetlinkedwbtestout";
        outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel");
	job.set("hadoopoffice.write.linkedworkbooks",linkedWorkbookFilename);
   	 outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writerMain = outputFormat.getRecordWriter(null, job, mainWBfileName, null);
	assertNotNull("Format returned  null RecordWriter", writerMain);
	writerMain.write(null,a1);
	writerMain.write(null,b1);
	writerMain.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+mainWBfileName+".xls");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// enable option to read linked workbooks
	job.setBoolean("hadoopoffice.read.linkedworkbooks",true);
	job.setBoolean("hadoopoffice.read.ignoremissinglinkedworkbooks",false);
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 Sheet1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+mainWBfileName+".xls]Sheet1!A1\"", "["+mainWBfileName+".xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 2 columns for Sheet1", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	// this comes from the external workbook
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
    }

    @Test
    public void writeExcelOutputFormatExcel2003SingleSheetTwoLinkedWorkbooks() throws IOException {
	// write linkedworkbook1
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO wb1a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO wb1b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO wb1c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String linkedWB1FileName="excel2003linkedwb1b";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, linkedWB1FileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,wb1a1);
	writer.write(null,wb1b1);
	writer.write(null,wb1c1);
	writer.close(reporter);
	// write linkedworkbook2
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO wb2a1 = new SpreadSheetCellDAO("test4","","","A1","Sheet1");
	SpreadSheetCellDAO wb2b1 = new SpreadSheetCellDAO("test5","","","B1","Sheet1");
	SpreadSheetCellDAO wb2c1 = new SpreadSheetCellDAO("test6","","","C1","Sheet1");
	// write
	 job = new JobConf(defaultConf);
    	String linkedWB2FileName="excel2003linkedwb2b";
    	 outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel");
        outputFormat = new ExcelFileOutputFormat();
    	 writer = outputFormat.getRecordWriter(null, job, linkedWB2FileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,wb2a1);
	writer.write(null,wb2b1);
	writer.write(null,wb2c1);
	writer.close(reporter);
	// write mainworkbook
	String linkedWorkbookFilename="["+tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+linkedWB1FileName+".xls]:["+tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+linkedWB2FileName+".xls]";
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test7","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("","","["+linkedWB1FileName+".xls]Sheet1!B1","B1","Sheet1"); // should be test2 in the end
         SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("","","["+linkedWB2FileName+".xls]Sheet1!B1","C1","Sheet1"); // should be test5 in the end
	// write
	job = new JobConf(defaultConf);
    	String mainWBfileName="excel2003singlesheetlinkedwb2testout";
        outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.ms-excel");
	job.set("hadoopoffice.write.linkedworkbooks",linkedWorkbookFilename);
   	 outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writerMain = outputFormat.getRecordWriter(null, job, mainWBfileName, null);
	assertNotNull("Format returned  null RecordWriter", writerMain);
	writerMain.write(null,a1);
	writerMain.write(null,b1);
	writerMain.write(null,c1);
	writerMain.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+mainWBfileName+".xls");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// enable option to read linked workbooks
	job.setBoolean("hadoopoffice.read.linkedworkbooks",true);
	job.setBoolean("hadoopoffice.read.ignoremissinglinkedworkbooks",false);
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 Sheet1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+mainWBfileName+".xls]Sheet1!A1\"", "["+mainWBfileName+".xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns for Sheet1", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test7\"", "test7", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	// this comes from the external workbook
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test5\"", "test5", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }

    @Test
    public void writeExcelOutputFormatExcel2013TemplateSingleSheet() throws IOException {
	// one row string and three columns ("test1","test2","test3")
    // change the cell A4 from Test4 to Test5 from the template
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("Test5","","","A4","Table1");
	// change b4 from 10 to 60
	SpreadSheetCellDAO b4 = new SpreadSheetCellDAO("","","60","B4","Table1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013basedontemplate";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new excel format
	// template
	ClassLoader classLoader = getClass().getClassLoader();
 	String fileNameTemplate=classLoader.getResource("templatetest1.xlsx").getFile();	
	job.set("hadoopoffice.write.template.file",fileNameTemplate);
	// 
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a4);
	writer.write(null,b4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Table1!A1\"", "["+fileName+".xlsx]Table1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"Test\"", "Test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"Test2\"", "Test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 2 with cell 2 == \"50\"", "50", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 2 columns", 2,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"Test3\"", "Test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"20\"", "20", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 2 columns", 2,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"Test5\"", "Test5", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());

	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"60\"", "60", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
    }
    
    @Test
    public void writeExcelOutputFormatExcel2013TemplateEncryptedSingleSheetPositive() throws IOException {
	// one row string and three columns ("test1","test2","test3")
    // change the cell A4 from Test4 to Test5 from the template
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("Test5","","","A4","Table1");
	// change b4 from 10 to 60
	SpreadSheetCellDAO b4 = new SpreadSheetCellDAO("","","60","B4","Table1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013basedontemplateencrypted";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new excel format
	// template
	ClassLoader classLoader = getClass().getClassLoader();
 	String fileNameTemplate=classLoader.getResource("templatetest1encrypt.xlsx").getFile();	
	job.set("hadoopoffice.write.template.file",fileNameTemplate);
	job.set("hadoopoffice.write.template.password", "test");
	// 
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a4);
	writer.write(null,b4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Table1!A1\"", "["+fileName+".xlsx]Table1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"Test\"", "Test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"Test2\"", "Test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 2 with cell 2 == \"50\"", "50", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 2 columns", 2,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"Test3\"", "Test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"20\"", "20", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 2 columns", 2,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"Test5\"", "Test5", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());

	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"60\"", "60", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
    }
    
    @Test
    public void writeExcelOutputFormatExcel2013TemplateEncryptedSingleSheetNegative() throws IOException {
	// one row string and three columns ("test1","test2","test3")
    // change the cell A4 from Test4 to Test5 from the template
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("Test5","","","A4","Table1");
	// change b4 from 10 to 60
	SpreadSheetCellDAO b4 = new SpreadSheetCellDAO("","","60","B4","Table1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013basedontemplateencrypted";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new excel format
	// template
	ClassLoader classLoader = getClass().getClassLoader();
 	String fileNameTemplate=classLoader.getResource("templatetest1encrypt.xlsx").getFile();	
	job.set("hadoopoffice.write.template.file",fileNameTemplate);
	job.set("hadoopoffice.write.template.password", "test2");
	// 
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNull("Format returned  null RecordWriter", writer);
	    }
    

    
    @Test
    public void readExcelInputFormatExcel2003SingleSheetLowFootprint() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003test.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2003test.xls]Sheet1!A1\"", "[excel2003test.xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }
    
    @Test
    public void readExcelInputFormatExcel2003MultiSheetAllLowFootPrint() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003testmultisheet.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2093testmultisheet.xls]Sheet1!A1\"", "[excel2003testmultisheet.xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 7 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 7 with cell 1 == \"8\"", "8", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with cell 2 == \"99\"", "99", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with 2 columns", 2, spreadSheetValue.get().length);
	assertTrue("Input Split for Excel file contains row 8 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 8 with 1 column",1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 8 with cell 1 == \"test\"", "test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 9 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 9 with 3 columns", 3, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 9 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertNull("Input Split for Excel file contains row 9 with cell 2 == null", spreadSheetValue.get()[1]);	
	assertEquals("Input Split for Excel file contains row 9 with cell 3 == \"seven\"", "seven", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }

    @Test
    public void readExcelInputFormatExcel2013SingleSheetLowFootprint() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013test.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013test.xlsx]Sheet1!A1\"", "[excel2013test.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }
    
    @Test
    public void readExcelInputFormatExcel2013MultiSheetAllLowFootPrint() throws IOException {
	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013testmultisheet.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013testmultisheet.xlsx]Sheet1!A1\"", "[excel2013testmultisheet.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 4 columns", 4, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 1 with cell 4 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[3]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 2 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 5 columns", 5, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"31/12/99\"", "31/12/99", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"5\"", "5", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertNull("Input Split for Excel file contains row 3 with cell 3 == null", spreadSheetValue.get()[2]);	
	assertNull("Input Split for Excel file contains row 3 with cell 4 == null", spreadSheetValue.get()[3]);	
	assertEquals("Input Split for Excel file contains row 3 with cell 5 == \"null\"", "null", ((SpreadSheetCellDAO)spreadSheetValue.get()[4]).getFormattedValue());		
	assertTrue("Input Split for Excel file contains row 4 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6 (first sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"15\"", "15", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 7 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 7 with cell 1 == \"8\"", "8", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with cell 2 == \"99\"", "99", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertEquals("Input Split for Excel file contains row 7 with 2 columns", 2, spreadSheetValue.get().length);
	assertTrue("Input Split for Excel file contains row 8 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 8 with 1 column",1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 8 with cell 1 == \"test\"", "test", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 9 (second sheet)", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 9 with 3 columns", 3, spreadSheetValue.get().length);
	assertNull("Input Split for Excel file contains row 9 with cell 1 == null", spreadSheetValue.get()[0]);	
	assertNull("Input Split for Excel file contains row 9 with cell 2 == null", spreadSheetValue.get()[1]);	
	assertEquals("Input Split for Excel file contains row 9 with cell 3 == \"seven\"", "seven", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }
    
    @Test
    public void readExcelInputFormatExcel2003SingleSheetEncryptedPositiveLowFootprint() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003encrypt.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2003encrypt.xls]Sheet1!A1\"", "[excel2003encrypt.xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }
    
    @Test
    public void readExcelInputFormatExcel2013SingleSheetEncryptedPositiveLowFootprint() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013encrypt.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013encrypt.xlsx]Sheet1!A1\"", "[excel2013encrypt.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 1 sheetname == \"Sheet1\"", "Sheet1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getSheetName());	
	assertEquals("Input Split for Excel file contains row 1 with cell 1 address == \"A1\"", "A1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getAddress());	
assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());	
    }

    @Test
    public void readExcelInputFormatExcel2013SingleSheetEncryptedNegativeLowFootprint() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013encrypt.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test2");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);	
    	assertNull("Null record reader implies invalid password",reader);
    }
    
    @Test
    public void readExcelInputFormatExcel2003SingleSheetEncryptedNegativeLowFootprint() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003encrypt.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
	// for decryption simply set the password
	job.set("hadoopoffice.read.security.crypt.password","test2");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
    	assertNull("Null record reader implies invalid password",reader);
    }
    
    @Test
    public void readExcelInputFormatExcel2013EmptyRowsLowFootprint() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2013testemptyrows.xlsx";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2013testemptyrows.xlsx]Sheet1!A1\"", "[excel2013testemptyrows.xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 0 columns", 0, spreadSheetValue.get().length);
	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
		assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 0 columns", 0, spreadSheetValue.get().length);
	
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }
    
    @Test
    public void readExcelInputFormatExcel2003EmptyRowsLowFootprint() throws IOException {
    	JobConf job = new JobConf(defaultConf);
    	ClassLoader classLoader = getClass().getClassLoader();
    	String fileName="excel2003testemptyrows.xls";
    	String fileNameSpreadSheet=classLoader.getResource(fileName).getFile();	
    	Path file = new Path(fileNameSpreadSheet);
    	FileInputFormat.setInputPaths(job, file);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.read.lowFootprint", "true");
   	ExcelFileInputFormat format = new ExcelFileInputFormat();
    	format.configure(job);
    	InputSplit[] inputSplits = format.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = format.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"[excel2003testemptyrows.xls]Sheet1!A1\"", "[excel2003testemptyrows.xls]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 0 columns", 0, spreadSheetValue.get().length);
	
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 2 with 2 columns", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
		assertEquals("Input Split for Excel file contains row 2 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 3 with 0 columns", 0, spreadSheetValue.get().length);
	
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 4 with 1 column", 1, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 4 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());	
	assertTrue("Input Split for Excel file contains row 5", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contains row 5 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 5 with cell 1 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());			 
	assertEquals("Input Split for Excel file contains row 5 with cell 2== \"6\"", "6", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 5 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 6", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file contains row 6 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 6 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());		 
	assertEquals("Input Split for Excel file contains row 6 with cell 2== \"4\"", "4", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 6 with cell 3== \"10\"", "10", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }
    
    @Test
    public void writeExcelOutputFormatExcel2013SingleSheetLowFootPrint() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheettestoutlowfootprint";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// low footprint
	job.set("hadoopoffice.write.lowFootprint", "true");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 2 and is empty", 0,spreadSheetValue.get().length);	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 3 columns", 3,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 3 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 1 column", 1,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
    }

    @Test
    public void writeExcelOutputFormatExcel2013SingleSheetEncryptedPositiveLowFootprint() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheettestoutencryptedpositivelowfootprint";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.write.lowFootprint", "true");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new excel format
	// security
	// for the new Excel format you need to decide on your own which algorithms are secure
	job.set("hadoopoffice.write.security.crypt.encrypt.mode","agile");
	job.set("hadoopoffice.write.security.crypt.encrypt.algorithm","aes256");
	job.set("hadoopoffice.write.security.crypt.chain.mode","cbc");
	job.set("hadoopoffice.write.security.crypt.hash.algorithm","sha512");
	job.set("hadoopoffice.write.security.crypt.password","test");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// you just need to provide the password to read encrypted data
	job.set("hadoopoffice.read.security.crypt.password","test");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+fileName+".xlsx]Sheet1!A1\"", "["+fileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 3 columns", 3, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test1\"", "test1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test3\"", "test3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 2", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 2 and is empty", 0,spreadSheetValue.get().length);	
	assertTrue("Input Split for Excel file contains row 3", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 3 with 3 columns", 3,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"1\"", "1", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 2 == \"2\"", "2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 3 with cell 3 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
	assertTrue("Input Split for Excel file contains row 4", reader.next(spreadSheetKey,spreadSheetValue));
	assertEquals("Input Split for Excel file contain row 4 with 1 column", 1,spreadSheetValue.get().length);	
	assertEquals("Input Split for Excel file contains row 3 with cell 1 == \"3\"", "3", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
    }

@Test
    public void writeExcelOutputFormatExcel2013SingleSheetEncryptedNegativeLowFootprint() throws IOException {
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// empty row => nothing todo
	// one row numbers (1,2,3)
	SpreadSheetCellDAO a3 = new SpreadSheetCellDAO("","","1","A3","Sheet1");
	SpreadSheetCellDAO b3 = new SpreadSheetCellDAO("","","2","B3","Sheet1");
	SpreadSheetCellDAO c3 = new SpreadSheetCellDAO("","","3","C3","Sheet1");
	// one row formulas (=A3+B3)
	SpreadSheetCellDAO a4 = new SpreadSheetCellDAO("","","A3+B3","A4","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String fileName="excel2013singlesheettestoutencryptednegativelowfoodprint";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");

	// low footprint
	job.set("hadoopoffice.write.lowFootprint", "true");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new excel format
	// security
	// for the new Excel format you need to decide on your own which algorithms are secure
	job.set("hadoopoffice.write.security.crypt.encrypt.mode","agile");
	job.set("hadoopoffice.write.security.crypt.encrypt.algorithm","aes256");
	job.set("hadoopoffice.write.security.crypt.chain.mode","cbc");
	job.set("hadoopoffice.write.security.crypt.hash.algorithm","sha512");
	job.set("hadoopoffice.write.security.crypt.password","test");
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, fileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,a1);
	writer.write(null,b1);
	writer.write(null,c1);
	writer.write(null,a3);
	writer.write(null,b3);
	writer.write(null,c3);
	writer.write(null,a4);
	writer.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+fileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// you just need to provide the password to read encrypted data
	job.set("hadoopoffice.read.security.crypt.password","test2");
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
    	assertNull("Null record reader implies invalid password",reader);
    }
    
    @Ignore("This does not work yet due to a bug in Apache POI that prevents writing correct workbooks containing external references: https://bz.apache.org/bugzilla/show_bug.cgi?id=57184")
    @Test
    public void writeExcelOutputFormatExcel2013SingleSheetOneLinkedWorkbook() throws IOException {
	// write linkedworkbook1
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO wb1a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO wb1b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO wb1c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String linkedWB1FileName="excel2013linkedwb1";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, linkedWB1FileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,wb1a1);
	writer.write(null,wb1b1);
	writer.write(null,wb1c1);
	writer.close(reporter);
	// write mainworkbook
	String linkedWorkbookFilename="["+tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+linkedWB1FileName+".xlsx]";
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test4","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("","","["+linkedWB1FileName+".xlsx]Sheet1!B1","B1","Sheet1"); // should be test2 in the end
	// write
	job = new JobConf(defaultConf);
    	String mainWBfileName="excel2013singlesheetlinkedwbtestout";
        outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
	job.set("hadoopoffice.write.linkedworkbooks",linkedWorkbookFilename);
   	 outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writerMain = outputFormat.getRecordWriter(null, job, mainWBfileName, null);
	assertNotNull("Format returned  null RecordWriter", writerMain);
	writerMain.write(null,a1);
	writerMain.write(null,b1);
	writerMain.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+mainWBfileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// enable option to read linked workbooks
	job.setBoolean("hadoopoffice.read.linkedworkbooks",true);
	job.setBoolean("hadoopoffice.read.ignoremissinglinkedworkbooks",false);
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 Sheet1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+mainWBfileName+".xlsx]Sheet1!A1\"", "["+mainWBfileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 2 columns for Sheet1", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test4\"", "test4", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	// this comes from the external workbook
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
    }

   @Ignore("This does not work yet due to a bug in Apache POI that prevents writing correct workbooks containing external references: https://bz.apache.org/bugzilla/show_bug.cgi?id=57184")
    @Test
    public void writeExcelOutputFormatExcel2013SingleSheetTwoLinkedWorkbooks() throws IOException {
	// write linkedworkbook1
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO wb1a1 = new SpreadSheetCellDAO("test1","","","A1","Sheet1");
	SpreadSheetCellDAO wb1b1 = new SpreadSheetCellDAO("test2","","","B1","Sheet1");
	SpreadSheetCellDAO wb1c1 = new SpreadSheetCellDAO("test3","","","C1","Sheet1");
	// write
	JobConf job = new JobConf(defaultConf);
    	String linkedWB1FileName="excel2013linkedwb1";
    	String tmpDir=tmpPath.toString();	
    	Path outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	ExcelFileOutputFormat outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writer = outputFormat.getRecordWriter(null, job, linkedWB1FileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,wb1a1);
	writer.write(null,wb1b1);
	writer.write(null,wb1c1);
	writer.close(reporter);
	// write linkedworkbook2
	// one row string and three columns ("test1","test2","test3")
	// (String formattedValue, String comment, String formula, String address,String sheetName)
	SpreadSheetCellDAO wb2a1 = new SpreadSheetCellDAO("test4","","","A1","Sheet1");
	SpreadSheetCellDAO wb2b1 = new SpreadSheetCellDAO("test5","","","B1","Sheet1");
	SpreadSheetCellDAO wb2c1 = new SpreadSheetCellDAO("test6","","","C1","Sheet1");
	// write
	 job = new JobConf(defaultConf);
    	String linkedWB2FileName="excel2013linkedwb2";
    	 outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
   	 outputFormat = new ExcelFileOutputFormat();
    	writer = outputFormat.getRecordWriter(null, job, linkedWB2FileName, null);
	assertNotNull("Format returned  null RecordWriter", writer);
	writer.write(null,wb2a1);
	writer.write(null,wb2b1);
	writer.write(null,wb2c1);
	writer.close(reporter);
	// write mainworkbook
	String linkedWorkbookFilename="["+tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+linkedWB1FileName+".xlsx]:["+tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+linkedWB2FileName+".xlsx]";
	SpreadSheetCellDAO a1 = new SpreadSheetCellDAO("test7","","","A1","Sheet1");
	SpreadSheetCellDAO b1 = new SpreadSheetCellDAO("","","'["+linkedWB1FileName+".xlsx]Sheet1'!B1","B1","Sheet1"); // should be test2 in the end
	SpreadSheetCellDAO c1 = new SpreadSheetCellDAO("","","'["+linkedWB2FileName+".xlsx]Sheet1'!B1","B1","Sheet1"); // should be test5 in the end
	// write
	job = new JobConf(defaultConf);
    	String mainWBfileName="excel2013singlesheetlinkedwbtestout";
        outputPath = new Path(tmpDir);
    	FileOutputFormat.setOutputPath(job, outputPath);
	// set generic outputformat settings
	job.set(JobContext.TASK_ATTEMPT_ID, attempt);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	job.set("hadoopoffice.write.mimeType","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"); // new Excel format, anyway default, but here for illustrative purposes
	job.set("hadoopoffice.write.linkedworkbooks",linkedWorkbookFilename);
   	 outputFormat = new ExcelFileOutputFormat();
    	RecordWriter<NullWritable,SpreadSheetCellDAO> writerMain = outputFormat.getRecordWriter(null, job, mainWBfileName, null);
	assertNotNull("Format returned  null RecordWriter", writerMain);
	writerMain.write(null,a1);
	writerMain.write(null,b1);
	writerMain.write(null,c1);
	writerMain.close(reporter);
	// try to read it again
	job = new JobConf(defaultConf);
	Path inputFile = new Path(tmpDir+File.separator+"_temporary"+File.separator+"0"+File.separator+"_temporary"+File.separator+attempt+File.separator+mainWBfileName+".xlsx");
    	FileInputFormat.setInputPaths(job, inputFile);
	// set locale to the one of the test data
	job.set("hadoopoffice.read.locale.bcp47","de");
	// enable option to read linked workbooks
	job.setBoolean("hadoopoffice.read.linkedworkbooks",true);
	job.setBoolean("hadoopoffice.read.ignoremissinglinkedworkbooks",false);
   	ExcelFileInputFormat inputFormat = new ExcelFileInputFormat();
    	inputFormat.configure(job);
    	InputSplit[] inputSplits = inputFormat.getSplits(job,1);
    	assertEquals("Only one split generated for Excel file", 1, inputSplits.length);
    	RecordReader<Text, ArrayWritable> reader = inputFormat.getRecordReader(inputSplits[0], job, reporter);
	assertNotNull("Format returned  null RecordReader", reader);
	Text spreadSheetKey = new Text();	
	ArrayWritable spreadSheetValue = new ArrayWritable(SpreadSheetCellDAO.class);
	assertTrue("Input Split for Excel file contains row 1 Sheet1", reader.next(spreadSheetKey,spreadSheetValue));	
	assertEquals("Input Split for Excel file has keyname == \"["+mainWBfileName+".xlsx]Sheet1!A1\"", "["+mainWBfileName+".xlsx]Sheet1!A1", spreadSheetKey.toString());
	assertEquals("Input Split for Excel file contains row 1 with 2 columns for Sheet1", 2, spreadSheetValue.get().length);
	assertEquals("Input Split for Excel file contains row 1 with cell 1 == \"test7\"", "test7", ((SpreadSheetCellDAO)spreadSheetValue.get()[0]).getFormattedValue());
	// this comes from the external workbook
	assertEquals("Input Split for Excel file contains row 1 with cell 2 == \"test2\"", "test2", ((SpreadSheetCellDAO)spreadSheetValue.get()[1]).getFormattedValue());
	assertEquals("Input Split for Excel file contains row 1 with cell 3 == \"test5\"", "test5", ((SpreadSheetCellDAO)spreadSheetValue.get()[2]).getFormattedValue());
    }


    
}
