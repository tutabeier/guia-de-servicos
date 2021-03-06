Vagrant.configure('2') do |config|

  config.vm.box = 'https://s3-sa-east-1.amazonaws.com/servicosgovbr/base.box'
  config.ssh.forward_agent = true

  if Vagrant.has_plugin? 'vagrant-cachier'
    config.cache.scope = :box
    config.cache.enable :yum
    config.cache.enable :generic, {
      'wget' => { cache_dir: '/var/cache/wget' }
    }
  end

  config.vm.provider 'virtualbox' do |vb|
    vb.customize ['modifyvm', :id, '--memory', '512']
  end

  config.vm.define 'ie8', autostart: false do |ie11|
    ie11.vm.box = "http://aka.ms/vagrant-win7-ie8"
    ie11.vm.communicator = :winrm

    ie11.vm.provider 'virtualbox' do |ieVb|
      ieVb.gui = true
      ieVb.customize ['modifyvm', :id, '--memory', '1024']
    end
  end

  config.vm.define 'bastion', primary: true do |lb|
    lb.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/vagrant/bastion-node-install'
    lb.vm.network 'private_network', ip: '10.16.0.180'
  end

  config.vm.define 'es1' do |es|
    es.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/prod-like/es-node-install 10.16.0.11 10.16.0.9'
    es.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/vagrant/es-node-install es1'
    es.vm.network 'private_network', ip: '10.16.0.11'
  end

  config.vm.define 'es2' do |es|
    es.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/prod-like/es-node-install 10.16.0.9 10.16.0.11'
    es.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/vagrant/es-node-install es2'
    es.vm.network 'private_network', ip: '10.16.0.9'
  end

  config.vm.define 'piwik' do |pw|
    pw.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/vagrant/piwik-node-install'
    pw.vm.network 'forwarded_port', guest: 80, host: 8084
    pw.vm.network 'private_network', ip: '10.16.0.181'
  end

  config.vm.define 'app1' do |app|
    app.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/prod-like/app-node-install 10.16.0.11 10.16.0.9'
    app.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/vagrant/app-node-install app1 10.16.0.11 10.16.0.9 10.16.0.181'
    app.vm.network 'forwarded_port', guest: 8080, host: 8082
    app.vm.network 'private_network', ip: '10.16.0.13'
  end

  config.vm.define 'app2' do |app|
    app.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/prod-like/app-node-install 10.16.0.11 10.16.0.9'
    app.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/vagrant/app-node-install app2 10.16.0.11 10.16.0.9 10.16.0.181'
    app.vm.network 'forwarded_port', guest: 8080, host: 8083
    app.vm.network 'private_network', ip: '10.16.0.12'
  end

  config.vm.define 'lb' do |lb|
    lb.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/prod-like/lb-node-install 10.16.0.13 10.16.0.12'
    lb.vm.provision :shell, inline: '/bin/bash /vagrant/scripts/vagrant/lb-node-install lb'
    lb.vm.network 'forwarded_port', guest: 80, host: 8081
    lb.vm.network 'private_network', ip: '10.16.0.10'
  end

end
