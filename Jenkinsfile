#!/usr/bin/env groovy

pipeline {
  options {
    timeout(time: 30, unit: 'MINUTES')
    disableConcurrentBuilds()
  }
  parameters {
    string (name: 'GIT_REVISION', defaultValue: 'HEAD', description: 'Git revision to build')
    string (name: 'ENV', defaultValue: 'dev', description: 'env to deploy to')
    string (name: 'TAG', defaultValue: '', description: 'for tracking build to stage or prod')
  }
  agent {
    node {
      label 'master'
      customWorkspace './open-termhub'
    }
  } // single node jenkins
  environment {
    ENV = "${params.ENV}"
    S3_CREDENTIALS = credentials('DOCKER_HELM_S3')
    AWS_ACCESS_KEY_ID = "${env.S3_CREDENTIALS_USR}"
    AWS_SECRET_ACCESS_KEY = "${env.S3_CREDENTIALS_PSW}"
    AWS_DEFAULT_REGION = 'us-west-2'
    CC_TEST_REPORTER_ID = '59e2bccfce77d873a104a4566bd643ec2ad0d3bbe4eae693b6b7c146934b0105'
  }
  stages {
    stage('Git clone and setup') {
      steps {
        echo 'Checkout code'
        script {
          if (params.GIT_REVISION == 'HEAD') {
            checkout scm
          } else {
            checkout([$class: 'GitSCM',
              branches: [[name: "${params.GIT_REVISION}"]],
              userRemoteConfigs: scm.userRemoteConfigs,
              submoduleCfg: []
            ])
          }
        }
        echo 'Setup Helm'
        sh 'helm init --client-only || :'
        sh 'helm plugin install https://github.com/hypnoglow/helm-s3.git --version 0.6.0 || :'
        sh 'helm repo add wci-helm s3://wci-helm/charts || :'
        echo 'Setup Gradle'
        sh 'mkdir -p .gradle || :'
        sh 'cp /var/lib/jenkins/.gradle/gradle.properties ./.gradle/gradle.properties || :'
        sh 'chown -R jenkins:docker .gradle || :'
      }
    }
    stage('Build and test') {
      when {
        environment name: 'ENV', value: 'dev'
      }
      steps {
        echo 'Build docker image and run unit tests'
        sh 'make build'
      }
    }
    stage('release') {
      when {
        branch 'master'
        environment name: 'ENV', value: 'dev'
      }
      steps {
        echo 'Package and push image and chart artifacts'
        sh 'make release'
      }
    }
    stage('deploy') {
      when {
        branch 'master'
      }
      steps {
        echo "Deploy to ${params.ENV}"
        withCredentials([file(credentialsId: "${params.ENV}-kubecfg", variable:'KUBECONFIG')]) {
          sh "ENV=${params.ENV} make deploy"
        }
      }
    }
  }
  post {
    always {
        echo 'Cleaning Workspace'
        cleanWs()
    }
    failure {
      slackSend (color: '#FF0000', message: "FAILED: Build '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
    }
  }
}
