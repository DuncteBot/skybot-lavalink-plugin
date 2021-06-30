#!/bin/sh

SERVERS="${ll_fr1_ip} ${ll_ca1_ip} ${ll_fr2_ip} ${ll_ca2_ip}"

# load all monitors
for SERVER in ${SERVERS}; do
  gnome-terminal -- /usr/bin/fish -c "ssh -p ${VPS_PORT} root@${SERVER} 'journalctl -fu lavalink -o cat'"
done


for SERVER in ${SERVERS}; do
  echo "Now updating ${SERVER}"

  # stop lavalink
  echo "stopping node"
  ssh -p $VPS_PORT root@$SERVER 'sudo systemctl stop lavalink'
  echo "stopped node"
  # upload file
  scp -P $VPS_PORT ./LavalinkServer/build/libs/Lavalink.jar root@$SERVER:./lavalink/
  # start lavalink
  echo "sarting node"
  ssh -p $VPS_PORT root@$SERVER 'sudo systemctl start lavalink'
  echo "started node"

  # wait a bit
  sleep 30s
done

echo "Done"