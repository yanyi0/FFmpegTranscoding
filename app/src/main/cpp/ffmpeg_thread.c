#include "libavcodec/avcodec.h"
#include "ffmpeg_thread.h"
#include "android_log.h"

pthread_t ntid;
char **argvs = NULL;
int num=0;


void *thread(void *arg)
{   //执行
    for(int i = 0;i< num;i++){
        LOGI("-------ffmpeg_thread_run_cmd *thread argv--------%s",argvs[i]);
    }
    int result = ffmpeg_exec(num, argvs);
    LOGI("--------ffmpeg_thread--ffmpeg_exec-%d-----",result);
    ffmpeg_thread_exit(result);
    return ((void *)0);
}
/**
 * 新建子线程执行ffmpeg命令
 */
int ffmpeg_thread_run_cmd(int cmdnum,char **argv){
    num=cmdnum;
    argvs=argv;
    for(int i = 0;i< cmdnum;i++){
        LOGI("-------ffmpeg_thread_run_cmd argv--------%s",argv[i]);
    }
    int temp =pthread_create(&ntid,NULL,thread,NULL);
    if(temp!=0)
    {
        LOGE("can't create thread: %s ",strerror(temp));
        return 1;
    }
    LOGI("create thread succes: %s ",strerror(temp));
    return 0;
}

static void (*ffmpeg_callback)(int ret);
/**
 * 注册线程回调
 */
void ffmpeg_thread_callback(void (*cb)(int ret)){
    LOGI("-------ffmpeg_thread.c ffmpeg_thread_callback--------");
    ffmpeg_callback = cb;
}

/**
 * 退出线程
 */
void ffmpeg_thread_exit(int ret){
    LOGI("-------ffmpeg_thread.c ffmpeg_thread_exit--------%d",ret);
    if (ffmpeg_callback) {
        LOGI("-------ffmpeg_thread.c ffmpeg_thread_exit ffmpeg_callback--------%d",ret);
        ffmpeg_callback(ret);

    }
    LOGI("-------ffmpeg_thread.c ffmpeg_thread_exit ffmpeg_callback finish--------%d",ret);
    pthread_exit("ffmpeg_thread_exit");
    LOGI("-------ffmpeg_thread.c ffmpeg_thread_exit pthread_exit finish--------%d",ret);
}

/**
 * 取消线程
 */
void ffmpeg_thread_cancel(){
    void *ret=NULL;
    pthread_join(ntid, &ret);
}
