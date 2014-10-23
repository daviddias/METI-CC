<?php
// If necessary, reference the sdk.class.php file. 
// For example, the following line assumes the sdk.class.php file is 
// in an sdk sub-directory relative to this file
require_once dirname(__FILE__) . '/sdk-1.5.17.1/sdk.class.php';

// Instantiate the class.
$dynamodb = new AmazonDynamoDB();
$dynamodb->set_region("dynamodb.us-west-1.amazonaws.com");
$table_name = 'weather';

$city = 'Lisboa';
$date = '2005-01-01';


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
	print_r($response);
	
?>
