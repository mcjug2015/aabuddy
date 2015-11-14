INSERT INTO meeting (name, description, address, location, day_of_week, start_time, end_time) VALUES 
	('5am Rockville', 'office bldg', '1700 Rockville Pike, Rockville, MD 20852', ST_GeographyFromText( 'SRID=4326;POINT(39.061899 -77.121706)' ), 7, '13:00:00', '15:00:00'),
	('Starbucks Coffee - Falls Grove', 'coffee shop', '14941 Shady Grove Rd, Rockville, MD 20850', ST_GeographyFromText( 'SRID=4326;POINT(39.098053 -77.192795)' ), 5, '17:00:00', '18:00:00'),
	('Coal Fire Pizza - Kentlands', 'restaurant', '116 Main Street, Gaithersburg, MD 20878', ST_GeographyFromText( 'SRID=4326;POINT(39.125732 -77.237577)' ), 6, '12:00:00', '01:30:00'),
	('Panera Bread Germantown', 'restaurant', '19965 Century Boulevard, Germantown, MD 20874', ST_GeographyFromText( 'SRID=4326;POINT(39.18357 -77.261996)' ), 7, '20:00:00', '21:00:00')
;