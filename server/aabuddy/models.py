''' module for aabuddy models '''
from django.contrib.gis.db import models
import datetime


class Meeting(models.Model):
    ''' meeting models class '''

    day_of_week = models.CharField(null=False, blank=False, default="Monday", max_length=10)
    start_time = models.TimeField(null=False, blank=False, default=datetime.time(11, 30))
    end_time = models.TimeField(null=False, blank=False, default=datetime.time(11, 30))
    name = models.CharField(max_length=100)
    description = models.CharField(max_length=255, null=True, blank=True)
    address = models.CharField(max_length=300)
    internal_type = models.CharField(max_length=10, default="Submitted")
    geo_location = models.PointField()
