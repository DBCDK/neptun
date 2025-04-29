#!groovy

def workerNode = "devel12"

pipeline {
	agent {label workerNode}
	tools {
		maven "Maven 3"
	}
	triggers {
		upstream(upstreamProjects: "Docker-payara6-bump-trigger",
			threshold: hudson.model.Result.SUCCESS)
	}
    environment {
        SONAR_SCANNER_HOME = tool 'SonarQube Scanner from Maven Central'
        SONAR_SCANNER = "$SONAR_SCANNER_HOME/bin/sonar-scanner"
        SONAR_PROJECT_KEY = "rawrepo-v2"
        SONAR_SOURCES="src"
        SONAR_TESTS="test"
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
        stage("build") {
            steps {
                withSonarQubeEnv(installationName: 'sonarqube.dbc.dk') {
                    script {
                        def status = 0

                        def sonarOptions = "-Dsonar.branch.name=${BRANCH_NAME}"
                        if (env.BRANCH_NAME != 'master') {
                            sonarOptions += " -Dsonar.newCode.referenceBranch=master"
                        }

                        // Do sonar via maven
                        status += sh returnStatus: true, script: """
                            mvn -B $sonarOptions -pl '!debian' sonar:sonar
                        """

                        if (status != 0) {
                            error("build failed")
                        }
                    }
                }
                script {
                    def status = sh returnStatus: true, script:  """
                        rm -rf \$WORKSPACE/.repo
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo dependency:resolve dependency:resolve-plugins >/dev/null || true
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo clean
                    """

                    // We want code-coverage and pmd/spotbugs even if unittests fails
                    status += sh returnStatus: true, script:  """
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo verify pmd:pmd pmd:cpd spotbugs:spotbugs javadoc:aggregate
                    """

                    junit testResults: '**/target/*-reports/*.xml'

                    def java = scanForIssues tool: [$class: 'Java']
                    def javadoc = scanForIssues tool: [$class: 'JavaDoc']
                    publishIssues issues:[java, javadoc], unstableTotalAll:1

                    def pmd = scanForIssues tool: [$class: 'Pmd']
                    publishIssues issues:[pmd], unstableTotalAll:1

                    def spotbugs = scanForIssues tool: [$class: 'SpotBugs']
                    publishIssues issues:[spotbugs], unstableTotalAll:1

                    if (status != 0) {
                        error("build failed")
                    } else {
                        docker.image("docker-metascrum.artifacts.dbccloud.dk/neptun-service:${env.BRANCH_NAME}-${env.BUILD_NUMBER}").push()
                    }
                }
            }
        }
    }
}
