from django.contrib.gis import admin
from aabuddy.models import Meeting, UserConfirmation, MeetingNotThere


class MeetingNotThereAdmin(admin.ModelAdmin):
    list_display = ('meeting', 'request_host', 'user', 'created_date')
    list_filter = ('meeting', 'request_host', 'user', 'created_date')


class MeetingNotThereInline(admin.TabularInline):

    def has_add_permission(self, request):
        return False
    
    model = MeetingNotThere
    extra = 0
    readonly_fields = ['user', 'request_host', 'user_agent', 'unique_phone_id', 'created_date']
    

class MeetingAdmin(admin.GeoModelAdmin):
    list_display = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type', 'creator', 'created_date', 'geo_location')
    list_filter = ('name', 'description', 'address', 'day_of_week', 'start_time', 'end_time', 'internal_type', 'creator', 'created_date',)
    inlines = [MeetingNotThereInline,]


class UserConfirmationAdmin(admin.GeoModelAdmin):
    list_display = ('user', 'created_date', 'expiration_date')
    list_filter = ('user', 'created_date', 'expiration_date')


admin.site.register(Meeting, MeetingAdmin)
admin.site.register(UserConfirmation, UserConfirmationAdmin)
admin.site.register(MeetingNotThere, MeetingNotThereAdmin)