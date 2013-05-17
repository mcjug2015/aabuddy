from django.contrib.gis import admin
from aabuddy.models import Meeting, UserConfirmation, MeetingNotThere,\
    PsvFileUpload, NotThereView, ActiveNotTheresView
from django import forms
from django.core.exceptions import ValidationError
import tempfile
from django.contrib import messages
from django.db import transaction
from aabuddy.psv_loader import PsvLoader
from django.utils.safestring import mark_safe
from django.core import urlresolvers


def add_meeting_link_field(target_model = None, field = '', app='', action='change', field_name='link',
                   link_text=unicode):
    def add_link(cls):
        reverse_name = target_model or cls.model.__name__.lower()
        def link(self, instance):
            link_obj = getattr(instance, field, None) or instance
            reverse_salt = "admin:%s_%s_%s" % (app, target_model, action)
            url = urlresolvers.reverse(reverse_salt, args=(link_obj.id,))
            return mark_safe("<a href='%s'>%s</a>" % (url, link_text(link_obj)))
        link.allow_tags = True
        link.short_description = reverse_name + ' link'
        setattr(cls, field_name, link)
        cls.readonly_fields = list(getattr(cls, 'readonly_fields', [])) + \
            [field_name]
        return cls
    return add_link

def add_meeting_not_there_link_field(target_model = 'meetingnotthere', field = 'unique_phone_id', app='aabuddy',
                                     field_name='link', link_text=unicode):
    def add_link(cls):
        reverse_name = target_model or cls.model.__name__.lower()
        def link(self, instance):
            link_obj = getattr(instance, field, None) or instance
            reverse_salt = "admin:%s_%s_changelist" % (app, target_model)
            reverse_salt = reverse_salt.replace('add', '')
            url = urlresolvers.reverse(reverse_salt)
            #HACK!
            return mark_safe("<a href='%s?%s=%s'>%s</a>" % (url, field, link_obj, link_text(link_obj)))
        link.allow_tags = True
        link.short_description = reverse_name + ' link'
        setattr(cls, field_name, link)
        cls.readonly_fields = list(getattr(cls, 'readonly_fields', [])) + \
            [field_name]
        return cls
    return add_link

@add_meeting_not_there_link_field()
class ActiveMeetingNotThereViewAdmin(admin.ModelAdmin):
    list_display = ('link', 'user' , 'total', 'distinct', 'created_date')
    list_filter = ('unique_phone_id', 'user', 'total', 'distinct', 'created_date')

class MeetingNotThereAdmin(admin.ModelAdmin):
    list_display = ('meeting', 'request_host', 'user', 'unique_phone_id', 'created_date')
    list_filter = ('meeting', 'request_host', 'user', 'unique_phone_id', 'created_date')

@add_meeting_link_field(target_model = 'meeting', field = 'meeting', app='aabuddy', field_name='link')
class NotThereViewAdmin(admin.ModelAdmin):
    list_display = ('link', 'not_there_count', 'meeting_name', 'latest_not_there')
    list_filter = ('not_there_count', 'meeting_name', 'latest_not_there')

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


class PsvFileUploadForm(forms.ModelForm):
    ''' The pipe separated file upload form '''
    
    psv_file = forms.FileField()
    
    def clean_psv_file(self):
        ''' make sure the file has the .csv extention '''
        uploaded_csv = self.cleaned_data['psv_file']
        if not uploaded_csv.name.endswith('.psv'):
            raise ValidationError("The system only supports .psv(pipe separated) files")
        
        return uploaded_csv
    
    class Meta:
        ''' the meta '''
        model = PsvFileUpload

class PsvFileUploadAdmin(admin.ModelAdmin):
    ''' admin for the psv file upload '''
    
    def has_change_permission(self, request, obj=None):
        ''' csv file uploads can not be updated '''
        return False

    def has_delete_permission(self, request, obj=None):
        ''' csv file uploads can not be deleted '''
        return False
    
    @transaction.autocommit
    def save_model(self, request, obj, form, change):
        ''' assign jive points based on the csv file. To have excel generated csvs work with pythons csv reader
            it has to be opened in 'rU' mode, so we write it to a temp file and read it from there.
        '''
        loader = PsvLoader()
        tf = tempfile.NamedTemporaryFile(delete=False)
        tf.write(form.cleaned_data['psv_file'].read())
        tf_name = tf.name
        tf.close()
        tf = open(tf_name, 'rU')
        errors = loader.load_psv(request.user, tf, True)
        tf.close()
        for error in errors:
            messages.error(request, message="The following rows could not be loaded: " + error)
        return
    
    form = PsvFileUploadForm
    actions = None


admin.site.register(Meeting, MeetingAdmin)
admin.site.register(UserConfirmation, UserConfirmationAdmin)
admin.site.register(MeetingNotThere, MeetingNotThereAdmin)
admin.site.register(PsvFileUpload, PsvFileUploadAdmin)
admin.site.register(NotThereView, NotThereViewAdmin)
admin.site.register(ActiveNotTheresView, ActiveMeetingNotThereViewAdmin)