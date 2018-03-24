## Optionally: Build from sources

    git clone http://www.github.com/pravussum/indiepim3
    cd indiepim3
    sudo apt-get install maven
    mvn clean install

You'll find the built JAR file at indiepim3/indieserver/target/indiepimserver-[version].jar
Copy it to your destination dir.

## ... or just download (once a release is ready)
... indiepimserver-[version].jar to your destination dir.
 
## Create a database
Note: currently only mySQL/MariaDB are supported

Create a new database for IndiePIM to use, like so:      
    
    create database indiepim character set utf8mb4 collate utf8mb4_unicode_ci;
    create user 'indiepim'@'localhost' identified by 'password';
    grant all on indiepim.* to 'indiepim'@'localhost'; 
 
    
## Configure
* copy indiepim3/indieserver/application.properties to your destination dir
* adapt it to your needs (database connection and credentials are mandatory)

### Keystore
* TODO
* choose password before first start, or otherwise you have to change the password with e. g. keystore explorer (or delete it if no message accounts have been created yet)    

## Run

Run with
    
    java -jar indiepimsever-[version].jar

The server should come up without errors. Point your browser to http://localhost:8080 to login.

The default credentials are **admin/admin**
 
**Please change your password as soon as you are logged in.**      