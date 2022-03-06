# mediasoup client for android

[![Android CI](https://github.com/crow-misia/libmediasoup-android/workflows/Android%20CI/badge.svg)](https://github.com/crow-misia/libmediasoup-android/actions)
[![License](https://img.shields.io/github/license/crow-misia/libmediasoup-android)](LICENSE)

mediasoup android client side library https://mediasoup.org


## Get Started

### Branches

- This repo has three notable branches namely: `main` , `dyte`, `upstream-changes`
    - `main` should only be used in read-only mode and for fetching changes from `upstream`
    - `dyte` contains all the changes for build used internally
    - `upstream-changes` is for porting all the changes `upstream`, try to make all the PRs to `upstream` by using this branch only

### Build

Follow instructions given [here](https://www.notion.so/dyte/Building-libmediasoup-android-9c49b42268974bf5a207d051ea6b1909)

## Side Notes

### Branch Name

- Please do not use `/` in branch name as it hinders workflow of people using _git worktrees_
