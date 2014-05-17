''' tastypie api module for aa buddy '''
from tastypie.resources import ModelResource
from aabuddy.models import ServerMessage
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
