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


/**
 * Simple Reducer for converting the rows to CSV rows
 */
package org.zuinnote.hadoop.office.example.tasks;

/**
* Author: Jörn Franke <zuinnote@gmail.com>
*
*/
import java.io.IOException;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;

import org.zuinnote.hadoop.office.format.common.dao.TextArrayWritable;
 
public class HadoopOfficeExcelReducer extends  Reducer<Text, TextArrayWritable, NullWritable, Text> {
private static final String CSV_SEPARATOR=",";
private static final NullWritable EMPTYKEY = NullWritable.get();

   public void reduce(Text key, Iterable<TextArrayWritable> values, Context context)
     throws IOException, InterruptedException {
       for (TextArrayWritable currentRow: values) {  // should be only called once
	   Writable[] currentRowTextArray = currentRow.get();
	   if (currentRowTextArray.length>0) {
	   	StringBuilder currentCSVRowSB=new StringBuilder();
	   	for (int i=0;i<currentRowTextArray.length;i++) {
			Text currentCell = (Text)currentRowTextArray[i];
			if (currentCell!=null) {
				currentCSVRowSB.append(currentCell.toString());
			} 
		    	currentCSVRowSB.append(HadoopOfficeExcelReducer.CSV_SEPARATOR);
	   	}
	   	// remove last separator (new line does not need to be added anymore, apperantly this is done by textoutputformat
	   	String currentCSVRowString = currentCSVRowSB.substring(0,currentCSVRowSB.length()-1);
		// add new line
	   	context.write(HadoopOfficeExcelReducer.EMPTYKEY, new Text(currentCSVRowString));
	   }
       }
   }
}


