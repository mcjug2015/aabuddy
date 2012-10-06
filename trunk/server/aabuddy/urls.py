''' urls module for aa buddy app '''
from tastypie.api import Api
from aabuddy.api import MeetingResource
from django.conf.urls import patterns, url, include

AABUDDY_API = Api(api_name="v1")
AABUDDY_API.register(MeetingResource())

urlpatterns = patterns('',
    url(r'^api/', include(AABUDDY_API.urls)),
    url(r'^get_meetings', 'aabuddy.views.get_meetings_within_distance'),
    url(r'^save_meeting', 'aabuddy.views.save_meeting')
)
