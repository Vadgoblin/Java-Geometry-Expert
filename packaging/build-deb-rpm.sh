#!/usr/bin/env bash
set -euo pipefail
export DEBIAN_FRONTEND=noninteractive


# ---------------------------------------------------------
# Prepare build
# ---------------------------------------------------------

# Detect machine architecture
RAW_ARCH="$(uname -m)"
case "${RAW_ARCH}" in
  x86_64)
    ARCH="x86_64"
    ;;
  aarch64|arm64)
    ARCH="aarch64"
    ;;
  *)
    echo "Unsupported architecture: ${RAW_ARCH}" && exit 1
    ;;
esac
export ARCH
echo ">>> Detected target architecture: ${ARCH}"

# Version
VERSION_STR="$(echo "${APP_VERSION:-dev}" | sed 's/^v//; s/_/-/g')"

# Paths
OUT_DIR="/out"
SRC_DIR="/app-src"

echo ">>> Installing build dependencies and tools..."
apt-get update --allow-unauthenticated --allow-insecure-repositories
apt-get install -y gettext openjdk-11-jdk tree binutils curl file

# Create a writable clone of source for building
JGEX_SRC_SHADOW="/tmp/jgex-src"
rm -rf "${JGEX_SRC_SHADOW}"
mkdir -p "${JGEX_SRC_SHADOW}"

echo ">>> Copying project source files..."
cp -r "${SRC_DIR}/src" "${JGEX_SRC_SHADOW}/"
cp -r "${SRC_DIR}/gradle" "${JGEX_SRC_SHADOW}/"
cp "${SRC_DIR}/gradlew" "${JGEX_SRC_SHADOW}/"
cp "${SRC_DIR}/settings.gradle" "${JGEX_SRC_SHADOW}/"
cp "${SRC_DIR}/gradle.properties" "${JGEX_SRC_SHADOW}/"
cp "${SRC_DIR}/build.gradle" "${JGEX_SRC_SHADOW}/"


# ---------------------------------------------------------
# Build JGEX
# ---------------------------------------------------------
echo ">>> Building GCLC GUI..."

cd "${JGEX_SRC_SHADOW}"
./gradlew package "-PsoftwareVersion=${VERSION_STR}"

# ---------------------------------------------------------
#  Copy created artifacts to output dir
# ---------------------------------------------------------
DEB_OUT_PATH="${OUT_DIR}/Java-Geometry-Expert-${VERSION_STR}-${ARCH}.deb"
RPM_OUT_PATH="${OUT_DIR}/Java-Geometry-Expert-${VERSION_STR}-${ARCH}.rpm"

cp "${JGEX_SRC_SHADOW}"/build/*.deb "${DEB_OUT_PATH}"
cp "${JGEX_SRC_SHADOW}"/build/*.rpm "${RPM_OUT_PATH}"
chown -h "${HOST_UID}:${HOST_GID}" "${DEB_OUT_PATH}"
chown -h "${HOST_UID}:${HOST_GID}" "${RPM_OUT_PATH}"
