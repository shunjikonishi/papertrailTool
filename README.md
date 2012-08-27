papertrailTool
==============

Tools for papertrail

## LogAnalyzer
Analyze heroku log.

    Usage:
       pt-a [OPTIONS] [-f FILENAME | -s3 S3-ACCESSKEY S3-SECRETKEY BUCKET-NAME [[yyyy-]MM-]dd] [-xlsx OUTPUTFILE]

    Example:
      pt-a -* -f 2012-08-21.log -xlsx HerokuLog.xlsx
  
## LogCutter
Cut a portion of log.

    Usage:
      pt-c -s STARTTIME [-e ENDTIME] [-f <OUTPUTFIELD>] FILENAME

    Example:
      pt-c -s 20:00:00 -e 21:00:00 2012-08-21.log
  

## S3
Download papertrail archive from S3.

    Usage:
      s3 AWS-ACCESSKEY AWS-SECRETKEY S3-BUCKET [[yyyy-]MM-]dd [OUTPUT-FILE]

## PapertrailClient
Wrapper class of Papertrail API.

---

Papertrailツール
================
Papertrailのログを扱うためのライブラリとアプリケーションです。

## LogAnalyzer
Herokuのログ解析を行うアプリケーションです。

- ログ件数(1時間区切り)
- その時間帯での1分間でのログ出力数の最大値
- その時間帯での1秒間でのログ出力数の最大値
- パスごとのレスポンスタイムの平均値(1時間区切り)
- その時間帯でのレスポンスタイムの最大値

などを出力します。  
カウント対象のログはオプションと正規表現で指定できます。

    Usage:
      pt-a [OPTIONS] [-f FILENAME | -s3 S3-ACCESSKEY S3-SECRETKEY BUCKET-NAME [[yyyy-]MM-]dd]

    Example:
      pt-a -* -f 2012-08-21.log -xlsx HerokuLog.xlsx


    OPTIONS
      -al すべてのログをカウントします。
      -ac アクセスログをカウントします。(Program=heroku/router)
     -sl 指定の時間以上時間のかかったリクエストをカウントします。(service=XXmsが指定時間以上)
         追加引数として時間(ms単位)を指定します。
     -rp 指定の文字列がパスに含まれるアクセスログをカウントします。
         追加引数として文字列を指定します。
     -ce クライアントエラーをカウントします。(status=40x)
     -se サーバーエラーをカウントします。(status=50x またはHerokuエラーコードのあるアクセスログ)
     -ds 再起動などのDynoの状態変更をカウントします。(メッセージが"State changed "で始まるHerokuログ)
     -pg プログラムごとのログをカウントします。
     -pt メッセージが指定のパターンにマッチするログをカウントします。
         追加引数として正規表現のパターンを指定します。
     -rg グループのある正規表現を使用してグループ毎にログをカウントします。
         追加引数としてグループのある正規表現のパターンを指定します。
     -he Herokuエラーをエラー種別ごとにカウントします。
         「-rg "Error ([A-Z]\\d{2} \\(.*?\\))"」と同じです。
     -rt パス毎のレスポンスタイムをカウントします。
     -rn グループのある正規表現を使用してグループ指定された数値をカウントします。
         例えばアプリログで「... delay=987ms」のようなログを出力している場合
         「-rn ".* delay=(\d*)ms"」とすることでその数値の時間毎の最大値と平均値がカウントされます。
     -*  追加引数を必要としないすべてのオプションが追加されます。
    
    -f   解析するログファイルを指定します。拡張子が「.gz」の場合はGZip解凍して読み込みます。
    -s3  S3にアーカイブされたログファイルをダウンロードして解析を行います。
         引数としてS3のアクセスキー、シークレットキー、バケット名、日付を指定します。
         日付の年、月が省略された場合は今日の日付から補完されます。
    
    -xlsx 解析結果をExcelファイルで出力します。
          引数として出力ファイル名を指定します。
          引数として指定されたファイルが存在する場合はそのファイルにシート追加されます。
          この引数が指定されない場合は解析結果はコンソールにCSV形式で出力されます。     

## LogCutter
巨大なログを指定時間で切り取ります。

    Usage:
      pt-c -s STARTTIME [-e ENDTIME] [-f <OUTPUTFIELD>] FILENAME

    Example:
      pt-c -s 20:00:00 -e 21:00:00 -f ripm 2012-08-21.log

    -s 切り取り開始時間を指定します。
    -e 切り取り終了時間を指定します。省略可能です。
    -f 出力フィールドを指定します。
       出力フィールドは英1文字での指定を連結した形式です。

       n  IDナンバー
       r  受信日付
       s  ソースID
       S  ソース名
       i  ソースIP
       f  ファシリティ
       l  ログレベル(Severity)
       p  プログラム
       m  メッセージ
       
       省略した場合はすべてのフィールドが出力されます。

切り取り結果はコンソールに出力されます。


## S3
Download papertrail archive from S3.

    Usage:
      s3 AWS-ACCESSKEY AWS-SECRETKEY S3-BUCKET [[yyyy-]MM-]dd [OUTPUT-FILE]

S3からログをダウンロードします。(gzの解凍も同時に行われます。)  
出力ファイル名を省略した場合は「<日付>.log」というファイル名になります。
