<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Results</title>
</head>
<body>
Search results
<br>

<table>
	 <tr> 
	   <th>Rank</th> 
	   <th>URL</th>
	   <th>Score</th>
	 </tr>  
	<c:forEach items="${results}" var="element">
	  <tr> 
	    <td>${element.rank}</td> 
	    <td>${element.url}</td>
	    <td>${element.score}</td>
	  </tr>
	  <br>
	</c:forEach> 
</table>

</body>
</html>