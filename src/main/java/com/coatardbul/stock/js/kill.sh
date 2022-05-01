listenInfo=$(netstat -antlp)

IFS=$'\n'
for c in $listenInfo; do
  echo "$c"
  for s in $c; do
    IFS=$'\t'
    echo "$s"
  done
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

aaaaaaaa  ddddddd vvvv
ssss