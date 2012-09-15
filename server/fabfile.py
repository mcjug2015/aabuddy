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
    env.hosts = ['192.168.1.4'] # replace by the one appropriate for you
    env.start_user = 'root' # the initial user pre-installed on image
    env.path = '/var/www/%(prj_name)s' % env
    env.virtualhost_path = env.path
    env.tmppath = '/var/tmp/django_cache/%(prj_name)s' % env
    env.deployment = 'localvm'
    env.use_ssh_keys = False
    env.mysql_super_user = 'root'


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


def local_db_init():
    db_config = base_settings.DATABASES
    env.db_user, env.db_pass, env.db_name, env.db_host, env.db_port = (db_config['default'][key] for key in
                                                                       ('USER', 'PASSWORD', 'NAME', 'HOST', 'PORT'))
    local('''mysql -h localhost -u %(mysql_super_user)s -p -e "grant usage on *.* to '%(db_user)s'@'localhost';
                                                drop user '%(db_user)s'@'localhost';
                                                drop database if exists %(db_name)s;
                                                create user '%(db_user)s'@'localhost' identified by '%(db_pass)s';
                                                create database %(db_name)s character set 'utf8';
                                                grant all privileges on %(db_name)s.* to '%(db_user)s'@'localhost';
                                                flush privileges;"''' % env)
    local('''python manage.py syncdb --noinput''')
    local('''python manage.py migrate''')


def clean():
    '''Cleans up generated files from the local file system'''
    local('rm -rf reports')


def setup_user():
    ''' creates the user under which guru will run  '''
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
    sudo('yum install -y apache2 apache2-dev apache2-utils') # apache2-threaded
    sudo('yum install -y libapache2-mod-wsgi') # outdated on hardy!
    sudo('yum install -y mysql-client libmysqlclient-dev python-mysqldb')

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
            
    ''' 
    TODO XXX !!!
    easy_install psycopg2
    for psql
    '''

def _get_default_db():
    sys.path.append(os.path.dirname(__file__))
    deployment_settings = __import__('environments.%(deployment)s.%(deployment)s_settings' % env, globals(), locals(), ['DATABASES'])
    db_config = getattr(deployment_settings, 'DATABASES', None) or base_settings.DATABASES
    return db_config['default']


def install_db():
    default_db = _get_default_db()
    env.db_user, env.db_pass, env.db_name, env.db_host, env.db_port = (default_db[key] for key in
                                                                       ('USER', 'PASSWORD', 'NAME', 'HOST', 'PORT'))

    if env.db_host == 'localhost':
        sudo('yum install -y mysql-server')
    run('''mysql -h %(db_host)s -P %(db_port)s -u %(mysql_super_user)s -p -e "create user '%(db_user)s' identified by '%(db_pass)s';
                                                create database %(db_name)s character set 'utf8';
                                                grant all privileges on %(db_name)s.* to '%(db_user)s'@'%%';
                                                flush privileges;"''' % env, pty=True)


def reinstall_db():
    default_db = _get_default_db()
    env.db_user, env.db_pass, env.db_name, env.db_host, env.db_port = (default_db[key] for key in
                                                                       ('USER', 'PASSWORD', 'NAME', 'HOST', 'PORT'))

    run('''mysql -h %(db_host)s -P %(db_port)s -u %(mysql_super_user)s -p -e "grant usage on *.* to '%(db_user)s';
                                                drop user '%(db_user)s';
                                                drop database if exists %(db_name)s;
                                                create user '%(db_user)s' identified by '%(db_pass)s';
                                                create database %(db_name)s character set 'utf8';
                                                grant all privileges on %(db_name)s.* to '%(db_user)s'@'%%';
                                                flush privileges;"''' % env, pty=True)


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
        sudo('cp environments/%(deployment)s/pluginwarehouse.conf /etc/apache2/sites-available/%(prj_name)s' % env, pty=True)
        # try logrotate
        with settings(warn_only=True):
            sudo('cp logrotate.conf /etc/logrotate.d/website-%(prj_name)s' % env, pty=True)
    with cd('%(path)s/releases/%(release)s' % env):
        run('mkdir static && cd static && ln -s %(path)s/local-python/lib/python2.6/site-packages/django/contrib/admin/media admin' % env)
    with settings(warn_only=True):
        sudo('cd /etc/apache2/sites-enabled/; ln -s ../sites-available/%(prj_name)s %(prj_name)s' % env, pty=True)
        run('gpg --import %(path)s/releases/current/%(prj_name)s/environments/secret.key' % env)


def install_requirements():
    "Install the required packages from the requirements file using pip"
    require('release')
    run('cd %(path)s; local-python/bin/pip install -r ./releases/%(release)s/%(prj_name)s/deploy_requirements.txt' % env, pty=True)


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
        run('%(path)s/local-python/bin/python manage.py syncdb --noinput' % env, pty=True)
        run('%(path)s/local-python/bin/python manage.py migrate' % env)


def restart_webserver():
    "Restart the web server"
    with settings(warn_only=True):
        sudo('/etc/init.d/apache2 reload', pty=True)
