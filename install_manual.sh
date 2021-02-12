#!/usr/bin/env bash

# Requirements to install fonts
REQUIREMENTS="awk curl fc-cache mkdir mktemp unlink unzip"
readonly REQUIREMENTS

# Fonts local directory
LOCAL_FONTS_DIR="${HOME}/.local/share/fonts"
readonly LOCAL_FONTS_DIR

# Latest fonts release info in JSON
LATEST_RELEASE_INFO="https://api.github.com/repos/JetBrains/JetBrainsMono/releases/latest"
readonly LATEST_RELEASE_INFO

RED=$(echo -en '\033[00;31m')
readonly RED

RESTORE=$(echo -en '\033[0m')
readonly RESTORE

# Print error message to STDERR and exit
function die() {

  echo >&2 "${RED}$*${RESTORE}"
  exit 1
}

# Check requirements
function check_requirements() {

  echo "Checking requirements..."

  for tool in ${REQUIREMENTS}; do

    echo -n "${tool}... "

    if command -v "${tool}" >/dev/null 2>&1; then
      echo "Found"
    else
      die "Not found. Please install \"${tool}\" to fix it."
    fi
  done
}

# Download URL
function download() {

  url="${1}"
  save_as="${2}"

  curl -sL "${1}" -o "${save_as}" || die "Unable to download: ${url}"
}

# Generate temporary filename
function get_tempfile() {

  mktemp
}

# Get item from latest release data
function get_item() {

  item="${1}"
  read_from="${2}"

  awk -F '"' "/${item}/ {print \$4}" "${read_from}"

}

# Extract fonts archive
function extract() {

  archive="${1}"
  extract_to="${2}"

  unzip -o "${archive}" -d "${extract_to}" >/dev/null 2>&1
}

# Create fonts directory
function create_fonts_dir() {

  if [ ! -d "${LOCAL_FONTS_DIR}" ]; then
    echo "Creating fonts directory: ${LOCAL_FONTS_DIR}"
    mkdir -p "${LOCAL_FONTS_DIR}" >/dev/null 2>&1 || die "Unable to create fonts directory: ${LOCAL_FONTS_DIR}"
  fi
}

# Build fonts cache
function build_fonts_cache() {

  fc-cache -f || die "Unable to build fonts cache"
}

# Remove temporary file
function cleanup() {

  unlink "${*}" || die "Unable to unlink: ${*}"
}

# Start point
function main() {

  echo "Installing latest JetBrains Mono fonts..."

  check_requirements

  TEMP_LATEST_INFO=$(get_tempfile)

  echo "Downloading latest release info: ${LATEST_RELEASE_INFO}"
  download "${LATEST_RELEASE_INFO}" "${TEMP_LATEST_INFO}"

  TAG_NAME=$(get_item "tag_name" "${TEMP_LATEST_INFO}")

  echo "Latest fonts version: ${TAG_NAME}"

  BROWSER_URL=$(get_item "browser_download_url" "${TEMP_LATEST_INFO}")
  TEMP_FONTS_ARCHIVE=$(get_tempfile)
  echo "Downloading fonts archive: ${BROWSER_URL}"
  download "${BROWSER_URL}" "${TEMP_FONTS_ARCHIVE}"

  create_fonts_dir

  echo "Extracting fonts: ${LOCAL_FONTS_DIR}"
  extract "${TEMP_FONTS_ARCHIVE}" "${LOCAL_FONTS_DIR}"

  echo "Building fonts cache..."
  build_fonts_cache

  echo "Cleaning up..."
  cleanup "${TEMP_LATEST_INFO}"
  cleanup "${TEMP_FONTS_ARCHIVE}"

  echo "Fonts have been installed"
}

main
