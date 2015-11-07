select ST_X(location::geometry), ST_Y(location::geometry), * from meeting;

select * from meeting
WHERE ST_DWithin(location, 'POINT(39.065181 -77.125422)', 1609.34); /*1 mile distance in meters from matchbox*/