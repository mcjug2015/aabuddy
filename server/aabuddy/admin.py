from django.contrib.gis import admin
from aabuddy.models import Meeting

admin.site.register(Meeting, admin.GeoModelAdmin)