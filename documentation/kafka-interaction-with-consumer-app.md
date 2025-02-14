# 📌 Fine-Grained Interaction Between Kafka Broker and Consumer App

## **1️⃣ Producer Sends a Message**
- A **producer** publishes an event to a Kafka topic.
- The **Kafka broker** appends the event to a partition based on the partitioning strategy (e.g., round-robin, key-based).

---

## **2️⃣ Broker Stores the Event**
- The broker **persists** the event in a specific partition.
- Each partition is a **log**, meaning messages are stored **sequentially**.
- The broker **does not push** messages to consumers; consumers must **pull** them.

---

## **3️⃣ Consumer Group Polls for New Messages**
- The **consumer app** (belonging to a consumer group) **polls the broker** for new messages using `KafkaConsumer.poll()`.
- The broker **returns a batch of messages** from the assigned partitions.

📌 **Key Details:**
- Each consumer **tracks its offset** (the last message it successfully processed).
- The broker **only returns unprocessed messages** starting from the latest committed offset.

---

## **4️⃣ Offset Management & Acknowledgment**
- Once the consumer processes a message, it **commits the offset** to Kafka.
- This can be done:
    - **Automatically** (`enable.auto.commit=true` → Kafka commits offsets periodically).
    - **Manually** (`enable.auto.commit=false` → Consumer commits offsets explicitly after processing).

⚠️ **Why is offset management important?**
- If a consumer **fails and restarts**, it resumes processing **from the last committed offset**.
- Without committing, **re-processing** may occur, leading to duplicate data handling.

---

## **5️⃣ Partition Assignment & Consumer Load Balancing**
- **If multiple consumer instances exist** (e.g., your **five replicas in AKS**), Kafka **distributes partitions across consumers**.
- Each consumer **exclusively reads from a subset of partitions** to **avoid duplicate processing**.
- If a consumer crashes, its partitions are **reassigned** to other consumers in the group.

### 🔹 **Partition Assignment Strategies**
- **RangeAssignor** (default) → Assigns contiguous partitions to consumers.
- **RoundRobinAssignor** → Distributes partitions evenly.
- **StickyAssignor** → Minimizes unnecessary partition reassignments.

---

## **6️⃣ Handling Failures**
- If a consumer **crashes**, Kafka **detects** the failure via heartbeats.
- The broker **reassigns partitions** to another active consumer in the group.
- If the consumer **restarts**, it resumes from the last committed offset.

---

## **🔹 Summary of Kafka Broker-Consumer Interaction**
| Step | Action |
|------|--------|
| **1** | Producer sends a message to Kafka |
| **2** | Broker appends message to a partition |
| **3** | Consumer polls for new messages |
| **4** | Broker returns messages based on committed offsets |
| **5** | Consumer processes the messages |
| **6** | Consumer commits offsets (auto or manual) |
| **7** | If a consumer fails, partitions are reassigned |

---

## **7️⃣ KafkaConsumer.poll() Behavior**
### **🔹 What Does `poll(time)` Do?**
- `poll(time)` is a **blocking call** that fetches records from Kafka.
- The consumer **waits up to `time` milliseconds** if no new messages are available.
- If records exist, it **returns immediately** without waiting.

### **🔹 Default Timeout in `poll(time)`**
- **There is no default timeout**; it must be **explicitly provided**.
- Example:
  ```java
  ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

