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
    	
    	/*
    	List<String> allSmiles = Arrays.asList("C1CC1", 
    			"CC1=C(C(=O)C[C@@H]1OC(=O)[C@@H]2[C@H](C2(C)C)/C=C(\\C)/C(=O)OC)C/C=C\\C=C", 
    			"CCc(c1)ccc2[n+]1ccc3c2[nH]c4c3cccc4CCc1c[n+]2ccc3c4ccccc4[nH]c3c2cc1", 
    			"[Cu+2].[O-]S(=O)(=O)[O-]",
    			"");
    	*/
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
        
    	for (String smiles : allSmiles) {
            // System.out.println(line);
            /*
        	try { Thread.sleep(500);
            } catch (Throwable t) {t.printStackTrace();}
            */
	        JsonObject jsonObj = new JsonObject();
	        jsonObj.addProperty("SMILES", smiles);
	        jsonObj.addProperty("Client", "A");
	        
	        writerToSocket.println(jsonConverter.toJson(jsonObj));
	        String answer = readerFromSocket.readLine();
            System.out.println("-->"+answer+" for "+jsonObj);
    	}
        
        socket.close();
    }
}