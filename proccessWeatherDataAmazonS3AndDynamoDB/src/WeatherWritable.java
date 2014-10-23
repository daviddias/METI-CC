package org.myorg;

import java.io.IOException;
import java.util.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;


public class WeatherWritable implements Writable{
	
	private Double temperature;
    private Double humidity;
    private Double rainfall;

    public WeatherWritable() {
        super();
        this.temperature = 0.0;
        this.humidity = 0.0;
        this.rainfall = 0.0;
    }

    public WeatherWritable(Double temperature, Double humidity, Double rainfall) {
        super();
        this.temperature = temperature;
        this.humidity = humidity;
        this.rainfall = rainfall;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        temperature = in.readDouble();
        humidity = in.readDouble();
        rainfall = in.readDouble();
        return;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(temperature);
        out.writeDouble(humidity);
        out.writeDouble(rainfall);       
        return;
    }

    @Override
    public String toString() {
        return temperature + "," + humidity + "," + rainfall;
    }

    /****** Getters and Setters *****/
    
    public Double getTemperature(){
    	return temperature;
    }

    public Double getHumidity(){
    	return humidity;
    }
    
    public Double getRainfall(){
    	return rainfall;
    }

}
