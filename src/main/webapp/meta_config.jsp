<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>

<style>
table, th, td {
  border: 1px solid black;
  width: 25%;
  margin-bottom: 12px;
}
</style>

<ul>
  <li style="display:inline"><a href="index.html">Text Search</a></li>
  <li style="display:inline"><a href="image_search.html">Image Search</a></li>
  <li style="display:inline"><a href="meta_search.html">Meta Search</a></li>
  <li style="display:inline"><a href="AdForm.jsp">Advertising</a></li>
</ul>
<c:if test="${not empty error}"><span>${error}</span></c:if>
<c:if test="${empty error}">
<table id="data-table" width="100%">
    <thead>
        <tr>
        	<th></th>
            <th>Url</th>
            <th>Status</th>
        </tr>
    </thead>
    <tbody>
    	<c:forEach items="${engines}" var="engine">
        	<tr>
        		<td>
					<form action="metaconfig" method="get">
            			<button type="submit" name="delete" value="${engine.id}">&#10006</button>
            		</form>
            	</td>
            	<td>${engine.url}</td>
            	<td>
            		<form action="metaconfig" method="get">
            			<button type="submit" name="toggle" value="${engine.id}">Toggle</button><span> ${engine.status}</span>
            		</form>
            	</td>
        	</tr>
       	</c:forEach> 
    </tbody>
</table>

<form action="metaconfig" method="get">
	<label>Add URL</label>
   	<input type="text" name="engine_url">
   	<button type="submit"> Add </button>
</form>
</c:if>
</body>
</html>