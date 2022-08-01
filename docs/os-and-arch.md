# OS and Arch

The detector in Provisio is normalized to return what `uname` will return not what Java will return. Just to make it a little easier when on the command line. So we call these the `uname` values for the os and architecture the normalized os and architecture.

All the descriptor files have mappings in the from the normalized os and architecture to the identifier for the os and architecture a given tool uses.

| Tables    |  uname | Java (os.name) | uname -m | Java (os.arch) |
|-----------|-------:|---------------:|---------:|---------------:|
| MBP 2018  | Darwin |       Mac OS X |   x86_64 |         x86_64 |
| MBP M1    | Darwin |       Mac OS X |    arm64 |        aarch64 |
