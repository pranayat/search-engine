<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Merged Search Results</title>
</head>
<body style="font-family: 'Courier New', monospace;">
	<ul>
	  <li style="display:inline"><a href="index.html">Text Search</a></li>
	  <li style="display:inline"><a href="image_search.html">Image Search</a></li>
	  <li style="display:inline"><a href="AdForm.jsp">Advertising</a></li>
	</ul>
	<hr>
	<form action="metasearch" style="display:flex; justify-content: center;">
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
			</div>
			<div style="display: flex; flex-direction: row; margin: 5px">
				<input type="checkbox" id="c1" name="c1" value="true">
				<label for="c1"> Group 1</label><br>
				<input type="checkbox" id="c2" name="c2" value="true">
				<label for="c2"> Group 2</label><br>
				<input type="checkbox" id="c3" name="c3" value="true">
				<label for="c3"> Group 3</label><br>
			</div>
		</div>		
	</form>
	<hr>
	<c:if test="${fn:length(results) > 0}"><div style="display:flex;justify-content:center">Showing results for - ${param.query}</div></c:if>
	<c:if test="${fn:length(results) == 0}"><div style="display:flex;justify-content:center">No results found for - ${param.query}</div></c:if>
	
	<c:if test="${fn:length(results) > 0}">
		<table style="display:flex;justify-content:center">
			<c:forEach items="${results}" var="element">
			  <tr>
			    <td>${element.sourceCollection}: </td>
			    <td>
			    	<div style="margin-bottom:10px">
			    		<a href="${element.url}">${element.url}</a>
			    	</div>
			    </td>
			    <td>${element.score}</td>
			  </tr>
			</c:forEach> 
		</table>
	</c:if>
</body>
</html>