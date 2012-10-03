# -*- coding: utf-8 -*-
import datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'Meeting'
        db.create_table('aabuddy_meeting', (
            ('id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('day_of_week', self.gf('django.db.models.fields.CharField')(default='Monday', max_length=10)),
            ('start_time', self.gf('django.db.models.fields.TimeField')(default=datetime.time(11, 30))),
            ('end_time', self.gf('django.db.models.fields.TimeField')(default=datetime.time(11, 30))),
            ('name', self.gf('django.db.models.fields.CharField')(max_length=100)),
            ('description', self.gf('django.db.models.fields.CharField')(max_length=255, null=True, blank=True)),
            ('address', self.gf('django.db.models.fields.CharField')(max_length=300)),
            ('internal_type', self.gf('django.db.models.fields.CharField')(default='Submitted', max_length=10)),
            ('geo_location', self.gf('django.contrib.gis.db.models.fields.PointField')()),
        ))
        db.send_create_signal('aabuddy', ['Meeting'])


    def backwards(self, orm):
        # Deleting model 'Meeting'
        db.delete_table('aabuddy_meeting')


    models = {
        'aabuddy.meeting': {
            'Meta': {'object_name': 'Meeting'},
            'address': ('django.db.models.fields.CharField', [], {'max_length': '300'}),
            'day_of_week': ('django.db.models.fields.CharField', [], {'default': "'Monday'", 'max_length': '10'}),
            'description': ('django.db.models.fields.CharField', [], {'max_length': '255', 'null': 'True', 'blank': 'True'}),
            'end_time': ('django.db.models.fields.TimeField', [], {'default': 'datetime.time(11, 30)'}),
            'geo_location': ('django.contrib.gis.db.models.fields.PointField', [], {}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'internal_type': ('django.db.models.fields.CharField', [], {'default': "'Submitted'", 'max_length': '10'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            'start_time': ('django.db.models.fields.TimeField', [], {'default': 'datetime.time(11, 30)'})
        }
    }

    complete_apps = ['aabuddy']