### To Compile Server/Client

```
javac --source-path src -d classes src/sg/edu/nus/iss/baccarat/server/*
javac --source-path src -d classes src/sg/edu/nus/iss/baccarat/client/*

```

### To Run Server/Client

```
java -cp classes sg.edu.nus.iss.baccarat.server.ServerApp 12345 4
java -cp classes sg.edu.nus.iss.baccarat.client.ClientApp localhost:12345

```

### To Compile Test

```
javac -cp lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar:src -d classes src/test/AppTest.java
```

### To Run Test

```
java -cp classes:lib/junit-4.13.2.jar:lib/hamcrest-core-1.3.jar org.junit.runner.JUnitCore test.AppTest
```
