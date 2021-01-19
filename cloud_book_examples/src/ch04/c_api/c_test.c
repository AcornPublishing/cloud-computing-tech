#include <unistd.h>
#include <stdio.h>
#include <zookeeper.h>

zhandle_t *zh;
int completeZkInit = -1;
clientid_t zkClientId;

void printChildren() {
        String_vector children;
        zoo_get_children(zh, "/test", 1, &children);

        char fullPath[1024];
        int result = -1;
        printf("==================== children of /test ==================\n");
        for (int i = 0; i < children.count; i++) {
                sprintf(fullPath, "/test/%s", children.data[i]);
                Stat stat;
                char *resultData;
                int resultLenth;
                result = zoo_get(zh, fullPath, 0, resultData, &resultLenth, &stat);

                if (result != ZOK) {
                        printf("Get Error:%s,%s", fullPath, zerror(result));
                        continue;
                }

                printf("%s\n", children.data[i]);
        }
}

//ZooKeeper Watcher
void watcher(zhandle_t *zzh, int type, int state, const char *path, void* context) {
    if (type == ZOO_SESSION_EVENT) {
        if (state == ZOO_CONNECTED_STATE) {
            const clientid_t *id = zoo_client_id(zzh);
            if (zkClientId.client_id == 0 || zkClientId.client_id != id->client_id) {
                zkClientId = *id; 
                completeZkInit = 1;
            }
        }
        } else if (state == ZOO_AUTH_FAILED_STATE || state == ZOO_EXPIRED_SESSION_STATE) {
                zookeeper_close(zzh);
                zh = 0;
        exit(-1);
        } else if (type == ZOO_CREATED_EVENT) {
                printf("\nZOO_CREATED_EVENT(%s)\n", path);
        } else if (type ==ZOO_DELETED_EVENT) {
                printf("\nZOO_DELETE_EVENT(%s)\n", path);
        } else if ( type == ZOO_CHANGED_EVENT) {
                printf("\nZOO_CHANGED_EVENT(%s)\n", path);
        } else if (type == ZOO_CHILD_EVENT) {
                printf("\nZOO_CHILD_CHANGED_EVENT(%s)\n", path);
                printChildren();
        } else {
                printf("ETC_EVENT(%d,%s)\n", type, path);
        }
}

int main(int argc, char *argv[])  {
        char zkServers[100] = "127.0.0.1:2181";

        zoo_set_debug_level(ZOO_LOG_LEVEL_WARN);
        zoo_deterministic_conn_order(1); 
        zh = zookeeper_init(zkServers, watcher, 60000, &zkClientId, 0, 0);

        if (!zh) {
                printf("ZK Init error(%s)\n", zkServers);
                return 0;
        }

        while (true) {
                sleep(1);
                if (completeZkInit == 1) {
                        break;
                }
        }

        char resultPath[1024] = {0,};
        int resultLength;

        zoo_create(zh, "/test", "", 0, &ZOO_OPEN_ACL_UNSAFE, 0, resultPath, 1024);

        //watchctx_t ctxWC;
        Stat zooStat;
        int result = zoo_exists(zh, "/test", 1, &zooStat);
        if (result == ZOK) {
                printf("node exists\n");
        }
        String_vector children;
        zoo_get_children(zh, "/test", 1, &children);
        zoo_create(zh, "/test/sub01", "", 0, &ZOO_OPEN_ACL_UNSAFE, ZOO_EPHEMERAL, resultPath, 1024);
        zoo_create(zh, "/test/sub02", "", 0, &ZOO_OPEN_ACL_UNSAFE, ZOO_EPHEMERAL, resultPath, 1024);

        zoo_delete(zh, "/test", -1);
        while(1) {
                sleep(20);
        }
}