from django.contrib.gis import admin
from aabuddy.models import Meeting

class MeetingAdmin(admin.GeoModelAdmin):
    list_display = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type', 'creator', 'created_date', 'geo_location')
    list_filter = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type', 'creator', 'created_date',)


admin.site.register(Meeting, MeetingAdmin)