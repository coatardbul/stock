#!/bin/bash
netstat -antlp >netstatInfo.txt
IFS='\n'
sed -i '1,2d' netstatInfo.txt
for c in $(cat netstatInfo.txt); do
  pidinfo=$(echo $c | awk '{print $7}')
  foreignAddr=$(echo $c | awk '{print $5}')
  sshdInfo="sshd"
  javaInfo="java"
  nginxInfo="nginx"

  containSshdInfo=$(echo $pidinfo | grep "$sshdInfo")
  containJavaInfo=$(echo $pidinfo | grep "$javaInfo")
  containNginxInfo=$(echo $pidinfo | grep "$nginxInfo")

  if [[ $foreignAddr == '0.0.0.0:*' && "$containSshdInfo" != "" ]]; then
    pid=$(echo ${propinfo} | cut -d '/' -f1)
    echo "kill -9 $pid"
    if [[ "$pid" != "" ]]; then
      kill -9 $pid
    fi
  fi
  if [[ "$containSshdInfo" == "" && "$containJavaInfo" == "" && "$containNginxInfo" == "" ]]; then
    pid=$(echo ${propinfo} | cut -d '/' -f1)
    echo "kill -9 $pid"
    if [[ "$pid" != "" ]]; then
      kill -9 $pid
    fi
  fi

done

strA="long string"
strB="string"
result=$(echo $strA | grep "${strB}")
if [[ "$result" != "" ]]; then
  echo "包含"
else
  echo "不包含"
fi
