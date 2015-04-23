#!/bin/bash

# Note: should work only for BSD / Mac OS X

set -e

git grep --files-with-matches 'import kotlin.Function' -- '*.java' | xargs sed -i '' 's/^import kotlin.Function.*;$/import kotlin.*;\
import kotlin.jvm.functions.*;/g'
