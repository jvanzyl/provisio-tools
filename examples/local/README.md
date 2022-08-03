# Onboarding

Provided in this repository is a mechanism for provisioning a consistent set of tools for SREs and developers. To see what tools make up the profile you can look at `.provisio/profiles/sre/profile.yaml`

To provision the `sre` tools profile run the following:

```
./onboard-sre
```

You will see output like the following:

```
Initializing provisio[profile=sre with .provisio/profiles/sre/profile.yaml, os=Darwin, arch=arm64]
Checking prerequistes for OSX ...
rekor-cli[0.5.0] Up to date
pulumi[3.37.2] Up to date
java[jdk-17.0.3+7] pathManagedBy(jenv)  Up to date
jenv[master] plugins[export, maven] Up to date
temurin64-17.0.3 added
17.0.3 added
17.0 added
 17.0.3 already present, skip installation
vscodium[1.69.2] Up to date
node[16.6.2] Up to date
gitleaks[8.9.0] Up to date
dive[0.10.0] Up to date
goreleaser[0.174.1] Up to date
go[1.18.5] Up to date
terraform[1.2.6] Up to date
yq[4.20.1] Up to date
mustache[1.4.0] Up to date
kind[0.12.0] Up to date
eksctl[0.77.0] Up to date
aws-cli[2.7.20] Up to date
sops[3.7.3] Up to date
jq[1.6] Up to date
kubectl[1.21.6] Up to date
helm[3.9.2] Up to date
krew[0.4.3] plugins[cert-manager, konfig, ctx, ns] Up to date
Adding "default" plugin index from https://github.com/kubernetes-sigs/krew-index.git.
Updated the local copy of plugin index.
WARNING: Detected stdin, but discarding it because of --manifest or args
Installing plugin: cert-manager
Installed plugin: cert-manager
\
 | Use this plugin:
 | 	kubectl cert-manager
 | Documentation:
 | 	https://github.com/cert-manager/cert-manager
/
WARNING: You installed plugin "cert-manager" from the krew-index plugin repository.
   These plugins are not audited for security by the Krew maintainers.
   Run them at your own risk.
Updated the local copy of plugin index.
WARNING: Detected stdin, but discarding it because of --manifest or args
Installing plugin: konfig
Installed plugin: konfig
\
 | Use this plugin:
 | 	kubectl konfig
 | Documentation:
 | 	https://github.com/corneliusweig/konfig
/
WARNING: You installed plugin "konfig" from the krew-index plugin repository.
   These plugins are not audited for security by the Krew maintainers.
   Run them at your own risk.
Updated the local copy of plugin index.
WARNING: Detected stdin, but discarding it because of --manifest or args
Installing plugin: ctx
Installed plugin: ctx
\
 | Use this plugin:
 | 	kubectl ctx
 | Documentation:
 | 	https://github.com/ahmetb/kubectx
 | Caveats:
 | \
 |  | If fzf is installed on your machine, you can interactively choose
 |  | between the entries using the arrow keys, or by fuzzy searching
 |  | as you type.
 |  | See https://github.com/ahmetb/kubectx for customization and details.
 | /
/
WARNING: You installed plugin "ctx" from the krew-index plugin repository.
   These plugins are not audited for security by the Krew maintainers.
   Run them at your own risk.
Updated the local copy of plugin index.
WARNING: Detected stdin, but discarding it because of --manifest or args
Installing plugin: ns
Installed plugin: ns
\
 | Use this plugin:
 | 	kubectl ns
 | Documentation:
 | 	https://github.com/ahmetb/kubectx
 | Caveats:
 | \
 |  | If fzf is installed on your machine, you can interactively choose
 |  | between the entries using the arrow keys, or by fuzzy searching
 |  | as you type.
 | /
/
WARNING: You installed plugin "ns" from the krew-index plugin repository.
   These plugins are not audited for security by the Krew maintainers.
   Run them at your own risk.
bats[master] Up to date

Updated: /Users/jason.vanzyl/.zprofile
jenv has been updated, process to refresh plugin links
Refresh plugin export
Refresh plugin maven
```

Provisio will detect the shell you are using and make a one line modification to your shell initialization script. In this case `${HOME}/.zprofile` is modified, and you will see the following within:

```
jason.vanzyl@yoshi ~ % cat .zprofile
#---- provisio-start ----
source ${HOME}/.provisio/bin/profiles/profile/.init.bash
#---- provisio-end ----

...
```

The tool profile provisioned is enabled by the addition of one line, and to disable it just remove the provisio lines, restart your shell and your environment will be restored to its previous state.
