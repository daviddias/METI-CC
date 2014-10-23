package org.myorg;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.QueryRequest;
import com.amazonaws.services.dynamodb.model.QueryResult;

public class AmazonDynamoDB_TryQuery {

    static AmazonDynamoDBClient dynamoDB;
    static String tableName = "weather";


    public static void main(String[] args) throws Exception {
        // try {
            String city = "Lisboa";
            String date = "2005-01-01";
            createClient();
            readItemFromDynamoDB(city,date);       
        // }  
        // catch (AmazonServiceException ase) {
        //     System.err.println(ase.getMessage());
        // }  
        System.out.println("Finished");
    }

    private static void createClient(){
        BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJDFDOJHFSMBOFFTA", "Y8xj41VbBFzFvi5LonxSFigvOISEwGuKwX0lxxmK");
        dynamoDB = new AmazonDynamoDBClient(credentials);
        dynamoDB.setEndpoint("dynamodb.us-west-1.amazonaws.com");
    }

    private static void readItemFromDynamoDB(String city, String date){
        dynamoDB.setEndpoint("dynamodb.us-west-1.amazonaws.com");
        AttributeValue hashKeyElement = new AttributeValue().withS(city);
        AttributeValue rangeKeyElement = new AttributeValue().withS(date);

        Key key = new Key(hashKeyElement, rangeKeyElement);
        GetItemRequest request = new GetItemRequest(tableName, key);
        request.withAttributesToGet(Arrays.asList("city", "date", "minHumidity", "totalRainfall", "maxHumidity", "avgTemperature", "maxTemperature", "minTemperature", "avgHumidity", "completeness"));
        GetItemResult result = dynamoDB.getItem(request);
        java.util.Map<String, AttributeValue> resultMap = result.getItem();

        if (resultMap == null){ //there was no previous occurrence for the giving key and range
            return;
        }

        Double maxTemperature = Double.parseDouble(resultMap.get("maxTemperature").getN());
        Double minTemperature = Double.parseDouble(resultMap.get("minTemperature").getN());
        Double avgTemperature = Double.parseDouble(resultMap.get("avgTemperature").getN());
        Double maxHumidity = Double.parseDouble(resultMap.get("maxHumidity").getN());
        Double minHumidity = Double.parseDouble(resultMap.get("minHumidity").getN());
        Double avgHumidity = Double.parseDouble(resultMap.get("avgHumidity").getN());
        Double totalRainfall = Double.parseDouble(resultMap.get("totalRainfall").getN());
        Double completeness = Double.parseDouble(resultMap.get("completeness").getN());

        System.out.println("Max Temperature: " + maxTemperature);
        System.out.println("Min Temperature: " + minTemperature);
        System.out.println("Avg Temperature: " + avgTemperature);
        System.out.println("Max Humidity: " + maxHumidity);
        System.out.println("Min Humidity: " + minHumidity);
        System.out.println("Avg Humidity: " + avgHumidity);
        System.out.println("Total Rainfall: " + totalRainfall);
        System.out.println("completeness: " + completeness);
    }
}








