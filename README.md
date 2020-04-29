<x-tag-head>
<x-tag-meta http-equiv="X-UA-Compatible" content="IE=edge"/>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/highlight.min.js"/>
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js" />
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.js" />
--></x-tag-script>

<x-tag-style><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/styles/github.min.css" />
--></x-tag-style>

<x-tag-style><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.css" />
--></x-tag-style>
</x-tag-head>

# Fortify SSC Parser Plugin for Clair / Yair

## Introduction

This Fortify SSC parser plugin allows for importing scan results from Clair (Vulnerability Static Analysis for Containers).

Clair itself doesn't provide any file-based reports; as such this parser plugin parses reports generated
by the Yair command line interface for Clair. See the [Usage](#Usage) section for more information.

### Related Links

* **Downloads**:  
  _Beta versions may be unstable or non-functional. The `*-licenseReport.zip` and `*-dependencySources.zip` files are for informational purposes only and do not need to be downloaded._
	* **Release versions**: https://bintray.com/package/files/fortify-ps/release/fortify-ssc-parser-clair-yair?order=desc&sort=fileLastModified&basePath=&tab=files  
	* **Beta versions**: https://bintray.com/package/files/fortify-ps/beta/fortify-ssc-parser-clair-yair?order=desc&sort=fileLastModified&basePath=&tab=files
	* **Sample input files**: [sampleData](sampleData)
* **GitHub**: https://github.com/fortify-ps/fortify-ssc-parser-clair-yair
* **Automated builds**: https://travis-ci.com/fortify-ps/fortify-ssc-parser-clair-yair
* **Clair and Yair resources**:
	* Yair: https://github.com/yfoelling/yair
	* Clair GitHub repository: https://github.com/quay/clair/tree/v2.1.2
	* Legacy Clair documentation: https://coreos.com/clair/docs/latest/
* **Alternatives**:
	* SSC Parser Plugin for Clair REST API: https://github.com/fortify-ps/fortify-ssc-parser-clair-rest

## Plugin Installation

These sections describe how to install, upgrade and uninstall the plugin.

### Install & Upgrade

* Obtain the plugin binary jar file
	* Either download from Bintray (see [Related Links](#related-links)) 
	* Or by building yourself (see [Developers](#developers))
* If you already have another version of the plugin installed, first uninstall the previously 
 installed version of the plugin by following the steps under [Uninstall](#uninstall) below
* In Fortify Software Security Center:
	* Navigate to Administration->Plugins->Parsers
	* Click the `NEW` button
	* Accept the warning
	* Upload the plugin jar file
	* Enable the plugin by clicking the `ENABLE` button
  
### Uninstall

* In Fortify Software Security Center:
	* Navigate to Administration->Plugins->Parsers
	* Select the parser plugin that you want to uninstall
	* Click the `DISABLE` button
	* Click the `REMOVE` button 


## Obtain results

Please see the Yair documentation for detailed usage instructions. Note that the SSC parser plugin for Yair 
expects a JSON file as input, so Yair will need to be configured to generate reports in JSON format.

As an example, the following Linux/bash commands were used to generate the 
[src/test/resources/node_10.14.2-jessie.yair.json](src/test/resources/node_10.14.2-jessie.yair.json) 
file:

```bash
# Generate Yair configuration file
cat <<'EOF' > $PWD/yair.config
---
registry:
  host: "registry.hub.docker.com"

clair:
  host: "clair:6060"

output:
  format: json

fail_on:
  score: 0
  big_vulnerability: false
EOF

# Analyze the node:10.14.2-jessie image and save results in JSON file
docker run -v $PWD/yair.config:/opt/yair/config/config.yaml:ro --link clair:clair yfoelling/yair node:10.14.2-jessie > node_10.14.2-jessie.yair.json
```

The example above assumes that you want to scan images from the Docker Hub registry, and that Clair is running inside
another Docker container named `clair`. For completeness, the following Linux/bash commands were used to set up
such a Clair container for testing and demonstration purposes:

```bash
# Start Postgres DB without superuser password (for testing only)
docker run -e POSTGRES_HOST_AUTH_METHOD=trust --name postgres -p 5432:5432 -d postgres

# Check Postgres started OK
docker logs postgres

# Create and navigate into clair directory
mkdir clair
cd clair

# Get sample config, save as clair.config
curl -L https://raw.githubusercontent.com/coreos/clair/master/config.yaml.sample -o $PWD/clair.config

# Update config file to use postgres docker container
sed -i 's/source: host=localhost/source: host=postgres/g' $PWD/clair.config

# Run clair as dameon
docker run --name clair --link postgres:postgres -p 6060:6060 -p 6061:6061 -v $PWD/clair.config:/config/config.yaml -d quay.io/coreos/clair:latest -config=/config/config.yaml

# Check Clair started OK
docker logs clair
```


## Upload results

SSC web interface (manual upload):

* Navigate to the Artifacts tab of your application version
* Click the `UPLOAD` button
* Click the `ADD FILES` button, and select the JSON file to upload
* Enable the `3rd party results` check box
* Select the `CLAIR_YAIR` type
  
SSC clients (FortifyClient, Maven plugin, ...):

* Generate a scan.info file containing a single line as follows:  
  `engineType=CLAIR_YAIR`
* Generate a zip file containing the following:
	* The scan.info file generated in the previous step
	* The JSON file containing scan results
* Upload the zip file generated in the previous step to SSC
	* Using any SSC client, for example FortifyClient
	* Similar to how you would upload an FPR file



## Developers

The following sections provide information that may be useful for developers of this 
parser plugin.

### IDE's

This project uses Lombok. In order to have your IDE compile this project without errors, 
you may need to add Lombok support to your IDE. Please see https://projectlombok.org/setup/overview 
for more information.

### Gradle Wrapper

It is strongly recommended to build this project using the included Gradle Wrapper
scripts; using other Gradle versions may result in build errors and other issues.

The Gradle build uses various helper scripts from https://github.com/fortify-ps/gradle-helpers;
please refer to the documentation and comments in included scripts for more information. 

### Common Commands

All commands listed below use Linux/bash notation; adjust accordingly if you
are running on a different platform. All commands are to be executed from
the main project directory.

* `./gradlew tasks --all`: List all available tasks
* Build: (plugin binary will be stored in `build/libs`)
	* `./gradlew clean build`: Clean and build the project
	* `./gradlew build`: Build the project without cleaning
	* `./gradlew dist`: Build distribution zip
* Version management:
	* `./gradlew printProjectVersion`: Print the current version
	* `./gradlew startSnapshotBranch -PnextVersion=2.0`: Start a new snapshot branch for an upcoming `2.0` version
	* `./gradlew releaseSnapshot`: Merge the changes from the current branch to the master branch, and create release tag
* `./fortify-scan.sh`: Run a Fortify scan; requires Fortify SCA to be installed

Note that the version management tasks operate only on the local repository; you will need to manually
push any changes (including tags and branches) to the remote repository.

### Versioning

The various version-related Gradle tasks assume the following versioning methodology:

* The `master` branch is only used for creating tagged release versions
* A branch named `<version>-SNAPSHOT` contains the current snapshot state for the upcoming release
* Optionally, other branches can be used to develop individual features, perform bug fixes, ...
	* However, note that the Gradle build may be unable to identify a correct version number for the project
	* As such, only builds from tagged versions or from a `<version>-SNAPSHOT` branch should be published to a Maven repository

### CI/CD

Travis-CI builds are automatically triggered when there is any change in the project repository,
for example due to pushing changes, or creating tags or branches. If applicable, binaries and related 
artifacts are automatically published to Bintray using the `bintrayUpload` task:

* Building a tagged version will result in corresponding release version artifacts to be published
* Building a branch named `<version>-SNAPSHOT` will result in corresponding beta version artifacts to be published
* No artifacts will be deployed for any other build, for example when Travis-CI builds the `master` branch

See the [Related Links](#related-links) section for the relevant Travis-CI and Bintray links.


## License
<x-insert text="<!--"/>

See [LICENSE.TXT](LICENSE.TXT)

<x-insert text="-->"/>

<x-include url="file:LICENSE.TXT"/>