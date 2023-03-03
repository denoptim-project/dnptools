// javac Client4.java && java Client4 > result4j.dat 

// https://www.codejava.net/java-se/networking/java-socket-client-examples-tcp-ip

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Arrays;

import java.net.Socket;

public class Client4 {
    public static void main(String[] args) throws java.io.IOException {
/*
        List<String> stringList = Arrays.asList("a_", "cc_", "cca_",
                  "a_", "cc_", "cca_", "a_", "cc_", "cca_");
*/
        List<String> stringList = Arrays.asList("{\"SMILES\": \"a_\"}",
                "{\"SMILES\": \"a_\"}",
                "{\"SMILES\": \"cc_\"}",
                "{\"SMILES\": \"cca_\"}",
                "{\"SMILES\": \"cccc_\"}",
                "{\"SMILES\": \"cccca_\"}",
                "{\"SMILES\": \"cccccc_\"}",
                "{\"SMILES\": \"cccccca_\"}",
                "{\"SMILES\": \"cccccccc_\"}",
                "{\"SMILES\": \"cccccccca_\"}",
                "{\"SMILES\": \"cccccccccc_\"}");

     

        Socket socket = new Socket("localhost", 0xf17);
        OutputStream output = socket.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);

        InputStream input = socket.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));

        for (String line : stringList) {
            try { Thread.sleep(500);
            } catch (Throwable t) {t.printStackTrace();}
            writer.println(line);
            String answer = reader.readLine();
            System.out.println("-->"+answer+" "+line); 
        }

        socket.close();

    }
}

