package producer.tweet;

import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.google.gson.Gson;

public class App {
   public static void main(String[] args) throws Exception {
      final LinkedBlockingQueue<Status> queue = new LinkedBlockingQueue<Status>(1000);
      
      String consumerKey = "GJU1va0TazRRaBGrsEiMRNH2m";//args[0].toString();
      String consumerSecret = "2ctfE1T7KG5s94UVw2uoUxwHuVD48oRjS3Df5KcFnUgLINgUbk"; //args[1].toString();
      String accessToken = "192976007-ofCyvzIgUotKnbkIdblPcPrVwxy1YdHDBmGoWRgp";//args[2].toString();
      String accessTokenSecret = "h7L0TSIaqsUUKWlg3uFo40btsC4l0VrAvTIul4M0f3uj1";//args[3].toString();
      String topicName = "sparktest";//args[4].toString();
      //String[] arguments = args.clone();
      //String[] keyWords = Arrays.copyOfRange(arguments, 5, arguments.length);
      if(args.length == 0) {
    	  System.err.println("Lack of arguments");
    	  System.exit(1);
      }
      
      int waitCount = Integer.parseInt(args[0]);
      String keywords = "";
      if(args.length == 2) {
    	  keywords = args[1];
      }

      ConfigurationBuilder cb = new ConfigurationBuilder();
      cb.setDebugEnabled(true)
         .setOAuthConsumerKey(consumerKey)
         .setOAuthConsumerSecret(consumerSecret)
         .setOAuthAccessToken(accessToken)
         .setOAuthAccessTokenSecret(accessTokenSecret);

      TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
      StatusListener listener = new StatusListener() {
        
         @Override
         public void onStatus(Status status) {      
            queue.offer(status);
            //System.out.println(status.getId());
            //System.out.println(status.getText());
//            System.out.println("@" + status.getUser().getScreenName() 
//               + " - " + status.getText());
            // System.out.println("@" + status.getUser().getScreen-Name());

            /*for(URLEntity urle : status.getURLEntities()) {
               System.out.println(urle.getDisplayURL());
            }*/

//            for(HashtagEntity hashtage : status.getHashtagEntities()) {
//               System.out.println(hashtage.getText());
//            }
         }
         
         @Override
         public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            System.out.println("Got a status deletion notice id:" 
               + statusDeletionNotice.getStatusId());
         }
         
         @Override
         public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            System.out.println("Got track limitation notice:" + 
            		numberOfLimitedStatuses);
         }

         @Override
         public void onScrubGeo(long userId, long upToStatusId) {
            System.out.println("Got scrub_geo event userId:" + userId + 
            "upToStatusId:" + upToStatusId);
         }      
         
         @Override
         public void onStallWarning(StallWarning warning) {
            // System.out.println("Got stall warning:" + warning);
         }
         
         @Override
         public void onException(Exception ex) {
            ex.printStackTrace();
         }
      };
      twitterStream.addListener(listener);
      if(keywords != null && keywords.length() > 0) {
    	  //"trump", "bitcoin", "football", "snow"
    	  System.out.println(keywords);
    	  FilterQuery query = new FilterQuery().track(keywords.split(","));
    	  twitterStream.filter(query);
      } else {
    	  twitterStream.sample();
      }
      Thread.sleep(5000);
      
      //Add Kafka producer config settings
      Properties props = new Properties();//
      props.put("bootstrap.servers", "localhost:9092");
      props.put("acks", "all");
      props.put("retries", 0);
      props.put("batch.size", 16384);
      props.put("linger.ms", 1);
      props.put("buffer.memory", 33554432);
      
      props.put("key.serializer", 
         "org.apache.kafka.common.serialization.StringSerializer");
      props.put("value.serializer", 
         "org.apache.kafka.common.serialization.StringSerializer");
      
      Producer<String, String> producer = new KafkaProducer<String, String>(props);
      int i = 0;
      int j = 0;
      Gson gson = new Gson();
      while(i < waitCount) {
         Status ret = queue.poll();
         
         if (ret == null) {
            Thread.sleep(1000);
            i++;
         } else {
        	 Tweet t = new Tweet(String.valueOf(ret.getId()), ret.getUser().getName(), ret.getText(), (int)(ret.getCreatedAt().getTime()/1000));
        	 System.out.println(ret.getCreatedAt().getTime());
        	 producer.send(new ProducerRecord<String, String>(topicName, Integer.toString(++j), gson.toJson(t)));
         }
      }
      
      producer.close();
      Thread.sleep(5000);
      twitterStream.shutdown();
   }
}