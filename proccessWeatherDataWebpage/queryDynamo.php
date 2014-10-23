<?php
// If necessary, reference the sdk.class.php file. 
// For example, the following line assumes the sdk.class.php file is 
// in an sdk sub-directory relative to this file
require_once dirname(__FILE__) . '/sdk-1.5.17.1/sdk.class.php';

// Instantiate the class.
$dynamodb = new AmazonDynamoDB();
$dynamodb->set_region("dynamodb.us-west-1.amazonaws.com");
$table_name = 'weather';

$city = $_POST['city'];
$date = $_POST['date'];


$response = $dynamodb->query(array(
    'TableName' => $table_name,
    'HashKeyValue' => array(
        AmazonDynamoDB::TYPE_STRING => $city,
    ),
    'RangeKeyCondition' => array(
        'ComparisonOperator' => AmazonDynamoDB::CONDITION_EQUAL,
        'AttributeValueList' => array(
            array(
                AmazonDynamoDB::TYPE_STRING => $date
            )
        )
    )
));

// Response code 200 indicates success
    $items = $response->body->Items;

    echo "<center>";

    echo "<br><strong>Cidade:";
	echo $items->city->S->to_string();
    echo "</strong></br>";
    
    echo "<br><strong>Date:";
    echo $items->date->S->to_string();
    echo "</strong></br>";


    if($_POST['maxTemperature'] == true){
        echo "<br>MaxTemperature:";
        echo $items->maxTemperature->N->to_string();
        echo "</br>";
    };

    if($_POST['minTemperature'] == true){
        echo "<br>MinTemperature:";
        echo $items->minTemperature->N->to_string();
        echo "</br>";
    };

    if($_POST['avgTemperature'] == true){
        echo "<br>AvgTemperature:";
        echo $items->avgTemperature->N->to_string();
        echo "</br>";
    };
	
    if($_POST['maxHumidity'] == true){
        echo "<br>MaxHumidity:";
        echo $items->maxHumidity->N->to_string();
        echo "</br>";
    };

    if($_POST['minHumidity'] == true){
        echo "<br>MinHumidity:";
        echo $items->minHumidity->N->to_string();
        echo "</br>";
    };

    if($_POST['avgHumidity'] == true){
        echo "<br>AvgHumidity:";
        echo $items->avgHumidity->N->to_string();
        echo "</br>";
    };

    if($_POST['totalRainfall'] == true){
        echo "<br>TotalRainfall:";
        echo $items->totalRainfall->N->to_string();
        echo "</br>";
    };

    if($_POST['completeness'] == true){
        echo "<br>Completeness:";
        echo $items->completeness->N->to_string();
        echo "</br>";
    };

    echo "</center>";

    //echo $response->{'url'};
?>
