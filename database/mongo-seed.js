// Seed data for MongoDB (database: cms, collection: prescriptions).
// Run with: mongosh "mongodb://root:<password>@localhost:27017/cms?authSource=admin" mongo-seed.js
// appointmentId 51-74 are completed appointments (status = 1) from mysql-seed.sql; patientName matches each one.
db = db.getSiblingDB('cms');
db.prescriptions.deleteMany({});
db.prescriptions.insertMany([
  {
    "_id": ObjectId("6807dd712725f013281e7201"),
    "patientName": "John Smith",
    "appointmentId": NumberLong("51"),
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walgreens SF", "location": "Market Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7202"),
    "patientName": "Emily Rose",
    "appointmentId": NumberLong("52"),
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "refillCount": NumberInt(2),
    "pharmacy": { "name": "CVS Pharmacy", "location": "Main Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7203"),
    "patientName": "Michael Jordan",
    "appointmentId": NumberLong("53"),
    "medication": "Ibuprofen",
    "dosage": "400mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Rite Aid", "location": "Oak Avenue" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7204"),
    "patientName": "Olivia Moon",
    "appointmentId": NumberLong("54"),
    "medication": "Antihistamine",
    "dosage": "10mg",
    "doctorNotes": "Take 1 tablet daily before bed.",
    "refillCount": NumberInt(2),
    "pharmacy": { "name": "Walmart Pharmacy", "location": "Central Plaza" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7205"),
    "patientName": "Liam King",
    "appointmentId": NumberLong("55"),
    "medication": "Vitamin C",
    "dosage": "1000mg",
    "doctorNotes": "Take 1 tablet daily.",
    "refillCount": NumberInt(3),
    "pharmacy": { "name": "Walgreens SF", "location": "Market Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7206"),
    "patientName": "Sophia Lane",
    "appointmentId": NumberLong("56"),
    "medication": "Antibiotics",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 12 hours.",
    "refillCount": NumberInt(0),
    "pharmacy": { "name": "CVS Pharmacy", "location": "Main Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7207"),
    "patientName": "Noah Brooks",
    "appointmentId": NumberLong("57"),
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Rite Aid", "location": "Oak Avenue" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7208"),
    "patientName": "Ava Daniels",
    "appointmentId": NumberLong("58"),
    "medication": "Ibuprofen",
    "dosage": "200mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walmart Pharmacy", "location": "Central Plaza" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7209"),
    "patientName": "William Harris",
    "appointmentId": NumberLong("59"),
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "refillCount": NumberInt(2),
    "pharmacy": { "name": "Walgreens SF", "location": "Market Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7210"),
    "patientName": "Mia Green",
    "appointmentId": NumberLong("60"),
    "medication": "Vitamin D",
    "dosage": "1000 IU",
    "doctorNotes": "Take 1 tablet daily with food.",
    "refillCount": NumberInt(3),
    "pharmacy": { "name": "CVS Pharmacy", "location": "Main Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7211"),
    "patientName": "James Brown",
    "appointmentId": NumberLong("61"),
    "medication": "Antihistamine",
    "dosage": "10mg",
    "doctorNotes": "Take 1 tablet every morning.",
    "refillCount": NumberInt(2),
    "pharmacy": { "name": "Rite Aid", "location": "Oak Avenue" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7212"),
    "patientName": "Amelia Clark",
    "appointmentId": NumberLong("62"),
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walmart Pharmacy", "location": "Central Plaza" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7213"),
    "patientName": "Ben Johnson",
    "appointmentId": NumberLong("63"),
    "medication": "Ibuprofen",
    "dosage": "400mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walgreens SF", "location": "Market Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7214"),
    "patientName": "Ella Monroe",
    "appointmentId": NumberLong("64"),
    "medication": "Vitamin C",
    "dosage": "1000mg",
    "doctorNotes": "Take 1 tablet daily.",
    "refillCount": NumberInt(3),
    "pharmacy": { "name": "CVS Pharmacy", "location": "Main Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7215"),
    "patientName": "Lucas Turner",
    "appointmentId": NumberLong("65"),
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "refillCount": NumberInt(2),
    "pharmacy": { "name": "Rite Aid", "location": "Oak Avenue" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7216"),
    "patientName": "Grace Scott",
    "appointmentId": NumberLong("66"),
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walmart Pharmacy", "location": "Central Plaza" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7217"),
    "patientName": "Ethan Hill",
    "appointmentId": NumberLong("67"),
    "medication": "Ibuprofen",
    "dosage": "400mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walgreens SF", "location": "Market Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7218"),
    "patientName": "Ruby Ward",
    "appointmentId": NumberLong("68"),
    "medication": "Vitamin D",
    "dosage": "1000 IU",
    "doctorNotes": "Take 1 tablet daily with food.",
    "refillCount": NumberInt(3),
    "pharmacy": { "name": "CVS Pharmacy", "location": "Main Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7219"),
    "patientName": "Jack Baker",
    "appointmentId": NumberLong("69"),
    "medication": "Antibiotics",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 12 hours.",
    "refillCount": NumberInt(0),
    "pharmacy": { "name": "Rite Aid", "location": "Oak Avenue" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7220"),
    "patientName": "Mia Hall",
    "appointmentId": NumberLong("70"),
    "medication": "Paracetamol",
    "dosage": "500mg",
    "doctorNotes": "Take 1 tablet every 6 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walmart Pharmacy", "location": "Central Plaza" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7221"),
    "patientName": "Owen Thomas",
    "appointmentId": NumberLong("71"),
    "medication": "Ibuprofen",
    "dosage": "200mg",
    "doctorNotes": "Take 1 tablet every 8 hours.",
    "refillCount": NumberInt(1),
    "pharmacy": { "name": "Walgreens SF", "location": "Market Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7222"),
    "patientName": "Ivy Jackson",
    "appointmentId": NumberLong("72"),
    "medication": "Antihistamine",
    "dosage": "10mg",
    "doctorNotes": "Take 1 tablet every morning.",
    "refillCount": NumberInt(2),
    "pharmacy": { "name": "CVS Pharmacy", "location": "Main Street" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7223"),
    "patientName": "Leo Martin",
    "appointmentId": NumberLong("73"),
    "medication": "Vitamin C",
    "dosage": "1000mg",
    "doctorNotes": "Take 1 tablet daily.",
    "refillCount": NumberInt(3),
    "pharmacy": { "name": "Rite Aid", "location": "Oak Avenue" },
    "_class": "com.project.back_end.models.Prescription"
  },
  {
    "_id": ObjectId("6807dd712725f013281e7224"),
    "patientName": "Ella Moore",
    "appointmentId": NumberLong("74"),
    "medication": "Aspirin",
    "dosage": "300mg",
    "doctorNotes": "Take 1 tablet after meals.",
    "refillCount": NumberInt(2),
    "pharmacy": { "name": "Walmart Pharmacy", "location": "Central Plaza" },
    "_class": "com.project.back_end.models.Prescription"
  }
]);
print('Prescriptions inserted: ' + db.prescriptions.countDocuments());
