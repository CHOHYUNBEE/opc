/*
 * ======================================================================== Copyright (c) 2005-2015
 * The OPC Foundation, Inc. All rights reserved.
 *
 * OPC Foundation MIT License 1.00
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The complete license agreement can be found here: http://opcfoundation.org/License/MIT/1.00/
 * ======================================================================
 */

package org.opcfoundation.ua.examples;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
//import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import javax.naming.spi.DirStateFactory.Result;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.LocalizedText;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.cert.CertificateCheck;
import org.opcfoundation.ua.cert.DefaultCertificateValidator;
import org.opcfoundation.ua.cert.DefaultCertificateValidatorListener;
import org.opcfoundation.ua.cert.PkiDirectoryCertificateStore;
import org.opcfoundation.ua.cert.ValidationResult;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ApplicationDescription;
import org.opcfoundation.ua.core.Attributes;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.BrowseResult;
import org.opcfoundation.ua.core.BrowseResultMask;
import org.opcfoundation.ua.core.Identifiers;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.ReferenceDescription;
import org.opcfoundation.ua.core.ReferenceNode;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.examples.certs.ExampleKeys;
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.HttpsSecurityPolicy;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.utils.CertificateUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

/**
 * Sample client creates a connection to OPC UA Server (1st arg), browses and reads a boolean value.
 * It is configured to work against NanoServer example, using the address opc.tcp://localhost:8666/
 * 
 * NOTE: Does not work against SeverExample1, since it does not support Browse
 */
public class SampleClient {
	private final static String QUEUE_NAME = "hello";
  private static class MyValidationListener implements DefaultCertificateValidatorListener {

    @Override
    public ValidationResult onValidate(Cert certificate, ApplicationDescription applicationDescription,
        EnumSet<CertificateCheck> passedChecks) {
      System.out.println("Validating Server Certificate...");
      if (passedChecks.containsAll(CertificateCheck.COMPULSORY)) {
        System.out.println("Server Certificate is valid and trusted, accepting certificate!");
        return ValidationResult.AcceptPermanently;
      } else {
        System.out.println("Certificate Details: " + certificate.getCertificate().toString());
        System.out.println("Do you want to accept this certificate?\n" + " (A=Always, Y=Yes, this time, N=No)");
        while (true) {
          try {
            char c;
            c = Character.toLowerCase((char) System.in.read());
            if (c == 'a') {
              return ValidationResult.AcceptPermanently;
            }
            if (c == 'y') {
              return ValidationResult.AcceptOnce;
            }
            if (c == 'n') {
              return ValidationResult.Reject;
            }
          } catch (IOException e) {
            System.out.println("Error reading input! Not accepting certificate.");
            return ValidationResult.Reject;
          }
        }
      }
    }

  }

  public static void main(String[] args) throws Exception {
//    if (args.length == 0) {
//      System.out.println("Usage: SampleClient [server uri]");
//      return;
//    }
	  
    String url = "opc.tcp://192.168.43.175:53530/OPCUA/SimulationServer";
//    String url = "opc.tcp://DESKTOP-D61DT4N:53530/OPCUA/SimulationServe";
    System.out.print("SampleClient: Connecting to " + url + " .. ");

    ////////////// CLIENT //////////////
    CertificateUtils.setKeySize(2048);
    final KeyPair pair = ExampleKeys.getCert("SampleClient");
    final Client myClient = Client.createClientApplication(pair);
    myClient.getApplication().addLocale(Locale.ENGLISH);
    myClient.getApplication().setApplicationName(new LocalizedText("Java Sample Client", Locale.ENGLISH));
    myClient.getApplication().setProductUri("urn:JavaSampleClient");
    final PkiDirectoryCertificateStore myCertStore = new PkiDirectoryCertificateStore("SampleClientPKI/CA");
    final DefaultCertificateValidator myValidator = new DefaultCertificateValidator(myCertStore);
    final MyValidationListener myValidationListener = new MyValidationListener();
    myValidator.setValidationListener(myValidationListener);
    myClient.getApplication().getOpctcpSettings().setCertificateValidator(myValidator);
    myClient.getApplication().getHttpsSettings().setCertificateValidator(myValidator);
    myClient.getApplication().getHttpsSettings().setHttpsSecurityPolicies(HttpsSecurityPolicy.ALL_104);
    KeyPair myHttpsCertificate = ExampleKeys.getHttpsCert("SampleClient");
    myClient.getApplication().getHttpsSettings().setKeyPair(myHttpsCertificate);
    SessionChannel mySession = myClient.createSessionChannel(url);
    // mySession.activate("username", "123");
    mySession.activate();
    //////////////////////////////////////

    ///////////// EXECUTE //////////////
    // Browse Root
    BrowseDescription browse = new BrowseDescription();
    browse.setNodeId(Identifiers.RootFolder);
    browse.setBrowseDirection(BrowseDirection.Forward);
    browse.setIncludeSubtypes(true);
    browse.setNodeClassMask(NodeClass.Object, NodeClass.Variable);
    browse.setResultMask(BrowseResultMask.All);
    BrowseResponse res3 = mySession.Browse(null, null, null, browse);
    
//    System.out.println(res3);

    /////////////////////Tags////////////////////////
    BrowseDescription browse2 = new BrowseDescription();
    BrowseResult[] result = res3.getResults();
    ReferenceDescription[] reference = result[0].getReferences();
    NodeId nodeid = new NodeId(reference[0].getNodeId().getNamespaceIndex(),reference[0].getNodeId().getValue().hashCode());
    browse2.setNodeId(nodeid);
    BrowseResponse tagresult = mySession.Browse(null, null, null, browse2);

    //simulation
    BrowseDescription browse_simulation = new BrowseDescription();
    BrowseResult[] result_simulation = tagresult.getResults();
    ReferenceDescription[] reference_simulation = result_simulation[0].getReferences();
    NodeId simulator_nodeid = new NodeId(reference_simulation[2].getNodeId().getNamespaceIndex(),(String)reference_simulation[2].getNodeId().getValue());
    browse_simulation.setNodeId(simulator_nodeid);
    browse_simulation.setBrowseDirection(BrowseDirection.Forward);
    browse_simulation.setIncludeSubtypes(true);
    browse_simulation.setNodeClassMask(NodeClass.Object, NodeClass.Variable);
    browse_simulation.setResultMask(BrowseResultMask.All);
    BrowseResponse simualtion_result = mySession.Browse(null, null, null, browse_simulation);
    
    
    /////////send tag value//////////
    BrowseResult[] simulation = simualtion_result.getResults();
    ReferenceDescription[] simulation_reference = simulation[0].getReferences();
    
    
    
    /////JSON//////
    JSONObject jsonObject = new JSONObject();
    JSONArray TagArray = new JSONArray();
    JSONArray TagValueArray = new JSONArray();
    JSONObject Taginfo = new JSONObject();
    JSONObject Tagvalue = new JSONObject();
    String jsonInfo  = null; 
    for(int i=0;i<simulation_reference.length;i++) {    
    	///////////////////////////////////print node value//////////////////////////////////////////////////
    	NodeId any_nodeid = new NodeId(simulation_reference[i].getNodeId().getNamespaceIndex(),(String)simulation_reference[i].getNodeId().getValue());
   	    ReadResponse any_result = mySession.Read(null, 500.0, TimestampsToReturn.Source,
   	            new ReadValueId(any_nodeid, Attributes.Value, null, null));
   	    Taginfo = new JSONObject();
	    Taginfo.put("DisplayName",simulation_reference[i].getDisplayName().getText());
	    Taginfo.put("IsForward",simulation_reference[i].getIsForward().toString());
	    Taginfo.put("NodeClass",simulation_reference[i].getNodeClass().toString());
	    Taginfo.put("NodeId",simulation_reference[i].getNodeId().toString());

    	DataValue[] oj = any_result.getResults();
    	for(int t=0;t<oj.length;t++) {
    		Tagvalue = new JSONObject();
    		TagValueArray = new JSONArray();
    		if(oj[t].getServerPicoseconds()!=null) {
    			Tagvalue.put("ServerPicoseconds",oj[t].getServerPicoseconds().toString());
    		}else {
    			Tagvalue.put("ServerPicoseconds",oj[t].getServerPicoseconds());
    		}
    		if(oj[t].getServerTimestamp()!=null) {
    			Tagvalue.put("ServerTimestamp",oj[t].getServerTimestamp().toString());
    		}else {
    			Tagvalue.put("ServerTimestamp",oj[t].getServerTimestamp());
    		}
    		if(oj[t].getSourcePicoseconds()!=null) {
    			Tagvalue.put("SourcePicoseconds",oj[t].getSourcePicoseconds().toString());
    		}else {
    			Tagvalue.put("SourcePicoseconds",oj[t].getSourcePicoseconds());
    		}
    		if(oj[t].getSourceTimestamp()!=null) {
    			Tagvalue.put("SourceTimestamp",oj[t].getSourceTimestamp().toString());
    		}else {
    			Tagvalue.put("SourceTimestamp",oj[t].getSourceTimestamp());
    		}
    	    Tagvalue.put("StatusCode",oj[t].getStatusCode().toString());
    	    Tagvalue.put("Value",oj[t].getValue().toString());
    	    System.out.println("Tagvalue : "+Tagvalue);
    	    TagValueArray.add(Tagvalue);
    	    System.out.println("TagValueArray : "+TagValueArray);
    	    Taginfo.put("TagValue : ",TagValueArray);
    	    System.out.println("Taginfo : "+Taginfo);
    	    TagArray.add(Taginfo);
    	    System.out.println("TagArray : "+TagArray);
    	    jsonObject.put("TagS", TagArray);
    	    System.out.println("jsonObject : "+jsonObject);
    	    jsonInfo = jsonObject.toJSONString();
    	}
    	
   	    /////////////////////////////////////////////////////////////////////////////////////////////////////
    	System.out.println("---------------------------------------");
    	if(simulation_reference[i].getNodeClass().getValue()==1) {
    		NodeId result_nodeid = new NodeId(simulation_reference[i].getNodeId().getNamespaceIndex(),(String)simulation_reference[i].getNodeId().getValue());
    		ReadResponse tag_result = mySession.Read(null, 500.0, TimestampsToReturn.Source,
       	            new ReadValueId(result_nodeid, Attributes.Value, null, null));
    		BrowseDescription new_simulation = new BrowseDescription();
    		new_simulation.setNodeId(result_nodeid);
    		new_simulation.setBrowseDirection(BrowseDirection.Forward);
    		new_simulation.setIncludeSubtypes(true);
    		new_simulation.setNodeClassMask(NodeClass.Object, NodeClass.Variable);
    		new_simulation.setResultMask(BrowseResultMask.All);
    		BrowseResponse new_result = mySession.Browse(null, null, null, new_simulation);
    		
    		find_folder(new_result,mySession,tag_result,jsonObject,TagArray,TagValueArray);

    	}else {
    		continue;
    	}
    	
    }
    send_message(jsonInfo);
    System.out.println(jsonInfo);
    

    
    
    /////////////////////////////oracle db/////////////////////////////////////////////
//    String driver = "oracle.jdbc.driver.OracleDriver";
//	String oracleurl = "jdbc:oracle:thin:@192.168.43.232:1521:orcl";
//	String user = "system";
//	String password = "gusql3413";
//	String query = "Select Url from SERVER";
//	
//	Connection con = null;
//	Statement stmt = null;
//	ResultSet rs = null;
//	try {
//		Class.forName(driver);
//		con = DriverManager.getConnection(oracleurl, user, password);
//		
//		stmt = con.createStatement();
//		rs = stmt.executeQuery(query);
//		System.out.println("jdbc driver 로딩 ");
//		if(rs.next()) {
//			String Rabbitmq_URL = rs.getString(0);
//			System.out.println("************************");
//			System.out.println(Rabbitmq_URL);
//			System.out.println("************************");
//		}
//	} catch (ClassNotFoundException e) {
//		System.out.println("jdbc driver 로딩 실패");
//	} catch (SQLException e) {
//		System.out.println("오라클 연결 실패");
//	}
    //////////////////////////////////////////////////////////////////////////////////

    ///////////// SHUTDOWN /////////////
    try {
    	Thread.sleep(3000);
    }catch(Exception e) {
    	
    }
    mySession.close();
    mySession.closeAsync();
    //////////////////////////////////////

  }
  
  public static void send_message(String message) {
	/////////////////////connection Rabbitmq///////////////////////////
	ConnectionFactory factory = new ConnectionFactory();
	factory.setHost("localhost");
	factory.setPort(5672);
	factory.setUsername("root");
	factory.setPassword("1234");
	//send
	try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {

	channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	for(int testnum=0;testnum<message.length();testnum++) {
	channel.basicPublish("", QUEUE_NAME, null,message.toString().getBytes());
	System.out.println(message);
//	System.out.println(" [x] Set '" +message.toString().getBytes() + "'");
	Thread.sleep(3000);
	}
	} catch (TimeoutException e) {
	e.printStackTrace();
	} catch (IOException e) {
	e.printStackTrace();
	} catch (InterruptedException e) {
	e.printStackTrace();
	}

  }

  public static void find_folder(BrowseResponse new_result,SessionChannel mySession,ReadResponse any_result,JSONObject jsonObject,JSONArray TagArray ,JSONArray TagValueArray) throws ServiceFaultException, ServiceResultException {
	    BrowseResult[] result_simulation = new_result.getResults();
	    ReferenceDescription[] reference_simulation = result_simulation[0].getReferences();
	    System.out.println("*******************catch***********************");
	    JSONObject Taginfo = new JSONObject();
	    JSONObject Tagvalue = new JSONObject();
	    for(int i=0;i<reference_simulation.length;i++) {
	    	Taginfo = new JSONObject();
		    Taginfo.put("DisplayName",reference_simulation[i].getDisplayName().getText());
		    Taginfo.put("IsForward",reference_simulation[i].getIsForward().toString());
		    Taginfo.put("NodeClass",reference_simulation[i].getNodeClass().toString());
		    Taginfo.put("NodeId",reference_simulation[i].getNodeId().toString());
		    DataValue[] oj = any_result.getResults();
	    	for(int t=0;t<oj.length;t++) {
	    		Tagvalue = new JSONObject();
	    		TagValueArray = new JSONArray();
	    		if(oj[t].getServerPicoseconds()!=null) {
	    			Tagvalue.put("ServerPicoseconds",oj[t].getServerPicoseconds().toString());
	    		}else {
	    			Tagvalue.put("ServerPicoseconds",oj[t].getServerPicoseconds());
	    		}
	    		if(oj[t].getServerTimestamp()!=null) {
	    			Tagvalue.put("ServerTimestamp",oj[t].getServerTimestamp().toString());
	    		}else {
	    			Tagvalue.put("ServerTimestamp",oj[t].getServerTimestamp());
	    		}
	    		if(oj[t].getSourcePicoseconds()!=null) {
	    			Tagvalue.put("SourcePicoseconds",oj[t].getSourcePicoseconds().toString());
	    		}else {
	    			Tagvalue.put("SourcePicoseconds",oj[t].getSourcePicoseconds());
	    		}
	    		if(oj[t].getSourceTimestamp()!=null) {
	    			Tagvalue.put("SourceTimestamp",oj[t].getSourceTimestamp().toString());
	    		}else {
	    			Tagvalue.put("SourceTimestamp",oj[t].getSourceTimestamp());
	    		}
	    		  Tagvalue.put("StatusCode",oj[t].getStatusCode().toString());
	      	    Tagvalue.put("Value",oj[t].getValue().toString());
	      	    
	      	    TagValueArray.add(Tagvalue);
	    	    Taginfo.put("TagValue",TagValueArray);
	    	    TagArray.add(Taginfo);
	    	    jsonObject.put("TagS", TagArray);
	    	  
	      	    
	    	    String jsonInfo = jsonObject.toJSONString();
		    	send_message(jsonInfo);
		   
	    	}

	    	if(reference_simulation[i].getNodeClass().getValue()==1) {  		
	    		NodeId result_nodeid = new NodeId(reference_simulation[i].getNodeId().getNamespaceIndex(),(String)reference_simulation[i].getNodeId().getValue());
	    		ReadResponse tags_result = mySession.Read(null, 500.0, TimestampsToReturn.Source,
	       	            new ReadValueId(result_nodeid, Attributes.Value, null, null));
	    		BrowseDescription new_simulation = new BrowseDescription();
	    		new_simulation.setNodeId(result_nodeid);
	    		new_simulation.setBrowseDirection(BrowseDirection.Forward);
	    		new_simulation.setIncludeSubtypes(true);
	    		new_simulation.setNodeClassMask(NodeClass.Object, NodeClass.Variable);
	    		new_simulation.setResultMask(BrowseResultMask.All);
	    		BrowseResponse result = mySession.Browse(null, null, null, new_simulation);
	    		find_folder(result,mySession,tags_result,jsonObject,TagArray,TagValueArray);

	    	}else {
	    		continue;
	    	}
	    }
  }

}



//// Read Namespace Array
//ReadResponse res5 = mySession.Read(null, null, TimestampsToReturn.Neither,
//  new ReadValueId(Identifiers.Server_NamespaceArray, Attributes.Value, null, null));
//String[] namespaceArray = (String[]) res5.getResults()[0].getValue().getValue();
//System.out.println(Arrays.toString(namespaceArray));

// Read a variable (Works with NanoServer example!)
//BrowseResult[] Tag_result = simualtion_result.getResults();
//ReferenceDescription[] Tag_Reference = Tag_result[0].getReferences();
//for(int i=0;i<Tag_Reference.length;i++) {
//	  NodeId any_nodeid = new NodeId(Tag_Reference[i].getNodeId().getNamespaceIndex(),(String)Tag_Reference[i].getNodeId().getValue());
//	    ReadResponse any_result = mySession.Read(null, 500.0, TimestampsToReturn.Source,
//	            new ReadValueId(any_nodeid, Attributes.Value, null, null));
////	    System.out.println(any_result);
//}

//ReadResponse res4 = mySession.Read(null, 500.0, TimestampsToReturn.Source,
//  new ReadValueId(new NodeId(3, "Random"), Attributes.Value, null, null));
//System.out.println(res4);

