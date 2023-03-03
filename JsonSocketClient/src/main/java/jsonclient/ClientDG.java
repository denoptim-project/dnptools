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

public class ClientDG {
    public static void main(String[] args) throws java.io.IOException {

        List<String> stringList = Arrays.asList("{\"SMILES\": \"c_\"}",
                "{\"SMILES\": \"\"}",
                "{\"SMILES\": \"cc_\"}",
                "{\"SMILES\": \"ccc_\"}",
                "{\"SMILES\": \"cccc_\"}",
                "{\"SMILES\": \"ccccc_\"}",
                "{\"SMILES\": \"cccccc_\"}",
                "{\"SMILES\": \"ccccccc_\"}",
                "{\"SMILES\": \"cccccccc_\"}",
                "{\"SMILES\": \"ccccccccc_\"}",
                //"",
                "{\"SMILES\": \"cccccccccc_\"}");
        

        long startSocketTime = System.currentTimeMillis();
        
        Socket socket = new Socket("localhost", 0xf17);
        OutputStream outputSocket = socket.getOutputStream();
        PrintWriter writerToSocket = new PrintWriter(outputSocket, true);

        InputStream inputFromSocket = socket.getInputStream();
        BufferedReader readerFromSocket = new BufferedReader(new InputStreamReader(inputFromSocket));

        long endSocketTime = System.currentTimeMillis();
        
        System.out.println("Time to make socket conenction: "+(endSocketTime-startSocketTime)+"ms");
        
        for (String line : stringList) {
            // System.out.println(line);
            /*
        	try { Thread.sleep(500);
            } catch (Throwable t) {t.printStackTrace();}
            */
            writerToSocket.println(line);
            String answer = readerFromSocket.readLine();
            System.out.println("-->"+answer+" for "+line);
        }

        socket.close();

    }
}
