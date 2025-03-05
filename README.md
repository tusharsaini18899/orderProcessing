# **Instructions for setting up and running:**
java, maven and mysql is required to run the application.
Please change database Credential(url,username,password) in application.properties file before running the application
`spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=root@123`



# **Example API requests and responses**
### 1: create Order:
Request:
`curl --location 'http://localhost:8080/orders' \
--header 'Content-Type: application/json' \
--data '{
"userId": 1,
"itemIds": "item1,item2",
"totalAmount": 100.50
}
'`

Response:
`{
"orderId": 52,
"userId": 1,
"itemIds": "item1,item2",
"totalAmount": 100.5,
"status": "Pending",
"createdAt": "2025-03-05T00:30:30.969187",
"processedAt": null
}`

### 2.Check order status by id:
Request:
`curl --location 'http://localhost:8080/orders/1'`

Response:
`{
"orderId": 1,
"userId": 1,
"itemIds": "item1,item2",
"totalAmount": 100.5,
"status": "Completed",
"createdAt": "2025-03-04T23:20:56",
"processedAt": "2025-03-04T23:20:58"
}`

### 3. Get Key Metrics:
Request:
`curl --location 'http://localhost:8080/orders/metrics'`
Response:
`   {
   "avgProcessingTime": 229.0,
   "pending": 16,
   "totalProcessed": 36,
   "processing": 0
   }`



# **Explanation of design decisions and trade-offs.**
RESTful API Structure
Decision: The API follows RESTful principles with clear endpoints:
* POST /orders → Create orders
* GET /orders/{orderId} → Retrieve order status
* GET /orders/metrics → Fetch system metrics
* Trade-off: REST APIs are easy to integrate, but WebSockets or GraphQL might provide real-time updates more efficiently.

Asynchronous Order Processing
Decision: Used an in-memory queue (ConcurrentLinkedQueue) for async order processing.
Trade-off: In-memory processing is fast but not persistent—if the app crashes, unprocessed orders are lost. A persistent queue like Kafka or RabbitMQ would be more reliable.



# Assumptions made during development.
* Orders Cannot Be Canceled
* User IDs and Item IDs Will Be Mapped to Separate Tables(USERS, ITEM)
* No orders will be lost due to application restarts.
