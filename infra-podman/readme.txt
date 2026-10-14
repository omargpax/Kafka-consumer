-----------------------------
--- antes de apagar la PC ---
-----------------------------

# step 1
podman stop -t 60 control-center schema-registry kafka-broker-1 zookeeper votes-infrastructure-pg-votes-1
# step 2
podman machine stop


-------------------------
--- Al encender la PC ---
-------------------------

podman machine start

podman start votes-infrastructure-pg-votes-1 zookeeper
Start-Sleep -Seconds 20

podman start kafka-broker-1
Start-Sleep -Seconds 30

podman start schema-registry control-center

# Actualizar el reenvío de localhost hacia la IP actual de la VM
$vm = (podman machine ssh "ip -4 -o addr show eth0" | Select-String 'inet (\d+\.\d+\.\d+\.\d+)/').Matches[0].Groups[1].Value; netsh interface portproxy reset; foreach ($p in 9021,19092,15432,8081,2181) { netsh interface portproxy add v4tov4 listenaddress=127.0.0.1 listenport=$p connectaddress=$vm connectport=$p }; netsh interface portproxy show v4tov4

podman ps