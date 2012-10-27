#!/usr/bin/env python
from fabric.api import env, local, sudo, require, run, put, settings, cd
import os
import sys
import time
try:
    import settings as base_settings
except ImportError:
    pass


# globals
env.prj_name = 'aabuddy'
env.user = 'aabuddy'
env.reject_unknown_hosts = False
env.mysql_super_user = 'root'


# environments
def localvm():
    "Use a local vmware instance"
    env.hosts = ['127.0.0.1:2222'] # replace by the one appropriate for you
    env.start_user = 'root' # the initial user pre-installed on image
    env.path = '/var/www/%(prj_name)s' % env
    env.virtualhost_path = env.path
    env.tmppath = '/var/tmp/django_cache/%(prj_name)s' % env
    env.deployment = 'localvm'
    env.use_ssh_keys = False

def prod():
    env.hosts = ['108.179.217.242'] # replace by the one appropriate for you
    env.start_user = 'root' # the initial user pre-installed on image
    env.path = '/var/www/%(prj_name)s' % env
    env.virtualhost_path = env.path
    env.tmppath = '/var/tmp/django_cache/%(prj_name)s' % env
    env.deployment = 'prod'
    env.use_ssh_keys = False

def _ensure_virtualenv():
    if "VIRTUAL_ENV" not in os.environ:
        sys.stderr.write("$VIRTUAL_ENV not found. Make sure to activate virtualenv first\n\n")
        sys.exit(-1)
    env.virtualenv = os.environ["VIRTUAL_ENV"]


def dev_setup():
    _ensure_virtualenv()
    local('mkdir -p reports')
    local('pip install -q -r deploy_requirements.txt')
    local('pip install -q -r dev_requirements.txt')


def _pep8():
    '''Run the pep8 tool to check code style'''
    local('pep8 -r --ignore=E501,W293 --exclude=migrations,tests.py aabuddy > reports/pep8-aabuddy.out')


def _pylint():
    '''Run the pep8 tool to check more code style and some basic flow checks'''
    local('pylint --rcfile=.pylintrc --ignore=migrations,tests.py aabuddy > reports/pylint-aabuddy.out')


def check():
    '''Run the static analysis tools'''
    dev_setup()
    _pep8()
    _pylint()


def test():
    '''Run the test suite and bail out if it fails'''
    dev_setup()
    local("python manage.py test --verbosity=2 --with-xunit --with-xcoverage --xunit-file=reports/nosetests.xml --xcoverage-file=reports/coverage.xml --cover-package=aabuddy --cover-html --cover-erase --cover-html-dir=reports/coverage")


def clean():
    '''Cleans up generated files from the local file system'''
    local('rm -rf reports')


def setup_user():
    ''' creates the user under which guru will run; localvm password is 1chpok1  '''
    user = env.user
    env.user = env.start_user
    run('useradd %s' % user)
    run('passwd %s' % user)
    run('usermod -a -G wheel %s' % user)
    if (env.use_ssh_keys):
        run('mkdir -p /home/{0}/.ssh && chmod 0700 /home/{0}/.ssh'.format(user), user=user)
        run('cp ~/.ssh/authorized_keys /home/{0}/.ssh && chown {0} /home/{0}/.ssh/authorized_keys'.format(user))
    env.user = user


def setup():
    """
    Setup a fresh virtualenv as well as a few useful directories, then run
    a full deployment
    """
    require('hosts')
    require('path')

    setup_user()

    # ensure apt is up to date
    sudo('yum update')
    # install subversion
    sudo('yum install -y subversion')

    # install webserver and database server
    # from here: http://www.venkysblog.com/install-python264-modwsgi-and-django-on-cento
    sudo('yum install -y httpd httpd-devel') # apache2-threaded
    with cd(''):
        sudo('wget http://modwsgi.googlecode.com/files/mod_wsgi-3.1.tar.gz')
        sudo('tar xvfz mod_wsgi-3.1.tar.gz')
    with cd('mod_wsgi-3.1'):
        sudo('./configure --with-python=/opt/python2.7.2/bin/python')
        sudo('make')
        sudo('make install')
        #nano /etc/httpd/conf/httpd.conf  
        #LoadModule wsgi_module /usr/lib64/httpd/modules/mod_wsgi.so
        #service httpd restart
        #httpd -M
        
        #nano -w /etc/httpd/conf/httpd.conf
            # append WSGISocketPrefix /etc/httpd/run/wsgi
        #/etc/httpd/conf.d/ <- .conf goes here
    
    #postgress
    #wget http://yum.pgrpms.org/9.1/redhat/rhel-5-x86_64/pgdg-centos91-9.1-4.noarch.rpm
    #rpm -i pgdg-centos91-9.1-4.noarch.rpm
    #nano -w /etc/yum.repos.d/CentOS-Base.repo
        #for [base] and [update]
        #exclude=postgresql*  
    #yum list postgres*  to verify 9.1
    #yum install postgresql-devel postgresql-server postgresql-contrib
    #service postgresql-9.1 initdb
    #service postgresql-9.1 start
    #service postgresql-9.1 stop
    #nano -w /var/lib/pgsql/9.1/data/postgresql.conf
        #listen_addresses = '*'
        #port = 5432
    #nano -w /var/lib/pgsql/9.1/data/pg_hba.conf
        #local    all    all    trust
        #host    all         all         127.0.0.1/32          trust
    #service postgresql-9.1 start
    #su - postgres
    #createdb aabuddy
    #psql aabuddy
    #CREATE ROLE aabuddy WITH SUPERUSER LOGIN PASSWORD '1chpok1';
    #su - root
    #psql -Uaabuddy aabuddy
    
    #postgis 1.5:
    #yum install postgis91 postgis91-utils
    #psql -d aabuddy -f /usr/pgsql-9.1/share/contrib/postgis-1.5/postgis.sql -U aabuddy
    #psql -d aabuddy -f /usr/pgsql-9.1/share/contrib/postgis-1.5/spatial_ref_sys.sql -U aabuddy
    #
    #
    #
    #
    
    
    #chkconfig httpd on
    #chkconfig postgresql-9.1 on
    
    # easy_install(as root):
    # wget http://pypi.python.org/packages/2.7/s/setuptools/setuptools-0.6c11-py2.7.egg
    # sh ./setuptools-0.6c11-py2.7.egg
    # pip:
    # sudo easy_install-2.7 pip
    # 
    # virutalenv
    # sudo pip-2.7 install virtualenv
    
    
    # CREATE AND SOURCE AABUDDY VIRTUALENV!
    # fabric:
    # pip install Fabric==1.4.3
    
    
    #psycopg2 make sure pg_config is on the path.
    #run('cd ~/; source .profile; cd %(path)s; source local-python/bin/activate; easy_install psycopg2' % env, pty=True)
    
    # disable default site
    with settings(warn_only=True):
        sudo('rm /etc/apache2/sites-enabled/*-default', pty=True)

    # new project setup
    sudo('mkdir -p %(path)s; chown %(user)s:%(user)s %(path)s;' % env, pty=True)
    sudo('mkdir -p %(tmppath)s; chown %(user)s:%(user)s %(tmppath)s;' % env, pty=True)
    with settings(warn_only=True):
        run('cd ~; ln -s %(path)s www;' % env, pty=True) # symlink web dir in home
    with cd(env.path):
        run('virtualenv --no-site-packages local-python') # activate with 'source ~/www/bin/activate'
        with settings(warn_only=True):
            run('mkdir -m a+w logs; mkdir releases; mkdir shared; mkdir backup;', pty=True)
            run('cd releases; ln -s . current; ln -s . previous;', pty=True)


def deploy_workingenv():
    env.release = time.strftime('%Y%m%d%H%M%S')
    upload_local_archive()
    _deploy_release()


def _deploy_release():
    """
    Deploy the latest version of the site to the servers, install any
    required third party modules, install the virtual host and
    then restart the webserver
    """
    require('hosts')
    require('path')
    symlink_current_release()
    install_requirements()
    install_site()
    migrate()
    restart_webserver()


def switch_to_version(version):
    "Specify a specific version to be made live"
    require('hosts')
    require('path')
    env.version = version
    with cd(env.path):
        run('rm -rf releases/previous; mv releases/current releases/previous;', pty=True)
        run('ln -s %(version)s releases/current' % env, pty=True)
    restart_webserver()


def upload_local_archive():
    "Create an archive from the working environment and upload it"
    require('release')
    local('mkdir -p dist')
    local('tar --exclude=.svn --exclude=local-python --exclude=dist --exclude=.cache --exclude=.settings --exclude=reports -czvf dist/%(release)s.tar.gz .' % env)
    run('mkdir -p %(path)s/releases/%(release)s/%(prj_name)s' % env, pty=True)
    put('dist/%(release)s.tar.gz' % env, '%(path)s/' % env)
    run('cd %(path)s/releases/%(release)s/%(prj_name)s && tar zxf ../../../%(release)s.tar.gz' % env, pty=True)
    local('rm -rf dist' % env)


def install_site():
    "Add the virtualhost config file to the webserver's config, activate logrotate"
    require('release')
    with cd('%(path)s/releases/%(release)s/%(prj_name)s' % env):
        if env.deployment == 'localvm':
            sudo('cp environments/%(deployment)s/aabuddy.conf /etc/httpd/conf.d/%(prj_name)s.conf' % env, pty=True)
        elif env.deployment == 'prod':
            sudo('cp environments/%(deployment)s/aabuddy.conf /usr/local/apache/conf/includes/%(prj_name)s.conf' % env, pty=True)
    with cd('%(path)s/releases/%(release)s' % env):
        run('mkdir static && cd static && ln -s %(path)s/local-python/lib/python2.7/site-packages/django/contrib/admin/static/admin/ admin' % env)


def install_requirements():
    "Install the required packages from the requirements file using pip"
    require('release')
    run('cd %(path)s; source local-python/bin/activate; local-python/bin/pip install -r ./releases/%(release)s/%(prj_name)s/deploy_requirements.txt' % env, pty=True)


def symlink_current_release():
    "Symlink our current release"
    require('release')
    with cd(env.path):
        run('rm releases/previous; mv releases/current releases/previous;', pty=True)
        run('ln -s %(release)s releases/current' % env, pty=True)


def migrate():
    "Update the database"
    require('prj_name')
    require('path')
    with cd('%(path)s/releases/current/%(prj_name)s' % env):
        run('cp environments/%(deployment)s/%(deployment)s_settings.py local_settings.py' % env, pty=True)
        run('%(path)s/local-python/bin/python manage.py syncdb --noinput --settings=settings' % env, pty=True)
        run('%(path)s/local-python/bin/python manage.py migrate --settings=settings aabuddy' % env)
        run('%(path)s/local-python/bin/python manage.py loaddata aabuddy initial_users --settings=settings' % env)


def restart_webserver():
    "Restart the web server"
    with settings(warn_only=True):
        sudo('cd /usr/local/apache/bin; ./apachectl restart;', pty=True)
