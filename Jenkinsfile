#!groovy

def workerNode = "devel10"

pipeline {
	agent {label workerNode}
	tools {
		maven "Maven 3"
	}
	triggers {
		pollSCM("H/03 * * * *")
		upstream(upstreamProjects: "Docker-payara5-bump-trigger",
			threshold: hudson.model.Result.SUCCESS)
	}
	options {
		timestamps()
	}
	stages {
		stage("clear workspace") {
			steps {
				deleteDir()
				checkout scm
			}
		}
		stage("verify") {
			steps {
				sh "mvn verify pmd:pmd findbugs:findbugs"
				junit "**/target/surefire-reports/TEST-*.xml,**/target/failsafe-reports/TEST-*.xml"
			}
		}
		stage('Warnings') {
			steps {
				warnings consoleParsers: [
						[parserName: "Java Compiler (javac)"],
						[parserName: "JavaDoc Tool"]
				],
						unstableTotalAll: "0",
						failedTotalAll: "0"
			}
		}

		stage('PMD') {
			steps {
				step([
						$class          : 'hudson.plugins.pmd.PmdPublisher',
						pattern         : '**/target/pmd.xml',
						unstableTotalAll: "0",
						failedTotalAll  : "0"
				])
			}
		}

		stage("docker") {
			steps {
				script {
					def image = docker.build("docker-io.dbc.dk/neptun-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER}")
					image.push()
				}
			}
		}
	}
}
