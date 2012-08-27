''' module for aabuddy models '''
from django.db import models
import datetime


class Meeting(models.Model):
    ''' meeting models class '''

    day_of_week = models.CharField(null=False, blank=False, default="Monday", max_length=10)
    start_time = models.DateTimeField(null=False, blank=False, default=datetime.datetime(1982, 12, 22, 11, 30))
    end_time = models.DateTimeField(null=False, blank=False, default=datetime.datetime(1982, 12, 22, 11, 30))
    latitude = models.FloatField()
    longitude = models.FloatField()
    description = models.CharField(max_length=255)
    address = models.CharField(max_length=255)
    internal_type = models.CharField(max_length=10, default="Submitted")
