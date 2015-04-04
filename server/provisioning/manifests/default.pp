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
                         "mlocate",
                         "nano",
                         "htop",
                         "postgresql",
                         "postgresql-devel",
                         "rabbitmq-server",]

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

class finalize_box {
  file_line { "set selinux to permissive":
      path => "/etc/selinux/config",
      line => "SELINUX=permissive",
      match => "^(SELINUX=enforcing)$",
      require => [Class["setup_wsgi"], Class["setup_httpd"], Class["install_pip_venv"], Class["dependencies"], Class["create_groups_users_dirs"], ],
  }
}
include finalize_box