''' module for aabuddy models '''
from django.contrib.gis.db import models
from django.db import models as classic_models
import datetime
from django.contrib.gis.db.models.manager import GeoManager
from django.contrib.auth.models import User


class PsvFileUpload(models.Model):
    ''' a blank csv file upload model object'''
    pass


class Meeting(models.Model):
    ''' meeting models class '''

    SUBMITTED = "submitted"
    APPROVED = "approved"
    REMOVED = "removed"
    INTERNAL_TYPE_CHOICES = [(SUBMITTED, "Submitted"),
                             (APPROVED, "Approved"),
                             (REMOVED, "Removed")]
    
    DAY_OF_WEEK_CHOICES = [(1, "Sunday"),
                           (2, "Monday"),
                           (3, "Tuesday"),
                           (4, "Wednesday"),
                           (5, "Thursday"),
                           (6, "Friday"),
                           (7, "Saturday")]

    objects = GeoManager()
    day_of_week = models.IntegerField(null=False, blank=False, default=1, choices=DAY_OF_WEEK_CHOICES)
    start_time = models.TimeField(null=False, blank=False, default=datetime.time(11, 30))
    end_time = models.TimeField(null=False, blank=False, default=datetime.time(12, 30))
    name = models.CharField(max_length=100)
    description = models.CharField(max_length=255, null=True, blank=True)
    address = models.CharField(max_length=300)
    internal_type = models.CharField(max_length=10, default=SUBMITTED, choices=INTERNAL_TYPE_CHOICES)
    creator = models.ForeignKey(User, related_name='meetings', null=True, on_delete=models.CASCADE)
    created_date = models.DateTimeField(editable=False,null=False, blank=False, default=datetime.datetime(1982,12,22))
    geo_location = models.PointField()
    
    def __str__(self):
        meeting_str = 'Meeting id: %s, ' % str(self.pk)

        if self.creator:
            meeting_str += "Creator: %s" % str(self.creator.username)
            
        return meeting_str

    def get_psv_row(self):
        retval = str(self.name) + "|"
        retval += str(self.description) + "|"
        retval += str(self.day_of_week) + "|"
        retval += self.start_time.strftime("%H:%M:%S") + "|"
        retval += self.end_time.strftime("%H:%M:%S") + "|"
        retval += str(self.address) + "|"
        retval += str(self.geo_location.y) + "|"
        retval += str(self.geo_location.x)
        return retval

    def save(self, **kwargs):
        if not self.id:
            self.created_date = datetime.datetime.now() # Edit created timestamp only if it's new entry
        super(Meeting, self).save()


class MeetingNotThere(classic_models.Model):
    meeting = classic_models.ForeignKey(Meeting, related_name='not_theres', null=False, blank=False, on_delete=classic_models.CASCADE)
    user = models.ForeignKey(User, related_name='not_theres', null=True, blank=True, on_delete=classic_models.SET_NULL)
    note = classic_models.CharField(max_length=200, null=True, blank=True)
    request_host = classic_models.CharField(max_length=200, null=True, blank=True)
    user_agent = classic_models.CharField(max_length=400, null=True, blank=True)
    unique_phone_id = classic_models.CharField(max_length=400, null=True, blank=True)
    created_date = models.DateTimeField(auto_now_add=True)
    def __str__(self):
        return ("Not There id: %s, host: %s" % (str(self.pk), str(self.request_host)))


class NotThereView(models.Model):
    meeting = models.ForeignKey(Meeting, on_delete=models.DO_NOTHING, null=False, blank=False)
    not_there_count = models.IntegerField()
    meeting_name = models.CharField(max_length=100)
    latest_not_there = models.DateTimeField()

    def __str__(self):
        return ("NTV row meeting_name: %s, count: %s" % (str(self.meeting_name), str(self.not_there_count)))


class ActiveNotTheresView(models.Model):
    user = models.ForeignKey(User, on_delete=models.DO_NOTHING, null=True, blank=True)
    unique_phone_id = models.CharField(max_length=400, null=True, blank=True)
    total = models.IntegerField()
    distinct = models.IntegerField()
    created_date = models.DateTimeField()


class UserConfirmation(models.Model):
    user = models.ForeignKey(User, related_name='confirmations', null=False, blank=False, on_delete=models.CASCADE)
    created_date = models.DateTimeField(null=False, blank=False, default=datetime.datetime.now())
    expiration_date = models.DateTimeField(null=False, blank=False)
    confirmation_key = models.CharField(max_length=64, null=False, blank=False)


class ServerMessage(models.Model):
    created_date = models.DateTimeField(auto_now_add=True)
    updated_date = models.DateTimeField(auto_now=True)
    message = models.TextField()
    short_message = models.CharField(null=True, blank=True, max_length=255)
    is_active = models.BooleanField(default=True)
