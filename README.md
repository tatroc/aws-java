# AWSConsoleApp
This command line application creates and deletes AWS Hardware VPN connections
It is useful for home network connections to AWS. 
For example, I use this application to quickly build a ipsec tunnel from my pfsense router to AWS.
When I'm done with the tunnel I run the CLI app to delete the tunnel thus avoding charges keeping it up


Parameters
-vt ipsec.1
-vgw vgw-eca54d85
-cgw cgw-c16e87a8
-r 10.77.77.0/24,172.16.1.0/24,172.18.1.0/24,10.66.66.0/24,10.8.1.0/24
-vi c:\\temp\\customerGatewayInfo.xml

Example: 

#!/bin/bash
java -jar aws-site-to-site-vpn.jar -vt ipsec.1 -vgw vgw-eca54d85 -cgw cgw-c16e87a8 -r 10.77.77.0/24,172.16.1.0/24,172.18.1.0/24,10.66.66.0/24,10.8.1.0/24 -vi c:/temp/customerGatewayInfo.xml
