<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Results</title>
</head>
<body style="color:green;font-family: 'Courier New', monospace;">
	<c:if test="${fn:length(results) > 0}"><div style="display:flex;justify-content:center">Showing results for - ${param.querytext}</div></c:if>
	<c:if test="${fn:length(results) == 0}"><div style="display:flex;justify-content:center">No results found for - ${param.querytext}</div></c:if>
	<c:if test="${fn:length(results) > 0}">
		<table style="display:flex;justify-content:center">
			 <tr> 
			   <th>Rank</th> 
			   <th>URL</th>
			   <th>Score</th>
			 </tr>
			<c:forEach items="${results}" var="element">
			  <tr> 
			    <td>${element.rank}</td> 
			    <td><a href="${element.url}">${element.url}</a></td>
			    <td>${element.score}</td>
			  </tr>
			  <br>
			</c:forEach> 
		</table>
	</c:if>
</body>
</html>