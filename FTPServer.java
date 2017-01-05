package FTP;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;



public class FTPServer {
	
	private static final String CRLF = "\r\n";
    private static final String LF="\n";
	private static int portNum;
	private static boolean loggedIn;
	private static boolean serverReady;
	private static boolean sentUN;
    private static int fileNum; 
	private StringTokenizer tokInput;
	private String input;
	private String myIP;
	public static int replyCode;
	public static String replyText;
	public static int portNum2;
	

	
	public static void main(String args[]) throws Exception {
		sentUN = false;
		String reply = "";

		
		Scanner scanner = new Scanner(System.in);
		
		
		portNum = Integer.parseInt(args[0]);
	
		
		ServerSocket srvr = null;
		try{
			srvr = new ServerSocket(portNum);
		}catch(Exception e){
			portNum = portNum+3+2+3+6;
			srvr = new ServerSocket(portNum);
		}
	    Socket skt = srvr.accept();
	    
	    
	    DataInputStream dIn = new DataInputStream(skt.getInputStream());
	    DataOutputStream dOut = new DataOutputStream(skt.getOutputStream());
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(skt.getInputStream()));
	    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(skt.getOutputStream()));
	    
	    System.out.println("220 COMP 431 FTP server ready.");
	    reply = "220 COMP 431 FTP server ready.";
	    dOut.writeUTF(reply+CRLF);
//	    dOut.flush();
	    
	    
	    boolean done = false;
	     
	    while(!done){
	    	String message = null;
	    	try{
	    		message = br.readLine();
 	    	}catch(Exception e){
 	    		System.out.println("Error");
	    		skt.close();
	    		srvr.close();
	    		try{
	    			srvr = new ServerSocket(portNum);
	    		}catch(Exception ex){
	    			srvr = new ServerSocket(portNum+3236);
	    		}
	    	    skt = srvr.accept();
	    	}
	    	if(message.contains(CRLF)){
	    		message = message.substring(0, message.length()-2);
	    	}
	    	System.out.println(message);
	    	String[] spltMsg = message.split(" ");
	    	String cmd = spltMsg[0];
	    	cmd = cmd.toUpperCase();

	    	switch(cmd){
	    	case "CONNECT": // **************  CONNECT  ******************
	    		skt.close();
	    		srvr.close();
	    		String hostaddr = spltMsg[1];
	    		int newPortNum = Integer.parseInt(spltMsg[2]);
	    		
	    		portNum = newPortNum;
	    		srvr = new ServerSocket(portNum);
	    		skt = srvr.accept();
	    	    reply = "220 COMP 431 FTP server ready.";
				System.out.println(reply);
	    		dIn = new DataInputStream(skt.getInputStream());
	    	    dOut = new DataOutputStream(skt.getOutputStream());
	    	    
	    	    dOut.writeUTF(reply+CRLF);
	    	    dOut.flush();
	    	    break;
	    	 
	    	case "USER": // **************  USER  *********************
	    		String userName = spltMsg[1];
	    		
	    		
	    		if(sentUN){
					reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}else if(userName.contains(" ")){
					reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}else if(!isAsciiPrintable(userName) || userName.contains("\\")){
					reply = "501 Syntax error in parameter.";
					System.out.println(reply);
				}else{
		    		// Valid USER command
					reply = "331 Guest access OK, send password.";
					System.out.println(reply);
					sentUN = true;
				}

//				dOut.flush();
//				dOut.writeUTF(reply+CRLF);
//				dOut.flush();

				bw.write(reply+CRLF);
				bw.flush();
	    		break;
	    		
	    		
	    	case "PASS": // **************  PASS  *********************
	    		message = spltMsg[1];
	    		
	    		if(!sentUN){
					reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}else if(message.contains(" ")){
					reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}else if(!isAsciiPrintable(message)){
					reply = "501 Syntax error in parameter.";
					System.out.println(reply);
				}else{
					// valid PASS parameter
					reply = "230 Guest login OK.";
					System.out.println(reply);
			        sentUN = false;
			        loggedIn = true;
				}
	    		
	    		
	    		bw.write(reply+CRLF);
	    		bw.flush();
//	    		dOut.writeUTF(reply+CRLF);
//		        dOut.flush();
	    		break;
	    		
	    		
	    	case "SYST": // **************  SYST  *********************
	    		
	    		if(!loggedIn){
	            	reply = "530 Not logged in.";
					System.out.println(reply);
				}else if(sentUN){
	            	reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}else{
					reply = "215 UNIX Type: L8.";
					System.out.println(reply);
				}
	    		
	    		bw.write(reply+CRLF);
	    		bw.flush();
//	    		dOut.writeUTF(reply+CRLF);
//	    		dOut.flush();
	    		break;
	    		
	    		
	    	case "TYPE": // **************  TYPE  *********************
	    		message = spltMsg[1];
	    		message = message.toLowerCase();
	    		
	    		if(!loggedIn){
	            	reply = "530 Not logged in.";
					System.out.println(reply);
				}else if(sentUN){
	                reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}else if( !(message.length() == 1) ){
	                reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}
				
				if(message.equals("a")){
	                reply = "200 Type set to A.";
					System.out.println(reply);
				}else if(message.equals("i")){
					reply = "200 Type set to I.";
					System.out.println(reply);
				}
				
				bw.write(reply+CRLF);
				bw.flush();
//				dOut.writeUTF(reply+CRLF);
//				dOut.flush();
				break;
				
				
	    	case "PORT": // **************  PORT  *********************
	    		boolean error = false; 
	    		message = spltMsg[1];
	    		if(!loggedIn){
					reply = "530 Not logged in.";
					System.out.println(reply);
					error = true;
				}else if(sentUN){
					reply = "503 Bad sequence of commands.";
					System.out.println(reply);
					error = true;
				}
				

				List<String> hostPort = Arrays.asList(message.split(","));
				
				// Ensure all parameters are valid
				if(hostPort.size() != 6){
					reply = "501 Syntax error in parameter.";
					System.out.println(reply);
					error = true;
				}
				
				for(String number : hostPort){
					if(!number.matches("[-+]?\\d*\\.?\\d+")){
						reply = "501 Syntax error in parameter.";
						System.out.println(reply);
						error = true;
					}
				}
				
				// Extrapolate Host Address and Port Number
				String hostAddress = hostPort.get(0);
				for(int i=1; i<4; i++){
					hostAddress = hostAddress.concat(".");
					hostAddress = hostAddress.concat(hostPort.get(i));
				}
				
				int pn1 = Integer.parseInt(hostPort.get(hostPort.size()-2));
				int pn2 = Integer.parseInt(hostPort.get(hostPort.size()-1));
				portNum2 = (pn1*256) + pn2;
								
				
				if(!loggedIn){
					reply = "530 Not logged in.";
					System.out.println(reply);
					error = true;
				}
				
				if(!error){
					reply = "200 Port command successful (" + hostAddress + "," + portNum2 + ").";
					System.out.println(reply);
				}
				
				bw.write(reply+CRLF);
				bw.flush();
//				dOut.writeUTF(reply+CRLF);
//				dOut.flush();
				
	    		break;
	    		
	    		
	    	case "RETR": // **************  RETR  *********************
	    		message = spltMsg[1];
	    		if(!loggedIn){
					reply = "530 Not logged in.";
					System.out.println(reply);
				}else if(sentUN){
					reply = "503 Bad sequence of commands.";
					System.out.println(reply);
				}
				
				String pathName = message;
				if(pathName.contains("%s")){
					pathName = pathName.substring(0, pathName.length()-2);
				}else if(!isAsciiPrintable(pathName) || pathName.contains("\\")){
					reply = "501 Syntax error in parameter.";
					System.out.println(reply);
				}else{
					try {
						FileInputStream fis = new FileInputStream(pathName);
					} catch (FileNotFoundException e1) {
						// TODO changed when not sent to client
						reply = "404 Not found.";
						System.out.println(reply);
						bw.write(reply+CRLF);
						bw.flush();
					}
						
					fileNum++;
					
					// check if first or last char is '/' or '\'
					if(pathName.charAt(0) == '/' || pathName.charAt(0) == '\\'){
						pathName = pathName.substring(1, pathName.length());
					}
					
					int l = pathName.lastIndexOf("/");
			
					String pathToFile = null;
					String fileName = pathName.substring(l+1, pathName.length());
					if(l!=-1){
						pathToFile = pathName.substring(0, l);
					}
					
					
					String destFolder = "retr_files";
					Path destPath = FileSystems.getDefault().getPath(destFolder, (pathName));
					
					Path srcPath;
					if(l==-1){
				    	 srcPath = FileSystems.getDefault().getPath(pathName);
				     }else{
				    	 srcPath = FileSystems.getDefault().getPath(pathToFile, fileName);
				     }
					
		             if(Files.exists(srcPath)){
		            	 reply = "150 File status okay.";
		            	 System.out.println(reply);
		            	 dOut.writeUTF(reply+CRLF);
							try {
								Socket sock = new Socket("classroom.cs.unc.edu", portNum2);
								File file = new File(srcPath.toString());
								DataOutputStream out = new DataOutputStream(sock.getOutputStream());
								FileInputStream fis = new FileInputStream(file);
								
								byte[] bts = new byte[16*1024];
								int ipBte;
								while((ipBte=fis.read(bts))>0){
									out.write(bts,0,ipBte);
								}
								out.flush();
								out.close();
								fis.close();
								
						        sock.close();
								
							} catch (IOException e) {
								reply = "425 Can not open data connection.";
								System.out.println(reply);
								dOut.writeUTF(reply+CRLF);
								break;
							}
							
				     }else{
				    	 reply = "550 File not found or access denied.";
							System.out.println(reply);
							bw.write(reply+CRLF);
							bw.flush();
				    	 break;
				     }
					
		             reply = "250 Requested file action completed.";
		             System.out.println(reply);
				}
				
				bw.write(reply+CRLF);
				bw.flush();
//				dOut.writeUTF(reply+CRLF);
				
				break;
				
	    	case "QUIT": // **************  QUIT  *********************
	    		reply = "221 Goodbye.";
				System.out.println(reply);
	    		dOut.writeUTF(reply+CRLF);
	    		skt.close();
	    		srvr.close();
	    		break;	
	    }
	      
	   }
		
	}
		
	//  ************************************
	//	*	        Other  Methods         *
	//	************************************
	
	// Methods to determine if a string is ASCII printable
	 public static boolean isAsciiPrintable(String str) {
	      if (str == null) {
	          return false;
	      }
	      int sz = str.length();
	      for (int i = 0; i < sz; i++) {
	          if (isAsciiPrintable(str.charAt(i)) == false) {
	              return false;
	          }
	      }
	      return true;
	  }
	 
	 public static boolean isAsciiPrintable(char ch) {
	      return ch >= 32 && ch < 127;
	  }
	 
	 // Methods to determine if a string is an int value
	
	public static boolean isInteger(String s) {
	    return isInteger(s,10);
	}

	public static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}
	

}



