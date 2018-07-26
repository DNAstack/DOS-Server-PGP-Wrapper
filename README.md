# DOS-Server-PGP-Wrapper
Uploads public PGP Canada Data to a DOS Server

## Run

First have a dos server running on `localhost:8080`. My implementation can be found [here](https://github.com/ekeilty17/GA4GH-DOS-Server).

Then run this application as a normal java application. The classpath of the main file is `./src/main/java/com/dnastack/pgp/PGPWrapper.java`

To see if it worked, execute:
```linux
$ curl http://localhost:8080/dataobjects
$ curl http://localhost:8080/databundles
```
