this android application will (when it is completed) track a users daily commutes
to see how much CO2 they are emitting in one day. The application does this
by using the phones accelerometer and google maps to figure out the source 
of the users transportation (walking, biking, car, bus, metro) in the DC area.
For now, it tracks the user and puts flags down on google maps to see how fast the user is going and exactly where they are going. if the flags are further apart from each other this means that the user is using a vehicle. If the flags are on top of a bus/metro station then the user is using a bus or metro train. In the end I will use a database with all the vehicle’s emissions of CO2 to calculate how much a user emits in their daily trips. Also if you want to test it out you will have to use your own Google Maps API code which you can get online and put it in the program’s src>debug>values>google_maps_api.xml and also in the src>debug>res>values>google_maps_api.xml files.
 