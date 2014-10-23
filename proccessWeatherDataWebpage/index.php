<?php include('factor.php');?>
<!DOCTYPE HTML>
<html lang="en">
<html>
<head>
	<title> Weather App </title>
</head>
<body align="center">
	<h1>Weather App</h1>
	<h2>You are using the Machine with IP:</h2>

	<h2>
	<?php
	echo gethostname();
	?>
	</h2>
	
	<form action="queryDynamo.php" method="POST">
		<p>Where do you want to check?</p>
		City: <input type="text" name="city" value="City Name" /> <br />
		Date: <input type="text" name="date" value="Date (yyyy-mm-dd)" /> <br />
		MaxTemperature: <input type="checkbox" name="maxTemperature" value="maxTemperature"> <br />
     	MinTemperature: <input type="checkbox" name="minTemperature" value="minTemperature"> <br />
     	AvgTemperature: <input type="checkbox" name="avgTemperature" value="avgTemperature"> <br />
     	MaxHumidity: <input type="checkbox" name="maxHumidity" value="maxHumidity"> <br />
     	MinHumidity: <input type="checkbox" name="minHumidity" value="minHumidity"> <br />
     	AvgHumidity: <input type="checkbox" name="avgHumidity" value="avgHumidity"> <br />
     	TotalRainfall: <input type="checkbox" name="totalRainfall" value="totalRainfall"> <br />
     	Completeness: <input type="checkbox" name="completeness" value="completeness"> <br />
		Go Go Go: <input type="submit"  value="Let's check it shall we =)!" /> <br />
	</form>


</body>


</html>
