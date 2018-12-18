**Spark Streaming Project with Hadoop, HBase, Spark Streaming, Spark SQL + Hive**

**(See How to build and run in the last page)**

# **Project idea**

Twitter is known as social network. This project aims to get some insights from twitter feeds for researching purposes

Do you know how people talk about what you are thinking on twitter?

Your thoughts maybe about Bitcoin, Trump, upcoming Christmas or TM.

How many people talk about them on Twitter per second?

How much “Trump” is mentioned compare to “Bitcoin” on Twitter every minute?

# **Data set**

Twitter API platform offers options for streaming their real-time Tweets.

# **Source code and technology**
## **Source code**

Unzip the attachment, under root folder you can find commands, HiveQL script and source codes of all applications

![image](https://user-images.githubusercontent.com/7671024/50123315-db6e5f80-0225-11e9-9187-e325ffa6117a.png)


You can also get the source code from github.com at [https://github.com/binhtv/twitter-streaming](https://github.com/binhtv/twitter-streaming) for the latest updates

- **Commands detail** (please see how to build and run part)
- **HiveQL script** (please see how to build and run part)
- **tweet folder** (tweet app)

Read real time tweets from twitter by using [Twitter4J](http://twitter4j.org/en/) library

[Apache Kafka](https://kafka.apache.org/) producer take the tweets to feed into Kafka topic

![image](https://user-images.githubusercontent.com/7671024/50123377-fe007880-0225-11e9-8b2e-2018c0e2bd22.png)

- **-spark\_streaming\_eg folder**: contains source code for streaming jobs

Using _KafkaUtils_ to create a direct stream, subscribe to Kafka topic for getting data from Kafka producter (tweet app)

![image](https://user-images.githubusercontent.com/7671024/50123422-330ccb00-0226-11e9-9b86-d63a84e57a35.png)

- **spark\_streaming\_visualization folder**

Contains source code for getting data from Hbase (with Hive table on top) for visualization by using Spark SQL

Then, data is broadcasted to web client app for visualization

![image](https://user-images.githubusercontent.com/7671024/50123513-a9a9c880-0226-11e9-9862-6dfb03d8395c.png)


- **visualization\_client folder**

Contain source code for running web client app for visualization. This is a simple web application using NodeJs, Pubnub and Highchart

Pubnub subscribe to a channel to get data from this channel

Highchart is javascript library for visualization data with various type of charts and graphs

_Source code structure_
![image](https://user-images.githubusercontent.com/7671024/50123542-c9d98780-0226-11e9-84d8-ef4972dd717c.png)

## **Components and flows**

_Components and flows_
![image](https://user-images.githubusercontent.com/7671024/50123626-1c1aa880-0227-11e9-82cf-9a1a28495087.png)

# **How to build and run**

**Make sure your latest Kafka service installed**, you can download at [https://kafka.apache.org/](https://kafka.apache.org/) and start manually

**You also need to install zookeeper service** , or you can skip this step if you use _zookeeper_ from another service such as HBase or Hadoop

**Make sure Hadoop, Hbase (master &amp; region server), Hive are installed** and working properly in your machine

**Make sure latest NodeJs is installed** on your machine

## **Step by step to run:**

### In Kafka folder, Start Kafka service

_$ bin/kafka-server-start.sh config/server.properties_

### Create a Kafka topic

_$ bin/kafka-topics.sh --create --topic sparktest --partitions 1 --replication-factor 1 --zookeeper localhost:2181_

### Copy _hive-site_ configuration file: Make hive and spark can work together

_$ sudo cp /usr/lib/hive/conf/hive-site.xml /usr/lib/spark/conf/_

### At hive shell, create hive tables: _tweet\_counts_ and _tweet\_words_

Execute the following scripts in _hive\_tweet.sql_

![image](https://user-images.githubusercontent.com/7671024/50123870-f510a680-0227-11e9-9ed1-8e8a76f989c7.png)

After running you should see as the following in Hue

![image](https://user-images.githubusercontent.com/7671024/50123891-05288600-0228-11e9-9322-f8d3a53acc70.png)

From _hbase shell_, you can also see the description of tables by _describe &#39;table\_name&#39;_

![image](https://user-images.githubusercontent.com/7671024/50123903-0fe31b00-0228-11e9-8e61-577852f73c55.png)
![image](https://user-images.githubusercontent.com/7671024/50123990-491b8b00-0228-11e9-9c9e-a37c0a4a0966.png)

### At ROOT source code folder, go inside **tweet** folder

_$ mvn clean install_

_$ java -cp target/tweet-0.0.1-SNAPSHOT.jar producer.tweet.App 500 trump,bitcoin,football,snow,rain,soccer,winter,iphone_

![image](https://user-images.githubusercontent.com/7671024/50124044-749e7580-0228-11e9-85cc-98186191d676.png)

### At ROOT source code folder, go inside **spark\_streaming\_eg** folder

_$ mvn clean install_

_$ spark-submit --class &quot;c523.spark\_streaming\_eg.SparkStreaming&quot; --master local target/spark\_streaming\_eg-0.0.1-SNAPSHOT.jar localhost:9092 trump,bitcoin,football,snow,iphone_

![image](https://user-images.githubusercontent.com/7671024/50124051-86801880-0228-11e9-89c1-19eff7d05fc4.png)

### At ROOT source code folder, go inside **spark\_streaming\_visualization** folder

_$ mvn clean install_

_$ spark-submit --class &quot;c523.spark\_streaming\_eg.SparkSql&quot; --master local target/spark\_streaming\_visualization-0.0.1-SNAPSHOT.jar_

![image](https://user-images.githubusercontent.com/7671024/50124061-91d34400-0228-11e9-8ab7-ac1fab295e61.png)

### At ROOT source code folder, go inside **visualization\_client** folder

_$ node app.js_

![image](https://user-images.githubusercontent.com/7671024/50124074-a4e61400-0228-11e9-9f1b-b318d15f28f1.png)

## **Test your work**

From browser (Chrome, Firefox, Safari, new IE) go to [http://localhost:3000](http://localhost:3000)

You should see as below:

![image](https://user-images.githubusercontent.com/7671024/50124108-c515d300-0228-11e9-8835-55d420a99e5f.png)