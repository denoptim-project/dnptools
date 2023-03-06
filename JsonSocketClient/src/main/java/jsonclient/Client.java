package jsonclient;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	
	 Gson jsonConverter;
	 String clientID;
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
	
	 Socket socket;
	 PrintWriter writerToSocket;
	 BufferedReader readerFromSocket;
	
	private  List<Task> submitted;
    private  ThreadPoolExecutor tpe;
    private  Map<Task,Future<Object>> futures;
    
    public static void main(String[] args) throws java.io.IOException 
    {	
    	Client c = new Client();
    	c.initialize(args);
    	c.run();
    	c.terminate();
    }
    
    public void initialize(String[] args) throws UnknownHostException, IOException 
    {
    	jsonConverter = new GsonBuilder()
    	        .create();
    	
    	clientID = "Client"+System.currentTimeMillis();
    	if (args.length==1)
    		clientID = args[0];
    	
    	futures = new HashMap<Task,Future<Object>>();
        submitted = new ArrayList<Task>();
    	tpe = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(1));
    	
    	/*
    	 * We'll need shotdown hook to close the socket
    	 * see https://stackoverflow.com/questions/8051863/how-can-i-close-the-socket-in-a-proper-way
    	 */

        socket = new Socket("localhost", 0xf17);
        OutputStream outputSocket = socket.getOutputStream();
        writerToSocket = new PrintWriter(outputSocket, true);
    	
        InputStream inputFromSocket = socket.getInputStream();
        readerFromSocket = new BufferedReader(
        		new InputStreamReader(inputFromSocket));
    }
    
    private void run()
    {   
        for (int i=0; i<4; i++)
        {
        	Task task = new Task("Tsk"+i);
        	submitted.add(task);
            futures.put(task, tpe.submit(task));
        }
    }
    
    public void terminate() throws IOException {
        socket.close();
    }
    
//------------------------------------------------------------------------------
    
    /*
     * A single-thread task asking for fitness independently on any other thread
     */
    public class Task implements Callable<Object>
    {
    	private String name = "noname";
    	public Task(String name) {
    		this.name = name;
    	}

		public Object call() throws Exception {
			keepAskingForFitness(name);
			return null;
		}
    	
    }
    
    private void keepAskingForFitness(String threadID)
    {
        boolean goon= true;
        int i = 0;
        while (goon)
        {
        	String smiles = allSmiles.get(i);
        	if (i==(allSmiles.size()-1))
        		i=0;
        	else
        		i++;
        	
        	try { Thread.sleep(0);
            } catch (Throwable t) {t.printStackTrace();}
            
	        JsonObject jsonObj = new JsonObject();
	        jsonObj.addProperty("SMILES", smiles);
	        jsonObj.addProperty("Client", threadID);
	        
	        writerToSocket.println(jsonConverter.toJson(jsonObj));
	        
	        JsonObject answer = null;
			try {
				answer = jsonConverter.fromJson(
						readerFromSocket.readLine(), JsonObject.class);
			} catch (JsonSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(-1);
			}
	        
	        String val = "none";
	        if (answer.has("FITNESS"))
	        	val = String.format("%.2f", Double.parseDouble(answer.get("FITNESS").toString()));

            String taskIdFromServer = answer.get("Client").getAsString();	
	        
            System.out.println("--> "+taskIdFromServer+" val:" + val);
            if (!threadID.equals(taskIdFromServer))
            {
            	System.out.println("Stopping because "+threadID+"!="+taskIdFromServer);
            	System.exit(-1);
            }
            
        	// This will never be satisfied
        	if (answer.equals("NOT_POSSIBLE"))
        		goon = false;
        }
    }
}