<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Ad Form</title>
</head>
<body>
<ul>
  <li style="display:inline"><a href="index.html">Text Search</a></li>
  <li style="display:inline"><a href="image_search.html">Image Search</a></li>
  <li style="display:inline"><a href="meta_search.html">Meta Search</a></li>
</ul>
<hr>
<h1 align="center">Ad-Placement</h1>
<h3 align="center">- Enter all the important information here and nothing will stand in the way of YOUR AD -</h3>
<br><br><br>
<br><br><br>
      <form action="placeAd">
      Please register to place your ad <br>
      <table style="width:100%">
		  <tr>
		    <td> </td>
		    <td>First name</td>
		    <td><input type="text" name="firstname"size="30px"><br /></td>
		  </tr>
		  <tr>
		    <td> </td>
		    <td>Last name</td>
		    <td><input type="text" name="lastname"size="30px"><br /></td>
		  </tr>
	 </table>		
			<hr>
		Set the content of your ad<br>
	<table style="width:100%">
		  <tr>
		    <td> </td>
		    <td>Ad URL</td>
		    <td><input type="text" name="adurl"size="100px" placeholder="https://dbis.informatik.uni-kl.de/index.php/en/teaching/winter-21-22/is-project-21-22"><br /></td>
		  </tr>
		  <tr>
		    <td> </td>
		    <td>Ad Text</td>
		    <td><input type="text" name="adtext"size="100px" placeholder="This project is offered in Wintersemester 2021/22. The number of participants is limited. Register now!"><br /></td>
		  </tr>
		  <tr>
		    <td> </td>
		    <td>Image URL (optional)</td>
		    <td><input type="text" name="imageurl"size="100px"><br /></td>
		  </tr>
		  <tr>
		    <td> </td>
		    <td>Enter the terms for which your ad should be displayed separated by ';'</td>
		    <td><input type="text" name="listngrams"size="100px" placeholder="computer science; thesis"><br /></td>
		  </tr>
		  <tr>
		    <td> </td>
		    <td>Total budget for you ad</td>
		    <td><input type="text" name="budget"size="5px" placeholder="10.0">&euro;<br /></td>
		  </tr>
		  <tr>
		    <td> </td>
		    <td>Money you pay per click on your ad</td>
		    <td><input type="text" name="onclick"size="5px" placeholder="0.01">&euro;<br /></td>
		  </tr>
		   <tr>
		    <td> </td>
		    <td>Language of your ad</td>
		    <td><select name="lang" id="language">
		    		<option value="eng">English</option>
		    		<option value="ger">Deutsch</option>
				</select><br /></td>
		  </tr>
	 </table>	
			<br><br>
			<input type="submit" value="place your ad" style="float:right;" >						
		</form>	

</body>
</html>