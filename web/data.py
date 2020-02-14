import firebase_admin
from firebase_admin import credentials
from firebase_admin import db

# Fetch the service account key JSON file contents
cred = credentials.Certificate('quakefind-e0f8e-firebase-adminsdk-555ob-d597d6b1fe.json')
# Initialize the app with a service account, granting admin privileges
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://quakefind-e0f8e.firebaseio.com/'
})

db = db.reference('/person')
results = db.get()
