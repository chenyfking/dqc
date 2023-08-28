package com.beagledata.gaea.workbench.util;

import com.beagledata.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by mahongfei on 2019/3/21.
 */
public class CsvUtil {
	/**
	 * @Author: mahongfei
	 * @description: 生成并下载csv文件
	 */
	public static void exportDataFile(HttpServletResponse response,
									  List<String> list,
									  String outPutPath,
									  String fileName) throws IOException {
		File csvFile = null;
		BufferedWriter csvFileOutputStream = null;
		try {
			File file = new File(outPutPath);
			if (!file.exists()) {
				file.mkdirs();
			}
			//定义文件名格式并创建
			csvFile = new File(outPutPath + fileName + ".csv");
			if (csvFile.exists()) {
				csvFile.delete();
			}
			csvFile.createNewFile();
			// UTF-8使正确读取分隔符","
			//如果生产文件乱码，windows下用gbk，linux用UTF-8
			csvFileOutputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csvFile), "UTF-8"), 1024);
			//写入前段字节流，防止乱码
			csvFileOutputStream.write(getBOM());
//            // 写入文件头部
//            for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
//                java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
//                csvFileOutputStream.write( propertyEntry.getValue() != null ? (String) propertyEntry.getValue() : "" );
//                if (propertyIterator.hasNext()) {
//                    csvFileOutputStream.write(",");
//                }
//              }
//              csvFileOutputStream.newLine();
//              // 写入文件内容
//            for (Iterator iterator = exportData.iterator(); iterator.hasNext();) {
//                Object row =  iterator.next();
//                for (Iterator propertyIterator = map.entrySet().iterator(); propertyIterator.hasNext();) {
//                    java.util.Map.Entry propertyEntry = (java.util.Map.Entry) propertyIterator.next();
//                    String str=row!=null?((String)((Map)row).get( propertyEntry.getKey())):"";
//                    if(StringUtils.isEmpty(str)){
//                        str="";
//                    }else{
//                        str=str.replaceAll("\"","\"\"");
//                        if(str.indexOf(",")>=0){
//                            str="\""+str+"\"";
//                        }
//                    }
//                    csvFileOutputStream.write(str);
//                    if (propertyIterator.hasNext()) {
//                        csvFileOutputStream.write(",");
//                    }
//                }
//                if (iterator.hasNext()) {
//                    csvFileOutputStream.newLine();
//                }
//              }
			for (String line : list) {
				if (StringUtils.isNotBlank(line)) {
					csvFileOutputStream.write(line);
					csvFileOutputStream.newLine();
				}
			}
			csvFileOutputStream.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				csvFileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		InputStream in = null;
		try {
			in = new FileInputStream(outPutPath + fileName + ".csv");
			int len = 0;
			byte[] buffer = new byte[1024];

			OutputStream out = response.getOutputStream();
			response.reset();

			response.setContentType("application/csv;charset=UTF-8");
			response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName + ".csv", "UTF-8"));
			response.setCharacterEncoding("UTF-8");
			while ((len = in.read(buffer)) > 0) {
//                out.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });
				out.write(buffer, 0, len);
			}
			out.close();
		} catch (FileNotFoundException e) {

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

	}

	public static String getBOM() {
		byte b[] = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
		return new String(b);
	}

	public static void exportDataFile(HttpServletResponse response,
									  List<String> list,
									  String fileName) throws IOException {
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			StringBuffer sbBuffer = new StringBuffer();
			for (String line : list) {
				sbBuffer.append(line).append("\n");
			}
			response.reset();
			response.setContentType("text/plain;charset=GBK");
			response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName + ".csv", "UTF-8"));
			response.setCharacterEncoding("GBK");
			String data = sbBuffer.toString();
			bis = new BufferedInputStream(new ByteArrayInputStream(data.getBytes("gbk")));
			bos = new BufferedOutputStream(response.getOutputStream());
			int len = 0;
			byte[] bytes = new byte[1024];
			while ((len = bis.read(bytes)) > 0) {
				bos.write(bytes, 0, len);
			}
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {

		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}


