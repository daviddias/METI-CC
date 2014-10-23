package org.myorg;

import org.myorg.WeatherWritable;
import org.myorg.WeatherStatsWritable;
import java.io.IOException;
import java.util.*;


import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

public class ProccessWeatherData {

	private static final Double NUMBER_OF_SAMPLES_FOR_EACH_DAY = 24.0;

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
			output.collect(key, new WeatherStatsWritable(maxTemperature,minTemperature,avgTemperature,maxHumidity,minHumidity,avgHumidity,totalRainfall, numberOfMeasures/NUMBER_OF_SAMPLES_FOR_EACH_DAY));
		}
	}

	public static void main(String[] args) throws Exception {
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

		FileInputFormat.setInputPaths(conf, new Path(args[0]));
		FileOutputFormat.setOutputPath(conf, new Path(args[1]));

		JobClient.runJob(conf);
	}
}
