DATABASES = {
    'default': {
        'ENGINE': 'django.contrib.gis.db.backends.postgis', # Add 'postgresql_psycopg2', 'postgresql', 'mysql', 'sqlite3' or 'oracle'.
        'NAME': 'aabuddy', # Or path to database file if using sqlite3.
        'USER': 'postgres', # Not used with sqlite3.
        'PASSWORD': '', # Not used with sqlite3.
        'HOST': 'localhost', # Set to empty string for localhost. Not used with sqlite3.
        'PORT': '5432', # Set to empty string for default. Not used with sqlite3.
        }
}

EMAIL_SUBJECT_PREFIX = '[AA Buddy] '
SERVER_EMAIL = 'fakeemail@gmail.com'
EMAIL_HOST = 'smtp.gmail.com'
EMAIL_HOST_PASSWORD = 'OOOOOO'
EMAIL_HOST_USER = 'really_fake_email'
EMAIL_PORT = 587
EMAIL_USE_TLS = True

LOGTAIL_FILES = {
                 'apache': '/var/www/www.foo.com.log',
                 'django': '/var/log/www.foo.com/project.log',
}

POSTGIS_VERSION=(2,0,1)