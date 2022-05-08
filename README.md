# Kafka Streams - Hello World

## Running the project

### Starting Kafka
First, we need to start Kafka. For that we have a [docker-compose.yml file](docker-compose.yml) that will create the necessary resources for us. It will start a Zookeeper instance and a Kafka broker. It will also create the necessary topics using the script found in the [create-topics.sh](./scripts/create-topics.sh) file.

Run the following from WSL because it involves drive mounts:
```shell
cd '/mnt/c/Documents and Settings/mdrrahman/My Documents/GitHub/kafka-streams-word-count'

# Convert via dos2unix
dos2unix scripts/create-topics.sh

# Run containers
docker compose -f ./docker-compose.yml up -d

# Browse to Kafdrop: localhost:19000
```
### Building and starting the Streams application

Run from this devcontainer:
```shell
# Convert via dos2unix
for f in *; do
	dos2unix $f
done

# Build package
clear && mvn clean install && java -jar target/kstream-arc-1.0-SNAPSHOT.jar
```

### Publishing a message

Open a new terminal and connect to the kafka docker container:
```shell
docker exec -it kafka /bin/bash
```
Create a console producer to insert sentences in the `sentences` topic:
```shell
kafka-console-producer --topic sentences --bootstrap-server localhost:9092
```
In your console producer, insert the following messages:
```
>Hello kafka streams
>Hello world
```
In Kafdrop, we see something like this
```
hello : 1
kafka : 1
streams : 1
hello : 2
world : 1
```

## Understanding the Topology
![Topology](docs/topology.png)
1️⃣ **Flat Map** operation to map 1 record into many. In our case, every sentences is mapped into multiple records: one for each word in the sentence. Also the case is lowered to make the process case-insensitive.

2️⃣ **Group By** the stream selecting a grouping key. In our case, the word. This will always return grouped stream, prepared to be aggregated. It will also trigger an operation called **repartition**. We will learn more about this later.

3️⃣ **Count** every appearance of the key in the stream. This will be stored in a **data store**.

Finally, we stream the results into the topic `word-count`. We can stream a table using the method `toStream`, which will stream the latest value that was stored for a given key, everytime that key is updated.
