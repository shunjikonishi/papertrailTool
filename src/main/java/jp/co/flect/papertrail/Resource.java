package jp.co.flect.papertrail;

import java.util.Locale;

public class Resource {
	
	public static final String ALL_LOG;
	public static final String ALL_LOG_HELP;
	public static final String ACCESS_LOG;
	public static final String ACCESS_LOG_HELP;
	public static final String SLOW_REQUEST;
	public static final String SLOW_REQUEST_HELP;
	public static final String PATH;
	public static final String PATH_HELP;
	public static final String CLIENT_ERROR;
	public static final String CLIENT_ERROR_HELP;
	public static final String SERVER_ERROR;
	public static final String SERVER_ERROR_HELP;
	public static final String DYNO_STATE;
	public static final String DYNO_STATE_HELP;
	public static final String PROGRAM;
	public static final String PROGRAM_HELP;
	public static final String HEROKU_ERROR;
	public static final String HEROKU_ERROR_HELP;
	public static final String RESPONSE_TIME;
	public static final String RESPONSE_TIME_HELP;
	public static final String REGEX_GROUP;
	public static final String REGEX_GROUP_HELP;
	public static final String PATTERN;
	public static final String PATTERN_HELP;
	public static final String REGEX_NUMBER;
	public static final String REGEX_NUMBER_HELP;
	
	static {
		boolean b = "ja".equals(Locale.getDefault().getLanguage());
		
		ALL_LOG = b ? 
			"すべてのログ" : 
			"All Log";
		ALL_LOG_HELP = b ? 
			"全てのログをカウントします。: [NAME]" : 
			"Count all logs. : [NAME]";
		ACCESS_LOG = b ? 
			"アクセスログ" : 
			"Access Log";
		ACCESS_LOG_HELP = b ? 
			"アクセスログをカウントします。: [NAME]" : 
			"Count access logs. : [NAME]";
		SLOW_REQUEST = b ? 
			"{0}ms以上かかったリクエスト" : 
			"Slow Request(More than {0}ms)";
		SLOW_REQUEST_HELP = b ? 
			"指定の時間以上かかったアクセスログをカウントします。： [NAME] TIME(ms)" : 
			"Count access logs that took more than specified time. : [NAME] TIME(ms)";
		PATH = b ? 
			"パス" : 
			"Path";
		PATH_HELP = b ? 
			"リクエストパスに指定の文字が入ったアクセスログをカウントします。： [NAME] STRING" : 
			"Count access logs that contains specified string. : [NAME] STRING";
		CLIENT_ERROR = b ? 
			"クライアントエラー(40x)" : 
			"Client Error";
		CLIENT_ERROR_HELP = b ? 
			"status=40xのアクセスログをカウントします。： [NAME]" : 
			"Count access logs that status = 40x. : [NAME]";
		SERVER_ERROR = b ? 
			"サーバーエラー(50x)" : 
			"Server Error";
		SERVER_ERROR_HELP = b ? 
			"Herokuエラーまたはstatus=50xのアクセスログをカウントします。： [NAME]" : 
			"Count access logs that status = 50x or contains Heroku error code. : [NAME]";
		DYNO_STATE = b ? 
			"Dynoステート変更" : 
			"Dyno state changed";
		DYNO_STATE_HELP = b ? 
			"再起動などのDynoの状態変更ログをカウントします。： [NAME]" : 
			"Count dyno state changed. : [NAME]";
		PROGRAM = b ? 
			"プログラム別" : 
			"By Program";
		PROGRAM_HELP = b ? 
			"プログラム別のログ件数をカウントします。： [NAME]" : 
			"Count by program. : [NAME]";
		HEROKU_ERROR = b ? 
			"Herokuエラー別" : 
			"By Heroku Error";
		HEROKU_ERROR_HELP = b ? 
			"Herokuエラー別のログ件数をカウントします。： [NAME]" : 
			"Count by Heroku error. : [NAME]";
		RESPONSE_TIME = b ? 
			"レスポンスタイム" : 
			"Response Time";
		RESPONSE_TIME_HELP = b ? 
			"リクエストパス毎の件数、最大値、平均値をカウントします。： [NAME]" : 
			"Count, Max and Average by request path. : [NAME]";
		REGEX_GROUP = b ? 
			"正規表現グループ" : 
			"Regex Group";
		REGEX_GROUP_HELP = b ? 
			"正規表現でグループ指定された文字列ごとのログをカウントします。： [NAME] PATTERN" : 
			"Count logs grouped by regex pattern. : [NAME] PATTERN";
		PATTERN = b ? 
			"パターン" : 
			"Pattern";
		PATTERN_HELP = b ? 
			"指定のパターンにマッチしたログをカウントします。： [NAME] PATTERN" : 
			"Count logs that matches specified pattern. : [NAME] PATTERN";
		REGEX_NUMBER = b ? 
			"正規表現数値" : 
			"Regex Number";
		REGEX_NUMBER_HELP = b ? 
			"正規表現でグループ指定された数値の件数、最大値、平均値をカウントします。： [NAME] PATTERN" : 
			"Count, Max, Average by specified regex pattern. : [NAME] PATTERN";
	}
}
