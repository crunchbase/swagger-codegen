#!/usr/bin/env bash
mvn clean package
cd spray-json-generator
mvn clean package
cd ..
cp modules/swagger-codegen-cli/target/swagger-codegen-cli.jar /crunchbase/metadata-service/bin/
cp spray-json-generator/target/spray-json-swagger-codegen-1.0.0.jar /crunchbase/metadata-service/bin/
