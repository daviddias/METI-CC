echo "Releasing new Version"
rm -R build/*
rm -R AmazonDynamoDB_TryQuery.jar
#Defining Mainclass in the manifest
echo Main-Class: org.myorg.AmazonDynamoDB_TryQuery > myManifest.txt
echo Class-Path: ./lib/*:./lib/third-party/* >> myManifest.txt

javac -classpath lib/aws-java-sdk-1.3.27.jar -d build src/*
jar -cfm AmazonDynamoDB_TryQuery.jar myManifest.txt -C build .
echo "Release complete"

echo "Starting Test"
#java -jar AmazonDynamoDB_TryQuery.jar org.myorg.AmazonDynamoDB_TryQuery
java -jar AmazonDynamoDB_TryQuery.jar
echo "Test Complete"