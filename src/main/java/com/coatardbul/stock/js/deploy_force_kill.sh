while 2>1; do

  pid1=$(ps -ef | grep priv | awk '{print $2}')
  kill -9 $pid1
  echo "kill -9  $pid1"

  pid4=$(ps -ef | grep kthreaddk | awk '{print $2}')
  kill -9 $pid4
  echo "kill -9  $pid4"

  pid7=$(netstat -antlp | awk '{print $7}' | grep -v 'java\|sshd\|-\|nginx\|Address' | cut -d '/' -f1)
  kill -9 $pid7
  echo "kill -9  $pid7"

  pid9=$(netstat -antlp | grep  unknown | awk '{print $7}'  | cut -d '/' -f1)
  kill -9 $pid9
  echo "kill -9  $pid9"

 pid11=$(netstat -antlp | grep  accepte | awk '{print $7}'  | cut -d '/' -f1)
  kill -9 $pid11
  echo "kill -9  $pid11"

  crontab -r
  sleep 3
done
