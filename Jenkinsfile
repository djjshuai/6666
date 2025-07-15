pipeline {
    agent any 
    
    environment {
        APP_NAME = "jenkins-test"
        ARTIFACT_PATH = "target/${APP_NAME}-*.jar"
        VERSION = "1.0.${env.BUILD_NUMBER}"
        GIT_REPO = "git@github.com:djjshuai/666.git"
        GIT_CRED_ID = "95d4aeb6-2265-4ff7-9889-2e6cadb7afc0"
        DEPLOY_SERVER = "root@192.168.127.100"
        DEPLOY_PATH = "/opt/apps"
        EMAIL_RECIPIENTS = "2313495658@qq.com"
    }
    
    post {  // 确保post块正确缩进
        success {  // 确保success块正确缩进
            timeout(time: 5, unit: 'MINUTES') {
                echo "✅ 构建成功！版本：${VERSION}"
                
                withCredentials([string(credentialsId: 'qq-email-auth-code', variable: 'EMAIL_AUTH_CODE')]) {
                    sh '''
                        echo "准备通过命令行发送邮件..."
                        
                        # 生成邮件内容
                        cat > email.txt <<'EOF'
From: "Jenkins" <2313495658@qq.com>
To: 2313495658@qq.com
Subject: Jenkins构建成功通知

项目 ${JOB_NAME} 构建成功！
版本: ${VERSION}
构建详情: ${BUILD_URL}
EOF

                        # 发送邮件
                        echo "尝试通过命令行发送邮件..."
                        (
                            echo "HELO $(hostname)"
                            echo "AUTH LOGIN"
                            echo "$(echo -n "2313495658@qq.com" | base64 | tr -d '\n')"
                            echo "$(echo -n "${EMAIL_AUTH_CODE}" | base64 | tr -d '\n')"
                            echo "MAIL FROM: <2313495658@qq.com>"
                            echo "RCPT TO: <2313495658@qq.com>"
                            echo "DATA"
                            cat email.txt
                            echo "."
                            echo "QUIT"
                        ) | timeout 30 openssl s_client -connect smtp.qq.com:465 2>&1
                        
                        echo "命令行邮件发送尝试完成"
                    '''
                }
            }
        }  // success块结束
        
        failure {  // failure块正确缩进
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
        }  // failure块结束
        
        always {  // always块正确缩进
            cleanWs()
        }  // always块结束
    }  // post块结束

    stages {
        // 保持stages部分不变...
        // ...
    }
}
