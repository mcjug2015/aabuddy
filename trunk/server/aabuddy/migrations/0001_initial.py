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
            ('dayOfWeek', self.gf('django.db.models.fields.CharField')(default='Monday', max_length=10)),
            ('timeOfDate', self.gf('django.db.models.fields.DateTimeField')(default=datetime.datetime(1982, 12, 22, 0, 0))),
            ('latitude', self.gf('django.db.models.fields.FloatField')()),
            ('longitude', self.gf('django.db.models.fields.FloatField')()),
            ('description', self.gf('django.db.models.fields.CharField')(max_length=255)),
            ('address', self.gf('django.db.models.fields.CharField')(max_length=255)),
            ('internal_type', self.gf('django.db.models.fields.CharField')(default='Submitted', max_length=10)),
        ))
        db.send_create_signal('aabuddy', ['Meeting'])


    def backwards(self, orm):
        # Deleting model 'Meeting'
        db.delete_table('aabuddy_meeting')


    models = {
        'aabuddy.meeting': {
            'Meta': {'object_name': 'Meeting'},
            'address': ('django.db.models.fields.CharField', [], {'max_length': '255'}),
            'dayOfWeek': ('django.db.models.fields.CharField', [], {'default': "'Monday'", 'max_length': '10'}),
            'description': ('django.db.models.fields.CharField', [], {'max_length': '255'}),
            'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'internal_type': ('django.db.models.fields.CharField', [], {'default': "'Submitted'", 'max_length': '10'}),
            'latitude': ('django.db.models.fields.FloatField', [], {}),
            'longitude': ('django.db.models.fields.FloatField', [], {}),
            'timeOfDate': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime(1982, 12, 22, 0, 0)'})
        }
    }

    complete_apps = ['aabuddy']