package jp.co.flect.papertrail;

import java.util.Locale;

public class Resource {
	
	public static final String ALL_LOG;
	public static final String ACCESS_LOG;
	public static final String SLOW_REQUEST;
	public static final String SLOW_CONNECT;
	public static final String SLOW_SQL;
	public static final String PATH;
	public static final String CLIENT_ERROR;
	public static final String SERVER_ERROR;
	public static final String DYNO_STATE;
	public static final String PROGRAM;
	public static final String HEROKU_ERROR;
	public static final String RESPONSE_TIME;
	public static final String REGEX_GROUP;
	public static final String PATTERN;
	public static final String REGEX_NUMBER;
	public static final String CONNECT_TIME;
	public static final String DYNO_BOOT;
	
	public static final String ALL_ACCESS;
	public static final String ALL_DURATION;
	public static final String OTHER;
	
	static {
		boolean b = "ja".equals(Locale.getDefault().getLanguage());
		
		ALL_LOG = b ? 
			"すべてのログ" : 
			"All Log";
		ACCESS_LOG = b ? 
			"アクセスログ" : 
			"Access Log";
		SLOW_REQUEST = b ? 
			"{0}ms以上かかったリクエスト" : 
			"Slow Request(More than {0}ms)";
		SLOW_CONNECT = b ? 
			"接続に{0}ms以上かかったリクエスト" : 
			"Slow Connect(More than {0}ms)";
		SLOW_SQL = b ? 
			"遅延SQL(50ms以上)" : 
			"Slow SQL(More than 50ms)";
		PATH = b ? 
			"パス" : 
			"Path";
		CLIENT_ERROR = b ? 
			"クライアントエラー(40x)" : 
			"Client Error";
		SERVER_ERROR = b ? 
			"サーバーエラー(50x)" : 
			"Server Error";
		DYNO_STATE = b ? 
			"Dynoステート変更" : 
			"Dyno state changed";
		PROGRAM = b ? 
			"プログラム別" : 
			"By Program";
		HEROKU_ERROR = b ? 
			"Herokuエラー別" : 
			"By Heroku Error";
		RESPONSE_TIME = b ? 
			"レスポンスタイム" : 
			"Response Time";
		REGEX_GROUP = b ? 
			"正規表現グループ" : 
			"Regex Group";
		PATTERN = b ? 
			"パターン" : 
			"Pattern";
		REGEX_NUMBER = b ? 
			"正規表現数値" : 
			"Regex Number";
		CONNECT_TIME = b ? 
			"接続時間" : 
			"Connect time";
		DYNO_BOOT = b ? 
			"Dyno起動時間" : 
			"Dyno boot time";
		ALL_ACCESS = b ? 
			"すべてのアクセス" : 
			"All Access";
		ALL_DURATION = b ? 
			"すべての遅延SQL" : 
			"All slow sql";
		OTHER = b ? 
			"その他" :
			"Other";
	}
}
