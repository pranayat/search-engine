<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Results</title>
<style type="text/css"> 
	button {
	  background: none!important;
	  border: none;
	  padding: 0!important;
	  /*optional*/
	  font-family: 'Courier New', monospace;
	  /*input has OS specific font-family*/
	  color: #069;
	  text-decoration: underline;
	  cursor: pointer;
	}
	table {
  width: 80%;
  margin-bottom: 0px;
  margin-top:0px;
}
img{
	        max-width: 100%;
	        max-height: 100%;
	        display: block; /* remove extra space below image */
	    }
	 .box1{
	        width: 100px; 
	        height:100px;       
	        
	    }   
</style> 

</head>
<body style="font-family: 'Courier New', monospace;">
<form action="search" style="display:flex; justify-content: center;">
		<div style="display: flex; flex-direction: column; align-items: flex-start">
			<div style="display: flex; flex-direction: row; margin: 5px">
				<input type="text" name="query" placeholder="Enter search terms" style="font-size:28px">
				<input type="submit" value="Search">
			</div>
			<div style="display: flex; flex-direction: row; margin: 5px">
				<select name="lang" id="language">
		    		<option value="eng">English</option>
		    		<option value="ger">Deutsch</option>
				</select>
				<select name="score" id="score">
		    		<option value="1">TF*IDF</option>
		    		<option value="2">Okap BM25</option>
		    		<option value="3">Combined</option>
				</select>
				<select hidden name="mode" id="mode">
		    		<option value="web"></option>
				</select>				
				<a href="image_search.html" style="color:black; margin-left: 4px">Image Search</a>
			</div>
		</div>		
	</form>
	<hr>
	<c:if test="${fn:length(results) > 0}"><div style="display:flex;justify-content:center">Showing results for - ${param.query}</div></c:if>
	<c:if test="${fn:length(results) == 0}"><div style="display:flex;justify-content:center">No results found for - ${param.query}</div></c:if>
	<div style="display:flex;align-items:center; flex-direction: column">
		<div>Did you mean</div>
		<c:forEach items="${suggestedQueries}" var="suggestedQuery">
			<div><a href="/is-project/search?query=${suggestedQuery}&lang=${queryLang}&score=1&mode=web">${suggestedQuery}</a></div>
		</c:forEach>
	</div>
	
	<hr>
	Place your add <a href="AdForm.jsp">here</a>!
	<hr>
	<c:if test="${fn:length(adresults) > 0}">
	<table  style="margin-left:auto;margin-right:auto;">
			<c:forEach items="${adresults}" var="adelement">
			  <tr>
			    	<td>
			    	<form action="AdClick">
			    		Advertisement - <button type="submit" name="adbutton" value="${adelement.url}">${adelement.url}</button>
			    		<div>${adelement.text}</div>
			    	</form>
			    	</td>
			  <c:if test="${not empty adelement.imageurl}">
				  <td>
					  <div class="box1">
				   <img src="${adelement.imageurl}">
				  </div>
					</td>
			  </c:if></tr>
			  </c:forEach> 
	</table>
    </c:if>
	<hr>
	<br>
	<c:if test="${fn:length(results) > 0}">
	<table  style="margin-left:auto;margin-right:auto;">
			<c:forEach items="${results}" var="element">
			  <tr>
			    	<td>
			    	<a href="${element.url}">${element.url}</a>
			    	<div style="width: 40%">${element.snippet}</div>
			    	<br>
			    	</td>
				  <td>
					  ${element.score}
					</td>
			  </tr>
			  </c:forEach> 
	</table>
    </c:if>
</body>
</html>