MAKEFILE_DIR := $(abspath $(dir $(lastword $(MAKEFILE_LIST))))
REPO_ROOT    := $(abspath $(MAKEFILE_DIR)/..)

VERSION ?= $(shell git -C $(REPO_ROOT) describe --tags --always 2>/dev/null || echo "dev")
OUT_DIR      ?= $(REPO_ROOT)/dist

DOCKER_UBUNTU ?= ubuntu:22.04
DOCKER_FEDORA ?= fedora:latest
DOCKER_ARCH ?= archlinux:latest

HOST_UID := $(shell id -u)
HOST_GID := $(shell id -g)

# Architecture handling (defaults to current host machine)
ARCH ?= $(shell uname -m)

ifeq ($(ARCH),aarch64)
  DOCKER_PLATFORM := linux/arm64
else ifeq ($(ARCH),arm64)
  DOCKER_PLATFORM := linux/arm64
else
  DOCKER_PLATFORM := linux/amd64
endif

.PHONY: help appimage clean-dist

help:
	@echo "Available packaging targets:"
	@echo "  make appimage          - Build Linux AppImage"
	@echo "  make deb-rpm           - Build Debian and RPM package"
	@echo "  make clean-dist        - Remove dist/ directory"

appimage:
	@mkdir -p "$(OUT_DIR)"
	docker run --rm \
	  --platform $(DOCKER_PLATFORM) \
	  -v "$(REPO_ROOT):/app-src:ro" \
	  -v "$(MAKEFILE_DIR)/build-appimage.sh:/build-appimage.sh:ro" \
	  -v "$(MAKEFILE_DIR)/AppRun:/AppRun:ro" \
	  -v "$(OUT_DIR):/out" \
	  -e APP_VERSION="$(VERSION)" \
	  -e HOST_UID="$(HOST_UID)" \
	  -e HOST_GID="$(HOST_GID)" \
	  $(DOCKER_UBUNTU) /build-appimage.sh

deb-rpm:
	@mkdir -p "$(OUT_DIR)"
	docker run --rm \
	  --platform $(DOCKER_PLATFORM) \
	  -v "$(REPO_ROOT):/app-src:ro" \
	  -v "$(MAKEFILE_DIR)/build-deb-rpm.sh:/build-deb-rpm.sh:ro" \
	  -v "$(MAKEFILE_DIR)/AppRun:/AppRun:ro" \
	  -v "$(OUT_DIR):/out" \
	  -e APP_VERSION="$(VERSION)" \
	  -e HOST_UID="$(HOST_UID)" \
	  -e HOST_GID="$(HOST_GID)" \
	  $(DOCKER_UBUNTU) /build-deb-rpm.sh

clean-dist:
	rm -rf "$(OUT_DIR)"