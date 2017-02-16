/*
 * Copyright 2010-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateVpnConnectionRequest;
import com.amazonaws.services.ec2.model.CreateVpnConnectionResult;
import com.amazonaws.services.ec2.model.CreateVpnConnectionRouteRequest;
import com.amazonaws.services.ec2.model.CreateVpnConnectionRouteResult;
import com.amazonaws.services.ec2.model.DeleteVpnConnectionRequest;
import com.amazonaws.services.ec2.model.DeleteVpnConnectionResult;
import com.amazonaws.services.ec2.model.DescribeVpnConnectionsResult;
import com.amazonaws.services.ec2.model.VpnConnection;
import com.amazonaws.services.ec2.model.VpnConnectionOptionsSpecification;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner; 

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;


import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

//import org.apache.commons.cli.Options;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;


public class AwsConsoleApp {

    /*
     * Before running the code:
     *      Fill in your AWS access credentials in the provided credentials
     *      file template, and be sure to move the file to the default location
     *      (C:\\Users\\tatroc\\.aws\\credentials) where the sample code will load the
     *      credentials from.
     *      https://console.aws.amazon.com/iam/home?#security_credential
     *
     */

    static AmazonEC2      ec2;
    private static final Logger log = Logger.getLogger(AwsConsoleApp.class.getName());
    static Options options = new Options();
    
    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    private static void init() throws Exception {

        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (C:\\Users\\tatroc\\.aws\\credentials).
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\tatroc\\.aws\\credentials), and is in valid format.",
                    e);
        }
        ec2 = new AmazonEC2Client(credentials);
    }

    //http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/services/ec2/AmazonEC2.html#createVpnConnection-com.amazonaws.services.ec2.model.CreateVpnConnectionRequest-
   

    public static void main(String[] args) throws Exception {

        System.out.println("===========================================");
        System.out.println("Welcome to the AWS VPN connection creator");
        System.out.println("===========================================");

        init();
        List<String> CIDRblocks = new ArrayList<String>();
    	String vpnType = null;
        String vpnGatewayId = null;
        String customerGatewayId = null;
        String customerGatewayInfoPath = null;
        String routes = null;
        
		options.addOption("h", "help", false, "show help.");
		options.addOption("vt", "vpntype", true, "Set vpn tunnel type e.g. (ipec.1)");
		options.addOption("vgw", "vpnGatewayId", true, "Set AWS VPN Gateway ID e.g. (vgw-eca54d85)");
		options.addOption("cgw", "customerGatewayId", true, "Set AWS Customer Gateway ID e.g. (cgw-c16e87a8)");
		options.addOption("r", "staticroutes", true, "Set static routes e.g. cutomer subnet 10.77.77.0/24");
		options.addOption("vi", "vpninfo", true, "path to vpn info file c:\\temp\\customerGatewayInfo.xml");
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		// Parse command line options
		try {
			cmd = parser.parse(options, args);

			if (cmd.hasOption("h"))
				help();

			if (cmd.hasOption("vt")) {
				log.log(Level.INFO, "Using cli argument -vt=" + cmd.getOptionValue("vt"));
				
				vpnType = cmd.getOptionValue("vt");
				
			// Whatever you want to do with the setting goes here
			} else {
				log.log(Level.SEVERE, "Missing vt option");
				help();
			}
			
			if (cmd.hasOption("vgw")) {
				log.log(Level.INFO, "Using cli argument -vgw=" + cmd.getOptionValue("vgw"));
				vpnGatewayId = cmd.getOptionValue("vgw");
			} else {
				log.log(Level.SEVERE, "Missing vgw option");
				help();
			}
			
			if (cmd.hasOption("cgw")) {
				log.log(Level.INFO, "Using cli argument -cgw=" + cmd.getOptionValue("cgw"));
				customerGatewayId = cmd.getOptionValue("cgw");
				
			} else {
				log.log(Level.SEVERE, "Missing cgw option");
				help();
			}
			
			if (cmd.hasOption("r")) {
				log.log(Level.INFO, "Using cli argument -r=" + cmd.getOptionValue("r"));
				routes =  cmd.getOptionValue("r");
				
				String [] routeItems = routes.split(",");
				CIDRblocks = Arrays.asList(routeItems);
					
			} else {
				log.log(Level.SEVERE, "Missing r option");
				help();
			}
			
			if (cmd.hasOption("vi")) {
				log.log(Level.INFO, "Using cli argument -vi=" + cmd.getOptionValue("vi"));
				customerGatewayInfoPath = cmd.getOptionValue("vi");
				
			} else {
				log.log(Level.SEVERE, "Missing vi option");
				help();
			}
			
				
		} catch (ParseException e) {
			log.log(Level.SEVERE, "Failed to parse comand line properties", e);
			help();
		}
		 
		
        
        /*
         * Amazon VPC
         * Create and delete VPN tunnel to customer VPN hardware
         */
        try {


        	//String vpnType = "ipsec.1";
            //String vpnGatewayId = "vgw-eca54d85";
            //String customerGatewayId = "cgw-c16e87a8";
            //List<String> CIDRblocks = new ArrayList<String>();
            //CIDRblocks.add("10.77.77.0/24");
            //CIDRblocks.add("172.16.1.0/24");
            //CIDRblocks.add("172.18.1.0/24");
            //CIDRblocks.add("10.66.66.0/24");
            //CIDRblocks.add("10.8.1.0/24");
            
            //String customerGatewayInfoPath = "c:\\temp\\customerGatewayInfo.xml";
            
            Boolean staticRoutesOnly = true;

            List<String> connectionIds = new ArrayList<String>();
            List<String> connectionIdList = new ArrayList<String>();
            
            connectionIdList = vpnExists(connectionIds);
            
            
            if(connectionIdList.size() == 0)
            {
	            CreateVpnConnectionRequest vpnReq = new CreateVpnConnectionRequest(vpnType, customerGatewayId, vpnGatewayId);
	            CreateVpnConnectionResult vpnRes = new  CreateVpnConnectionResult();
	
	            VpnConnectionOptionsSpecification vpnspec = new VpnConnectionOptionsSpecification();
	            vpnspec.setStaticRoutesOnly(staticRoutesOnly);
	            vpnReq.setOptions(vpnspec);
	
	            System.out.println("Creating VPN connection");
	            vpnRes = ec2.createVpnConnection(vpnReq);
	            String vpnConnId = vpnRes.getVpnConnection().getVpnConnectionId();
	            String customerGatewayInfo = vpnRes.getVpnConnection().getCustomerGatewayConfiguration();
	
	            //System.out.println("Customer Gateway Info:" + customerGatewayInfo);
	            
	            // Write Customer Gateway Info to file
	            System.out.println("Writing Customer Gateway Info to file:" + customerGatewayInfoPath);
	            try (PrintStream out = new PrintStream(new FileOutputStream(customerGatewayInfoPath))) {
	                out.print(customerGatewayInfo);
	            }
	            
	            
	            System.out.println("Creating VPN routes");
	            for (String destCIDR : CIDRblocks) 
	            {
	                CreateVpnConnectionRouteRequest routeReq = new CreateVpnConnectionRouteRequest() ;
	                CreateVpnConnectionRouteResult routeRes = new CreateVpnConnectionRouteResult();
	                
	                routeReq.setDestinationCidrBlock(destCIDR);
	                routeReq.setVpnConnectionId(vpnConnId);
	                
	                routeRes = ec2.createVpnConnectionRoute(routeReq);
	            }
	
	            // Parse XML file
	            File file = new File(customerGatewayInfoPath);
	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	            DocumentBuilder db = dbf.newDocumentBuilder();
	            Document document = db.parse(customerGatewayInfoPath);
	            
	            XPathFactory xPathfactory = XPathFactory.newInstance();
	            XPath xpath = xPathfactory.newXPath();
	            XPathExpression exprGetipAddress = xpath.compile("/vpn_connection/ipsec_tunnel/vpn_gateway/tunnel_outside_address/ip_address");
	            NodeList vpnGateway = (NodeList) exprGetipAddress.evaluate(document, XPathConstants.NODESET);     
	            if (vpnGateway != null) 
	            {
	                for (int i = 0; i < vpnGateway.getLength(); i++) {
	                	String vpnGatewayIP = vpnGateway.item(i).getTextContent();
	                	System.out.println("AWS vpnGatewayIP for tunnel " + Integer.toString(i) + " " + vpnGatewayIP);
	                }
	            }
	            
	            System.out.println("==============================================");
	
	            XPathExpression exprGetKey = xpath.compile("/vpn_connection/ipsec_tunnel/ike/pre_shared_key");
	            NodeList presharedKeyList = (NodeList) exprGetKey.evaluate(document, XPathConstants.NODESET);
	            if (presharedKeyList != null) 
	            {
	                for (int i = 0; i < presharedKeyList.getLength(); i++) {
	                	String pre_shared_key = presharedKeyList.item(i).getTextContent();
	                	System.out.println("AWS pre_shared_key for tunnel " + Integer.toString(i) + " " + pre_shared_key);
	                }
	            }
	  
	            System.out.println("Creating VPN creation completed!");
            
            }
            else
            {
            	boolean yn;
            	Scanner scan = new Scanner(System.in);
            	System.out.println("Enter yes or no to delete VPN connection: ");
            	String input = scan.next();
            	String answer = input.trim().toLowerCase();
            	while (true) {
            		  if (answer.equals("yes")) {
            		    yn = true;
            		    break;
            		  } else if (answer.equals("no")) {
            		    yn = false;
            		    System.exit(0);
            		  } else {
            		     System.out.println("Sorry, I didn't catch that. Please answer yes/no");
            		  }
            		}
            	
            	
            	
            	// Delete all existing VPN connections
            	System.out.println("Deleting AWS VPN connection(s)");

				for(String vpnConID : connectionIdList){
                DeleteVpnConnectionResult delVPNres = new DeleteVpnConnectionResult();
                DeleteVpnConnectionRequest delVPNreq = new DeleteVpnConnectionRequest();
                delVPNreq.setVpnConnectionId(vpnConID);

                delVPNres= ec2.deleteVpnConnection(delVPNreq);
                System.out.println("Successfully deleted AWS VPN conntion: " + vpnConID);
                
            	}
            
            }
 
            
            
        } catch (AmazonServiceException ase) {
                System.out.println("Caught Exception: " + ase.getMessage());
                System.out.println("Reponse Status Code: " + ase.getStatusCode());
                System.out.println("Error Code: " + ase.getErrorCode());
                System.out.println("Request ID: " + ase.getRequestId());
        }

    
    }


    private static void help() {
	  // This prints out some help
	  HelpFormatter formater = new HelpFormatter();

	  formater.printHelp("Main", options);
	  System.exit(0);
	 }

    // Check if vpn connections exists
    public static List<String> vpnExists(List<String> connectionIds)
    {
		
        DescribeVpnConnectionsResult descVPNres = new DescribeVpnConnectionsResult(); 
        descVPNres = ec2.describeVpnConnections();
        
        if(descVPNres.getVpnConnections().size() != 0) 
        {
	        for (VpnConnection vpnConns : descVPNres.getVpnConnections()) 
	        {
	        	if(!vpnConns.getState().toLowerCase().contains("deleted"))
	        	{
	        		connectionIds.add(vpnConns.getVpnConnectionId());
	        		String connectionID = vpnConns.getVpnConnectionId();
	        		System.out.println("Found AWS VPN connection ID:" + connectionID);
	        	}
	        }
        }
    	
    	return connectionIds;
    	
    }


}
