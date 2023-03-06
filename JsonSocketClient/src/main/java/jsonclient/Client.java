package jsonclient;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    public static void main(String[] args) throws java.io.IOException 
    {	
    	Gson jsonConverter = new GsonBuilder()
    	        .create();
    	
    	List<String> allSmiles = Arrays.asList("c_", 
    			"",
    			"cc_",
    			"ccc_",
    			"cccc_",
    			"ccccc_",
    			"cccccc_",
    			"ccccccc_",
    			"cccccccc_",
    			"ccccccccc_",
    			"cccccccccc_");
    			
    	/*
    	 * We'll need shotdown hook 
    	 * see https://stackoverflow.com/questions/8051863/how-can-i-close-the-socket-in-a-proper-way
    	 */

        Socket socket = new Socket("localhost", 0xf17);
        OutputStream outputSocket = socket.getOutputStream();
        PrintWriter writerToSocket = new PrintWriter(outputSocket, true);
    	
        InputStream inputFromSocket = socket.getInputStream();
        BufferedReader readerFromSocket = new BufferedReader(
        		new InputStreamReader(inputFromSocket));
        
        boolean goon= true;
        int i = 0;
        while (goon)
        {
        	String smiles = allSmiles.get(i);
        	if (i==(allSmiles.size()-1))
        		i=0;
        	else
        		i++;
        	
        	try { Thread.sleep(500);
            } catch (Throwable t) {t.printStackTrace();}
            
	        JsonObject jsonObj = new JsonObject();
	        jsonObj.addProperty("SMILES", smiles);
	        jsonObj.addProperty("Client", "A");
	        
	        writerToSocket.println(jsonConverter.toJson(jsonObj));
	        String answer = readerFromSocket.readLine();
            System.out.println("-->"+answer+" for "+jsonObj);
        	
        	// This will never be satisfied
        	if (answer.equals("NOT_POSSIBLE"))
        		goon = false;
        }
        
        socket.close();
    }
}