''' tastypie api module for aa buddy '''
from tastypie.resources import ModelResource
from aabuddy.models import Meeting
from tastypie.authentication import Authentication
from tastypie.authorization import Authorization


class MeetingResource(ModelResource):
    ''' meeting resource class '''

    class Meta:  # pylint: disable=W0232
        '''options for the rest api'''
        queryset = Meeting.objects.all()  # pylint: disable=E1101
        authentication = Authentication()
        authorization = Authorization()
        allowed_methods = ['get', 'post']
