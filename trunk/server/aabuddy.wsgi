import os
import sys

print >> sys.stderr, "MOOOOO " + sys.path
#sys.path.append(os.path.dirname(__file__))
print >> sys.stderr, "MOOOOO1"
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
print >> sys.stderr, "MOOOOO2"
sys.path.append(os.path.join(os.path.dirname(__file__), '../..'))
print >> sys.stderr, "MOOOOO3"
os.environ['DJANGO_SETTINGS_MODULE'] = 'aabuddy.settings'
print >> sys.stderr, "MOOOOO4" + sys.path
import django.core.handlers.wsgi
print >> sys.stderr, "MOOOOO5" + sys.path
application = django.core.handlers.wsgi.WSGIHandler()
print >> sys.stderr, "MOOOOO6"