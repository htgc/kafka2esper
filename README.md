
# kafka2esper

Sample code of Apache Kafka consumer to connect to Esper.

## conf/kafka2esper.properties

```
## For Zookeeper
zookeeper.connect=<zookeeperhost>
zookeeper.session.timeout.ms=400
zookeeper.sync.time.ms=200
auto.commit.interval.ms=1000
group.id=<group.id>

## For Kafka
kafka.topic=<Consume Kafka topic>
consumer.numThreads=4

## Esper
esper.query=<Esper Query(EPL)>

```
