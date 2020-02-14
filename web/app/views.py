from flask import render_template
from app import app
from data import results
import os

front = "https://maps.googleapis.com/maps/api/staticmap?zoom=13&size=600x300&maptype=roadmap"
p1 = "&markers=color:red%7PotentialSurvivorlabel:S%7C{},{}".format(str(results['person0']['latitude']), str(results['person0']['longitude']))
p2 = "&markers=color:green%7PotentialSurvivorlabel:S%7C{},{}".format(str(results['person1']['latitude']), str(results['person1']['longitude']))
key = "&key=" + os.environ.get('MY_API_KEY')
maps = "https://google.com/maps/@{},{},20z".format(str(results['person0']['latitude']), str(results['person0']['longitude']))
@app.route('/')
def index():
	return render_template('index.html', 
	link = front+p1+p2+key, 
	time=str(results['person0']['date']), 
	latitude=str(results['person0']['latitude']), 
	longitude=str(results['person0']['longitude']),
	maps_link = maps)