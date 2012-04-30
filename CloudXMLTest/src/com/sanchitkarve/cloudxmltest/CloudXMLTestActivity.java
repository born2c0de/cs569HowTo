package com.sanchitkarve.cloudxmltest;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CloudXMLTestActivity extends Activity {
	
	private Button btnGetXML;
	private TextView lblXmlResult;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Call Parent constructor.
    	super.onCreate(savedInstanceState);
    	// Compile UI from main.xml and set it as the UI for this activity.
        setContentView(R.layout.main);
        // Get a reference to the Button from main.xml 
        btnGetXML = (Button)findViewById(R.id.btnGetXML);
        // Add a onClickListner to the button.
        btnGetXML.setOnClickListener(btnGetXMLOnClick);
        // Get a reference to the TextView from main.xml
        lblXmlResult = (TextView)findViewById(R.id.lblXmlResult);
    }
    
    /** Used as the onClickListener for the button. */
    View.OnClickListener btnGetXMLOnClick = new View.OnClickListener() {
		
		@Override
		public void onClick(View v)
		{
			// Display a "toast" notification to inform the user that the file is being downloaded.
			Toast.makeText(getBaseContext(), "Downloading XML File", Toast.LENGTH_SHORT).show();
			// Clear the text present in lblXmlResult TextView.
			lblXmlResult.setText("");
			// Download and Parse the XML present at the cloud's XML web service end-point.
			new CloudAccessTask().execute("http://fantastic4guestbook.appspot.com/xml");			
		}
	};
	
	/** Opens a connection to a URL and returns an InputStream from the connection. */
	private InputStream OpenHttpConnection(String urlString) throws IOException {
			 InputStream in = null;
			 int response = -1;
			  
			 // Open connection to URL
			 URL url = new URL(urlString);			 
			 URLConnection conn = url.openConnection();

			 // Throw exception if connection is not valid HTTP.
			 if (!(conn instanceof HttpURLConnection)) {				 
				 throw new IOException("Not an HTTP connection");
			 }

			 try {
				 // Set the appropriate request headers and make the request.
				 HttpURLConnection httpConn = (HttpURLConnection) conn;
				 httpConn.setAllowUserInteraction(false);
				 httpConn.setInstanceFollowRedirects(true);
				 httpConn.setRequestMethod("GET");				 				 
				 httpConn.connect(); 

				 // Get Response from HTTP Request.
				 response = httpConn.getResponseCode(); 
				 // Get InputStream only if response is OK else throw exception.
				 if (response == HttpURLConnection.HTTP_OK) {
					 in = httpConn.getInputStream();
				 } 
			 }
			 catch (Exception ex) {
				 throw new IOException(ex.getMessage()); 
			 }
			 // Return InputStream
			 return in; 
	}
			 
	
	/** Retrieves XML from Cloud End-point, parses it using DOM and returns a string with formatted data. */
	public String GetAndParseXMLFromCloud(String url) {		
		InputStream in = null;
		String result = "";
		try	{
			// Get InputStream from URL
			in = OpenHttpConnection(url);
			// Set up DOM objects
			Document doc = null;
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = null;
			try	{
				// Create an instance of DocumentBuilder
				db = dbf.newDocumentBuilder();
				// Parse XML from InputStream
				doc = db.parse(in);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			// Normalizes XML file (i.e., places all text-based attribute nodes in the full sub-tree.)
			// Not necessary in HowTo example as all elements are child nodes, but it's good practice to normalize it anyway.
			doc.getDocumentElement().normalize();
			
			// Get a List of all "entry" elements.
			NodeList contactsElements = doc.getElementsByTagName("entry");			
			// For each entry element,
			for(int i=0; i < contactsElements.getLength(); i++)	{
				// Get the ith Entry.
				Element personElement = (Element)contactsElements.item(i);
				// Get the Email from the ith element
				String email = personElement.getElementsByTagName("email").item(0).getChildNodes().item(0).getNodeValue();
				// Only consider email addresses that end with @gmail.com
				if(email.toLowerCase().endsWith("@gmail.com"))
				{
					// Get the Author, Date and Message for the ith elements.
					String author = personElement.getElementsByTagName("author").item(0).getChildNodes().item(0).getNodeValue();
					String date = personElement.getElementsByTagName("date").item(0).getChildNodes().item(0).getNodeValue();
					String message = personElement.getElementsByTagName("message").item(0).getChildNodes().item(0).getNodeValue();
					//Append values from the "author", "email", "date" and "message" elements into a result string.
					result += "Element #" + String.valueOf(i+1) + "\n";
					result += "Author : " + author + ".\n";
					result += "Email : " + email + ".\n";
					result += "Date : " + date + ".\n";
					result += "Message : " + message + ".\n\n";
				}
			}
			
		}
		catch(Exception e) {			
			e.printStackTrace();
		}
		// Return formatted string
		return result;
	}
	
	/** Class used to download and parse XML from the cloud server asynchronously. */
	private class CloudAccessTask extends AsyncTask<String, Void, String> {

		/** Called when CloudAccessTask::execute() is called. Returns formatted XML. */
		@Override
		protected String doInBackground(String... urls)	{
			return GetAndParseXMLFromCloud(urls[0]);
		}
		
		/** Called when the background task is complete. */
		@Override
		protected void onPostExecute(String result) {
			// Display a "toast" notification to inform the user that the XML file has been downloaded and parsed.
			Toast.makeText(getBaseContext(), "XML File Parsed", Toast.LENGTH_LONG).show();
			// Sets the result string as the text of the lblXmlResult TextView.
			lblXmlResult.setText(result);
		}
		
	}
}