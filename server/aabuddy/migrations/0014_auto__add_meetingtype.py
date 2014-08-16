# -*- coding: utf-8 -*-
from south.utils import datetime_utils as datetime
from south.db import db
from south.v2 import SchemaMigration
from django.db import models


class Migration(SchemaMigration):

    def forwards(self, orm):
        # Adding model 'MeetingType'
        db.create_table(u'aabuddy_meetingtype', (
            (u'id', self.gf('django.db.models.fields.AutoField')(primary_key=True)),
            ('short_name', self.gf('django.db.models.fields.CharField')(max_length=250)),
            ('name', self.gf('django.db.models.fields.CharField')(max_length=250)),
            ('description', self.gf('django.db.models.fields.CharField')(max_length=250)),
        ))
        db.send_create_signal(u'aabuddy', ['MeetingType'])

        # Adding M2M table for field types on 'Meeting'
        m2m_table_name = db.shorten_name(u'aabuddy_meeting_types')
        db.create_table(m2m_table_name, (
            ('id', models.AutoField(verbose_name='ID', primary_key=True, auto_created=True)),
            ('meeting', models.ForeignKey(orm[u'aabuddy.meeting'], null=False)),
            ('meetingtype', models.ForeignKey(orm[u'aabuddy.meetingtype'], null=False))
        ))
        db.create_unique(m2m_table_name, ['meeting_id', 'meetingtype_id'])


    def backwards(self, orm):
        # Deleting model 'MeetingType'
        db.delete_table(u'aabuddy_meetingtype')

        # Removing M2M table for field types on 'Meeting'
        db.delete_table(db.shorten_name(u'aabuddy_meeting_types'))


    models = {
        u'aabuddy.activenottheresview': {
            'Meta': {'object_name': 'ActiveNotTheresView'},
            'created_date': ('django.db.models.fields.DateTimeField', [], {}),
            'distinct': ('django.db.models.fields.IntegerField', [], {}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'total': ('django.db.models.fields.IntegerField', [], {}),
            'unique_phone_id': ('django.db.models.fields.CharField', [], {'max_length': '400', 'null': 'True', 'blank': 'True'}),
            'user': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['auth.User']", 'null': 'True', 'on_delete': 'models.DO_NOTHING', 'blank': 'True'})
        },
        u'aabuddy.meeting': {
            'Meta': {'object_name': 'Meeting'},
            'address': ('django.db.models.fields.CharField', [], {'max_length': '300'}),
            'created_date': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime(1982, 12, 22, 0, 0)'}),
            'creator': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'meetings'", 'null': 'True', 'to': u"orm['auth.User']"}),
            'data_source': ('django.db.models.fields.CharField', [], {'default': "'user_submitted'", 'max_length': '255', 'null': 'True', 'blank': 'True'}),
            'day_of_week': ('django.db.models.fields.IntegerField', [], {'default': '1'}),
            'description': ('django.db.models.fields.CharField', [], {'max_length': '255', 'null': 'True', 'blank': 'True'}),
            'end_time': ('django.db.models.fields.TimeField', [], {'default': 'datetime.time(12, 30)'}),
            'geo_location': ('django.contrib.gis.db.models.fields.PointField', [], {}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'internal_type': ('django.db.models.fields.CharField', [], {'default': "'submitted'", 'max_length': '10'}),
            'is_active': ('django.db.models.fields.BooleanField', [], {'default': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            'start_time': ('django.db.models.fields.TimeField', [], {'default': 'datetime.time(11, 30)'}),
            'types': ('django.db.models.fields.related.ManyToManyField', [], {'blank': 'True', 'related_name': "'meetings'", 'null': 'True', 'symmetrical': 'False', 'to': u"orm['aabuddy.MeetingType']"})
        },
        u'aabuddy.meetingnotthere': {
            'Meta': {'object_name': 'MeetingNotThere'},
            'created_date': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'meeting': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'not_theres'", 'to': u"orm['aabuddy.Meeting']"}),
            'note': ('django.db.models.fields.CharField', [], {'max_length': '200', 'null': 'True', 'blank': 'True'}),
            'request_host': ('django.db.models.fields.CharField', [], {'max_length': '200', 'null': 'True', 'blank': 'True'}),
            'unique_phone_id': ('django.db.models.fields.CharField', [], {'max_length': '400', 'null': 'True', 'blank': 'True'}),
            'user': ('django.db.models.fields.related.ForeignKey', [], {'blank': 'True', 'related_name': "'not_theres'", 'null': 'True', 'on_delete': 'models.SET_NULL', 'to': u"orm['auth.User']"}),
            'user_agent': ('django.db.models.fields.CharField', [], {'max_length': '400', 'null': 'True', 'blank': 'True'})
        },
        u'aabuddy.meetingtype': {
            'Meta': {'object_name': 'MeetingType'},
            'description': ('django.db.models.fields.CharField', [], {'max_length': '250'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '250'}),
            'short_name': ('django.db.models.fields.CharField', [], {'max_length': '250'})
        },
        u'aabuddy.notthereview': {
            'Meta': {'object_name': 'NotThereView'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'latest_not_there': ('django.db.models.fields.DateTimeField', [], {}),
            'meeting': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['aabuddy.Meeting']", 'on_delete': 'models.DO_NOTHING'}),
            'meeting_name': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            'not_there_count': ('django.db.models.fields.IntegerField', [], {})
        },
        u'aabuddy.psvfileupload': {
            'Meta': {'object_name': 'PsvFileUpload'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'})
        },
        u'aabuddy.servermessage': {
            'Meta': {'object_name': 'ServerMessage'},
            'created_date': ('django.db.models.fields.DateTimeField', [], {'auto_now_add': 'True', 'blank': 'True'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'is_active': ('django.db.models.fields.BooleanField', [], {'default': 'True'}),
            'message': ('django.db.models.fields.TextField', [], {}),
            'short_message': ('django.db.models.fields.CharField', [], {'max_length': '255', 'null': 'True', 'blank': 'True'}),
            'updated_date': ('django.db.models.fields.DateTimeField', [], {'auto_now': 'True', 'blank': 'True'})
        },
        u'aabuddy.userconfirmation': {
            'Meta': {'object_name': 'UserConfirmation'},
            'confirmation_key': ('django.db.models.fields.CharField', [], {'max_length': '64'}),
            'created_date': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime(2014, 8, 16, 0, 0)'}),
            'expiration_date': ('django.db.models.fields.DateTimeField', [], {}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'user': ('django.db.models.fields.related.ForeignKey', [], {'related_name': "'confirmations'", 'to': u"orm['auth.User']"})
        },
        u'auth.group': {
            'Meta': {'object_name': 'Group'},
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '80'}),
            'permissions': ('django.db.models.fields.related.ManyToManyField', [], {'to': u"orm['auth.Permission']", 'symmetrical': 'False', 'blank': 'True'})
        },
        u'auth.permission': {
            'Meta': {'ordering': "(u'content_type__app_label', u'content_type__model', u'codename')", 'unique_together': "((u'content_type', u'codename'),)", 'object_name': 'Permission'},
            'codename': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            'content_type': ('django.db.models.fields.related.ForeignKey', [], {'to': u"orm['contenttypes.ContentType']"}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '50'})
        },
        u'auth.user': {
            'Meta': {'object_name': 'User'},
            'date_joined': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime.now'}),
            'email': ('django.db.models.fields.EmailField', [], {'max_length': '75', 'blank': 'True'}),
            'first_name': ('django.db.models.fields.CharField', [], {'max_length': '30', 'blank': 'True'}),
            'groups': ('django.db.models.fields.related.ManyToManyField', [], {'to': u"orm['auth.Group']", 'symmetrical': 'False', 'blank': 'True'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'is_active': ('django.db.models.fields.BooleanField', [], {'default': 'True'}),
            'is_staff': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'is_superuser': ('django.db.models.fields.BooleanField', [], {'default': 'False'}),
            'last_login': ('django.db.models.fields.DateTimeField', [], {'default': 'datetime.datetime.now'}),
            'last_name': ('django.db.models.fields.CharField', [], {'max_length': '30', 'blank': 'True'}),
            'password': ('django.db.models.fields.CharField', [], {'max_length': '128'}),
            'user_permissions': ('django.db.models.fields.related.ManyToManyField', [], {'to': u"orm['auth.Permission']", 'symmetrical': 'False', 'blank': 'True'}),
            'username': ('django.db.models.fields.CharField', [], {'unique': 'True', 'max_length': '30'})
        },
        u'contenttypes.contenttype': {
            'Meta': {'ordering': "('name',)", 'unique_together': "(('app_label', 'model'),)", 'object_name': 'ContentType', 'db_table': "'django_content_type'"},
            'app_label': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            u'id': ('django.db.models.fields.AutoField', [], {'primary_key': 'True'}),
            'model': ('django.db.models.fields.CharField', [], {'max_length': '100'}),
            'name': ('django.db.models.fields.CharField', [], {'max_length': '100'})
        }
    }

    complete_apps = ['aabuddy']