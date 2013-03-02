''' tastypie api module for aa buddy '''
from tastypie.resources import ModelResource
from aabuddy.models import MeetingNotThere
from tastypie.authentication import Authentication
from tastypie.authorization import Authorization


class MeetingNotThereResource(ModelResource):
    
    class Meta:
        queryset = MeetingNotThere.objects.all()
        authentication = Authentication()
        authorization = Authorization()
        allowed_methods = ['get', 'post']
