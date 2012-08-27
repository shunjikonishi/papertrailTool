package jp.co.flect.papertrail;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.GZIPInputStream;

public class S3Archive {
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private String accessKey;
	private String secretKey;
	private String bucket;
	
	public S3Archive(String accessKey, String secretKey, String bucket) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.bucket = bucket;
	}
	
	private String getLogPath(String dateStr) {
		return "papertrail/logs/dt=" + dateStr + "/" + dateStr + ".tsv.gz";
	}
	
	public InputStream getDailyArchive(Date date) throws IOException {
		return getDailyArchive(date, true);
	}
	
	public InputStream getDailyArchive(Date date, boolean unzip) throws IOException {
		String dateStr = new SimpleDateFormat(DATE_FORMAT).format(date);
		return getDailyArchive(dateStr, unzip);
	}
	
	public InputStream getDailyArchive(String dateStr) throws IOException {
		return getDailyArchive(dateStr, true);
	}
	
	public InputStream getDailyArchive(String dateStr, boolean unzip) throws IOException {
		AmazonS3Client client = new AmazonS3Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
		GetObjectRequest request = new GetObjectRequest(this.bucket, getLogPath(dateStr));
		InputStream is = client.getObject(request).getObjectContent();
		if (unzip) {
			return new GZIPInputStream(is);
		}
		return is;
	}
	
	public void saveToFile(Date date, boolean unzip, File file) throws IOException {
		String dateStr = new SimpleDateFormat(DATE_FORMAT).format(date);
		saveToFile(dateStr, unzip, file);
	}
	
	public void saveToFile(String dateStr, boolean unzip, File file) throws IOException {
		InputStream is = getDailyArchive(dateStr);
		try {
			OutputStream os = new FileOutputStream(file);
			try {
				byte[] buf = new byte[4096];
				int n = is.read(buf, 0, buf.length);
				while (n > 0) {
					os.write(buf, 0, n);
					n = is.read(buf, 0, buf.length);
				}
			} finally {
				os.close();
			}
		} finally {
			is.close();
		}
	}
	
	public static String getDateStr(String s) {
		Calendar cal = Calendar.getInstance();
		String[] strs = s.split("-");
		String year = null;
		String month = null;
		String day = null;
		switch (strs.length) {
			case 1:
				year = Integer.toString(cal.get(Calendar.YEAR));
				month = Integer.toString(cal.get(Calendar.MONTH) + 1);
				day = strs[0];
				break;
			case 2:
				year = Integer.toString(cal.get(Calendar.YEAR));
				month = strs[0];
				day = strs[1];
				break;
			case 3:
				year = strs[0];
				month = strs[1];
				day = strs[2];
			default:
				throw new IllegalArgumentException(s);
		}
		if (month.length() == 1) {
			month = "0" + month;
		}
		if (day.length() == 1) {
			day = "0" + day;
		}
		return year + "-" + month + "-" + day;
	}
	
	public static void main(String[] args) throws Exception {
		if (args.length < 4) {
			System.err.println("Usage: s3 AWS-ACCESSKEY AWS-SECRETKEY S3-BUCKET [[yyyy-]MM-]dd [OUTPUT-FILE]");
			return;
		}
		String accessKey = args[0];
		String secretKey = args[1];
		String bucket = args[2];
		String dateStr = getDateStr(args[3]);
		String outputFile = args.length > 4 ? args[4] : null;
		if (outputFile == null) {
			outputFile = dateStr + ".log";
		}
		S3Archive s3 = new S3Archive(accessKey, secretKey, bucket);
		s3.saveToFile(dateStr, true, new File(outputFile));
	}
}
