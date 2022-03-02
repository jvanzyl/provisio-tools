# TODO

- [x] need one directory of resources to extract from the classpath so I can move/rename atomically
- [ ] if tool resources are removed they have to be removed on disk, i removed bash-template.txt for krew and it's still in .provisio
- [ ] collect all constants in one place, too hard to follow them everywhere
- [ ] fish shell support, shell generation, templates
- [ ] M1/arm support (emulation works)
- [ ] if there are no symlink or current file for the profile use the default profile directory
- [ ] need tests on a per tool basis
- [ ] allow specification of the shell init files to generate (docker builds have no SHELL envar)
- [ ] progress meters when downloading/updating
- [ ] fix message when updating, tool says up-to-date when it's not
- [ ] fix selfupdate message, profile shows as null
- [ ] fix NPE when specified tool has no descriptor
- [ ] errors, stack traces to GitHub issues automatically for fast fixing, being responsive to users
- [ ] send jenv setup messages to /dev/null
- [ ] create tool descriptors and make a PR
- [ ] prepare to post tool descriptors to a site
- [ ] from the CLI search for tools and add them to a profile
- [ ] krew has two path that need to be added, parse a csv or make a list of paths
- [ ] jenv and krew bash-template.txt, just inline that in the descriptor to keep it in one place
- [x] fix error trying to copy profile after install

