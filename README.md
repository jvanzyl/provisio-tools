 # Provisio

- tool:version = installation
- tool:version:userdata = ???
- user profiles are relocatable, all symlinks are relative to PROVISIO_ROOT
- installs are unpacked and left as they would be be if manually unpacked, so if you need to debug it's easier. Symlinks are created to the original structure
- many profiles reuse as much from a shared installation as possible, layer where necessary
- accounts for tool installation that change substantively between versions like krew 0.4.1 and 0.4.2
- replicate profiles to internal object storage or servers to ensure profiles are available to your users
- no connection to source control is required
- sigstore integration to ensure binaries can be validated
- a version of tool is called an installation, there can be many installations for a given tool
- we endeavor to make OSX the same as Linux with GNU core utils: https://apple.stackexchange.com/questions/69223/how-to-replace-mac-os-x-utilities-with-gnu-core-utilities
- careful layering of installations and what can be shared versus what cannot:
  - jenv has an installation, but modifications are made to that installation adding new JVMs and plugins
  - krew has an installation, but modifications are made to that installation adding new plugins
- care is taken to make sure all downlads are intact