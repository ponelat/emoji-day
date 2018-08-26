#!/bin/bash

# Build js bundle
set -e
lein clean
lein fig:min

# Commit to gh-pages
mkdir -p ./tmp-deploy
(
  cd tmp-deploy
  mkdir -p cljs-out
  cp ../target/public/cljs-out/dev-main.js ./cljs-out
  cp -R ../resources/public/* .
  git init \
    && git add . \
    && git commit -m "Deploy to GitHub Pages" \
    && git push --force --quiet "git@github.com:ponelat/emoji-day.git" master:gh-pages
)
rm -rf ./tmp-deploy/
