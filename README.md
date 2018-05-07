# SOS Contacts
---------------------
This is an Android application that allows users to send an SOS message via SMS automatically by clicking on a notification or button on the home screen.
Firstly, there is a setup that user needs to go through and once this is completed, the application can be used. 

Secondly, the application contains a background service to send the messages; 
initial message followed by updated messages that include updated current location. 

Lastly, It has a fall-back mechanism that will try to send the message to the recipient. 
For instance send via Whatsapp by connecting to a Wi-Fi network if there is no Mobile connection/service,
if there is no Wi-Fi network then try to connect to a nearby peer if there are around and build notification on their mobile stating the situation.


