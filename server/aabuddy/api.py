''' tastypie api module for aa buddy '''
from tastypie.resources import ModelResource
from aabuddy.models import ServerMessage, MeetingType
from tastypie.authentication import Authentication
from tastypie.authorization import Authorization


class ServerMessageResource(ModelResource):
    
    class Meta:
        resource_name = 'server_message'
        queryset = ServerMessage.objects.all()
        authentication = Authentication()
        authorization = Authorization()
        allowed_methods = ['get']
        filtering = {'is_active': ['exact']}


class MeetingTypeResource(ModelResource):
    
    class Meta:
        resource_name = 'meeting_type'
        queryset = MeetingType.objects.all()
        authentication = Authentication()
        authorization = Authorization()
        allowed_methods = ['get']
