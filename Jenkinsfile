#!groovy

def workerNode = "devel8"

void deploy(String deployEnvironment) {
	dir("deploy") {
		git(url: "gitlab@git-platform.dbc.dk:metascrum/deploy.git", credentialsId: "gitlab-meta")
	}
	sh """
		virtualenv -p python3 .
		. bin/activate
		pip3 install --upgrade pip
		pip3 install -U -e \"git+https://github.com/DBCDK/mesos-tools.git#egg=mesos-tools\"
		marathon-config-producer deploy/marathon single neptun-${deployEnvironment} --template-keys BUILD_NUMBER=${env.BUILD_NUMBER} -o neptun-service-${deployEnvironment}.json
		marathon-deployer -a ${MARATHON_TOKEN} -b https://mcp1.dbc.dk:8443 deploy neptun-service-${deployEnvironment}.json
	"""
}

pipeline {
	agent {label workerNode}
	tools {
		// refers to the name set in manage jenkins -> global tool configuration
		maven "Maven 3"
	}
	environment {
		MARATHON_TOKEN = credentials("METASCRUM_MARATHON_TOKEN")
	}
	triggers {
		pollSCM("H/03 * * * *")
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
				sh "mvn verify pmd:pmd"
				junit "target/surefire-reports/TEST-*.xml,target/failsafe-reports/TEST-*.xml"
			}
		}
		stage("publish pmd results") {
			steps {
				step([$class: 'hudson.plugins.pmd.PmdPublisher', checkstyle: 'target/pmd.xml'])
			}
		}
		stage("docker build") {
			steps {
				script {
					def image = docker.build("docker-io.dbc.dk/neptun-service:${env.BUILD_NUMBER}")
					image.push()
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
	}
}
