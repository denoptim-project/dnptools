This should be done in an environment that includes maven. The dnp_devel from https://github.com/denoptim-project/DENOPTIM/blob/41a53d8b5ba740ce92c4c37899a30011370caefe/environment.yml  can be used for this purpose.


To run the server
```
python python_server/server.py
```

To compile and run the java client
```
cd java_client/
mvn package
java -jar target/JsonSocketClient-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

