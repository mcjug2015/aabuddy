from django.contrib.gis import admin
from aabuddy.models import Meeting

class MeetingAdmin(admin.GeoModelAdmin):
    list_display = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type', 'geo_location')
    list_filter = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type')


admin.site.register(Meeting, MeetingAdmin)