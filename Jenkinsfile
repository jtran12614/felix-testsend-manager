#!/bin/groovy
// put this file in the branches of the main/code repository

node {
    //checkout main project files
    // need to manually checkout in scripted pipeline (differs from declarative pipeline which checkouts by default)
    scmVars = checkout scm

    // Note: the code base still uses jdk8
    jdk = tool name: 'jdk 11'
    env.JAVA_HOME = "${jdk}"

    sh '$JAVA_HOME/bin/java -version'

    //checkout devops repository (other repository)
    sh 'mkdir -p devops'
    dir("devops")
            {
                script {
                    git branch: "develop",
                            url: 'ssh://git@git.rakuten-it.com:7999/FELIX/felix-jenkins-operations.git'
                }
            }

    println("scmVars: " + scmVars)

    // pass the environment variables to new pipeline
    // CHANGE_BRANCH is used for PR and GIT_BRANCH is for building directly at branch
    withEnv(["COMMIT=${scmVars.GIT_COMMIT}","BRANCH=${env.CHANGE_BRANCH ? env.CHANGE_BRANCH : scmVars.GIT_BRANCH}","ROOT_DIR=devops"]) {
        // load Jenkinsfile Pipeline file from devops repository
        load 'devops/Jenkinsfile_build_spring_modules.groovy'
    }
}