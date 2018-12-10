#!groovy

def workerNode = "devel8"
def configFileBranches = ["master", "develop"]

void deploy(String deployEnvironment) {
	dir("deploy") {
		git(url: "gitlab@git-platform.dbc.dk:metascrum/deploy.git", credentialsId: "gitlab-meta")
	}
	sh """
		virtualenv -p python3 .
		. bin/activate
		pip3 install --upgrade pip
		pip3 install -U -e \"git+https://github.com/DBCDK/mesos-tools.git#egg=mesos-tools\"
		marathon-config-producer neptun-${deployEnvironment} --root deploy/marathon --template-keys DOCKER_TAG=${env.BRANCH_NAME}-${env.BUILD_NUMBER} -o neptun-service-${deployEnvironment}.json
		marathon-deployer -a ${MARATHON_TOKEN} -b https://mcp1.dbc.dk:8443 deploy neptun-service-${deployEnvironment}.json
	"""
}

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
		upstream(upstreamProjects: "neptun/dbckat-config-files/develop,neptun/dbckat-config-files/next",
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
				sh "gradle build pmdMain"
				junit "build/test-results/test/TEST-*.xml"
			}
		}
		stage("publish pmd results") {
			steps {
				step([$class: "hudson.plugins.pmd.PmdPublisher", pattern: "build/reports/pmd/*.xml"])
			}
		}
		stage("docker build") {
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
		stage("deploy staging") {
			when {
				branch "master"
			}
			steps {
				deploy("staging")
			}
		}
		stage("deploy next") {
			when {
				branch "master"
			}
			steps {
				deploy("next")
			}
		}
	}
}
