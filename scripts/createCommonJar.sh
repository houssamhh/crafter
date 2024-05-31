#! /bin/bash

echo "Installing common jar .."
mvn clean install -f commons/.
echo "Done!"
echo "Copying jar to project's resources ..."
cp commons/target/commons-0.0.1-SNAPSHOT-jar-with-dependencies.jar location/resources/.
echo "Done!"
echo "Copying configuration file to project's resources ..."
cp commons/resources/config.properties location/resources/.
echo "Done!"
exit 0
