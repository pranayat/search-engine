package main.java.com.indexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Set;

public class StopwordRemover {
	
	private Set<String> stopwords;
	
	public StopwordRemover() {
		this.stopwords = this.initializeStopwords();
	}
	
	public Set<String> getStopwords(){
		return this.stopwords;
	}
    
    public Set<String> initializeStopwords() {
    	Set<String> stopwords = new HashSet<String>();
        try {
             
            URL url = new URL("http://snowball.tartarus.org/algorithms/english/stop.txt");
            
            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
		    while ((line = in.readLine()) != null) {
		      String[] parts = line.split("[|]");
		      if (!(parts[0] == null) && !(parts[0].length() == 0)) {
		    	  parts[0].toLowerCase();
		    	  stopwords.add(parts[0].trim());
		      }
		    }
            in.close();
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        
        return stopwords;
    }
    
    public Set<String> removeStopwords(String[] text) {
    	Set<String> textwithoutStopwords = new HashSet<String>();
    	for (String term : text) {
    		if (!this.stopwords.contains(term)) {
    			textwithoutStopwords.add(term);
    		}
    	}
    	return textwithoutStopwords;
    }

}
