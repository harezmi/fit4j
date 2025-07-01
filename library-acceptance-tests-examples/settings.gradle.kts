rootProject.name = "library-acceptance-tests-examples"

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		mavenLocal()
		maven(url = "https://packages.confluent.io/maven/")
		maven {
			name = "GitHubPackages"
			url = uri("https://maven.pkg.github.com/udemy/packages-repo")
			credentials {
				username = System.getenv("GITHUB_PACKAGES_USERNAME")
				password = System.getenv("GITHUB_PACKAGES_TOKEN")
			}
		}
		maven {
			name = "Artifactory"
			url = uri("https://pkg.udemy.dev/artifactory/udemy-maven")
			credentials {
				username = System.getenv("ART_USER_NAME")
				password = System.getenv("ART_API_KEY")
			}
		}
	}
	versionCatalogs {
		create("libs") {
			from("com.udemy.services:version-catalog:2.7.8")
		}
	}
}

include("example-basic")
include("example-kafka")
include("example-grpc")
include("example-event-tracking")
include("example-experimentation-platform")
include("example-elastic-search")
include("example-redis")
include("example-mysql")
include("example-kafka-testcontainers")
include("example-rest")
include("example-h2")
include("example-dynamodb-testcontainers")
include("example-dynamodb")
include("example-redis-embedded")
include("example-s3")
include("example-coroutines")
