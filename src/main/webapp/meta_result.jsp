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