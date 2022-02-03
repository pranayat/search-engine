<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Image Results</title>
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
	    .box2{
	        width: 250px; 
	        height:250px;       
	        
	    }    
</style> 
</head>
<ul>
  <li style="display:inline"><a href="index.html">Text Search</a></li>
  <li style="display:inline"><a href="meta_search.html">Meta Search</a></li>
  <li style="display:inline"><a href="AdForm.jsp">Advertising</a></li>
</ul>
<hr>
<form action="search" style="display:flex; justify-content: center;">
		<div style="display: flex; flex-direction: column; align-items: flex-start">
			<div style="display: flex; flex-direction: row; margin: 5px">
				<input type="text" name="query" placeholder="Enter search terms" style="font-size:28px">
				<input type="submit" value="Search">
			</div>
			<div hidden style="display: flex; flex-direction: row; margin: 5px">
				<select hidden name="mode" id="mode">
		    		<option value="image"></option>
				</select>
				<select name="lang" id="language">
		    		<option value="eng">English</option>
		    		<option value="ger">Deutsch</option>
				</select>
			</div>
		</div>		
	</form>
	<hr>
	
<body style="color:green;font-family: 'Courier New', monospace;">
	<c:if test="${fn:length(results) > 0}"><div style="display:flex;justify-content:center">Showing results for - ${param.query}</div></c:if>
	<c:if test="${fn:length(results) == 0}"><div style="display:flex;justify-content:center">No results found for - ${param.query}</div></c:if>
	<div style="display:flex;align-items:center; flex-direction: column">
		<div>Did you mean</div>
		<c:forEach items="${suggestedQueries}" var="suggestedQuery">
			<div><a href="/is-project/search?query=${suggestedQuery}&mode=image">${suggestedQuery}</a></div>
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
		<div style="display:flex;flex-wrap:wrap;justify-content:center">
			<c:forEach items="${results}" var="element">
				<div class="box2">
				<a href="${element.url}">
	        		<img src="${element.url}" class=center;>
	        	</a><br>
	        	${element.score}
	    		</div>
			</c:forEach> 
		</div>
	</c:if>
</body>
</html>