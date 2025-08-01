docker-compose up -d

docker exec -it kafka kafka-topics --create --topic chat-messages --bootstrap-server localhost:29092 --partitions 3 --replication-factor 1
docker exec -it kafka kafka-topics --create --topic group-messages --bootstrap-server localhost:29092 --partitions 3 --replication-factor 1

mvn protobuf:compile protobuf:compile-custom

mvn clean compile

mvn spring-boot:run
