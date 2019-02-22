package com.dnastack.pgp;

import com.dnastack.pgp.client.DosClient;
import com.dnastack.pgp.client.PgpWebsiteScraper;
import com.dnastack.pgp.model.Ga4ghDataObject;

import java.io.IOException;
import java.net.URI;
import java.util.List;

public class PGPWrapper 
{
	public static void main( String[] args ) throws IOException
    {
		String serverUrl = requiredEnv("DOS_SERVER_URL");
		if (!serverUrl.endsWith("/")) {
			serverUrl += "/";
		}

		DosClient dosClient = new DosClient(
				URI.create(serverUrl),
				requiredEnv("DOS_SERVER_USERNAME"),
				requiredEnv("DOS_SERVER_PASSWORD"));

    	PgpWebsiteScraper pgpHttp = new PgpWebsiteScraper();

		List<Ga4ghDataObject> allData = pgpHttp.getData();
		System.out.printf("Found %d data objects on PGP website\n\n", allData.size());

		for (Ga4ghDataObject dataObject : allData) {
			dosClient.postDataObject(dataObject);
		}
		System.out.println("Finished posting objects to DOS server\n\n");
		
//		dosClient.postDataBundles(allData, "http://localhost:8080/databundles");
//    	System.out.println("Data Bundles added" + "\n\n");
		
    }


	private static String requiredEnv(String name) {
		String value = System.getenv(name);
		if (value == null) {
			System.err.println("Missing required environment variable " + name);
			System.exit(1);
		}
		return value;
	}

}
