package opcuaClient.MOM;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opcufoundation.ua.examples.entity.TagValue_Entity;
import org.opcufoundation.ua.examples.entity.Tag_Entity;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class testrecv {

	private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
//    	EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
//    	EntityManager em = emf.createEntityManager();
//    	EntityTransaction tx = em.getTransaction();   
//    	try {
//    		tx.begin();
//    		logic(em);
//    		tx.commit();
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    		tx.rollback();
//    	} finally {
//    		em.close();
//    	}
//    	emf.close();
    	
    	ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("root");
        factory.setPassword("1234");
    	Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
//            System.out.println(" [x] Received '" + message + "'");
//           System.out.println(message);
            ArrayList<String> tag_array = new ArrayList<String>();
            ArrayList<String> tag_Value_array = new ArrayList<String>();
            ArrayList<Tag_Entity> tag_Name = new ArrayList<Tag_Entity>();
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
//            		String TagValue = tagsObject.get("TagValue").toString();
//            		tag_array = new ArrayList<String>();
            		tag_array.add(DisplayName);
        			tag_array.add(NodeClass);
        			tag_array.add(NodeId);      
//            		System.out.println(tag_array);
        			String TagValues = tagsObject.get("TagValue").toString();
        			System.out.println(tagsObject.get("TagValue").toString());
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
    					
    					tag_Value_array.add(ServerTimestamp);
    					tag_Value_array.add(SourceTimestamp);
    					tag_Value_array.add(SourcePicoseconds);
    					tag_Value_array.add(ServerPicoseconds);
    					tag_Value_array.add(StatusCode);
    					tag_Value_array.add(Value);
    					tag_Name.add((Tag_Entity) tagsObject.get("DisplayName"));
   				}
        			
            	}
				
				

			} catch (ParseException e) {
				e.printStackTrace();
			}   
//            System.out.println(tag_array);
            InsertTagDB(tag_array,tag_Value_array,tag_Name);
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });

    }
    

	public static void logic(EntityManager em,ArrayList<String> tag_array,ArrayList<String> tag_Value_array,ArrayList<Tag_Entity> tag_Name) throws IOException, TimeoutException {
    	for(int all=0;all<tag_Name.size();all++) {
    		for(int i=0;i<tag_array.size();i+=3) {
         		Tag_Entity Tag_entity = new Tag_Entity();
         		Tag_entity.setDisplayName(tag_array.get(i));
         		Tag_entity.setNodeId(tag_array.get(i+1));
         		Tag_entity.setNodeClass(tag_array.get(i+2));
         		em.persist(Tag_entity);
         	}
    		for(int j=0;j<tag_Value_array.size();j+=6) {
    			TagValue_Entity Tag_Value_entity = new TagValue_Entity();
    			Tag_Value_entity.setServerTimestamp(tag_Value_array.get(j));
    			Tag_Value_entity.setSourceTimestamp(tag_Value_array.get(j+1));
    			Tag_Value_entity.setSourcePicoseconds(tag_Value_array.get(j+2));
    			Tag_Value_entity.setServerPicoseconds(tag_Value_array.get(j+3));
    			Tag_Value_entity.setStatusCode(tag_Value_array.get(j+4));
    			Tag_Value_entity.setValue(tag_Value_array.get(j+5));
    			Tag_Value_entity.setTAG(tag_Name.get(all));
         		em.persist(Tag_Value_entity);
    		}
    	}
    }
   
	
	public static void InsertTagDB(ArrayList<String> tag_array,ArrayList<String> tag_Value_array,ArrayList<Tag_Entity> tag_Name) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
    	EntityManager em = emf.createEntityManager();
    	EntityTransaction tx = em.getTransaction();   
    	try {
    		tx.begin();
    		logic(em,tag_array,tag_Value_array,tag_Name);
    		tx.commit();
    	} catch (Exception e) {
    		e.printStackTrace();
    		tx.rollback();
    	} finally {
    		em.close();
    	}
    	emf.close();
		
	}
}





//	System.out.println("----------------------------Tags----------------------");
//	System.out.println("NodeClass : " + NodeClass);
//	System.out.println("DisplayName : " + DisplayName);
//	System.out.println("NodeId : " + NodeId);
//	System.out.println("TagValue : " + TagValue);
//	System.out.println("---------------------------------------------------------------");
////	Object test = parser.parse(newjsonobj);
////	
//	String TagValues = tagsObject.get("TagValue").toString();
//	JSONArray TagsValue_array = (JSONArray)parser.parse(TagValues);
//	String ServerTimestamp = null;
//	String SourceTimestamp = null;
//	for(int j=0;j<TagsValue_array.size();j++) {
//		JSONObject TagValues_obj = (JSONObject)TagsValue_array.get(j);
//		if(TagValues_obj.get("ServerTimestamp")!=null) {
//			ServerTimestamp = TagValues_obj.get("ServerTimestamp").toString();
//		}
//		if(TagValues_obj.get("SourceTimestamp")!=null) {
//			SourceTimestamp = TagValues_obj.get("SourceTimestamp").toString();
//		}             		
//		String SourcePicoseconds = TagValues_obj.get("SourcePicoseconds").toString();
//		String ServerPicoseconds = TagValues_obj.get("ServerPicoseconds").toString();
//		String StatusCode = TagValues_obj.get("StatusCode").toString();
//		String Value = TagValues_obj.get("Value").toString();
////		System.out.println("----------------------------TagValues----------------------");
////		System.out.println("SourcePicoseconds : " + SourcePicoseconds);
////		System.out.println("ServerTimestamp : " + ServerTimestamp);
////		System.out.println("SourceTimestamp : " + SourceTimestamp);
////		System.out.println("ServerPicoseconds : " + ServerPicoseconds);
////		System.out.println("StatusCode : " + StatusCode);
////		System.out.println("Value : " + Value);
////		System.out.println("---------------------------------------------------------------");
//	}

