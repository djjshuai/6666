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
        EMAIL_RECIPIENTS = "2313495658@qq.com"
    }
    
    post {  // post 块包含所有后续子块（success/failure/always）
        success {
            timeout(time: 5, unit: 'MINUTES') {
                echo "✅ 构建成功！版本：${VERSION}"
                
                withCredentials([string(credentialsId: 'qq-email-auth-code', variable: 'EMAIL_AUTH_CODE')]) {
                    sh '''
                        # 使用 openssl s_client 手动发送邮件
                        echo "准备通过命令行发送邮件..."
                        
                        # 修正EOF格式：确保EOF前后无空格，且单独成行
                        cat > email.txt <<'EOF'
From: "Jenkins" <2313495658@qq.com>
To: 2313495658@qq.com
Subject: Jenkins构建成功通知

项目 ${JOB_NAME} 构建成功！
版本: ${VERSION}
构建详情: ${BUILD_URL}
EOF

                        # 使用 openssl 连接 SMTP 服务器并发送邮件
                        echo "尝试通过命令行发送邮件..."
                        (
                            echo "HELO jenkins.example.com"
                            echo "AUTH LOGIN"
                            echo "$(echo -n "2313495658@qq.com" | base64)"  # 邮箱地址 Base64 编码
                            echo "$(echo -n "${EMAIL_AUTH_CODE}" | base64)"   # 授权码 Base64 编码
                            echo "MAIL FROM: <2313495658@qq.com>"
                            echo "RCPT TO: <2313495658@qq.com>"
                            echo "DATA"
                            cat email.txt
                            echo "."
                            echo "QUIT"
                        ) | openssl s_client -connect smtp.qq.com:465 -quiet
                        
                        echo "命令行邮件发送尝试完成"
                    '''
                }
            }
        }  // success 块结束
        
        failure {  // failure 是 post 的子块，正确嵌套
            timeout(time: 5, unit: 'MINUTES') {
                echo "❌ 构建失败！"
                emailext(
                    to: "${EMAIL_RECIPIENTS}",
                    subject: "[失败] ${APP_NAME} 构建 #${BUILD_NUMBER}",
                    mimeType: 'text/html',
                    body: """
                        <html>
                        <body>
                            <h3 style="color: #ef4444;">构建失败通知</h3>
                            <p><strong>项目：</strong>${APP_NAME}</p>
                            <p><strong>版本：</strong>${VERSION}</p>
                            <p><strong>构建编号：</strong>${BUILD_NUMBER}</p>
                            <p><strong>构建状态：</strong><span style="color: #ef4444;">失败</span></p>
                            <p><strong>触发人：</strong>${env.BUILD_USER}</p>
                            <p><strong>失败原因：</strong>请查看构建日志</p>
                            <p><strong>构建详情：</strong><a href="${BUILD_URL}console">点击查看日志</a></p>
                        </body>
                        </html>
                    """
                )
            }
        }  // failure 块结束
        
        always {  // always 是 post 的子块，正确嵌套
            cleanWs()
        }  // always 块结束
    }  // post 块整体结束


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

        stage("测试SMTP网络连接") {
            steps {
                echo "测试能否连接QQ邮箱SMTP服务器（smtp.qq.com:465）..."
                sh """
                    openssl s_client -connect smtp.qq.com:465 -crlf << EOF
                    QUIT
                    EOF
                """
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
