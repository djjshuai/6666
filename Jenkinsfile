pipeline {
    agent any 
    
    environment {
        APP_NAME = "jenkins-test"
        ARTIFACT_PATH = "target/${APP_NAME}-*.jar"
        VERSION = "1.0.${env.BUILD_NUMBER}"
        GIT_REPO = "git@github.com:djjshuai/6666.git"
        GIT_CRED_ID = "95d4aeb6-2265-4ff7-9889-2e6cadb7afc0"
        DEPLOY_SERVER = "root@192.168.127.100"
        DEPLOY_PATH = "/opt/apps"
    }
    
    post {
        success {
            echo "✅ 构建成功！版本：${VERSION}"
        }
        
        failure {
            echo "❌ 构建失败！"
        }
        
        always {
            cleanWs()
        }
    }

    stages {
        stage("拉取代码") {
            steps {
                echo "从 GitHub 拉取最新代码..."
                checkout scm
                sh "git log -1 --pretty=format:'%h - %an, %ar : %s'"
            }
        }

        stage("编译打包") {
            steps {
                echo "开始编译，版本号：${VERSION}..."
                sh "mvn clean package -DskipTests -Dproject.version=${VERSION}"
            }
            post {
                success {
                    archiveArtifacts(
                        artifacts: "${ARTIFACT_PATH}",
                        fingerprint: true,
                        onlyIfSuccessful: true
                    )
                    echo "编译成功！产物路径：${ARTIFACT_PATH}"
                }
                failure {
                    error("编译失败，终止流程！")
                }
            }
        }

        stage("单元测试") {
            steps {
                echo "执行单元测试..."
                sh "mvn test"
            }
            post {
                always {
                    junit(
                        allowEmptyResults: true,
                        testResults: "target/surefire-reports/*.xml"
                    )
                }
                failure {
                    error("测试失败，终止流程！")
                }
            }
        }

        stage("版本管理（打 Tag）") {
            steps {
                echo "为版本 ${VERSION} 创建 Git 标签..."
                sh """
                    git config --global user.name "Jenkins"
                    git config --global user.email "jenkins@example.com"
                    git tag -a "v${VERSION}" -m "自动打 Tag：${VERSION}"
                    git push origin "v${VERSION}"
                """
            }
            post {
                success {
                    echo "Tag v${VERSION} 已推送到 GitHub！"
                }
                failure {
                    error("打 Tag 失败，终止流程！")
                }
            }
        }


        stage("部署到服务器") {
            steps {
                echo "部署 ${VERSION} 到服务器 ${DEPLOY_SERVER}..."
                sshagent(credentials: ['a0682bfb-c489-4ece-b201-ade94b13e1bc']) {
                    sh """
                        ssh ${DEPLOY_SERVER} "mkdir -p ${DEPLOY_PATH}"
                        scp ${ARTIFACT_PATH} ${DEPLOY_SERVER}:${DEPLOY_PATH}/${APP_NAME}-${VERSION}.jar
                        ssh ${DEPLOY_SERVER} "ps -ef | grep ${APP_NAME} | grep -v grep | awk '{print \$2}' | xargs -r kill -9"
                        ssh ${DEPLOY_SERVER} "nohup java -jar ${DEPLOY_PATH}/${APP_NAME}-${VERSION}.jar > ${DEPLOY_PATH}/app.log 2>&1 &"
                    """
                }
            }
            post {
                success {
                    echo "部署成功！应用已启动，版本：${VERSION}"
                }
                failure {
                    error("部署失败，请检查服务器连接！")
                }
            }
        }
    }
}
