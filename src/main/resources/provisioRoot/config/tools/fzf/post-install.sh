#!/usr/bin/env bash

version=${6}
installLocation=${8}

# TODO: these additional resources can likely be listed in the descriptor and download along with the binary

(
cd ${installLocation}
[ ! -d bash ] && mkdir bash
  (
    cd bash
    [ ! -f key-bindings.bash ] && curl -OL https://raw.githubusercontent.com/junegunn/fzf/${version}/shell/key-bindings.bash
    [ ! -f completion.bash ] && curl -OL https://raw.githubusercontent.com/junegunn/fzf/${version}/shell/completion.bash
  )
[ ! -d zsh ] && mkdir zsh
  (
    cd zsh
    [ ! -f key-bindings.zsh ] && curl -OL https://raw.githubusercontent.com/junegunn/fzf/${version}/shell/key-bindings.zsh
    [ ! -f completion.zsh ] && curl -OL https://raw.githubusercontent.com/junegunn/fzf/${version}/shell/completion.zsh
  )
[ ! -d fish ] && mkdir fish
  (
    cd fish
    [ ! -f key-bindings.fish ] && curl -OL https://raw.githubusercontent.com/junegunn/fzf/${version}/shell/key-bindings.fish
    [ ! -f completion.fish ] && curl -OL https://raw.githubusercontent.com/junegunn/fzf/${version}/shell/completion.fish
  )
)
