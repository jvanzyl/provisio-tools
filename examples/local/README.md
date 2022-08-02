# Onboarding

Provided in this repository is a mechanism for provisioning a consistent set of tools for SREs and developers. To see what tools make up the profile you can look at `.provisio/profiles/sre/profile.yaml`

To provision the `sre` tools profile run the following:

```
./onboard-sre
```

You will see output like the following:

```
Initializing provisio[profile=aetion with /Users/jason.vanzyl/.provisio/profiles/aetion/profile.yaml]
Checking prerequistes for OSX ...
mc[4.8.27] Up to date
rekor-cli[0.5.0] Up to date
pulumi[3.22.1] Up to date
java[jdk8u312-b07, jdk-11.0.12+7, jdk-17+35] pathManagedBy(jenv)  Up to date
jenv[master] plugins[export, maven] Up to date
vscodium[1.63.2] Up to date
node[16.6.2] Up to date
gitleaks[7.5.0] Up to date
dive[0.10.0] Up to date
goreleaser[0.174.1] Up to date
go[1.17.6] Up to date
terraform[0.15.3] Up to date
yq[4.20.1] Up to date
cr[1.2.1] Up to date
mustache[1.2.1] Up to date
kind[0.11.1] Up to date
eksctl[0.77.0] Up to date
aws-cli[2.4.22] Up to date
sops[3.7.1] Up to date
jq[1.6] Up to date
kubectl[1.21.6] Up to date
helm[3.8.0] Up to date
krew[0.4.2] plugins[cert-manager, konfig, ctx, ns] Up to date
bats[master] Up to date

Updated: /Users/jason.vanzyl/.zprofile

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
