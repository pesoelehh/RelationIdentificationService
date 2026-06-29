# Keycloak Realm Import

This directory is intended to contain the Keycloak realm export required to run the project.

The realm export is **not included** in this repository because it contains confidential configuration and credentials associated with the external DEHub environment.

To run the project:

1. Obtain a compatible Keycloak realm export from the DEHub project or the responsible maintainer.
2. Copy the realm export JSON file into this directory.
3. Start the project using Docker Compose.

If a realm export is present in this directory, Keycloak will automatically import it during startup.
