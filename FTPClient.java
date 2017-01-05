package FTP;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;





public class FTPClient {
	
	private static StringTokenizer tokInput;
	private static final String CRLF = "\r\n";
	private static int portNum;
	private static int portNum2;
    private static final String LF="\n";
	private static boolean connected;
	private static String myIP;
	private static String host = "classroom.cs.unc.edu"; 
//	private static String host = "localhost"; 

	private static String replyText;
	private static int replyCode;
	
	

	public static void main(String args[]) throws Exception {
		BufferedReader br = null;
		BufferedWriter bw = null;
		try{
			portNum2 = Integer.parseInt(args[0]);
		}catch(Exception e){
		}
		
		Scanner scanner = new Scanner(System.in);
		Socket skt = null;	
		DataOutputStream dOut = null;
		DataInputStream dIn = null;

		
	    boolean done = false;
	    boolean error;
		while(!done){
			
			String input = scanner.nextLine();
			System.out.println(input);
			error = false;
			
			tokInput = new StringTokenizer(input);
			int numTokens = tokInput.countTokens();
			String command = tokInput.nextToken().toLowerCase();
		    
		        
	    
		    switch(command){
			// =================== CONNECT command ================
			case "connect":
				if(connected){
					bw.write(input);
					bw.flush();
					skt.close();
					connected = false;
					Thread.sleep(10);
				}
				if(!tokInput.hasMoreTokens()){
					System.out.printf("ERROR -- server-host.%s",CRLF);
					break;
				}
				String serverHost = tokInput.nextToken();
				for(char ch : serverHost.toCharArray()){
					if(Character.isAlphabetic(ch)){
						if(!isAsciiPrintable(ch)){
							System.out.printf("ERROR -- server-host%s",CRLF);
							error = true;
							break;
						}
						// ch is alphabetic and ASCII printable -- pass
					}else if(Character.isDigit(ch)){
						// ch is numeric -- pass
					}else if(ch == '.'){
						// dot in address -- pass
					}else if(ch == '-'){
						// dash in address -- pass
					}else{
						System.out.printf("ERROR -- server-host%s",CRLF);
						break;
					}
				}
				if(error){ 
					break;
				}
				host = serverHost;
				
				if(!tokInput.hasMoreTokens()){
					System.out.printf("ERROR -- server-host%s",CRLF);
					break;
				}
				
				String serverPort = tokInput.nextToken();
				if(!isInteger(serverPort)){
					System.out.printf("ERROR -- server-port%s",CRLF);
					break;
				}
				
				int portNum = Integer.parseInt(serverPort);
				if(portNum < 0 || portNum > 65535){
					System.out.printf("ERROR -- server-port%s",CRLF);
					break;
				}
				
				
				// CONNECT command is valid syntax
				try{
					skt = new Socket(host, portNum);
				}catch(Exception e){
					System.out.println("CONNECT failed");
					break;
				}
			
				dOut = new DataOutputStream(skt.getOutputStream());
				dIn = new DataInputStream(skt.getInputStream());	
				
				br = new BufferedReader(new InputStreamReader(skt.getInputStream()));
				bw = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
								
				connected = true;
				System.out.println("CONNECT accepted for FTP server at host "+serverHost+" and port "+Integer.toString(portNum));
				
				// retrieve reply information from USER
				String reply = br.readLine();
				while(reply.charAt(0)!='2'){
					reply = reply.substring(1,reply.length());
				}
				int replyCode = Integer.parseInt(reply.substring(0, 3));
				String replyText = reply.substring(4);
				String accOrDen;
				if(replyCode == 220){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
				
				System.out.println("FTP reply "+ "accepted" + " " + accOrDen + ". Text is: " + replyText);
				
				
				// send USER
				System.out.println("USER anonymous");
				bw.write("USER anonymous"+CRLF);
				bw.flush();
//				bw.flush();
				
				// retrieve reply information from USER
				reply = br.readLine();
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 331){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
				
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);
				
				
	
				// send PASS
				System.out.printf("PASS guest@%s",CRLF);
				bw.write("PASS guest@"+CRLF);
				bw.flush();
				
				// retrieve reply information from PASS
				reply = dIn.readLine();
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 230){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
				
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);
	

				// send SYST
				System.out.println("SYST");
				bw.write("SYST"+CRLF);
				bw.flush();
				
				// retrieve reply information from SYST
				reply = br.readLine();
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 215){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
				
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);
				

				// send TYPE
				System.out.println("TYPE I");
				bw.write("TYPE I"+CRLF);
				bw.flush();
				
				// retrieve reply information from TYPE
				reply = dIn.readLine();
				
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 200){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
							
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);
				break;
				
			// =================== GET command ====================
			case "get":
				if(!connected){
					System.out.printf("ERROR -- expecting CONNECT%s",CRLF);
					break;
				}
				
				if(!tokInput.hasMoreTokens()){
					System.out.printf("ERROR -- request%s",CRLF);
					break;
				}
				
				String pathName = tokInput.nextToken();
				
				// GET is valid!
				System.out.printf("GET accepted for "+pathName+"%s",LF);
				InetAddress myInet = null;
				try {
					myInet = InetAddress.getLocalHost();
				} catch (UnknownHostException e) {
					System.out.printf("ERROR -- request%s",CRLF);
					break;
				}
				myIP = myInet.getHostAddress();
				String hostAdr = myIP.replace('.', ',');
				int pn1 = 31;
				int pn2 = portNum2 - (pn1 * 256);

				//send PORT to server
				System.out.printf("PORT "+hostAdr+","+pn1+","+pn2+"%s",CRLF);
				bw.write("PORT "+hostAdr+","+pn1+","+pn2);
				bw.flush();		
				
				//retrieve reply for PORT
				reply = br.readLine();
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 200){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
							
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);
				
				
				
				// send RETR to server
				System.out.printf("RETR "+pathName+"%s",CRLF);
				bw.write("RETR "+pathName);
				bw.flush();
				
				// retrieve reply from RETR
				
				
				reply = dIn.readLine();
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 150 || replyCode == 250){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
					
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);

				if(accOrDen.equals("accepted")){
					break;
				}
				
				// create new socket to retrieve file
				ServerSocket svrSkt = new ServerSocket(portNum2);
				Socket sock = svrSkt.accept();
				
				byte[] bts = new byte[16*1024];
				InputStream is = sock.getInputStream();
				OutputStream opStr = new FileOutputStream(new File(pathName));
				
				int ct = 0;
				while((ct=is.read(bts))>0){
					opStr.write(bts,0,ct);
				}
				
				opStr.close();
				is.close();
				
			      
			    sock.close();
				
				reply = dIn.readLine();
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 150 || replyCode == 250){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
				
				
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);
				
	
							
				break;
				
			// ============== QUIT command ==============
			case "quit":
				if(input.contains(" ")){
					System.out.printf("ERROR -- request%s",CRLF);
					break;
				}else if(!connected){
					System.out.printf("ERROR -- expecting CONNECT%s",CRLF);
					break;
				}else if(tokInput.hasMoreTokens()){
					System.out.printf("ERROR -- request%s",CRLF);
					break;
				} 
				System.out.printf("QUIT accepted, terminating FTP client%s",LF);
				bw.write("QUIT");
				bw.flush();
				
				// read reply for QUIT
				reply = br.readLine();
				
				replyCode = Integer.parseInt(reply.substring(0, 3));
				replyText = reply.substring(4);
				if(replyCode == 221){
					accOrDen = "accepted";
				}else{
					accOrDen = "accepted";
				}
							
				System.out.println("FTP reply "+ replyCode + " " + accOrDen + ". Text is: " + replyText);
				skt.close();
				connected = false;
			
				break;
				
			// ============== Unknown command ===========
			default:
				System.out.printf("ERROR -- request%s",CRLF);
				break;
			}
	    }
	     
	  
	}
	
	
	//	************************************
	//	*	        Other  Methods         *
	//	************************************
	
	
	// Methods to determine if a string is ASCII printable
	 public static boolean isAsciiPrintable(String str){
	      if (str == null){
	          return false;
	      }
	      int sz = str.length();
	      for (int i = 0; i < sz; i++){
	          if (isAsciiPrintable(str.charAt(i)) == false){
	              return false;
	          }
	      }
	      return true;
	  }
	 
	 public static boolean isAsciiPrintable(char ch){
	      return ch >= 32 && ch < 127;
	  }
	 
	 
	 // Method to test if string is int
	 public static boolean isInteger(String s){
	      boolean isValidInteger = false;
	      try{
		     // s is a valid integer
	         Integer.parseInt(s);
	         isValidInteger = true;
	      }
	      catch (NumberFormatException ex){
	         // s is not an integer
	      }
	      return isValidInteger;
	   }

}

	
	
	
	
	


