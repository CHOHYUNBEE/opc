package org.opcfoundation.ua.examples;

import java.io.IOException;
import java.sql.Date;
import java.util.concurrent.TimeoutException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opcufoundation.ua.examples.entity.Tag_Entity;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class testrecv {

	private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
    	
    	
    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
    	EntityManager em = emf.createEntityManager();
    	EntityTransaction tx = em.getTransaction();
    	
    	 ConnectionFactory factory = new ConnectionFactory();
         factory.setHost("localhost");
         factory.setPort(5672);
         factory.setUsername("root");
         factory.setPassword("1234");
         
         Connection connection = factory.newConnection();
         Channel channel = connection.createChannel();
    
    	try {
    		tx.begin();
    		logic(em, channel);
    		tx.commit();
    	} catch (Exception e) {
    		e.printStackTrace();
    		tx.rollback();
    	} finally {
    		em.close();
    	}
    	emf.close();

    }
    
    public static void logic(EntityManager em,Channel channel) throws IOException, TimeoutException {
         channel.queueDeclare(QUEUE_NAME, false, false, false, null);
         DeliverCallback deliverCallback = (consumerTag, delivery) -> {
             String message = new String(delivery.getBody(), "UTF-8");
//             System.out.println(" [x] Received '" + message + "'");
//            System.out.println(message);
             try {
             	JSONParser parser = new JSONParser();
             	Object obj = parser.parse(message);
 				JSONObject jsonobj = (JSONObject)obj;
 				String Tags = jsonobj.get("TagS").toString();
 				JSONArray Tags_array = (JSONArray)parser.parse(Tags);
 				for(int i=0;i<Tags_array.size();i++) {
             		JSONObject tagsObject = (JSONObject)Tags_array.get(i);
             		String NodeClass = tagsObject.get("NodeClass").toString();
             		String DisplayName = tagsObject.get("DisplayName").toString();
             		String NodeId = tagsObject.get("NodeId").toString();
//             		String TagValue = tagsObject.get("TagValue").toString();
             		
             		Tag_Entity Tag_entity = new Tag_Entity();
             		Tag_entity.setDisplayName(DisplayName);
             		Tag_entity.setNodeId(NodeId);
             		Tag_entity.setNodeClass(NodeClass);
             		
             		System.out.println(Tag_entity);
            		em.persist(Tag_entity);
//            		
//             		System.out.println("----------------------------Tags----------------------");
//             		System.out.println("NodeClass : " + NodeClass);
//             		System.out.println("DisplayName : " + DisplayName);
//             		System.out.println("NodeId : " + NodeId);
//             		System.out.println("TagValue : " + TagValue);
//             		System.out.println("---------------------------------------------------------------");
////             		Object test = parser.parse(newjsonobj);
////             		
             		String TagValues = tagsObject.get("TagValue").toString();
     				JSONArray TagsValue_array = (JSONArray)parser.parse(TagValues);
     				String ServerTimestamp = null;
     				String SourceTimestamp = null;
     				for(int j=0;j<TagsValue_array.size();j++) {
     					JSONObject TagValues_obj = (JSONObject)TagsValue_array.get(j);
                 		if(TagValues_obj.get("ServerTimestamp")!=null) {
                 			ServerTimestamp = TagValues_obj.get("ServerTimestamp").toString();
                 		}
                 		if(TagValues_obj.get("SourceTimestamp")!=null) {
                 			SourceTimestamp = TagValues_obj.get("SourceTimestamp").toString();
                 		}             		
                 		String SourcePicoseconds = TagValues_obj.get("SourcePicoseconds").toString();
                 		String ServerPicoseconds = TagValues_obj.get("ServerPicoseconds").toString();
                 		String StatusCode = TagValues_obj.get("StatusCode").toString();
                 		String Value = TagValues_obj.get("Value").toString();
//                 		System.out.println("----------------------------TagValues----------------------");
//                 		System.out.println("SourcePicoseconds : " + SourcePicoseconds);
//                 		System.out.println("ServerTimestamp : " + ServerTimestamp);
//                 		System.out.println("SourceTimestamp : " + SourceTimestamp);
//                 		System.out.println("ServerPicoseconds : " + ServerPicoseconds);
//                 		System.out.println("StatusCode : " + StatusCode);
//                 		System.out.println("Value : " + Value);
//                 		System.out.println("---------------------------------------------------------------");
     				}
             	}
 				
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         };
         channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
   
}



