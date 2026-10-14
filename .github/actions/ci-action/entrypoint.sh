#!/bin/bash
set -o errexit

mvn --version
make ci
