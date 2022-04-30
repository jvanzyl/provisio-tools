 # Provisio

- user profiles are relocatable, all symlinks are relative to PROVISIO_ROOT
- tool:version = installation
- tool:version:userdata = ???
- installs are unpacked and left as they would be be if manually unpacked, so if you need to debug it's easier. Symlinks are created to the original structure
- many profiles reuse as much from a shared installation as possible, layer where necessary
- accounts for tool installation that change substantively between versions like krew 0.4.1 and 0.4.2
- no connection to source control is required
- a version of tool is called an installation, there can be many installations for a given tool
- supported shells: bash, zsh, fish
- replicate profiles to internal object storage or servers to ensure profiles are available to your users
- sigstore integration to ensure binaries can be validated
- we endeavor to make OSX the same as Linux with GNU core utils: https://apple.stackexchange.com/questions/69223/how-to-replace-mac-os-x-utilities-with-gnu-core-utilities
- careful layering of installations and what can be shared versus what cannot:
  - jenv has an installation, but modifications are made to that installation adding new JVMs and plugins
  - krew has an installation, but modifications are made to that installation adding new plugins
- care is taken to make sure all downlads are intact
- docker images are built such that each tool is in its own layer so deltas are efficient, like JIB
- when provisio is activated all resources modified that are used by your applications are restore: jdk.table.xml for IDEA for example
- utility for automating the generation of new tool descriptors
- multi field version support
- command to update versions of tools
- schemas/versions for tool descriptors
- schemas/versions for profile descriptors

# Adding Tools

It needs to be very simple to add new tools.

The following tools have built-in support:
- GitHub releases