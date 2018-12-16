CREATE TABLE tweet_words(id string, word string, tweetCount int, createdAt int)
    STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
    WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,wordInfo:word,wordInfo:tweetCount,wordInfo:createdAt")
    TBLPROPERTIES ("hbase.table.name" = "tweet_words");

--Tweet count by second
CREATE TABLE tweet_counts(id string, elapseTime int, tweetCount int)
    STORED BY 'org.apache.hadoop.hive.hbase.HBaseStorageHandler'
    WITH SERDEPROPERTIES ("hbase.columns.mapping" = ":key,countInfo:elapseTime,countInfo:tweetCount")
    TBLPROPERTIES ("hbase.table.name" = "tweet_counts");

