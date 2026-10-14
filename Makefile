# Build, test and release aem-orchestrator. Run inside shinesolutions/aem-platform-buildenv
# (Java 21, Maven 3.10+), as CI does.
SHELL := /bin/bash
MVN ?= mvn -B -ntp

# Changelog format, then checkstyle, unit tests, integration tests, SpotBugs and packaging, as run
# by CI
ci: clean deps changelog-check
	$(MVN) verify

clean:
	$(MVN) clean

# Download all dependencies and plugins into the local Maven repository
deps:
	$(MVN) dependency:go-offline

# Package the Spring Boot jar (target/aem-orchestrator-<version>.jar) without running tests
build:
	$(MVN) package -DskipTests

# Checkstyle and unit tests (src/test/java)
test:
	$(MVN) test

# Integration tests only (src/integration-test/java, *IT classes); unit tests are skipped
integration-test:
	$(MVN) verify -Dtest=none -Dsurefire.failIfNoSpecifiedTests=false

# Check CHANGELOG.md follows Keep a Changelog: a single `## [Unreleased]` section first (the release
# targets date its entries), then `## [x.y.z] - YYYY-MM-DD` releases (`unknown` dates are kept for
# old releases), with only the standard Added/Changed/Deprecated/Removed/Fixed/Security subsections.
# With CHANGELOG_BASE (e.g. origin/master, set by CI for pull requests) it also requires at least
# one `## [Unreleased]` entry that is not in the base branch's CHANGELOG.md.
changelog-check:
	@set -euo pipefail; \
	export GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=safe.directory GIT_CONFIG_VALUE_0=$(CURDIR); \
	awk ' \
	  function bad(msg) { printf "CHANGELOG.md:%d: %s: %s\n", NR, msg, $$0; errors++ } \
	  /^## / { \
	    if ($$0 == "## [Unreleased]") { if (seen++ || releases) bad("[Unreleased] must appear once, before any release") } \
	    else if ($$0 ~ /^## \[[0-9]+\.[0-9]+\.[0-9]+\] - ([0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]|unknown)$$/) { \
	      if (!seen && !early++) bad("[Unreleased] must come before releases"); releases++ } \
	    else bad("version heading must be ## [x.y.z] - YYYY-MM-DD") } \
	  /^### / && $$0 !~ /^### (Added|Changed|Deprecated|Removed|Fixed|Security)$$/ { bad("unknown subsection") } \
	  END { if (!seen) { print "CHANGELOG.md: missing ## [Unreleased]"; errors++ } \
	        if (errors) exit 1; print "CHANGELOG.md format OK (" releases " releases)" }' CHANGELOG.md; \
	if [[ -n "$${CHANGELOG_BASE:-}" ]]; then \
	  unreleased() { awk '/^## \[Unreleased\]$$/ { found = 1; next } /^## / { found = 0 } found && /^- /'; }; \
	  added=$$(comm -13 <(git show "$$CHANGELOG_BASE:CHANGELOG.md" | unreleased | sort -u) \
	    <(unreleased < CHANGELOG.md | sort -u)); \
	  if [[ -z "$$added" ]]; then \
	    echo "CHANGELOG.md: add an entry under ## [Unreleased] describing this change (compared with $$CHANGELOG_BASE)"; \
	    exit 1; \
	  fi; \
	  echo "New ## [Unreleased] entries compared with $$CHANGELOG_BASE:"; echo "$$added"; \
	fi

# Release with maven-release-plugin. First moves the CHANGELOG.md `## [Unreleased]` entries under
# `## [<version>] - <today>` and commits that, then commits the release version, tags it
# aem-orchestrator-<version> and commits the next -SNAPSHOT version. Nothing is pushed; review,
# then run `git push --follow-tags`. Needs a clean working tree and entries under [Unreleased].
#   release-patch: 4.0.1-SNAPSHOT -> 4.0.1, next 4.0.2-SNAPSHOT
#   release-minor: 4.0.1-SNAPSHOT -> 4.1.0, next 4.1.1-SNAPSHOT
#   release-major: 4.0.1-SNAPSHOT -> 5.0.0, next 5.0.1-SNAPSHOT
define release
	set -euo pipefail; \
	export GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=safe.directory GIT_CONFIG_VALUE_0=$(CURDIR); \
	git diff --quiet && git diff --cached --quiet \
	  || { echo "Commit or stash your changes before releasing"; exit 1; }; \
	awk '/^## \[Unreleased\]$$/ { found = 1; next } /^## / { found = 0 } found && /^- / { n++ } \
	  END { exit n ? 0 : 1 }' CHANGELOG.md \
	  || { echo "CHANGELOG.md has no entries under ## [Unreleased]"; exit 1; }; \
	current=$$($(MVN) -q help:evaluate -Dexpression=project.version -DforceStdout); \
	IFS=. read -r major minor patch <<< "$${current%-SNAPSHOT}"; \
	case "$(1)" in \
	  major) release="$$((major + 1)).0.0" ;; \
	  minor) release="$$major.$$((minor + 1)).0" ;; \
	  patch) release="$$major.$$minor.$$patch" ;; \
	esac; \
	IFS=. read -r major minor patch <<< "$$release"; \
	next="$$major.$$minor.$$((patch + 1))-SNAPSHOT"; \
	echo "Releasing $$current as $$release, next development version $$next"; \
	awk -v heading="## [$$release] - $$(date +%Y-%m-%d)" \
	  '{ print } /^## \[Unreleased\]$$/ && !done { print ""; print heading; done = 1 }' \
	  CHANGELOG.md > CHANGELOG.md.tmp && mv CHANGELOG.md.tmp CHANGELOG.md; \
	git commit -q -m "Update CHANGELOG for $$release" CHANGELOG.md; \
	$(MVN) release:prepare -DreleaseVersion="$$release" -DdevelopmentVersion="$$next" \
	  -DpushChanges=false; \
	$(MVN) release:clean
endef

# Offline release dry run: build, GPG-sign and bundle the committed HEAD for Maven Central at its
# release version (current version without -SNAPSHOT), without uploading anything (central-publishing
# skipUpload). Runs in a temporary copy so pom.xml is untouched, then checks the bundle has the
# pom, jar, sources and javadoc with signatures and checksums, and verifies every signature.
# The bundle is kept at target/release-check/central-bundle.zip.
# Signing key, either:
#   - MAVEN_GPG_KEY (ASCII-armored secret key) and MAVEN_GPG_PASSPHRASE environment variables: signs
#     with the Bouncy Castle signer and verifies with a temporary keyring in memory (/dev/shm), so
#     no key file or keyring is needed, e.g. `docker run -e MAVEN_GPG_KEY -e MAVEN_GPG_PASSPHRASE ...`
#   - otherwise the GPG secret key in your keyring, with MAVEN_GPG_PASSPHRASE or gpg-agent
release-check:
	@set -euo pipefail; \
	work=$$(mktemp -d); keyring=""; \
	trap 'rm -rf "$$work"; if [[ -n "$$keyring" ]]; then gpgconf --kill gpg-agent; rm -rf "$$keyring"; fi' EXIT; \
	signer=""; \
	if [[ -n "$${MAVEN_GPG_KEY:-}" ]]; then \
	  signer="-Dgpg.signer=bc"; \
	  keyring=$$(mktemp -d -p /dev/shm 2>/dev/null || mktemp -d); \
	  export GNUPGHOME="$$keyring"; \
	  gpg --batch --quiet --import <<< "$$MAVEN_GPG_KEY"; \
	fi; \
	GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=safe.directory GIT_CONFIG_VALUE_0=$(CURDIR) \
	  git archive HEAD | tar -x -C "$$work"; \
	cd "$$work"; \
	current=$$($(MVN) -q help:evaluate -Dexpression=project.version -DforceStdout); \
	version="$${current%-SNAPSHOT}"; \
	echo "Checking release bundle of HEAD as version $$version (nothing is uploaded)"; \
	$(MVN) -q versions:set -DnewVersion="$$version" -DgenerateBackupPoms=false; \
	$(MVN) -Prelease deploy -DskipTests -DskipITs -DskipUpload=true $$signer; \
	mkdir -p bundle "$(CURDIR)/target/release-check"; \
	cp target/central-publishing/central-bundle.zip "$(CURDIR)/target/release-check/"; \
	unzip -q target/central-publishing/central-bundle.zip -d bundle; \
	base="bundle/com/shinesolutions/aem-orchestrator/$$version/aem-orchestrator-$$version"; \
	for file in "$$base.pom" "$$base.jar" "$$base-sources.jar" "$$base-javadoc.jar"; do \
	  for suffix in "" .asc .md5 .sha1; do \
	    test -f "$$file$$suffix" || { echo "Missing from bundle: $${file#bundle/}$$suffix"; exit 1; }; \
	  done; \
	done; \
	for signature in $$(find bundle -name '*.asc'); do \
	  gpg --verify "$$signature" "$${signature%.asc}"; \
	done; \
	echo "Release check OK: target/release-check/central-bundle.zip"

# Central Portal deployments (`mvn -Prelease deploy` prints the deployment ID). Uses the user token in
# MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD; the Authorization header is passed to curl on
# stdin so the token is not visible in the process list.
#   release-status: show a deployment's state (e.g. VALIDATED, PUBLISHED, FAILED) and any errors
#   release-drop:   drop (delete) a deployment that has not been published
define central_api
	set -euo pipefail; \
	test -n "$(DEPLOYMENT_ID)" || { echo "Set DEPLOYMENT_ID, e.g. make $@ DEPLOYMENT_ID=<id>"; exit 1; }; \
	test -n "$${MAVEN_CENTRAL_USERNAME:-}" && test -n "$${MAVEN_CENTRAL_PASSWORD:-}" \
	  || { echo "Set MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD"; exit 1; }; \
	token=$$(printf '%s:%s' "$$MAVEN_CENTRAL_USERNAME" "$$MAVEN_CENTRAL_PASSWORD" | base64 | tr -d '\n'); \
	printf 'Authorization: Bearer %s\n' "$$token" \
	  | curl -sS --fail-with-body -X $(1) -H @- -w '\nHTTP %{http_code}\n' \
	      "https://central.sonatype.com/api/v1/publisher/$(2)"
endef

release-status:
	@$(call central_api,POST,status?id=$(DEPLOYMENT_ID))

release-drop:
	@$(call central_api,DELETE,deployment/$(DEPLOYMENT_ID))

release-major:
	@$(call release,major)

release-minor:
	@$(call release,minor)

release-patch:
	@$(call release,patch)


.PHONY: ci clean deps build test integration-test changelog-check release-check release-status release-drop release-major release-minor release-patch
