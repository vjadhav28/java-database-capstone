## MySQL Database Design

### Tables

#### Patients
- `id` (Primary Key)
- `first_name`
- `last_name`
- `email`
- `phone`
- `date_of_birth`
- `address`

#### Doctors
- `id` (Primary Key)
- `first_name`
- `last_name`
- `email`
- `phone`
- `specialization`

#### Appointments
- `id` (Primary Key)
- `patient_id` (Foreign Key referencing Patients)
- `doctor_id` (Foreign Key referencing Doctors)
- `appointment_date`
- `status`

### Table: appointments
- id: INT, Primary Key, Auto Increment
- doctor_id: INT, Foreign Key → doctors(id)
- patient_id: INT, Foreign Key → patients(id)
- appointment_time: DATETIME, Not Null
- status: INT (0 = Scheduled, 1 = Completed, 2 = Cancelled)

### Table: clinic_locations
- id: INT, Primary Key, Auto Increment
- name: VARCHAR(255), Not Null
- address: TEXT, Not Null
- phone: VARCHAR(20)
- email: VARCHAR(255)
- opening_hours: VARCHAR(255)
- latitude: DECIMAL(10, 8)
- longitude: DECIMAL(11, 8)
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP
- updated_at: TIMESTAMP, Default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
- is_active: BOOLEAN, Default TRUE
- ### Table: medical_records
- id: INT, Primary Key, Auto Increment
- patient_id: INT, Foreign Key → patients(id)
- doctor_id: INT, Foreign Key → doctors(id)
- record_date: DATETIME, Not Null
- diagnosis: TEXT
- treatment: TEXT
- notes: TEXT
- created_at: TIMESTAMP, Default CURRENT_TIMESTAMP
- updated_at: TIMESTAMP, Default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
- is_active: BOOLEAN, Default TRUE

#### Admins
- `id` (Primary Key)
- `username`
- `password`
- `email`

### Relationships
- A patient can have multiple appointments (One-to-Many relationship between Patients and Appointments).
- A doctor can have multiple appointments (One-to-Many relationship between Doctors and Appointments).
- An appointment is associated with one patient and one doctor (Many-to-One relationship from Appointments to Patients and Doctors).
- An admin can manage multiple doctors and patients, but this relationship is not explicitly defined in the schema.
- ### Indexes
- Indexes should be created on foreign key columns (`patient_id`, `doctor_id`) in the Appointments table to optimize query performance.
- Additional indexes can be created on frequently queried columns such as `email` in the Patients and Doctors tables for faster lookups.
- ### Constraints
- Ensure that the `email` fields in both Patients and Doctors tables are unique to prevent duplicate entries.
- Implement foreign key constraints on `patient_id` and `doctor_id` in the Appointments table to maintain referential integrity.
- Use appropriate data types and lengths for each column to ensure data consistency and storage efficiency. For example, use VARCHAR for string fields with a defined maximum length,
- and use DATE for date fields like `date_of_birth` and `appointment_date`.
- ### Additional Considerations
- Consider implementing soft deletes for records in the Patients, Doctors, and Appointments tables to allow for recovery of deleted records if necessary. This can be achieved by adding a `deleted_at` timestamp column to each table and updating the application logic to filter out records with a non-null `deleted_at` value.
- Regularly back up the MySQL database to prevent data loss and ensure data recovery in case of failures. Implementing automated backup solutions can help maintain data integrity and availability.
- Monitor database performance and optimize queries as needed. Use tools like MySQL's EXPLAIN command to analyze query execution plans and identify potential bottlenecks. Consider adding additional indexes or optimizing existing ones based on query patterns and performance metrics.
- Ensure that the database is properly secured by implementing access controls, encryption for sensitive data, and regular security audits. Limit access to the database to only authorized personnel and applications.
## MongoDB Database Design
- ### Collections
- #### Prescriptions
- `_id` (Primary Key)
- `patient_id` (Reference to Patients collection)
- `doctor_id` (Reference to Doctors collection)
- `medications` (Array of medication objects, each containing name, dosage, frequency, and duration)
- `prescription_date`
- `notes`
- ### Relationships
- Each prescription is associated with one patient and one doctor (Many-to-One relationship from Prescriptions to Patients and Doctors).
- A patient can have multiple prescriptions (One-to-Many relationship between Patients and Prescriptions).
- A doctor can issue multiple prescriptions (One-to-Many relationship between Doctors and Prescriptions).
- ### Indexes
- Indexes should be created on the `patient_id` and `doctor_id` fields in the Prescriptions collection to optimize query performance.
- Additional indexes can be created on frequently queried fields such as `prescription_date` for faster lookups and reporting.
- ### Constraints
- Ensure that the `patient_id` and `doctor_id` fields in the Prescriptions collection are properly validated to reference existing records in the Patients and Doctors collections, respectively.
- Implement validation rules for the `medications` array to ensure that each medication object contains valid data (e.g., non-empty name, positive dosage, valid frequency and duration).
- ### Additional Considerations
- Consider implementing a versioning system for prescriptions to track changes over time. This can be achieved by adding a `version` field to the Prescriptions collection and updating it whenever a prescription is modified. This allows for historical tracking of prescription changes and can be useful for auditing purposes.
- Regularly back up the MongoDB database to prevent data loss and ensure data recovery in case of failures. Implementing automated backup solutions can help maintain data integrity and availability.
- Monitor database performance and optimize queries as needed. Use MongoDB's explain() method to analyze query execution plans and identify potential bottlenecks. Consider adding additional indexes or optimizing existing ones based on query patterns and performance metrics.
- Ensure that the MongoDB database is properly secured by implementing access controls, encryption for sensitive data, and regular security audits. Limit access to the database to only authorized personnel and applications.
- ### Summary
- The MySQL database is designed to handle structured data related to patients, doctors, appointments, and admins, while the MongoDB database is designed to handle unstructured data related to prescriptions. The combination of these two databases allows for efficient storage and retrieval of both structured and unstructured data, enabling the application to provide a comprehensive healthcare management system. By following best practices for database design, indexing, constraints, and security, the application can ensure data integrity, performance, and reliability for its users.

#### feedback: The schema design is well-structured and covers the necessary entities and relationships for a healthcare management system. The use of both MySQL and MongoDB allows for efficient handling of structured and unstructured data. The inclusion of indexes and constraints helps optimize query performance and maintain data integrity. Additionally, the consideration of soft deletes, backups, and security measures demonstrates a comprehensive approach to database management. Overall, this design provides a solid foundation for building a robust healthcare application.

### Collection: prescriptions

```json
{
  "_id": "ObjectId('64abc123456')",
  "patientName": "John Smith",
  "appointmentId": 51,
  "medication": "Paracetamol",
  "dosage": "500mg",
  "doctorNotes": "Take 1 tablet every 6 hours.",
  "refillCount": 2,
  "pharmacy": {
    "name": "Walgreens SF",
    "location": "Market Street"
  }
}
```

### Collection: feedback

```json
{
  "_id": "ObjectId('64def456789')",
  "patientName": "Jane Doe",
  "appointmentId": 52,
  "feedbackText": "The doctor was very attentive and explained everything clearly.",
  "rating": 5,
  "submittedAt": "2024-06-01T10:30:00Z"
}
```
### Collection: logs

```json
{
  "_id": "ObjectId('64ghi789012')",
  "timestamp": "2024-06-01T12:00:00Z",
  "logLevel": "ERROR",
  "message": "Failed to connect to MySQL database.",
  "stackTrace": "java.sql.SQLException: Connection refused..."
}
```

### Collection: messages

```json
{
  "_id": "ObjectId('64jkl345678')",
  "senderId": "user123",
  "receiverId": "user456",
  "messageText": "Hello, I have a question about my appointment.",
  "sentAt": "2024-06-01T14:00:00Z",
  "isRead": false
}
```
