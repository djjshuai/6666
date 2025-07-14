pipeline {
    agent any 
   environment {
   
    APP_NAME = "jenkins-test"
   
    ARTIFACT_PATH = "target/${APP_NAME}-*.jar"  // 匹配 target/jenkins-test-*.jar

    VERSION = "1.0.${env.BUILD_NUMBER}"
    GIT_REPO = "git@github.com:djjshuai/6666.git"
    GIT_CRED_ID = "95d4aeb6-2265-4ff7-9889-2e6cadb7afc0"
    DEPLOY_SERVER = "root@192.168.127.100"  // 替换为你的部署服务器
    DEPLOY_PATH = "/opt/apps"
}
    // 构建后操作：成功/失败都执行
    post {
        success {
            // 成功通知
            echo "✅ 构建成功！版本：${VERSION}"
            // 邮件通知
            emailext(
                to: "2313495658@qq.com",
                subject: "[成功] ${APP_NAME} 构建 #${BUILD_NUMBER}",
                body: "版本：${VERSION}\n详情：${BUILD_URL}"
            )
        }
        failure {
            echo "❌ 构建失败！"
            emailext(
                to: "2313495658@qq.com",
                subject: "[失败] ${APP_NAME} 构建 #${BUILD_NUMBER}",
                body: "版本：${VERSION}\n详情：${BUILD_URL}"
            )
        }
        always {
            // 无论成功失败，都清理工作区临时文件
            cleanWs()
        }
    }

    stages {
        // 阶段 1：拉取代码
        stage("拉取代码") {
            steps {
                echo "从 GitHub 拉取最新代码..."
                checkout scm  // 自动拉取配置的仓库代码
                // 打印最新提交信息，确认代码正确
                sh "git log -1 --pretty=format:'%h - %an, %ar : %s'"
            }
        }

        // 阶段 2：编译打包
       stage("编译打包") {
    steps {
        echo "开始编译，版本号：${VERSION}..."
        sh "mvn clean package -DskipTests -Dproject.version=${VERSION}"
    }
    post {
        success {
            archiveArtifacts(
                artifacts: "${ARTIFACT_PATH}",  // 使用上面定义的 ARTIFACT_PATH
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

        // 阶段 3：单元测试
        stage("单元测试") {
            steps {
                echo "执行单元测试..."
                sh "mvn test"  // 执行测试用例
            }
            post {
                always {
                    // 收集测试报告
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

        // 阶段 4：版本管理（自动打 Tag 并推送到 GitHub）
        stage("版本管理（打 Tag）") {
            steps {
                echo "为版本 ${VERSION} 创建 Git 标签..."
                // 配置 Git 用户名和邮箱（提交 Tag 时需要）
                sh """
                    git config --global user.name "Jenkins"
                    git config --global user.email "jenkins@example.com"
                    git tag -a "v${VERSION}" -m "自动打 Tag：${VERSION}"  # 创建带注释的 Tag
                    git push origin "v${VERSION}"  # 推送到 GitHub
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

        // 阶段 5：部署到服务器（核心新步骤）
        stage("部署到服务器") {
            steps {
                echo "部署 ${VERSION} 到服务器 ${DEPLOY_SERVER}..."
                // 使用 SSH 代理执行部署命令
                sshagent(credentials: [GIT_CRED_ID]) {
                    sh """
                        # 1. 检查目标服务器部署目录，不存在则创建
                        ssh ${DEPLOY_SERVER} "mkdir -p ${DEPLOY_PATH}"
                        
                        # 2. 上传构建产物到目标服务器
                        scp ${ARTIFACT_PATH} ${DEPLOY_SERVER}:${DEPLOY_PATH}/${APP_NAME}-${VERSION}.jar
                        
                        # 3. 在目标服务器启动应用（以 Java 为例）
                        # 先停止旧进程（如果存在）
                        ssh ${DEPLOY_SERVER} "ps -ef | grep ${APP_NAME} | grep -v grep | awk '{print \$2}' | xargs -r kill -9"
                        # 启动新进程（后台运行，输出日志到文件）
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
