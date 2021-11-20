package main.java.com.crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Page {
	private String pageSource;
    private ArrayList<String> outgoingLinks = new ArrayList<String>();
    
    public Page () {
    	
    }

	public Page (String pageSource) {
		this.pageSource = pageSource;
	}
	
	public void setPageSource(String pageSource) {
		this.pageSource = pageSource.replace("\n", "").replace("\r", "");
	}

	public String getPageText() {
		String text = this.pageSource;
		// remove all inline styles
		text = text.replaceAll("<style>(.*)</style>", "");
		// remove all inline scripts
		text = text.replaceAll("<script>(.*)</script>", "");
		// remove all html tags
		text = text.replaceAll("<[^>]*>", "");
		return text;
	}
	
	private Boolean isLinkCrawlable (String link) {
		// remove media links
		String[] splitLink = link.split("\\.");
		String extensionCandidate = splitLink.length > 1 ? splitLink[splitLink.length - 1] : "";
		if (extensionCandidate.length() > 0 && Arrays.asList(new String[] {"svg", "ogg", "png", "jpg", "jpeg"}).contains(extensionCandidate)) {
			return false;
		}

		// remove links to elements on the same page
		if (link.substring(0,1).equals("#")) {
			return false;
		}
		
		return true;
	}

	public ArrayList<String> getOutgoingLinks () throws Exception {
		Tidy tidy = new Tidy();
		tidy.setShowWarnings(false);
        tidy.setXmlTags(true);
        tidy.setInputEncoding("UTF-8");
        tidy.setOutputEncoding("UTF-8");
        tidy.setXHTML(true);
        tidy.setMakeClean(true);
//		tidy.setForceOutput(true);
                
        try {
			ByteArrayInputStream inputStream = new ByteArrayInputStream(this.pageSource.getBytes("UTF-8"));
		    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		    tidy.parseDOM(inputStream, outputStream);
		    String xhtmlString = outputStream.toString("UTF-8");
		    
		    XPath xpath = XPathFactory.newInstance().newXPath();
		    XPathExpression xPathExpression = xpath.compile("//a/@href");
		    InputSource xhtmlDoc = new InputSource(new StringReader(xhtmlString));
		    Object result = xPathExpression.evaluate(xhtmlDoc, XPathConstants.NODESET);	    
		    NodeList nodes = (NodeList) result;
		    
		    String link;
		    for (int i = 0; i < nodes.getLength(); i++) {
		    	link = nodes.item(i).getNodeValue();

				if (!this.isLinkCrawlable(link)) {
					continue;
				}
		    	
		    	this.outgoingLinks.add(link);
		    	
		    }
		    
		    return this.outgoingLinks;
        } catch (Exception e) {
        	throw e;
		}
	}

	public ArrayList<String> getOutgoingLinksViaRegex () {
		String regex = "(<a)(.*?)(href=\")(.+?)(\")";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(this.pageSource);
		while (matcher.find()) {
			String link = matcher.group(4);
			
			if (!this.isLinkCrawlable(link)) {
				continue;
			}

			this.outgoingLinks.add(link);
		}
		
		return this.outgoingLinks;
	}
	
	// Not using this for now
	public Boolean isInEnglish() {
		String languageRegex = "(<html .*lang=\")(.+?)(\")";
		Pattern pattern = Pattern.compile(languageRegex);
		Matcher matcher = pattern.matcher(this.pageSource);
		
		if (matcher.find() && Arrays.asList(new String[] {"en", "en-US"}).contains(matcher.group(2))) {
			return true;
		}
		
		return false;
	}
}
