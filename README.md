# DOS PGP Loader
Uploads public [PGP Canada](https://personalgenomes.ca/data) Data to a DOS Server.

## Usage

First have a dos server running on http://localhost:8101/. These instructions are tested against the
[DNAstack DOS server](https://github.com/DNAstack/GA4GH-DOS-Server) created under Google Summer of Code.

Run the GCS data loader:
```
DOS_SERVER_URL=http://localhost:8101 \
DOS_SERVER_USERNAME=dosadmin \
DOS_SERVER_PASSWORD=dosadmin \
mvn exec:java
```

To see if it worked, execute:
```
$ curl http://localhost:8101/dataobjects
$ curl http://localhost:8101/databundles
```
This should display the objects that have been added to the database
