echo "Releasing new Version"
rm -R build/*
rm -R proccessWeatherData.jar
#javac -classpath libs/hadoop-core-1.1.1.jar  -d build src/*
javac -classpath /usr/local/Cellar/hadoop/1.1.1/libexec/hadoop-core-1.1.1.jar   -d build src/*
jar -cf proccessWeatherData.jar  -C build .
echo "Release complete"
