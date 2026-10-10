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
    JDK_ARCH="x64"
    ;;
  aarch64|arm64)
    ARCH="aarch64"
    JDK_ARCH="aarch64"
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
APPDIR="/tmp/AppDir"
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
./gradlew installDist "-PsoftwareVersion=${VERSION_STR}"


# ---------------------------------------------------------
# Copy JGEX and assets into AppDir
# ---------------------------------------------------------
rm -rf "${APPDIR}"
mkdir -p "${APPDIR}/usr/lib"

cp -r "${JGEX_SRC_SHADOW}/build/install/jgex" "${APPDIR}/usr/lib"
cp "${SRC_DIR}/assets/linux/Java-Geometry-Expert.desktop" "${APPDIR}"
cp "${SRC_DIR}/assets/linux/Java-Geometry-Expert.png" "${APPDIR}"
cp "/AppRun" "${APPDIR}"


# ---------------------------------------------------------
# Download and prepare jdk
# ---------------------------------------------------------
JDK_TMP="/tmp/jdk"
mkdir -p "${JDK_TMP}"
cd "${JDK_TMP}"

curl -L -o openjdk21.tar.gz "https://api.adoptium.net/v3/binary/latest/21/ga/linux/${JDK_ARCH}/jdk/hotspot/normal/eclipse"
mkdir -p extracted-jdk
tar -xzf openjdk21.tar.gz -C extracted-jdk --strip-components=1
./extracted-jdk/bin/jlink \
     --add-modules java.base,java.datatransfer,java.xml,java.prefs,java.desktop,java.logging,java.security.sasl,java.naming,java.transaction.xa,java.sql \
     --strip-debug \
     --no-man-pages \
     --no-header-files \
     --compress=zip-6 \
     --output "${APPDIR}/usr/lib/jgex/jdk"


# ---------------------------------------------------------
# Package into AppImage
# ---------------------------------------------------------
echo ">>> Packaging into AppImage..."
WORKDIR_DEPLOY="/tmp/linuxdeploy"
mkdir -p "${WORKDIR_DEPLOY}" && cd "${WORKDIR_DEPLOY}"
mkdir -p "${OUT_DIR}"

curl -L -o appimagetool.AppImage "https://github.com/AppImage/appimagetool/releases/latest/download/appimagetool-${ARCH}.AppImage"
chmod +x appimagetool.AppImage

export APPIMAGE_EXTRACT_AND_RUN=1
export VERSION="${VERSION_STR}"

APPIMAGE_PATH="${OUT_DIR}/Java-Geometry-Expert-${VERSION}-${ARCH}.AppImage"
./appimagetool.AppImage --appimage-extract-and-run "${APPDIR}" "${APPIMAGE_PATH}"
chown -h "${HOST_UID}:${HOST_GID}" "${APPIMAGE_PATH}"