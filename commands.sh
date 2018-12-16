#Start kafka server
bin/kafka-server-start.sh config/server.properties
#Create Kafka topics
bin/kafka-topics.sh --create --topic sparktest --partitions 1 --replication-factor 1 --zookeeper localhost:2181
#List all topics
bin/kafka-topics.sh --list --zookeeper localhost:2181
#Writing test data to topic
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic sparktest
#Copy hive-site.xml to spark/config
sudo cp /usr/lib/hive/conf/hive-site.xml /usr/lib/spark/conf/
#spark submit streaming
spark-submit --class "c523.spark_streaming_eg.SparkStreaming" --master local ~/workspace/FinalProject/spark_streaming_eg/target/spark_streaming_eg-0.0.1-SNAPSHOT.jar localhost:9092 trump,bitcoin,football,snow,iphone
#spark submit visualization
spark-submit --class "c523.spark_streaming_eg.SparkSql" --master local target/spark_streaming_visualization-0.0.1-SNAPSHOT.jar
#Tweet producer
java -cp target/tweet-0.0.1-SNAPSHOT.jar producer.tweet.App 500 trump,bitcoin,football,snow,rain,soccer,winter,iphone 
