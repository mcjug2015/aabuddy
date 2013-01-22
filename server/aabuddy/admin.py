from django.contrib.gis import admin
from aabuddy.models import Meeting, UserConfirmation


class MeetingAdmin(admin.GeoModelAdmin):
    list_display = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type', 'creator', 'created_date', 'geo_location')
    list_filter = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type', 'creator', 'created_date',)


class UserConfirmationAdmin(admin.GeoModelAdmin):
    list_display = ('user', 'created_date', 'expiration_date')
    list_filter = ('user', 'created_date', 'expiration_date')


admin.site.register(Meeting, MeetingAdmin)
admin.site.register(UserConfirmation, UserConfirmationAdmin)