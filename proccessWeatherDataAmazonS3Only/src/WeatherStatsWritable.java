package org.myorg;

import java.io.IOException;
import java.util.*;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

public class WeatherStatsWritable implements Writable{

	private Double maxTemperature;
	private Double minTemperature;
	private Double avgTemperature;
    private Double maxHumidity;
    private Double minHumidity;
    private Double avgHumidity;
    private Double totalRainfall;
    private Double completeness;

    public WeatherStatsWritable() {
        super();
        this.maxTemperature = 0.0;
        this.minTemperature = 0.0;
        this.avgTemperature = 0.0;
        this.maxHumidity = 0.0;
        this.minHumidity = 0.0;
        this.avgHumidity = 0.0;
        this.totalRainfall = 0.0;
        this.completeness = 0.0;
    }

    public WeatherStatsWritable(Double maxTemperature, Double minTemperature, Double avgTemperature, Double maxHumidity, Double minHumidity, Double avgHumidity, Double totalRainfall, Double completeness) {
        super();
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.avgTemperature = avgTemperature;
        this.maxHumidity = maxHumidity;
        this.minHumidity = minHumidity;
        this.avgHumidity = avgHumidity;
        this.totalRainfall = totalRainfall;
        this.completeness = completeness;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        maxTemperature = in.readDouble();
        minTemperature = in.readDouble();
        avgTemperature = in.readDouble();

        maxHumidity = in.readDouble();
        minHumidity = in.readDouble();
        avgHumidity = in.readDouble();

        totalRainfall = in.readDouble();
        completeness = in.readDouble();
        return;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(maxTemperature);
        out.writeDouble(minTemperature);
        out.writeDouble(avgTemperature);

        out.writeDouble(maxHumidity);
        out.writeDouble(minHumidity);
        out.writeDouble(avgHumidity);

        out.writeDouble(totalRainfall);
        out.writeDouble(completeness);
        return;
    }

    @Override
    public String toString() {
        return maxTemperature + "," + minTemperature + "," + avgTemperature + "," + maxHumidity + "," + minHumidity + "," + avgHumidity + "," + totalRainfall + "," + completeness;
    }	

}