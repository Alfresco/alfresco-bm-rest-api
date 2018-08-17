### Alfresco Benchmark Rest Api
V1 (and later) ACS REST API load tests

This project provides the creation of multiple scenarios in Alfresco, using V1 (and later) rest api. 
This is aimed at measuring the responsiveness of the system through REST V1 APIs scenarios.

### Get the code

Git:

    git clone https://github.com/Alfresco/alfresco-bm-rest-api.git
    cd alfresco-bm-rest-api

Subversion:

    svn checkout https://github.com/Alfresco/alfresco-bm-rest-api.git
    cd alfresco-bm-rest-api
### Prerequisites
There are a few components we need before we can kick off any tests.

#### 1. Java
Before you can start any form of testing with the Alfresco Benchmark Framework, you need to install Java SDK version 1.7.0_51 or later on the Benchmark Management Manager host and on each host running the Benchmark Driver Server.

#### 2. Maven
Maven is used as the build tool so make sure you have Apache Maven 3 installed. 

#### 3. MongoDB
There also needs to be an instance of MongoDB, version 2.6.3 or later. The Benchmark Framework servers expects the port number to be *27017*.

Additionally, installing **Robo 3T** (MongoDB GUI) is helpfull to see the database connection.

#### 4. Alfresco Benchmark Manager running
A compatible version of the Benchmark Manager is required, running at port 9080.

### Start Alfresco Benchmark Driver

1. Build:
```
    mvn clean install
```
2. Start Driver as follows:

```
    $ mvn clean spring-boot:run -Dmongo.config.host=127.0.0.1
         …
    INFO: Starting ProtocolHandler ["http-bio-9084"]
```

3. Access benchmark server UI:
```
    Browse to http://localhost:9080/alfresco-bm-manager
```
4. Create a Test:
```
    Click [+] if not presented with "Create Test" options.  
    Fill in test details:   
        - Test Name: MyFirstTest01  
        - Test Description: Getting started 
        - Test Definition: alfresco-bm-rest-api-xxx
    Click "Ok".
```
5. Edit test properties:
```
    It is a requirement that all test runs get told where to store the generated results.   
    Change property "mongo.test.host" to your mongo-host (e.g 127.0.0.1:27017)
    Click: "MyFirstTest01" on top left
```
6. Create a Test Run:
```
    Click [+] if not presented with "Create Test Run" options.  
    Fill in test run details:   
        - Test run name: 01     
    Click "Ok".
```
7. Start the Test Run:
```
    Click "Play" button next to Test Run "01".  
    The progress bar will auto-refresh as the test run completion estimate changes.
```
8. Download results:
```
    At any time - usually when the test run completes - click through on the test run.  
    Click the download button and open the CSV file in a spreadsheet.
```

### Contributing guide
Please use [this guide](CONTRIBUTING.md) to make a contribution to the project.
