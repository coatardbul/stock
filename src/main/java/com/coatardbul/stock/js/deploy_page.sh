cd /usr/local/learnvue/
unzip -o dist.zip
cd /usr/local/
sh deploy_nginx.sh
/usr/local/nginx/sbin/nginx -c /usr/local/nginx/conf/nginx.conf

