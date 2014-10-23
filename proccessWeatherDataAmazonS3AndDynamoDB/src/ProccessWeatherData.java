package org.myorg;

import org.myorg.WeatherWritable;
import org.myorg.WeatherStatsWritable;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.dynamodb.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodb.model.AttributeValue;
import com.amazonaws.services.dynamodb.model.ComparisonOperator;
import com.amazonaws.services.dynamodb.model.Condition;
import com.amazonaws.services.dynamodb.model.CreateTableRequest;
import com.amazonaws.services.dynamodb.model.DescribeTableRequest;
import com.amazonaws.services.dynamodb.model.GetItemRequest;
import com.amazonaws.services.dynamodb.model.GetItemResult;
import com.amazonaws.services.dynamodb.model.Key;
import com.amazonaws.services.dynamodb.model.KeySchema;
import com.amazonaws.services.dynamodb.model.KeySchemaElement;
import com.amazonaws.services.dynamodb.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodb.model.PutItemRequest;
import com.amazonaws.services.dynamodb.model.PutItemResult;
import com.amazonaws.services.dynamodb.model.ScanRequest;
import com.amazonaws.services.dynamodb.model.ScanResult;
import com.amazonaws.services.dynamodb.model.TableDescription;
import com.amazonaws.services.dynamodb.model.TableStatus;


public class ProccessWeatherData {

	private static final Double NUMBER_OF_SAMPLES_FOR_EACH_DAY = 24.0;
	private static BasicAWSCredentials credentials = new BasicAWSCredentials("AKIAJDFDOJHFSMBOFFTA", "Y8xj41VbBFzFvi5LonxSFigvOISEwGuKwX0lxxmK");


    private static AmazonS3 s3 = new AmazonS3Client(credentials);
    private static final String s3Path = "s3n://";
    
    private static AmazonDynamoDBClient dynamoDB = new AmazonDynamoDBClient(credentials);
    private static String tableName = "weather";



	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, WeatherWritable> {
		private Text word = new Text();
		/**
		 * @param key is the line number
		 * @param value is the line read
		 * @param output  Text is the key going to be passed, WeatherWritable is the content passed to Reducer
		 */
		public void map(LongWritable key, Text value, OutputCollector<Text, WeatherWritable> output, Reporter reporter) throws IOException {
			try{
				String line = value.toString();
				String[] lineSplited = line.split(","); 

				String city = lineSplited[0];
				String date = lineSplited[1];
				String time = lineSplited[2];
				Double temperature = Double.parseDouble(lineSplited[3]);
				Double humidity = Double.parseDouble(lineSplited[4]);
				Double rainfall = Double.parseDouble(lineSplited[5]);

				WeatherWritable weatherWritable = new WeatherWritable(temperature,humidity,rainfall);
				word.set(city + "," + date); //Create the Key
				output.collect(word, weatherWritable);   //Connect data with Key
			} catch(Exception e){/*If Line is with wrong format "Temos pena"*/}
		}
	}

	public static class Reduce extends MapReduceBase implements Reducer<Text, WeatherWritable, Text, WeatherStatsWritable> {
		public void reduce(Text key, Iterator<WeatherWritable> values, OutputCollector<Text, WeatherStatsWritable> output, Reporter reporter) throws IOException {
			Double maxTemperature = Double.MIN_VALUE;;
			Double minTemperature = Double.MAX_VALUE;;
			Double avgTemperature = 0.0;
			Double maxHumidity = 0.0;
			Double minHumidity = 0.0;
			Double avgHumidity = 0.0;
			Double totalRainfall = 0.0;
			Double numberOfMeasures = 0.0;

			WeatherWritable sample = null;
			while(values.hasNext()){
				sample = values.next();
				numberOfMeasures++;
				if (maxTemperature < sample.getTemperature()){
					maxTemperature = sample.getTemperature();
				}
				if (minTemperature > sample.getTemperature()){
					minTemperature = sample.getTemperature();
				}
				avgTemperature = avgTemperature + sample.getTemperature();

				if (maxHumidity < sample.getTemperature()){
					maxHumidity = sample.getTemperature();
				}
				if (minHumidity < sample.getTemperature()){
					maxHumidity = sample.getTemperature();
				}

				avgHumidity = avgHumidity + sample.getHumidity();
				totalRainfall = totalRainfall + sample.getRainfall();
			}
			avgTemperature = avgTemperature / numberOfMeasures;
			avgHumidity = avgHumidity / numberOfMeasures;
			
			WeatherStatsWritable wsw = new WeatherStatsWritable(maxTemperature,minTemperature,avgTemperature,maxHumidity,minHumidity,avgHumidity,totalRainfall, numberOfMeasures/NUMBER_OF_SAMPLES_FOR_EACH_DAY);
			writeItemOnDynamoDB(key,wsw);
			output.collect(key, wsw);
		}
	}

	public static void main(String[] args) throws Exception {
		// try{
		// 	s3 = new AmazonS3Client(new PropertiesCredentials(
  		// 			   	ProccessWeatherData.class.getResourceAsStream("AwsCredentials.properties")));
		// 	s3.setEndpoint("s3-eu-west-1.amazonaws.com"); 
		// }catch(IOException e){System.out.println(e.getMessage());}
		s3.setEndpoint("s3-us-west-1.amazonaws.com");
		dynamoDB.setEndpoint("dynamodb.us-west-1.amazonaws.com");

		//Input and Output Paths
		if (args.length != 2) {
		 	System.out.println("Arguments should be: <bucketname> <file>");
		 	System.out.println("Example: pwd sample_log_big.txt");
		}

		Date currentDate = new Date();
		String bucketname = args[0];
		String file = args[1];
		String inputFile = s3Path + bucketname + "/" + file;
		String outputFolder = s3Path + bucketname + "/" + "outputPWD-" + currentDate.toString();

		//---

		JobConf conf = new JobConf(ProccessWeatherData.class);
		conf.setJobName("processWeatherDataJob");

		//Mapper Outputs
		conf.setMapOutputKeyClass(Text.class);
        conf.setMapOutputValueClass(WeatherWritable.class);

        //Reducer Outputs
		conf.setOutputKeyClass(Text.class);
		conf.setOutputValueClass(WeatherStatsWritable.class);

		conf.setMapperClass(Map.class);
		//conf.setCombinerClass(Reduce.class);
		conf.setReducerClass(Reduce.class);

		conf.setInputFormat(TextInputFormat.class);
		conf.setOutputFormat(TextOutputFormat.class);

		FileInputFormat.setInputPaths(conf, new Path(inputFile));
		FileOutputFormat.setOutputPath(conf, new Path(outputFolder));

		JobClient.runJob(conf);
	}


	/**************************************************************
	*							Amazon Dynamo					  *
	***************************************************************/



	private static void writeItemOnDynamoDB(Text key, WeatherStatsWritable localWSW){
		dynamoDB.setEndpoint("dynamodb.us-west-1.amazonaws.com");
		//1 Fazer split da key para usar como Two-Part Key na DynamoDB
		String[] cityAndDate = key.toString().split(",");
		String city = cityAndDate[0];
		String date = cityAndDate[1];

		//2 Verificar a completeness (caso contrário, fazer o fetch e após o merge)
		WeatherStatsWritable wswToWriteOnDynamo = null;
		if(localWSW.getCompleteness() < 1.0){
			wswToWriteOnDynamo = mergeWeatherStats(localWSW, readItemFromDynamoDB(city, date));
		}else{
			wswToWriteOnDynamo = localWSW;
		}
		//WeatherStatsWritable wswToWriteOnDynamo = localWSW;

		//3 Escrever na Dynamo
		java.util.Map<String, AttributeValue> item = new HashMap<String, AttributeValue>();
        item.put("city", new AttributeValue().withS(city));
        item.put("date", new AttributeValue().withS(date));
        item.put("maxTemperature", new AttributeValue().withN(wswToWriteOnDynamo.getMaxTemperature().toString()));
        item.put("minTemperature", new AttributeValue().withN(wswToWriteOnDynamo.getMinTemperature().toString()));
        item.put("avgTemperature", new AttributeValue().withN(wswToWriteOnDynamo.getAvgTemperature().toString()));
        item.put("maxHumidity", new AttributeValue().withN(wswToWriteOnDynamo.getMaxHumidity().toString()));
        item.put("minHumidity", new AttributeValue().withN(wswToWriteOnDynamo.getMinHumidity().toString()));
        item.put("avgHumidity", new AttributeValue().withN(wswToWriteOnDynamo.getAvgHumidity().toString()));
        item.put("totalRainfall", new AttributeValue().withN(wswToWriteOnDynamo.getTotalRainfall().toString()));
        item.put("completeness", new AttributeValue().withN(wswToWriteOnDynamo.getCompleteness().toString()));
        PutItemRequest putItemRequest = new PutItemRequest().withTableName(tableName).withItem(item);
        PutItemResult result = dynamoDB.putItem(putItemRequest);
	}


	private static WeatherStatsWritable readItemFromDynamoDB(String city, String date){
		dynamoDB.setEndpoint("dynamodb.us-west-1.amazonaws.com");
		AttributeValue hashKeyElement = new AttributeValue().withS(city);
        AttributeValue rangeKeyElement = new AttributeValue().withS(date);

        Key key = new Key(hashKeyElement, rangeKeyElement);
        GetItemRequest request = new GetItemRequest(tableName, key);
        request.withAttributesToGet(Arrays.asList("city", "date", "minHumidity", "totalRainfall", "maxHumidity", "avgTemperature", "maxTemperature", "minTemperature", "avgHumidity", "completeness"));
        GetItemResult result = dynamoDB.getItem(request);
        java.util.Map<String, AttributeValue> resultMap = result.getItem();

        if (resultMap == null){ //there was no previous occurrence for the giving key and range
        	return null;
        }

        Double maxTemperature = Double.parseDouble(resultMap.get("maxTemperature").getN());
        Double minTemperature = Double.parseDouble(resultMap.get("minTemperature").getN());
        Double avgTemperature = Double.parseDouble(resultMap.get("avgTemperature").getN());
        Double maxHumidity = Double.parseDouble(resultMap.get("maxHumidity").getN());
        Double minHumidity = Double.parseDouble(resultMap.get("minHumidity").getN());
        Double avgHumidity = Double.parseDouble(resultMap.get("avgHumidity").getN());
        Double totalRainfall = Double.parseDouble(resultMap.get("totalRainfall").getN());
        Double completeness = Double.parseDouble(resultMap.get("completeness").getN());

        WeatherStatsWritable readedWSW = new WeatherStatsWritable( maxTemperature,  minTemperature,  avgTemperature,  maxHumidity,  minHumidity,  avgHumidity,  totalRainfall,  completeness);

        return readedWSW;
	}

	private static WeatherStatsWritable mergeWeatherStats(WeatherStatsWritable localWSW, WeatherStatsWritable dynamoWSW){
		//1 Vericar se o dynamoWSW é null, caso sim devolve logo o localWSW
		if (dynamoWSW == null){
			return localWSW;
		}
		WeatherStatsWritable mergedWSW = new WeatherStatsWritable();
		mergedWSW.setMaxTemperature(maxDouble(localWSW.getMaxTemperature()*localWSW.getCompleteness(),localWSW.getMaxTemperature()*dynamoWSW.getCompleteness())); 
		mergedWSW.setMinTemperature(minDouble(localWSW.getMinTemperature()*localWSW.getCompleteness(),localWSW.getMinTemperature()*dynamoWSW.getCompleteness())); 
		mergedWSW.setAvgTemperature(localWSW.getAvgTemperature()*localWSW.getCompleteness() + localWSW.getAvgTemperature()*dynamoWSW.getCompleteness()); 
		mergedWSW.setMaxHumidity(maxDouble(localWSW.getMaxHumidity()*localWSW.getCompleteness(),localWSW.getMaxHumidity()*dynamoWSW.getCompleteness())); 
		mergedWSW.setMinHumidity(minDouble(localWSW.getMinHumidity()*localWSW.getCompleteness(),localWSW.getMinHumidity()*dynamoWSW.getCompleteness())); 
		mergedWSW.setAvgHumidity(localWSW.getAvgHumidity()*localWSW.getCompleteness() + localWSW.getAvgHumidity()*dynamoWSW.getCompleteness()); 
		mergedWSW.setTotalRainfall(localWSW.getTotalRainfall()*localWSW.getCompleteness() + localWSW.getTotalRainfall()*dynamoWSW.getCompleteness()); 
 
		if((localWSW.getCompleteness() + dynamoWSW.getCompleteness()) >= 1.0){
			mergedWSW.setCompleteness(1.0);	
		}else{
			mergedWSW.setCompleteness(localWSW.getCompleteness() + dynamoWSW.getCompleteness());
		}
		return mergedWSW;
	}


    private static Double maxDouble(Double d1, Double d2) {
        if (d1 > d2) {return d1;} else {return d2;}
    }

    private static Double minDouble(Double d1, Double d2) {
        if (d1 < d2) {return d1;} else {return d2;}
    }














}
