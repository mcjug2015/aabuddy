from django.conf.urls.defaults import patterns, include, url
from django.contrib import admin
from django_logtail import urls as logtail_urls

admin.autodiscover()

urlpatterns = patterns('',
    url(r'^admin/django_logtail/', include(logtail_urls)),
    url(r'^admin/', include(admin.site.urls)),
    (r'^aabuddy/', include('aabuddy.urls')),
)