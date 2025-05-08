

mvn archetype:generate \
  -DgroupId=com.xinchentechnote \
  -DartifactId=jmh-lab \
  -DarchetypeArtifactId=maven-archetype-quickstart \
  -DinteractiveMode=false

mvn -f jmh-lab/pom.xml clean verify
