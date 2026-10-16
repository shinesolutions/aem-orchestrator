# Build, test and release aem-orchestrator. Run inside shinesolutions/aem-platform-buildenv
# (Java 21, Maven 3.10+), as CI does.
SHELL := /bin/bash
MVN ?= mvn -B -ntp
# Lets git run as root in the buildenv container on this repository, which is owned by your user
GIT_SAFE_DIRECTORY := GIT_CONFIG_COUNT=1 GIT_CONFIG_KEY_0=safe.directory GIT_CONFIG_VALUE_0=$(CURDIR)

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
# targets date its entries), then `## [x.y.z] - YYYY-MM-DD` releases, as rtk's keep-a-changelog
# parser requires, with only the standard Added/Changed/Deprecated/Removed/Fixed/Security subsections.
# With CHANGELOG_BASE (e.g. origin/master, set by CI for pull requests) it also requires at least
# one `## [Unreleased]` entry that is not in the base branch's CHANGELOG.md.
changelog-check:
	@set -euo pipefail; \
	export $(GIT_SAFE_DIRECTORY); \
	awk ' \
	  function bad(msg) { printf "CHANGELOG.md:%d: %s: %s\n", NR, msg, $$0; errors++ } \
	  /^## / { \
	    if ($$0 == "## [Unreleased]") { if (seen++ || releases) bad("[Unreleased] must appear once, before any release") } \
	    else if ($$0 ~ /^## \[[0-9]+\.[0-9]+\.[0-9]+\] - [0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$$/) { \
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

# Release with rtk (https://github.com/cliffano/rtk), configured in .rtk.json. Sets the release version
# in pom.xml and CHANGELOG.md (the `## [Unreleased]` entries are dated as the release) and commits
# that, tags it aem-orchestrator-<version>, then commits the next pre-release version in pom.xml and a
# new empty `## [Unreleased]` section. Nothing is pushed; review, then run `git push --follow-tags`.
# Needs a clean working tree and entries under [Unreleased].
#   release-patch: 4.0.1-pre.0 -> 4.0.1, next 4.0.2-pre.0
#   release-minor: 4.0.1-pre.0 -> 4.1.0, next 4.1.1-pre.0
#   release-major: 4.0.1-pre.0 -> 5.0.0, next 5.0.1-pre.0
define release
	set -euo pipefail; \
	export $(GIT_SAFE_DIRECTORY); \
	git diff --quiet && git diff --cached --quiet \
	  || { echo "Commit or stash your changes before releasing"; exit 1; }; \
	awk '/^## \[Unreleased\]$$/ { found = 1; next } /^## / { found = 0 } found && /^- / { n++ } \
	  END { exit n ? 0 : 1 }' CHANGELOG.md \
	  || { echo "CHANGELOG.md has no entries under ## [Unreleased]"; exit 1; }; \
	rtk release --release-increment-type $(1)
endef

release-major:
	@$(call release,major)

release-minor:
	@$(call release,minor)

release-patch:
	@$(call release,patch)

# Shared by release-check and release-test: extract the committed HEAD into a temporary directory and
# cd into it, set $$current to its project version and $$signer to the GPG signer option (Bouncy
# Castle with MAVEN_GPG_KEY in a temporary in-memory keyring, otherwise your GPG keyring), and remove
# the directory and keyring on exit.
define release_workdir
	set -euo pipefail; \
	work=$$(mktemp -d); keyring=""; \
	trap 'rm -rf "$$work"; if [[ -n "$$keyring" ]]; then gpgconf --kill gpg-agent; rm -rf "$$keyring"; fi' EXIT; \
	signer=""; \
	if [[ -n "$${MAVEN_GPG_KEY:-}" ]]; then \
	  signer="-Dgpg.signer=bc"; \
	  keyring=$$(mktemp -d -p /dev/shm 2>/dev/null || mktemp -d); \
	  export GNUPGHOME="$$keyring"; \
	  gpg --batch --quiet --import <<< "$$MAVEN_GPG_KEY"; \
	fi; \
	$(GIT_SAFE_DIRECTORY) git archive HEAD | tar -x -C "$$work"; \
	cd "$$work"; \
	current=$$($(MVN) -q help:evaluate -Dexpression=project.version -DforceStdout)
endef

# Offline release dry run: build, GPG-sign and bundle the committed HEAD for Maven Central at its
# release version (current version without its pre-release suffix, e.g. 4.0.1-pre.0 -> 4.0.1),
# without uploading anything (central-publishing skipUpload). Runs in a temporary copy so pom.xml is
# untouched, then checks the bundle has the pom, jar, sources and javadoc with signatures and
# checksums, and verifies every signature.
# The bundle is kept at target/release-check/central-bundle.zip.
# Signing key, either:
#   - MAVEN_GPG_KEY (ASCII-armored secret key) and MAVEN_GPG_PASSPHRASE environment variables: signs
#     with the Bouncy Castle signer and verifies with a temporary keyring in memory (/dev/shm), so
#     no key file or keyring is needed, e.g. `docker run -e MAVEN_GPG_KEY -e MAVEN_GPG_PASSPHRASE ...`
#   - otherwise the GPG secret key in your keyring, with MAVEN_GPG_PASSPHRASE or gpg-agent
release-check:
	@$(release_workdir); \
	version="$${current%%-*}"; \
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

# Upload test: sign the committed HEAD and upload it to the Central Portal as a pre-release test
# version, never publishing it (-DautoPublish=false), then print the deployment ID to check with
# release-status and drop with release-drop. Checks the GPG key, the Central token, the upload and
# Central's validation. Only pre-release versions are accepted; a dropped version can be reused.
# Signs with MAVEN_GPG_KEY and MAVEN_GPG_PASSPHRASE and uploads with MAVEN_CENTRAL_USERNAME and
# MAVEN_CENTRAL_PASSWORD.
#   make release-test RELEASE_TEST_VERSION=5.0.0-rc.0   (default: current release version with -rc.0)
# For example, in aem-platform-buildenv with Docker, with this repository mounted read-only:
#   docker run --rm -v "$(pwd)":/workspace/aem-orchestrator:ro -w /workspace/aem-orchestrator \
#     -e MAVEN_GPG_KEY -e MAVEN_GPG_PASSPHRASE -e MAVEN_CENTRAL_USERNAME -e MAVEN_CENTRAL_PASSWORD \
#     shinesolutions/aem-platform-buildenv:6.3.0 make release-test RELEASE_TEST_VERSION=5.0.0-rc.0
release-test:
	@$(release_workdir); \
	version="$(RELEASE_TEST_VERSION)"; \
	[[ -n "$$version" ]] || version="$${current%%-*}-rc.0"; \
	[[ "$$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+-[0-9A-Za-z.-]+$$ && "$$version" != *SNAPSHOT* ]] \
	  || { echo "release-test only uploads pre-release versions such as 5.0.0-rc.0, not $$version"; exit 1; }; \
	echo "Uploading HEAD to the Central Portal as $$version (it is not published)"; \
	$(MVN) -q versions:set -DnewVersion="$$version" -DgenerateBackupPoms=false; \
	$(MVN) -Prelease deploy -DskipTests -DskipITs -DautoPublish=false $$signer | tee deploy.log; \
	id=$$(sed -n 's/.*deploymentId: \([0-9a-f-]*\).*/\1/p' deploy.log | tail -1); \
	echo "Deployment $$id ($$version) is validated and not published. Check and drop it with:"; \
	echo "  make release-status DEPLOYMENT_ID=$$id"; \
	echo "  make release-drop DEPLOYMENT_ID=$$id"

# Upload the checked-out release version to the Central Portal, run by the publish workflow on a pushed
# aem-orchestrator-<version> tag. Signs with MAVEN_GPG_KEY and MAVEN_GPG_PASSPHRASE and uploads with
# MAVEN_CENTRAL_USERNAME and MAVEN_CENTRAL_PASSWORD. Waits until Central has validated the upload,
# which then waits for Publish or Drop in the Central Portal.
publish:
	$(MVN) -Prelease deploy -DskipTests -DskipITs -Dgpg.signer=bc

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

.PHONY: ci clean deps build test integration-test changelog-check \
	release-major release-minor release-patch publish release-check release-test \
	release-status release-drop
