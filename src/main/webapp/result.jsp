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
</style> 

</head>
<body style="font-family: 'Courier New', monospace;">
	<c:if test="${fn:length(results) > 0}"><div style="display:flex;justify-content:center">Showing results for - ${param.query}</div></c:if>
	<c:if test="${fn:length(results) == 0}"><div style="display:flex;justify-content:center">No results found for - ${param.query}</div></c:if>
	<div style="display:flex;align-items:center; flex-direction: column">
		<div>Did you mean</div>
		<c:forEach items="${suggestedQueries}" var="suggestedQuery">
			<div><a href="/is-project/search?query=${suggestedQuery}&lang=${queryLang}&score=1&mode=web">${suggestedQuery}</a></div>
		</c:forEach>
	</div>
	<a href="AdForm.jsp">place an ad</a><br>
	<button> your button that looks like a link</button>
	
	<c:if test="${fn:length(results) > 0}">
		<table style="display:flex;justify-content:center">
			<c:forEach items="${results}" var="element">
			  <tr>
			    <td>
			    	<div style="margin-bottom:10px">
			    		<a href="${element.url}">${element.url}</a>
			    		<div style="width: 30%">${element.snippet}</div>
			    	</div>
			    </td>
			    <td>${element.score}</td>
			  </tr>
			  <br>
			</c:forEach> 
		</table>
	</c:if>
	
	<c:if test="${fn:length(adresults) > 0}">
		<table style="display:flex;justify-content:center">
			<c:forEach items="${adresults}" var="adelement">
			  <tr>
			    <td>
			    	<div style="margin-bottom:10px">
			    	<form action="AdClick">
			    		<button type="submit" name="adbutton" value="${adelement.url}">${adelement.url}</button>
			    		<a href="${adelement.url}">${adelement.url}</a>
			    		<div style="width: 30%">${adelement.text}</div>
			    	</form>
			    	</div>
			    </td>
			  </tr>
			  <br>
			</c:forEach> 
		</table>
	</c:if>
</body>
</html>