''' module for aabuddy models '''
from django.contrib.gis.db import models
import datetime
from django.contrib.gis.db.models.manager import GeoManager


class Meeting(models.Model):
    ''' meeting models class '''
    SUBMITTED = "submitted"
    APPROVED = "approved"
    REMOVED = "removed"
    INTERNAL_TYPE_CHOICES = [(SUBMITTED, "Submitted"),
                             (APPROVED, "Approved"),
                             (REMOVED, "Removed")]

    objects = GeoManager()
    day_of_week = models.IntegerField(null=False, blank=False, default=1)
    start_time = models.TimeField(null=False, blank=False, default=datetime.time(11, 30))
    end_time = models.TimeField(null=False, blank=False, default=datetime.time(11, 30))
    name = models.CharField(max_length=100)
    description = models.CharField(max_length=255, null=True, blank=True)
    address = models.CharField(max_length=300)
    internal_type = models.CharField(max_length=10, default=SUBMITTED)
    geo_location = models.PointField()
