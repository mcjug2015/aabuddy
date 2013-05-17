''' csv loader module '''
# pylint: disable=C0103,C0111
import csv
import sys
from django.core.exceptions import ValidationError
import codecs
from aabuddy.models import Meeting
import datetime
from django.contrib.gis.geos.factory import fromstr
from aabuddy.views import find_similar_to_meeting
import logging
logger = logging.getLogger(__name__)


class PsvLoader():
    ''' loads csv files '''
    
    delimiter = None
    quote_char = None
    load_errors = None
    
    def __init__(self):
        ''' init the loader '''
        self.delimiter = '|'
        self.quote_char = '"'
        self.load_errors = []
        
    def load_psv(self, assigner, psv_in, skip_header=False):
        ''' load a whole csv '''
        self.load_errors = []
        row_number = 1
        reader = UnicodeReader(psv_in, delimiter=self.delimiter, quotechar=self.quote_char,
                               dialect=csv.excel, encoding="utf-8")
        if skip_header:
            reader.next()
            row_number += 1
        
        for row in reader:
            self.load_row(assigner, row, row_number)
            row_number += 1
        
        return self.load_errors
    
    def load_row(self, assigner, row, row_number):
        ''' load a list of values. row[0] = assignee, row[1] = reason, row[2] = num_points '''
        try:
            meeting = Meeting()
            meeting.name = row[0]
            meeting.description = row[1]
            meeting.day_of_week = row[2]
            meeting.start_time = datetime.datetime.strptime(row[3], '%H:%M:%S')
            meeting.end_time = datetime.datetime.strptime(row[4], '%H:%M:%S')
            meeting.address = row[5]
            meeting.geo_location = fromstr('POINT(%s %s)' % (row[7], row[6]), srid=4326)
            meeting.creator = assigner
            similar_meetings = find_similar_to_meeting(meeting)
            if similar_meetings:
                self.load_errors.append('#%s %s seems to be a duplicate of %s and %s other meetings. It will not be loaded' %
                                      (str(row_number), str(meeting), str(similar_meetings[0]), str(len(similar_meetings) - 1)))
            else:
                meeting.save()
        except ValidationError as error:
            logger.exception("#%s Could not load %s due to validation error: %s" % (str(row_number), str(row), error.messages[0]))
            self.load_errors.append("#%s Could not load %s due to validation error: %s" % (str(row_number), str(row), error.messages[0]))
        except:
            logger.exception("#%s Could not load %s due to unexpected error: %s" % (str(row_number), str(row),
                                                                                    str(sys.exc_info()[0])))
            self.load_errors.append("#%s Could not load %s due to unexpected error: %s" % (str(row_number), str(row),
                                                                                    str(sys.exc_info()[0])))


class UTF8Recoder:
    """
    code taken from http://docs.python.org/library/csv.html
    Iterator that reads an encoded stream and reencodes the input to UTF-8
    """
    def __init__(self, f, encoding):
        self.reader = codecs.getreader(encoding)(f)

    def __iter__(self):
        return self

    def next(self):
        return self.reader.next().encode("utf-8")


class UnicodeReader:
    """
    code taken from http://docs.python.org/library/csv.html
    A CSV reader which will iterate over lines in the CSV file "f",
    which is encoded in the given encoding.
    """

    def __init__(self, f, dialect=csv.excel, encoding="utf-8", **kwds):
        f = UTF8Recoder(f, encoding)
        self.reader = csv.reader(f, dialect=dialect, **kwds)

    def next(self):
        row = self.reader.next()
        return [unicode(s, "utf-8") for s in row]

    def __iter__(self):
        return self
