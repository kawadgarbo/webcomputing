package com.shopping;

import java.util.Random;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import com.logistic.Shiporder;
import com.pay.PayInterface;
import com.pay.PayingImplService;
import com.pay.Receipt;

public class ShopMediator {
    private Client client;
    private WebTarget userTarget;
	private PayingImplService service;
	private PayInterface ps;

    private String srcaccnt, destaccnt;
	private Confirm cnfirm;
	
    public ShopMediator() {
    	Random r = new Random(); 
//		//String responsestr = "Dear buyer, xxxxx, your address:";
//      URL wsdlURL = new URL("http://localhost:8080/PayService/?wsdl");
//      //check above URL in browser, you should see WSDL file
//       
//      //creating QName using targetNamespace and name
//      QName qname = new QName("http://pay.com/", "PayingImplService"); 
//       
//      Service service = Service.create(wsdlURL, qname);  
//       
//      //We need to pass interface and model beans to client
//      PayInterface ps = service.getPort(PayInterface.class);
		
        //  Connect to soap based PayService
    	//  PayimgImplService and PayInterface were generated by using
    	//  JDK tool wsimport,
    	//  e.g. wsimport -keep http://localhost:8080/PayService/?wsdl
    	service = new PayingImplService();
    	ps = service.getPayingImplPort();

    	//  Connect to Restful style LogisticService
    	//  Using Jax-RS 2.0 client API,
    	//  first instanciate a Client with ClientBuilder and target to the rest resource,
    	//  then use userTarget.request() to request the resource (refer to doBuy()).
    	client = ClientBuilder.newClient();
        userTarget = client.target("http://localhost:8080/LogisticService/rest/Shipping");
 
        cnfirm = new Confirm();
        srcaccnt = String.valueOf(r.nextInt(99999)+1000000);
        destaccnt = String.valueOf(r.nextInt(999999)+1000000);
    }
    
    public Confirm doBuy(Order order) {
    	Shiporder shiporder = new Shiporder();
    	shiporder.setFromaddress("YF1011,Jiaoda East Road,BJTU");
    	shiporder.setToaddress("SY 201, Jiaoda West Road, BJTU");
        
    	// Request to LogisticService 
    	Entity<Shiporder> entity = Entity.xml(shiporder);
    	shiporder = userTarget.request(MediaType.APPLICATION_XML).post(entity, Shiporder.class);
		System.out.println("mediate to logistic service:" + shiporder.getNotes());

    	// Invoke PayService with PayInterface 
		Receipt rcpt = ps.paying(srcaccnt, destaccnt, Float.valueOf(order.getTotalpay()), order.getNotes());
		System.out.println("mediate to pay service:" + rcpt);
        
		// compose confirm message 
        cnfirm.setOrderid("order-0001");
        cnfirm.setReceipt(rcpt);
        cnfirm.setShiporder(shiporder);
    	
    	return cnfirm;
    }
}
