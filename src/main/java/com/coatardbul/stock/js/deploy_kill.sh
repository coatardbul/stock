listenInfo=$(netstat -antlp)

IFS=$'\n'
for c in $listenInfo; do
  echo "$c"
    echo `echo $c|awk '{print $7}'`
done

#num=-16
#
#pidStr='4444/ssss'
#for i in $listenInfo
#do
#        num=`expr ${num} + 1`
#        echo "$num"
#        echo "$i"
#done