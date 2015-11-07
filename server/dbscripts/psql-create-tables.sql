DROP TABLE IF EXISTS meeting;

CREATE TABLE meeting (
	id SERIAL PRIMARY KEY,
	name VARCHAR(255) NOT NULL,
	description VARCHAR(255) NOT NULL,
	address VARCHAR(255) NOT NULL,
	location GEOGRAPHY(Point, 4326) NOT NULL,
	day_of_week SMALLINT NOT NULL,
	start_time TIME NOT NULL,
	end_time TIME NOT NULL
);

-- Index the table with a spherical index
CREATE INDEX meeting_gidx ON meeting USING GIST ( location );