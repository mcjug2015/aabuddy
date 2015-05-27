$db_pg_password = "NOPE"
$db_aabuddy_password = "NOPE"
include repoforge
include epel
$aabuddy_folder = "/opt/aabuddy/"
$aabuddy_log_folder = "/opt/aabuddy/logs/"
$aabuddy_user_home = "/home/aabuddy/"

class create_groups_users_dirs {
  group { "aabuddyowners":
          ensure => present,
          gid    => 1002
  }
  user { "aabuddy":
          ensure     => present,
          gid        => "aabuddyowners",
          membership => minimum,
          require    => Group["aabuddyowners"],
          home       => $aabuddy_user_home,
          managehome => true,
  }
  
  class { 'sudo':
    purge               => false,
    config_file_replace => false,
  }
  
  class privileges {
    sudo::conf { 'wheel':
      ensure  => present,
      content => '%wheel ALL=(ALL) NOPASSWD: ALL',
    }
  }
  include privileges
  user { "aabuddy_sudo":
          ensure     => present,
          gid        => "aabuddyowners",
          membership => minimum,
          require    => [Group["aabuddyowners"], Class["privileges"], ],
          groups     => ["wheel"],
          home       => "/home/aabuddy_sudo/",
          managehome => true,
  }
  
  $aabuddy_dirs = [$aabuddy_folder,
                   $aabuddy_log_folder,
                   "${aabuddy_folder}served_content"]
  file { $aabuddy_dirs:
      ensure => "directory",
      owner  => "aabuddy",
      group  => "aabuddyowners",
      mode   => "700",
      require    => [Group["aabuddyowners"], User["aabuddy"], ],
  }
}
include create_groups_users_dirs

class dependencies{
    $aabuddy_packages = ["wget",
                         "nano",
                         "htop",
                         "postgresql-devel",]

    package { $aabuddy_packages:
        ensure => latest,
        require    => [Class["repoforge"], Class["epel"], ],
    }
}
include dependencies

class install_pip_venv {
  exec { "download get-pip":
      command => "/usr/bin/wget --no-check-certificate --no-verbose --output-document='/tmp/get-pip.py' 'https://bootstrap.pypa.io/get-pip.py'",
      cwd     => "/tmp/",
      creates => "/tmp/get-pip.py",
      require => Class["dependencies"],
  }
  
  exec { "install pip":
      command => "/usr/bin/python /tmp/get-pip.py",
      cwd     => "/tmp/",
      creates => "/usr/bin/pip",
      user  => "root",
      group  => "root",
      require => Exec["download get-pip"],
  }
  
  exec { "install virtualenv":
      command => "/usr/bin/pip install virtualenv==12.0.7",
      cwd     => "/tmp/",
      creates => "/usr/bin/virtualenv",
      user  => "root",
      group  => "root",
      require => Exec["install pip"],
  }
    
  exec { "create venv":
      command => "/usr/bin/virtualenv ${aabuddy_folder}venv/",
      cwd     => "/tmp/",
      creates => "${aabuddy_folder}venv/bin/activate",
      group   => "aabuddyowners",
      user    => "aabuddy",
      require => [Exec["install virtualenv"], Class["create_groups_users_dirs"], ],
  }
}
include install_pip_venv

class setup_httpd {
  class { "apache":  
      confd_dir => "${aabuddy_folder}apache_conf.d/",
  }
}
include setup_httpd

class setup_wsgi {
  class { 'apache::mod::wsgi':
    wsgi_socket_prefix => "/var/run/wsgi",
    wsgi_python_home   => '${aabuddy_folder}venv',
    wsgi_python_path   => '${aabuddy_folder}venv/lib/python2.7/site-packages',
    require => [Class["setup_httpd"], ],
  }
  file_line { "apache user aabuddy":
      path => "/etc/httpd/conf/httpd.conf",
      line => "User aabuddy",
      match => "^(User apache)$",
      require => [Class["apache::mod::wsgi"], ],
  }
  file_line { "apache group aabuddyowners":
      path => "/etc/httpd/conf/httpd.conf",
      line => "Group aabuddyowners",
      match => "^(Group apache)$",
      require => [Class["apache::mod::wsgi"], ],
  }
}
include setup_wsgi


class do_postgres {

class {'postgresql::globals':
  version => '9.3',
  manage_package_repo => true,
  encoding => 'UTF8',
  locale   => "en_US.UTF-8",
}->
class { 'postgresql::server':
  listen_addresses => '*',
  pg_hba_conf_defaults => false,
}

postgresql::server::pg_hba_rule { 'ident local postgres':
  description => "ident local postgres",
  type => 'local',
  database => 'all',
  user => 'postgres',
  auth_method => 'ident',
  order => 100,
}

postgresql::server::pg_hba_rule { 'md5 local all':
  description => "md5 local all",
  type => 'local',
  database => 'all',
  user => 'all',
  auth_method => 'md5',
  order => 200,
}

postgresql::server::pg_hba_rule { 'md5 localhost all':
  description => "md5 local all",
  type => 'host',
  database => 'all',
  user => 'all',
  address => '127.0.0.1/32',
  auth_method => 'md5',
  order => 200,
}

# Install contrib modules
class { 'postgresql::server::contrib':
  package_ensure => 'present',
}

  postgresql::server::db { 'aabuddy':
    user     => 'aabuddy',
    password => postgresql_password("aabuddy", "${db_aabuddy_password}"),
    require => [Class["postgresql::globals"], Class["postgresql::server"], Class["postgresql::server::contrib"]],
  }
  
  exec {"set postgres password":
    command => "/usr/bin/psql -c \"ALTER USER Postgres WITH PASSWORD '${db_pg_password}';\"",
    group   => "postgres",
    user    => "postgres",
    require => [Postgresql::Server::Db["aabuddy"],],
  }
  
  exec {"install postgis2":
    command => "/bin/yum -y install postgis2_93",
    group   => "root",
    user    => "root", 
    require => [Class["repoforge"], Class["epel"], 
                Class["dependencies"], Class["setup_wsgi"], 
                Class["setup_httpd"], Class["install_pip_venv"],
                Postgresql::Server::Db["aabuddy"], Exec["set postgres password"], ],
  }
  
  exec {"install postgis extensions":
    command => "/usr/bin/psql aabuddy -c \"CREATE EXTENSION postgis; CREATE EXTENSION postgis_topology; CREATE EXTENSION fuzzystrmatch; CREATE EXTENSION postgis_tiger_geocoder;\"",
    group   => "postgres",
    user    => "postgres",  
    require => [Exec["install postgis2"], ],
  }
}
include do_postgres


class finalize_box {
  file_line { "set selinux to permissive":
      path => "/etc/selinux/config",
      line => "SELINUX=permissive",
      match => "^(SELINUX=enforcing)$",
      require => [Class["setup_wsgi"], Class["setup_httpd"], Class["install_pip_venv"], Class["dependencies"], Class["create_groups_users_dirs"], 
                  Class["do_postgres"], ],
  }
}
