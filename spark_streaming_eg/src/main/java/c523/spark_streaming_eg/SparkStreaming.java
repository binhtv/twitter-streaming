package c523.spark_streaming_eg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import kafka.serializer.StringDecoder;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import scala.Tuple2;

import com.google.gson.Gson;

public class SparkStreaming {
	private static Connection connection;
	
	private static void initialize() {
		Configuration config = HBaseConfiguration.create();

		try {
			connection = ConnectionFactory.createConnection(config);
		} catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	
	private static void saveRDD(String tableName, List<Put> puts) throws IOException {
		Table hTable = connection.getTable(TableName.valueOf(tableName));
		hTable.put(puts);
		hTable.close();
	}
	
	private static void saveCounts(JavaPairInputDStream<String, String> stream) {
		String table = "tweet_counts";
		String cf = "countInfo";
		JavaPairDStream<Integer, Integer>  tweetCounts = stream.map(s -> new Gson().fromJson(s._2, Tweet.class))
									.mapToPair(s -> new Tuple2<Integer, Integer>(s.getCreatedAt(), 1))
									.reduceByKey(Integer::sum);
		
		tweetCounts.foreachRDD(t -> {
			if (!t.isEmpty()) {
				List<Tuple2<Integer, Integer>> obj = t.collect();
				List<Put> puts = new ArrayList<Put>();
				for (Tuple2<Integer, Integer> o : obj) {
					Put put = new Put(Bytes.toBytes(o._1.toString()));
					put.addImmutable(Bytes.toBytes(cf), Bytes.toBytes("id"), Bytes.toBytes(o._1.toString()));
					put.addImmutable(Bytes.toBytes(cf), Bytes.toBytes("elapseTime"), Bytes.toBytes(o._1.toString()));
					put.addImmutable(Bytes.toBytes(cf), Bytes.toBytes("tweetCount"), Bytes.toBytes(o._2.toString()));
					
					puts.add(put);
				}
				
				saveRDD(table, puts);
			}
		});
	} 
	
	private static boolean filterWord(String statusText, String[] words) {
		boolean contained = false;
		for(String w : words) {
			contained = contained || statusText.toLowerCase().contains(w);
			if(contained) {
				break;
			}
		}
		
		return contained;
	}
	
	private static void saveWordCounts(JavaPairInputDStream<String, String> stream) {
		String table = "tweet_words";
		String cf = "wordInfo";
		String filteredWords[] = new String[]{"trump", "bitcoin", "football", "snow"};
		JavaDStream<String> tweetStream = stream.map(s -> new Gson().fromJson(s._2, Tweet.class))
													.filter(s -> SparkStreaming.filterWord(s.getStatusText(), filteredWords))
													.map(s -> s.getStatusText());
		tweetStream.foreachRDD(t -> {
	    	if(!t.isEmpty()) {
	    		List<String> tweets = t.collect();
	    		Map<String, Integer> wordCounts = new HashMap<String, Integer>();
	    		for(String statusText : tweets) {
	    			for(String filterW : filteredWords) {
	    				if(statusText.toLowerCase().contains(filterW)) {
	    					if(wordCounts.containsKey(filterW)) {
	    						wordCounts.put(filterW, wordCounts.get(filterW) + 1);
	    					} else {
	    						wordCounts.put(filterW, 1);
	    					}
	    				}
	    			}
	    		}
	    		
	    		Iterator<Entry<String, Integer>> iterator = wordCounts.entrySet().iterator();
	    		long timeStamp = System.currentTimeMillis() / 1000;
	    		List<Put> puts = new ArrayList<Put>();
	    		while(iterator.hasNext()) {
	    			Entry<String, Integer> entry = iterator.next();
	    			String id = timeStamp + entry.getKey();
	    			System.out.println(id);
	    			Put put = new Put(Bytes.toBytes(id));
	    			put.addImmutable(Bytes.toBytes(cf), Bytes.toBytes("id"), Bytes.toBytes(id));
	    			put.addImmutable(Bytes.toBytes(cf), Bytes.toBytes("word"), Bytes.toBytes(entry.getKey()));
	    			put.addImmutable(Bytes.toBytes(cf), Bytes.toBytes("tweetCount"), Bytes.toBytes(entry.getValue().toString()));
	    			put.addImmutable(Bytes.toBytes(cf), Bytes.toBytes("createdAt"), Bytes.toBytes(String.valueOf(timeStamp)));
	    			puts.add(put);
	    		}
    			
	    		saveRDD(table, puts);
	    	}
	    });
	}
	
	public static void main(String[] args) throws Exception {
	    if (args.length < 1) {
	      System.err.println("Usage: JavaNetworkWordCount <hostname>:<port>");
	      System.exit(1);
	    }
	    initialize();
	    // Create the context with a 1 second batch size
	    SparkConf sparkConf = new SparkConf().setAppName("JavaNetworkWordCount").setMaster("local");
	    JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, Durations.seconds(5));

	    Map<String, String> kafkaParams = new HashMap<>();
	    kafkaParams.put("bootstrap.servers", args[0]);
	    kafkaParams.put("group.id", "kafka-spark-streaming-example");

	    Set<String> topics = new HashSet<String>();
	    topics.add("sparktest");

	    JavaPairInputDStream<String, String> stream = KafkaUtils.createDirectStream(ssc, String.class,
				String.class, StringDecoder.class, StringDecoder.class, kafkaParams, topics);
	    
	    saveCounts(stream);
	    saveWordCounts(stream);
	    
	    ssc.start();
	    ssc.awaitTermination();
	  }
}
