# Ansible

Ansible files required for Felix release.

Before we can deploy the app, we need some files from [Felix Ansible Common](https://git.rakuten-it.com/projects/FELIX/repos/felix-ansible-common).

```
ansible-playbook import-ansible-common.yml -i hosts/local.ini --extra-vars "target_ansible_dir=${dir} git_branch=${branch-name}"
```

where,\
&nbsp;&nbsp;&nbsp;&nbsp;dir = `/path/to/repo/deployment/ansible`  
&nbsp;&nbsp;&nbsp;&nbsp;git_branch= `master`

In order to deploy the app, run following command.

```
ansible-playbook app.yml -i hosts/dev.ini --extra-vars "boot=true target_repo_dir=${dir}"
..
ansible-playbook app.yml -i hosts/prod-release-x01.ini --extra-vars "boot=true target_repo_dir=${dir}"
```

where dir = `/path/to/repo` (example), `target_repo_dir` should just point to root of project directory.