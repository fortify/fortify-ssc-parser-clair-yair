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
