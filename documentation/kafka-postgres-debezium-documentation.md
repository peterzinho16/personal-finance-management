*Shortcut to open MD Preview: SHIFT + CMD + V*

# Setting Up Debezium with PostgreSQL and Kafka (KRaft) on macOS

## **1. Install and Configure Kafka (KRaft Mode)**

### **Install Kafka using Homebrew**

```bash
brew install kafka
```

When installing with homebrew, the installation path will be: `/opt/homebrew/Cellar/kafka/{version}`
*Version in my case is 3.9.0*

### **Initialize Kafka Storage using KRaft**

```bash
KAFKA_UUID=$(kafka-storage random-uuid)
kafka-storage format -t $KAFKA_UUID -c /opt/homebrew/Cellar/kafka/3.9.0/.bottle/etc/kafka/kraft/server.properties
```

### **Start Kafka Server**

```bash
kafka-server-start /opt/homebrew/Cellar/kafka/3.9.0/.bottle/etc/kafka/kraft/server.properties
```

## **2. Install and Configure PostgreSQL**

### **Install PostgreSQL**

```bash
brew install postgresql@17
```

### **Modify `postgresql.conf` to Enable Logical Replication**

Edit the configuration file:

```bash
nano /Library/PostgreSQL/17/data/postgresql.conf
```

Set the following parameters:

```ini
wal_level = logical
max_replication_slots = 4
max_wal_senders = 4
```
*You will see them commented, uncomment the props and modify as described above*

### **Restart PostgreSQL**

```bash
brew services restart postgresql@17
```

## **3. Configure PostgreSQL for Debezium**

This needs to be created on the database you want to connect through Kafka Connect's connector

### **Create a Replication User**

```sql
CREATE USER debezium WITH REPLICATION PASSWORD 'dbz';
```

### **Create a Publication**

```sql
CREATE PUBLICATION dbz_pub FOR ALL TABLES;
```

## **4. Install and Configure Kafka Connect with Debezium**

### **Download and Install the Debezium Connector**

```bash
mkdir -p /opt/homebrew/Cellar/kafka/3.9.0/libexec/plugins/debezium
cp debezium-connector-postgres-3.0.6.Final/* /opt/homebrew/Cellar/kafka/3.9.0/libexec/plugins/debezium/
```
*Important:* You need to import jars like `debezium-core`, `debezium-api`, `protobuf-java-3.25.5` among others inside the folder downloaded.

### **Start Kafka Connect**
##### 1. Generate a config folder in the path you decide and put inside this config file:  
> connect-distributed.properties
```
bootstrap.servers=localhost:9092
group.id=debezium-group
key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter
key.converter.schemas.enable=false
value.converter.schemas.enable=false
plugin.path=/opt/homebrew/Cellar/kafka/3.9.0/libexec/plugins

# Required Kafka topics for Kafka Connect
offset.storage.topic=connect-offsets
offset.storage.replication.factor=1

config.storage.topic=connect-configs
config.storage.replication.factor=1

status.storage.topic=connect-status
status.storage.replication.factor=1
```
##### 2. Start the service  

```bash
connect-distributed ~/.kafka/config/connect-distributed.properties
```
ℹ️ *With this first Kafka Connect run, the storage topics will be created (if not already exist)*

## **5. Configure the Debezium PostgreSQL Connector**

### **Create the Connector Configuration**

Create a JSON file `debezium-postgres.json`:

Most information about props, check out at [Debezium Doc](https://debezium.io/documentation/reference/stable/connectors/postgresql.html)

```json
{
  "name": "debezium-postgres-connector",
  "config": {
    "connector.class": "io.debezium.connector.postgresql.PostgresConnector",
    "database.hostname": "localhost",
    "database.port": "5432",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.dbname": "finance",
    "database.server.name": "pgserver1",
    "plugin.name": "pgoutput",
    "publication.autocreate.mode": "disabled",
    "slot.name": "debezium_slot",
    "database.history.kafka.bootstrap.servers": "localhost:9092",
    "database.history.kafka.topic": "schema-changes.finance",
    "publication.name": "dbz_pub",
    "topic.prefix": "finance",
    "snapshot.mode": "no_data"
  }
}
```

### **Register the Connector**

```bash
curl -X POST -H "Content-Type: application/json" --data @debezium-postgres.json http://localhost:8083/connectors
```

## **6. Verify Debezium and Kafka Integration**

### **Check Connector Status**

```bash
curl -s http://localhost:8083/connectors/debezium-postgres-connector/status | jq
```

### **List Kafka Topics**

```bash
kafka-topics --list --bootstrap-server localhost:9092
```

Expected topics:

```
finance.public.transactions
finance.public.users
schema-changes.finance
```

### **Consume Change Events from Kafka**

```bash
kafka-console-consumer --bootstrap-server localhost:9092 --topic finance.public.expenditures --from-beginning
```

### **Insert Data in PostgreSQL to Trigger CDC (Change Data Capture)**

```sql
INSERT INTO YOUR_TABLE_NAME (col1, col2) values (val1, v2);
UPDATE YOUR_TABLE_NAME SET col1 = 'updateVal1' WHERE col1 = 1;
DELETE FROM YOUR_TABLE_NAME WHERE col1 = 1;
```

### **Verify Events in Kafka Consumer**

If everything is working, JSON change events should appear in Kafka.

---

## **Next Steps**

- **Process Events**: Use Kafka Streams or microservices to consume and process change events.
- **Sink Connectors**: Store events in Elasticsearch, MongoDB, or another database.
- **Monitor Performance**: Use tools like Kafka UI or Prometheus/Grafana for monitoring.

🎉 **You have successfully set up Debezium with Kafka and PostgreSQL on macOS!** 🚀

