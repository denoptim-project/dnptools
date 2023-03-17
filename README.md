
## Prepare the Environment
To build the java client we need an environment that includes maven. You can create such environment with conda:
```
conda env create -f environment.yml
```
followed by
```
conda activate json_client-server
```

## Run the example 
To run the server
```
python python_server/server.py
```
Once the server is running, we can compile and run the java client
```
cd java_client/
mvn package
java -jar target/JsonSocketClient-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

