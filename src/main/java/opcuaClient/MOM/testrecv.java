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
import org.opcufoundation.ua.examples.entity.Tag_Entity;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class testrecv {
	
	private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
    	ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("root");
        factory.setPassword("1234");
    	
    	Mainmethod(factory);
    }
    
    public static void Mainmethod(ConnectionFactory factory) throws IOException, TimeoutException {
    	Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            ArrayList<String> tag_array = new ArrayList<String>();
            try {
            	System.out.println(message);
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
            		String Value = tagsObject.get("Value").toString();
            		tag_array.add(DisplayName);
        			tag_array.add(NodeClass);
        			tag_array.add(NodeId);      
        			tag_array.add(Value); 
            	}
			} catch (ParseException e) {
				e.printStackTrace();
			}   
            InsertTagDB(tag_array);
        };
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }

	public static void logic(EntityManager em,ArrayList<String> tag_array) throws IOException, TimeoutException {
    		for(int i=0;i<tag_array.size();i+=4) {
         		Tag_Entity Tag_entity = new Tag_Entity();
         		Tag_entity.setDisplayName(tag_array.get(i));
         		Tag_entity.setNodeId(tag_array.get(i+1));
         		Tag_entity.setNodeClass(tag_array.get(i+2));
         		Tag_entity.setNodeValue(tag_array.get(i+3));
         		em.persist(Tag_entity);
         	}
    }
   
	public static void InsertTagDB(ArrayList<String> tag_array) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabook");
    	EntityManager em = emf.createEntityManager();
    	EntityTransaction tx = em.getTransaction();   
    	try {
    		tx.begin();
    		logic(em,tag_array);
    		tx.commit();
    	} catch (Exception e) {
    		e.printStackTrace();
    		tx.rollback();
    	} finally {
    		em.close();
    		long end = System.currentTimeMillis();
			System.out.println("***************************end TIME******************************");
			System.out.println("***                        "+end+"                            ***");
			System.out.println("***************************end TIME******************************");
    	}
    	emf.close();
	}
}

