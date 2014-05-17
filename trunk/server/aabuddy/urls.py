''' urls module for aa buddy app '''
from tastypie.api import Api
from aabuddy.api import ServerMessageResource
from django.conf.urls import patterns, url, include

AABUDDY_API = Api(api_name="v1")
AABUDDY_API.register(ServerMessageResource())

urlpatterns = patterns('',
    url(r'^api/', include(AABUDDY_API.urls)),
    url(r'^get_meetings', 'aabuddy.views.get_meetings_within_distance'),
    url(r'^get_meeting_by_id', 'aabuddy.views.get_meeting_by_id'),
    url(r'^save_meeting', 'aabuddy.views.save_meeting'),
    url(r'^create_user', 'aabuddy.views.create_user'),
    url(r'^validate_user_creds', 'aabuddy.views.validate_user_creds'),
    url(r'^change_password', 'aabuddy.views.change_password'),
    url(r'^reset_password', 'aabuddy.views.reset_password'),
    url(r'^send_reset_conf', 'aabuddy.views.send_reset_conf'),
    url(r'^find_similar', 'aabuddy.views.find_similar'),
    url(r'^delete_my_meeting', 'aabuddy.views.delete_my_meeting'),
    url(r'^post_meeting_not_there', 'aabuddy.views.post_meeting_not_there')
)
