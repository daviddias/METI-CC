echo "MapReduce Test Init"
hadoop fs -rmr /weather/output
#hadoop jar proccessWeatherData.jar org.myorg.ProccessWeatherData /weather/sample_input_mini.txt /weather/output
#hadoop jar proccessWeatherData.jar org.myorg.ProccessWeatherData /weather/sample_input.txt /weather/output
hadoop jar proccessWeatherData.jar org.myorg.ProccessWeatherData /weather/sample_log_big.txt /weather/output
rm -R output
hadoop fs -get /weather/output output
echo "MapReduce Test Complete"