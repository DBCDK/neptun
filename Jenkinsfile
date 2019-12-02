#!groovy

def workerNode = "devel8"
def configFileBranches = ["master", "develop"]

pipeline {
	agent {label workerNode}
	tools {
		// refers to the name set in manage jenkins -> global tool configuration
		gradle "gradle-4"
	}
	environment {
		MARATHON_TOKEN = credentials("METASCRUM_MARATHON_TOKEN")
		SONARQUBE_HOST = "http://sonarqube.mcp1.dbc.dk"
		SONARQUBE_TOKEN = credentials("dataio-sonarqube")
	}
	triggers {
		pollSCM("H/03 * * * *")
		upstream(upstreamProjects: "neptun/dbckat-config-files/develop,neptun/dbckat-config-files/next,Docker-payara5-bump-trigger",
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
					// temporarily disabled because jenkins enters a build loop when the
					// config files are fetched from this repository
					//configFileBranches.each {
					//	dir("config-files/${it}/tmp") {
					//		git(url: "gitlab@git-platform.dbc.dk:metascrum/dbckat-config-files.git",
					//			credentialsId: "gitlab-meta", branch: it)
					//		// TODO: structure versioning with git tags
					//		sh "zip -r ../1.zip *"
					//	}
					//	sh "rm -rf config-files/${it}/tmp"
					//}
					sh """
						curl -LO https://is.dbc.dk/job/neptun/job/dbckat-config-files/job/develop/lastSuccessfulBuild/artifact/config-files.zip
						mkdir config-files && unzip config-files.zip -d config-files
					"""
					def image = docker.build("docker-io.dbc.dk/neptun-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER}")
					image.push()

					sh """
						rm -rf config-files config-files.zip
						curl -LO https://is.dbc.dk/job/neptun/job/dbckat-config-files/job/next/lastSuccessfulBuild/artifact/config-files.zip
						mkdir config-files && unzip config-files.zip -d config-files
					"""
					image = docker.build("docker-io.dbc.dk/neptun-service-config-files-next:${env.BRANCH_NAME}-${env.BUILD_NUMBER}")
					image.push()
				}
			}
		}
		stage("sonarqube") {
			when {
				branch "master"
			}
			steps {
				script {
					try {
						sh """
							mvn sonar:sonar \
							-Dsonar.host.url=$SONARQUBE_HOST \
							-Dsonar.login=$SONARQUBE_TOKEN
						"""
					} catch(e) {
						printf "sonarqube connection failed: %s", e.toString()
					}
				}
			}
		}
	}
}
