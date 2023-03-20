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
	 List<String> allSmiles = Arrays.asList("C", "CCO", "CCCO", "CC(C)OC");
	
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
    			.setPrettyPrinting()
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
    	
        for (String smiles : allSmiles)
        {
        	Task task = new Task(smiles);
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
    	System.out.println("Run time: "+(endttime-starttime)/100.0);
    	
    	try {
			if (!tpe.awaitTermination(3, TimeUnit.SECONDS))
			{
			    tpe.shutdownNow(); //Cancel running tasks
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
//------------------------------------------------------------------------------
    
    /*
     * A single-thread task asking for fitness independently on any other thread
     */
    public class Task implements Callable<Object>
    {
    	private String smiles;
    	
    	public Task(String smiles)
    	{
    		this.smiles = smiles;
    	}
    	
		public Object call() throws Exception 
		{

			Socket socket = new Socket("localhost", 0xf17);
	        OutputStream outputSocket = socket.getOutputStream();
	        PrintWriter writerToSocket = new PrintWriter(outputSocket, true);
	    	
	        Runtime.getRuntime().addShutdownHook(new Thread(){public void run(){
	            try {
	                socket.close();
	            } catch (IOException e) { /* failed */ }
	        }});
	        
	        InputStream inputFromSocket = socket.getInputStream();
	        BufferedReader readerFromSocket = new BufferedReader(
	        		new InputStreamReader(inputFromSocket));
		
	        JsonObject jsonObj = new JsonObject();
	        jsonObj.addProperty("SMILES", smiles);
	        
	        String json = jsonConverter.toJson(jsonObj);
	        writerToSocket.println(json);
	        socket.shutdownOutput();
	        
	        JsonObject answer = null;
			try {
				answer = jsonConverter.fromJson(
						readerFromSocket.readLine(), JsonObject.class);
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				System.exit(-1);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			
	        String scoreStr = "none";
	        double expected = 0.0;
	        if (answer.has("SCORE"))
	        {
				double value = Double.parseDouble(answer.get("SCORE").toString());
				scoreStr = String.format("%.2f", value);

				// WARNING: this is an hard-coded calculation of the score 
				// as done in the server.
				expected =  Math.pow(smiles.chars().filter(ch -> ch == 'C').count(),2.5);
				
				if (Math.abs(expected-value)>0.05)
				{
	            	System.out.println("Stopping because "+expected+"!="+value);
	            	System.exit(-1);
	            }	
	        }
	        
            System.out.println("-->  score " + scoreStr + " for " + smiles);
	            
	        socket.close();
			return null;
		}
    }
}