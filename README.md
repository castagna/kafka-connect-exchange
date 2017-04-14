# Kafka Connect Exchange Sink Connector (Experimental!)

kafka-connect-exchange is a [Kafka Connector](http://kafka.apache.org/documentation.html#connect) for extracting data from Microsoft Exchange into Kafka.

This is experimental, currently unfinished and untested. Don't use it (yet). 

# TODO

- 

# Running in development

```
mvn clean package
export CLASSPATH="$(find target/ -type f -name '*.jar'| grep '\-package' | tr '\n' ':')"
$CONFLUENT_HOME/bin/connect-standalone $CONFLUENT_HOME/etc/schema-registry/connect-avro-standalone.properties config/ExchangeSourceConnector.properties
```

# Development

To build a development version you'll need a recent version of Apache Kafka. 
You can build kafka-connect-exchange with Maven using the standard lifecycle phases.

# Contribute

If you want to help out with development or testing:

```
git clone https://github.com/castagna/kafka-connect-exchange.git
cd kafka-connect-exchange
mvn package
cp -a target/kafka-connect-exchange-1.0-SNAPSHOT-package/* $CONFLUENT_HOME/
```

- Source Code: https://github.com/castagna/kafka-connect-exchange
- Issue Tracker: https://github.com/castagna/kafka-connect-exchange/issues

# License

The project is licensed under the Apache 2 license.

