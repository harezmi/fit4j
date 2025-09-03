#!/bin/bash
to_camel_case() {
   echo "$1" | awk '
     BEGIN { FS="-"; OFS="" }
     {
         for(i=1; i<=NF; i++) {
             $i = toupper(substr($i,1,1)) substr($i,2)
         }
         print
     }' | sed 's/-//g'
}

if [ $# -eq 0 ]; then
    echo "Error: No project name provided."
    echo "Usage: $0 <project-name without example prefix>"
    exit 1
fi

echo "Project name provided: $1"

projectName="example-$1"
buildFileContent=\
"dependencies{
   // dependencies for the project in addition to parent project dependencies
}
"
propertiesFileContent=\
""

packageName="com.fit4j.examples."$(echo $1 | sed 's/-//g')
testClassName=$(to_camel_case $1)"ExampleAcceptanceTest"
testFileName=$testClassName".kt"
testFileContent=\
"package $packageName

import com.fit4j.AcceptanceTest
import com.fit4j.helpers.AcceptanceTestHelper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

@AcceptanceTest
class $testClassName {
    @Autowired
    private lateinit var helper: AcceptanceTestHelper

    @Test
    fun \`it should work\`() {
      Assertions.assertNotNull(helper)
    }
}
"

packageDir=$(echo $packageName | sed 's/\./\//g')

mkdir -p $projectName/src/test/kotlin/$packageDir
mkdir -p $projectName/src/test/resources

touch $projectName/src/test/resources/application-test.properties
touch $projectName/build.gradle.kts
touch $projectName/src/test/kotlin/$packageDir/$testFileName

cat <<EOL > $projectName/build.gradle.kts
$buildFileContent
EOL
cat <<EOL > $projectName/src/test/resources/application-test.properties
$propertiesFileContent
EOL
cat <<EOL > $projectName/src/test/kotlin/$packageDir/$testFileName
$testFileContent
EOL
echo "include(\"$projectName\")" >> settings.gradle.kts

echo "Project generated: $projectName"

