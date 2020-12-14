#!groovy

def workerNode = "devel10"
def configFileBranches = ["master", "develop"]

pipeline {
	agent {label workerNode}
	tools {
		maven "Maven 3"
	}
	environment {
		MARATHON_TOKEN = credentials("METASCRUM_MARATHON_TOKEN")
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
	}
}
