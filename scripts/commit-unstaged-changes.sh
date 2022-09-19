 #!/bin/bash

NEW_VERSION=$1
BRANCH_NAME="version-update/${NEW_VERSION}"
MESSAGE="Update project version to ${NEW_VERSION}"

git checkout -b $BRANCH_NAME
git add -A
git commit -m $MESSAGE
git push origin "$BRANCH_NAME":"$BRANCH_NAME"

curl \
  -X POST \
  -H "Accept: application/vnd.github+json" \
  -H "Authorization: Bearer $GITHUB_API_TOKEN" \
  https://api.github.com/repos/salemove/android-sdk-widgets/pulls \
  -d "{\"title\":\"${MESSAGE}\",\"head\":\"${BRANCH_NAME}\",\"base\":\"master\"}"