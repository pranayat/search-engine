package com.crawler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;
import org.xml.sax.InputSource;

import com.common.CharacterSanitizer;

public class Page {
	private String pageSource;
    private ArrayList<String> outgoingLinks = new ArrayList<String>();
    private Url url;
    
    public Page (Url url) {
    	this.url = url;
    }
	
	public void setPageSource(String pageSource) {
		// remove comments
		this.pageSource = pageSource.replaceAll("<!--(.*?)-->", " ");;
	}

	public String getPageText() {
		String text = this.pageSource;
		// remove all line breaks, convert to single line source
		text = text.replaceAll("[\r\n]", "");
		// remove all inline styles, lazy regex
		text = text.replaceAll("<style>(.*?)</style>", " ");
		// remove all inline scripts, lazy regex
		text = text.replaceAll("<script>(.*?)</script>", " ");
		// remove all html tags
		text = text.replaceAll("<[^>]*>", " ");	
		// replace multiple white spaces with single white space
		text = text.replaceAll("\\s+"," ");
		
		text = CharacterSanitizer.sanitize(text.toLowerCase());

		return text;
	}
	
	public void indexImages(Connection conn, int docId) throws Exception {
		String text = this.pageSource;
		// remove all line breaks, convert to single line source
		text = text.replaceAll("[\r\n]", "");
		
		// remove all inline styles, lazy regex
		text = text.replaceAll("<style>(.*?)</style>", " ");
		// remove all inline scripts, lazy regex
		text = text.replaceAll("<script>(.*?)</script>", " ");
		// remove all html tags except img, do a negative lookahead for img after encountering <
		text = text.replaceAll("<(?!img)[^>]*>", " ");
		
		// replace multiple white spaces with single white space
		text = text.replaceAll("\\s+"," ");
		
		String imageTag;
		String imageUrl;
		String src = null;
		String title = null;
		String alt = null;
		Pattern srcPattern = Pattern.compile("src=\"(.+?)\"");
		Pattern titlePattern = Pattern.compile("title=\"(.+?)\"");
		Pattern altPattern = Pattern.compile("alt=\"(.+?)\"");
		
		Pattern preTextPattern = Pattern.compile("([^>]{1,500}?)(<img[^>]*>?)");
		Matcher matcher = preTextPattern.matcher(text);
		String preText;
		String[] preTerms;
		Map<String, Image> imageMap = new HashMap<String, Image>();
		
		PreparedStatement pstmtFind = conn.prepareStatement("SELECT language FROM documents WHERE docid = ?");
		pstmtFind.setInt(1, docId);
		ResultSet rs = pstmtFind.executeQuery();
		String docLanguage = "eng";
		if (rs.next()) {
			docLanguage = rs.getString("language");
		}
		
		while(matcher.find()) {
			preText = CharacterSanitizer.sanitize(matcher.group(1).toLowerCase());
			preTerms = preText.trim().split("\\s+");
			imageTag = matcher.group(2);

			Matcher srcMatcher = srcPattern.matcher(imageTag);
			if (srcMatcher.find()) {
				src = srcMatcher.group(1);
			}
			Matcher titleMatcher = titlePattern.matcher(imageTag);
			if (titleMatcher.find()) {
				title = CharacterSanitizer.sanitize(srcMatcher.group(1).toLowerCase());
			}
			Matcher altMatcher = altPattern.matcher(imageTag);
			if (altMatcher.find()) {
				alt = CharacterSanitizer.sanitize(altMatcher.group(1).toLowerCase());
			}
			
			// terms towards the end are nearer to the image, so reverse the term array
			List<String> orderedTerms = Arrays.asList(preTerms);
		    Collections.reverse(orderedTerms);

		    Image image = null;
		    try {
		    	image = new Image(new Url(src, this.url), title, alt);		    	
		    	image.setPreTerms(orderedTerms.toArray(new String[0]));
		    	imageMap.put(src, image);
		    } catch (Exception e) {
		    	// skip this image
		    	continue;
		    }
 		}
		
		Pattern postTextPattern = Pattern.compile("(<img[^>]*>)([^<]{1,500})");
		matcher = postTextPattern.matcher(text);
		String postText;
		String[] postTerms;
		while(matcher.find()) {
			postText = CharacterSanitizer.sanitize(matcher.group(2).toLowerCase());
			postTerms = postText.trim().split("\\s+");
			imageTag = matcher.group(1);

			Matcher srcMatcher = srcPattern.matcher(imageTag);
			if (srcMatcher.find()) {
				src = srcMatcher.group(1);
			}
			Matcher titleMatcher = titlePattern.matcher(imageTag);
			if (titleMatcher.find()) {
				title = CharacterSanitizer.sanitize(srcMatcher.group(1).toLowerCase());
			}
			Matcher altMatcher = altPattern.matcher(imageTag);
			if (altMatcher.find()) {
				alt = CharacterSanitizer.sanitize(altMatcher.group(1).toLowerCase());
			}
			
			Image image = imageMap.get(src);
			
			if (image != null) {
				image.setPostTerms(postTerms);
				image.index(conn, docId, docLanguage);				
			}
 		}		
		
//		([>]*.{1,500}?)(<img[^>]*>?) - to capture text before image, capture max 500 letters before image till > tag is reached
// 		(<img[^>]*>?)([>]*.{1,500}?) - to capture text after image, capture max 500 letters after image till > tag is reached
	}
	
	private Boolean isLinkCrawlable (String link) {
		// remove media links
		String[] splitLink = link.split("\\.");
		String extensionCandidate = splitLink.length > 1 ? splitLink[splitLink.length - 1] : "";
		
		// TODO better to test for positive cases .html than reject negative cases
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
