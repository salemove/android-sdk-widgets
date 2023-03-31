#!/bin/bash

separator="/"
# shellcheck disable=SC2027
basePackage="$separator""widgetssdk"$separator"src"$separator"main"$separator"java"$separator"com"$separator"glia"$separator"widgets"$separator
activity="Activity"
view="View"
controller="Controller"
manager="Manager"
contract="Contract"
repository="Repository"
usecase="UseCase"
controllerPackage="controller"
domainPackage="domain"
extension=".kt"
temp="temp"


function goToWidget() {
  scriptPath=$(readlink -f "$0")
  absolutePath=$(dirname "$scriptPath")
  cd $absolutePath$basePackage || exit

}

function createFile() {
  case $1 in
    "a" )
      createActivity ;;
    "c" )
      createController ;;
    "m" )
      createManager ;;
    "u" )
      createUseCase ;;
  esac
}

function createUseCase() {
  mkdir -p "$domainPackage"
  cd "$_" || return
  cp -v "$absolutePath""/widgetssdk/templates/usecase_template.txt" "$temp$extension" > /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$usecase$extension"
  rm $temp$extension
  cd ..
}

function createManager() {
  mkdir -p "$domainPackage"
  cd "$_" || return
  cp -v "$absolutePath""/widgetssdk/templates/public_interface_template.txt" "$temp$extension" > /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$extension"
  cp -v "$absolutePath""/widgetssdk/templates/repository_template.txt" "$temp$extension" > /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$repository$extension"
  rm $temp$extension
  cd ..
  cp -v "$absolutePath""/widgetssdk/templates/manager_template.txt" "$temp$extension" > /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$manager$extension"
  rm $temp$extension
}

function createContract() {
  cp -v "$absolutePath""/widgetssdk/templates/contract_template.txt" "$temp$extension" > /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$contract$extension"
  rm "$temp$extension"
}

function createActivity() {
  cp -v "$absolutePath""/widgetssdk/templates/activity_template.txt" "$temp$extension" > /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$activity$extension"
  cp -v "$absolutePath""/widgetssdk/templates/view_template.txt" "$temp$extension"> /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$view$extension"
  createContract
  foo=$(echo ${featureName:0:1} | tr '[A-Z]' '[a-z]')${featureName:1}
  f=$(echo $foo | sed -e 's/[A-Z]/_&/g' -e 's/[0-9].../ &/g' -e 's/^ //g')
  feature_name=$(echo "$f" | tr '[:upper:]' '[:lower:]')
  cp -v "$absolutePath""/widgetssdk/templates/layout_template.txt" "$absolutePath""/widgetssdk/src/main/res/layout/$feature_name""_activity.xml" > /dev/null
  cp -v "$absolutePath""/widgetssdk/templates/layout_template.txt" "$absolutePath""/widgetssdk/src/main/res/layout/$feature_name""_view.xml" > /dev/null
}

function createController() {
  mkdir -p "$controllerPackage"
  cd "$controllerPackage" || return
  cp -v "$absolutePath""/widgetssdk/templates/controller_template.txt" "$temp$extension" > /dev/null
  packageReplacement="$packageName" nameReplacement="$featureName" envsubst < "$temp$extension" > "$featureName$controller$extension"
  rm "$temp$extension"
  cd ..
  contractFileName="$featureName$contract$extension"
  if [ -f "${contractFileName}" ];then
      if [ -s "${contractFileName}" ];then
          echo
      else
          createContract
      fi
  else
      createContract
  fi
}

function createBaseDirectory() {
    OIFS=$IFS
    IFS='.'
    read -r -a packages <<< "$1"

    # Create directory and move inside
    for package in "${packages[@]}";
    do
      mkdir -p "$package"
      cd "$package" || echo "No $package found"
    done
    IFS=$OIFS
}

function gatherBaseInfo() {
  echo "Enter feature base name:"
  # shellcheck disable=SC2162
  read featureName
  echo

  echo "Enter package name on top of /widgetssdk/src/main/java/com/glia/widgets/"
  # shellcheck disable=SC2162
  read pn
  packageName=$(echo "$pn" | tr "/" ".") || $pn
  echo "$packageName"
  echo
  createBaseDirectory "$packageName"
}

function createFeatures() {
  echo "What files do you want to create? In any order write the following letters to signify features added:"
  echo "a for Activity, View and Contract"
  echo "c for Controller and Contract, if not available"
  echo "m for Manager, Public Interface and Repository"
  echo "u for usecase"
  # shellcheck disable=SC2162
  read classes

  for (( i=0; i<${#classes}; i++ )); do
    createFile "${classes:$i:1}"
  done
}

function writeIntro() {
    echo "
                   .---.
                   |   |.--.
           .--./)  |   ||__|
          /,--\ \  |   |.--.
         | |  | |  |   ||  |  _____
          \ \_/ /  |   ||  | /,--.'\\
          /(--'    |   ||  |/ |   \ |
          \ '---.  |   ||  |-- __ | |
           /-----.\|   ||__| / .--| |
          ||     |||___|    / /   | |_
          \'. __/ /         \ \._,\ '/
           \_____/           \___/ \/   ACMU"
    echo
    echo
    echo "This script enables you to simply and quickly generate base files for a new feature."
    echo
    echo "First designate the feature name, this will be used as a package name for your feature and will be the base of all of the generated files"
    echo
    echo "The possible options are:"
    echo "a for *Activity, *View and *Contract"
    echo "c for *Controller and *Contract"
    echo "m for *Manager, *Repository and a basic feature interface"
    echo "u for *UseCase"
    echo
}

# Actual code starts here
writeIntro
goToWidget
gatherBaseInfo
createFeatures

echo "Thank you for using Glia ACMU"