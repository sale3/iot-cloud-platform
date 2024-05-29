#!/bin/bash

# READ EVERYTHING

# INFO====================================================================================================================
# By default, this script runs the whole system. Idea behind it is to allow easy way to run all pieces on the same
# machine, which is useful for development. Parts of the script can be commented if only certain portions of the
# system are to be run. Since it assumes that everything is run on the same machine, it is not intended for use
# outside of development, where system would be distributed.

# This script assumes specific directory/file structure when it is executed. It looks like this:
#	start.sh
#		app
#			iot-cloud-platform
#				app.jar
#				logback.xml
#			iot-cloud-dashboard
#				<all contents from iot-cloud-dashboard repository>
#			iot-gateway-app
#				<only contents of src folder from iot-gateway-app repository>
#			mqtt-conf
#				gateway-cloud-broker.conf
#				gateway-periferals-broker.conf

# Notes on these files and directories:
#	app.jar - This file is a renamed SNAPSHOT jar file that is produced when building iot-cloud-platform with Maven
#		  package option. File is usually located in 'target' folder. If not already present, it can be built
#		  in intellij with: View -> Tool Windows -> Maven -> Expand <project name> -> Expand Lifecycle -> Package.
#		  After building, rename file to 'app.jar' and copy it to indicated place in assumed directory tree.
#
#	logback.xml - This file is already present in iot-cloud-platform repository and should just be copied.
#
#	iot-cloud-dashboard - Contains everything from iot-cloud-dashboard repository.
#			      Additionally, when script is run it should contain 'node-modules' folder.
#                             For this, run 'npm install' and 'npm install react-scripts' in folder where package.json
#			      is located.
#	gateway-cloud-broker.conf - Configuration for broker located between gateway and cloud.
#	gateway-periferals-broker.conf - Configuration for broker located between gateway and sensors/periferals.

# There are 4 parts to this script:
#	INFO    - This part that gives more information about the whole system.
# 	INSTALL - Information about things that need to be installed.
#	SETUP   - Everything that needs to be set up before system (at its current form) is run.
#	RUN     - Runs all pieces of the system.


# INSTALL=================================================================================================================
# Prerequisites are provided in README
# For convenience, it is possible to uncomment specific prerequisites here.

# sudo apt update

# Install nodejs and npm for dashboard
# sudo apt install nodejs
# sudo apt install npm
# npm install react-scripts (in folder where package.json is located)

# Install jre/jdk 17 for cloud .jar app
# sudo apt install openjdk-17-jdk

# Install postgres
# sudo apt install postgres

# Install python3 and pip3
# sudo apt install python3
# sudo apt install python3-pip

# Install required python packages
# pip install -r requirements.txt

# Install mosquitto
# sudo apt install mosquitto

# SETUP===================================================================================================================
echo "Setup started"

echo "    Closing mosquitto background service"
# Stop automatic mosquitto service
# This script line also acts as sudo forcing point
sudo systemctl stop mosquitto

echo "    Setting up environment variables"
# Setup env vars for cloud to use in order to connect to database
export POSTGRESQL_URL=jdbc:postgresql://localhost:5432/iot-platform-database
export POSTGRESQL_USER=postgres
export POSTGRESQL_PASSWORD=mysecretpassword
export HISTORY=1
# Setup cloud mqtt client id
export CLOUD_MQTT_CLIENT_ID=my_test_cloud_client
# Setup env vars for react app
export REACT_APP_API_URL="http://localhost:8080/iot-cloud-platform"

echo "    Setting up mosquitto broker users"
# Setup users folder for mosquitto broker so that it matches with given mqtt configurations
sudo cp app/mqtt-conf/users /etc/mosquitto/users

echo "    Setting up database"
# Setup 'iot-platform-database'
sudo -u postgres psql -c "CREATE DATABASE \"iot-platform-database\"" > /dev/null 2> /dev/null
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE \"iot-platform-database\" TO postgres" > /dev/null 2> /dev/null

# RUN=====================================================================================================================
echo "Run started"

# In the case of testing or if something is not working, remove redirections to /dev/null

echo "    Starting database service"
# Run database service
sudo systemctl start postgresql

echo "    Starting mqtt brokers"
# Run mqtt brokers
pushd app/mqtt-conf > /dev/null
sudo mosquitto -c gateway-cloud-broker.conf &
sudo mosquitto -c gateway-periferals-broker.conf &
popd > /dev/null

echo "    Starting sensors"
# Run sensors
pushd app/iot-gateway-app > /dev/null
python3 sensor_devices.py > /dev/null &
popd > /dev/null

echo "    Starting cloud platform"
# Run cloud platform
pushd app/iot-cloud-platform > /dev/null
java -jar app.jar 2> /dev/null &
popd > /dev/null

echo "    Starting gateway rest server"
# Run gateway restapi
pushd app/iot-gateway-app > /dev/null
python3 rest_api.py > /dev/null 2> /dev/null &

echo "    Starting gateway"
# Run gateway
python3 app.py &
popd > /dev/null

echo "    Starting cloud dashboard"
# Run cloud dashboard
pushd app/iot-cloud-dashboard > /dev/null
npm start > /dev/null 2> /dev/null &
popd > /dev/null

echo "All cogs are in motion..."

