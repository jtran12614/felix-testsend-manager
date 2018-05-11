# Ansible deployment
Before run below command, install `helper-tasks` and `group_vars/${env}/vault` and `host_vars/*.yml`
from : [felix-ansible-repository](https://git.rakuten-it.com/projects/FELIX/repos/felix-ansible/browse)

In order to run installation, run following command.

```
ansible-playbook main.yml -i hosts/dev.ini
ansible-playbook main.yml -i hosts/stg-test.ini
ansible-playbook main.yml -i hosts/stg-bu.ini
ansible-playbook main.yml -i hosts/prod.ini
```

You can stop existing service process and start latest service process using extra_vars like below.

```
ansible-playbook main.yml -i hosts/dev.ini --extra-vars "boot=true"
```