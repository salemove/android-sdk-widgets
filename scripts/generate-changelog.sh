#!/bin/bash
# Generate a changelog in markdown format from commits between two tags

generate_changelog() {
  #                    -c 'versionsort.suffix=v' is used to make "v1.2.3" be lesser version then "2.0.0"
  local git_tags=$(git -c 'versionsort.suffix=v' tag --list --sort=-v:refname) # Get a list of all tags in reverse order
  local tags_array=($git_tags) # Make the tags an array
  local latest_tag="${tags_array[0]}"
  local previous_tag="${tags_array[1]}"
  echo "latest_tag=$latest_tag"
  echo "previous_tag=$previous_tag"

  local commits=$(git log "$previous_tag".."$latest_tag" --no-merges --pretty=format:"%H")
  local commits_as_array=($commits)
  local commits_count=${#commits_as_array[@]}
  echo "Number of commits from previous_tag: $commits_count"

  # Start generating the changelog
  MARKDOWN="## What's Changed"
  MARKDOWN+='\n'

  # Add each commit details to the changelog
  for current_commit in $commits; do
    MARKDOWN+='* ' # So that list of commits would look nice in Markdowns
    # Read more about "--pretty=format:" in here: https://git-scm.com/docs/pretty-formats
    MARKDOWN+=$(git log -1 ${current_commit} --pretty=format:"[%h] %s")
    MARKDOWN+='\n'
  done

  MARKDOWN+='\n'
  MARKDOWN+="[Full Changelog]($REPOSITORY_URL/compare/$previous_tag...$latest_tag)"

  # Save our markdown to a file
  echo -e $MARKDOWN > "$CHANGELOG_FILE_NAME"
}

# Repo URL to base links off of
REPOSITORY_URL=https://github.com/salemove/android-sdk-widgets
CHANGELOG_FILE_NAME=CHANGELOG.md

generate_changelog

# Expose the changelog as Bitrise environment variable for the next steps.
# `envman` is Bitrise command https://github.com/bitrise-io/envman
envman add --key BITRISE_CHANGELOG --valuefile $CHANGELOG_FILE_NAME

printf "\n"
echo "$BITRISE_CHANGELOG"
