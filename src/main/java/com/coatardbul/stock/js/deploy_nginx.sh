
APP_NAME=nginx
# 获取进程ID
PID=`ps -ef  |grep $APP_NAME |awk '{print $2}'`
# 停止 应用
if [ -z "$PID" ]
then
    echo "Application $APP_NAME is already stopped."
else
    for i in $PID
    do
        echo "Kill the $1 process [ $i ]"
        kill -9 $i
    done
fi
# 启动应用
/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/nginx.conf

# 暂停5秒
sleep 5

