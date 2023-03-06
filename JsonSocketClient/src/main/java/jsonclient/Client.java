package jsonclient;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
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
	 List<String> allSmiles = Arrays.asList("cccc_", 
			"",
			"c_",
			"cc_",
			"ccc_",
			"cccc_",
			"ccccc_",
			"cccccc_",
			"ccccccc_",
			"cccccccc_",
			"ccccccccc_",
			"cccccccccc_");
	
	private  List<Task> submitted;
    private  ThreadPoolExecutor tpe;
    private  Map<Task,Future<Object>> futures;
    
    public static void main(String[] args) throws java.io.IOException 
    {	
    	Client c = new Client();
    	c.initialize(args);
    	c.run();
    	System.out.println("END");
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
              
        // by default the ThreadPoolExecutor will throw an exception
        tpe.setRejectedExecutionHandler(new RejectedExecutionHandler()
        {
            public void rejectedExecution(Runnable r, 
                    ThreadPoolExecutor executor)
            {
                try
                {
                    // this will block if the queue is full
                    executor.getQueue().put(r);
                }
                catch (InterruptedException ex)
                {
                    //nothing, really
                }
            }
        });
    }
    
    private void run()
    {   
    	long starttime = System.currentTimeMillis();
        for (int i=0; i<1000; i++)
        {
        	Task task = new Task("Tsk"+i);
        	submitted.add(task);
            futures.put(task, tpe.submit(task));
        }
		tpe.shutdownNow();
    	try {
			tpe.awaitTermination(30, TimeUnit.SECONDS);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	long endttime = System.currentTimeMillis();
    	System.out.println("Runtime: "+(endttime-starttime)/100.0);
    	
    	try {
			if (!tpe.awaitTermination(3, TimeUnit.SECONDS))
			{
			    tpe.shutdownNow(); //Cancel running tasks
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("END TPE");
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

		public Object call() throws Exception 
		{
	    	/*
	    	 * We'll need shotdown hook to close the socket
	    	 * see https://stackoverflow.com/questions/8051863/how-can-i-close-the-socket-in-a-proper-way
	    	 */

			Socket socket = new Socket("localhost", 0xf17);
	        OutputStream outputSocket = socket.getOutputStream();
	        PrintWriter writerToSocket = new PrintWriter(outputSocket, true);
	    	
	        InputStream inputFromSocket = socket.getInputStream();
	        BufferedReader readerFromSocket = new BufferedReader(
	        		new InputStreamReader(inputFromSocket));
		
	        boolean goon= true;
	        int j=0;
	        int i = 0;
	        while (goon)
	        {
	        	j++;
	        	String smiles = allSmiles.get(i);
	        	if (i==(allSmiles.size()-1))
	        		i=0;
	        	else
	        		i++;
	            
		        JsonObject jsonObj = new JsonObject();
		        jsonObj.addProperty("SMILES", smiles);
		        jsonObj.addProperty("Client", name);
		        
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
		        double expected = 0.0;
		        if (answer.has("FITNESS"))
		        {
					double value = Double.parseDouble(answer.get("FITNESS").toString());
		        	val = String.format("%.2f", value);

					expected =  Math.pow(smiles.chars().filter(ch -> ch == 'c').count(),2.5);
					
					if (Math.abs(expected-value)>0.05)
					{
		            	System.out.println("Stopping because "+expected+"!="+value);
		            	System.exit(-1);
		            }	
		        }
		        
	            String taskIdFromServer = answer.get("Client").getAsString();	
		        
	            System.out.println(j + "--> "+taskIdFromServer+" val:" + val);
	            if (!name.equals(taskIdFromServer))
	            {
	            	System.out.println("Stopping because "+name+"!="+taskIdFromServer);
	            	System.exit(-1);
	            }
	            
	        	// This will never be satisfied
	        	//if (answer.equals("NOT_POSSIBLE"))
	        	if (j>0)
	        		goon = false;
	        }
	        socket.close();
			return null;
		}
    }
}