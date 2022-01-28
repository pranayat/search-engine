<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="ISO-8859-1">
<title>Ad Form</title>
</head>
<body>
      <form action="placeAd">			
			Please register <br>
			Your first Name:<input type="text" name="firstname"size="20px"><br />
			Your last Name:<input type="text" name="lastname"size="20px"><br />
			Ad URL:<input type="text" name="adurl"size="20px"><br />
			Ad Text:<input type="text" name="adtext"size="20px"><br />
			Image URL (optional):<input type="text" name="imageurl"size="20px"><br />
			Money that you pay per click:<input type="text" name="onclick"size="20px"><br />
			Total budget for you ad:<input type="text" name="budget"size="20px"><br />
			For which search queries do you want to show your ad<input type="text" name="listngrams"size="20px"><br />
			<input type="submit" value="submit">						
		</form>	

</body>
</html>