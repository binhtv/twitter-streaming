package c523.spark_streaming_eg;

import java.util.concurrent.TimeUnit;

import org.apache.spark.SparkConf;
import org.apache.spark.SparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.hive.HiveContext;

import com.google.gson.JsonObject;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

public class SparkSql {
	public static final void main(String... strings) throws InterruptedException {
		 SparkConf conf = new SparkConf().setAppName("SparkHive_Example").setMaster("local");
		 SparkContext sc = new SparkContext(conf);
		 HiveContext sqlContext = new HiveContext(sc);
		 

		PNConfiguration pnConfiguration = new PNConfiguration();
		
		pnConfiguration
				.setSubscribeKey("sub-c-30f86508-cee8-11e7-91cc-2ef9da9e0d0e");
		pnConfiguration
				.setPublishKey("pub-c-7c748e9e-6003-42be-ab7a-b92472d65f44");
//		pnConfiguration
//		.setSubscribeKey("sub-c-25fe06a8-cee8-11e7-91cc-2ef9da9e0d0e");
//		pnConfiguration
//		.setPublishKey("pub-c-9d465a0e-ded6-4539-96da-c215b4f19071");
		pnConfiguration.setSecure(false);

		PubNub pubnub = new PubNub(pnConfiguration);

		int call = 1;
		int second = 10;
		if(strings.length > 0) {
			second = Integer.valueOf(strings[0]);
		}
		while (true) {
			System.out.println(call);
			Row[] rows = sqlContext.sql("SELECT tweetcount FROM tweet_counts WHERE CAST(elapsetime AS INT) == (unix_timestamp() - "+second+")")
					.collect();
			call++;
			
			JsonObject position = new JsonObject();
			if(rows.length > 0) {
				position.addProperty("count", rows[0].getInt(0));
			} else {
				position.addProperty("count", 0);
			}
			
			if(call%5 == 0) {
				Row[] sum = sqlContext
						.sql("SELECT COALESCE(SUM(tweetcount), 0) AS sum_count FROM tweet_counts WHERE CAST(elapsetime AS INT) >= (unix_timestamp() - "
								+ (60 + second)
								+ ") AND CAST(elapsetime AS INT) <= unix_timestamp()")
						.collect();
				if (sum.length > 0) {
					position.addProperty("avg", sum[0].getLong(0)/ 60);
				} else {
					position.addProperty("avg", 0);
				}
				
				Row[] wordCount = sqlContext.sql("SELECT word, COALESCE(SUM(tweetcount), 0) as sum_tc FROM tweet_words WHERE CAST(createdat AS INT) >= (unix_timestamp() - "
								+ (60 + second)
								+ ") AND CAST(createdat AS INT) <= unix_timestamp() GROUP BY word")
								.collect();
				if (wordCount.length > 0) {
					for(Row r : wordCount) {
						position.addProperty(r.getString(0), r.getLong(1));
					}
				}
			}
			
 			System.out.println("before pub: " + position);
			pubnub.publish().message(position).channel("my_channel")
					.async(new PNCallback<PNPublishResult>() {
						@Override
						public void onResponse(PNPublishResult result,
								PNStatus status) {
							// handle publish result, status always present,
							// result if successful
							// status.isError() to see if error happened
							if (!status.isError()) {
								System.out.println("pub timetoken: "
										+ result.getTimetoken());
								System.exit(1);
							}
							System.out.println("pub status code: "
									+ status.getStatusCode());
						}
					});
			
			TimeUnit.SECONDS.sleep(1);
		}
	}
}
