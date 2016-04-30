package jp.co.flect.papertrail;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.AmazonS3Exception;
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
	private String directory;

	private AmazonS3Client client = null;

	private AmazonS3Client getClient() {
		if (client == null) {
			client = new AmazonS3Client(new BasicAWSCredentials(this.accessKey, this.secretKey));
		}
		return client;
	}
	
	public S3Archive(String accessKey, String secretKey, String bucket) {
		this(accessKey, secretKey, bucket, "papertrail/logs");
	}
	
	public S3Archive(String accessKey, String secretKey, String bucket, String directory) {
		this.accessKey = accessKey;
		this.secretKey = secretKey;
		this.bucket = bucket;
		this.directory = directory;
	}
	
	private String getDailyLogPath(String dateStr) {
		return directory + "/dt=" + dateStr + "/" + dateStr + ".tsv.gz";
	}
	
	private String getHourlyLogPath(String dateStr, int hour) {
		String hourStr = hour > 9 ? Integer.toString(hour) : "0" + hour;
		return directory + "/dt=" + dateStr + "/" + dateStr + "-" + hourStr + ".tsv.gz";
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
		AmazonS3Client client = getClient();
		GetObjectRequest request = new GetObjectRequest(this.bucket, getDailyLogPath(dateStr));
		InputStream is = client.getObject(request).getObjectContent();
		if (unzip) {
			return new GZIPInputStream(is);
		}
		return is;
	}

	public InputStream getHourlyArchive(Date date) throws IOException {
		return getHourlyArchive(date, true);
	}

	public InputStream getHourlyArchive(Date date, boolean unzip) throws IOException {
		String dateStr = new SimpleDateFormat(DATE_FORMAT).format(date);
		int hour = Integer.parseInt(new SimpleDateFormat("HH").format(date));
		return getHourlyArchive(dateStr, hour, true);
	}

	public InputStream getHourlyArchive(String dateStr, int hour) throws IOException {
		return getHourlyArchive(dateStr, hour, true);
	}
	
	public InputStream getHourlyArchive(String dateStr, int hour, boolean unzip) throws IOException {
		AmazonS3Client client = getClient();
		GetObjectRequest request = new GetObjectRequest(this.bucket, getHourlyLogPath(dateStr, hour));
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
		try {
			saveToFileForDaily(dateStr, unzip, file);
		} catch (AmazonS3Exception e) {
			saveToFileForHourly(dateStr, unzip, file);
		}
	}

	private void saveToFileForDaily(String dateStr, boolean unzip, File file) throws IOException {
		InputStream is = getDailyArchive(dateStr, unzip);
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
	
	private void saveToFileForHourly(String dateStr, boolean unzip, File file) throws IOException {
		OutputStream os = new FileOutputStream(file);
		try {
			byte[] buf = new byte[4096];
			for (int i=0; i<24; i++) {
				InputStream is = getHourlyArchive(dateStr, i, unzip);
				try {
					int n = is.read(buf, 0, buf.length);
					while (n > 0) {
						os.write(buf, 0, n);
						n = is.read(buf, 0, buf.length);
					}
				} finally {
					is.close();
				}
			}
		} finally {
			os.close();
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
				break;
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
		if (args.length < 5) {
			System.err.println("Usage: s3 AWS-ACCESSKEY AWS-SECRETKEY S3-BUCKET DIRECTORY [[yyyy-]MM-]dd [OUTPUT-FILE]");
			return;
		}
		String accessKey = args[0];
		String secretKey = args[1];
		String bucket = args[2];
		String dir = args[3];
		String dateStr = getDateStr(args[4]);
		String outputFile = args.length > 5 ? args[5] : null;
		if (outputFile == null) {
			outputFile = dateStr + ".log";
		}
		S3Archive s3 = new S3Archive(accessKey, secretKey, bucket, dir);
		s3.saveToFile(dateStr, true, new File(outputFile));
	}
}
