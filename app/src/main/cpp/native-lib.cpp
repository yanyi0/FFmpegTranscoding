#include <jni.h>
#include <string>
#include <unistd.h>

extern "C" {
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavfilter/avfilter.h>
#include <libavcodec/jni.h>
#include <media/NdkMediaCodec.h>
#include <android/log.h>
#define FF_LOG_TAG     "FFmpeg"
#define FF_LOG_VERBOSE     ANDROID_LOG_VERBOSE
#define FF_LOG_DEBUG         ANDROID_LOG_DEBUG
#define FF_LOG_INFO              ANDROID_LOG_INFO
#define FF_LOG_WARN           ANDROID_LOG_WARN
#define FF_LOG_ERROR        ANDROID_LOG_ERROR
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  FF_LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, FF_LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, FF_LOG_TAG, __VA_ARGS__)

static void log_callback_test2(void *ptr, int level, const char *fmt, va_list vl)
{
    va_list vl2;
    char *line = (char *)malloc(128 * sizeof(char));
    static int print_prefix = 1;
    va_copy(vl2, vl);
    av_log_format_line(ptr, level, fmt, vl2, line, 128, &print_prefix);
    va_end(vl2);
    line[127] = '\0';
    LOGI("%s", line);
    free(line);
}

JNIEXPORT jstring JNICALL
Java_com_fish_ffmpegtranscoding_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
    }
JNIEXPORT jstring JNICALL
Java_com_fish_ffmpegtranscoding_MainActivity_ffmpegInfo(JNIEnv *env, jobject  /* this */) {
    av_log_set_callback(log_callback_test2);
    char info[40000] = {0};
    AVCodec *c_temp = av_codec_next(NULL);
    while (c_temp != NULL) {
        if (c_temp->decode != NULL) {
            sprintf(info, "%sdecode:", info);
        } else {
            sprintf(info, "%sencode:", info);
        }
        switch (c_temp->type) {
            case AVMEDIA_TYPE_VIDEO:
                sprintf(info, "%s(video):", info);
                break;
            case AVMEDIA_TYPE_AUDIO:
                sprintf(info, "%s(audio):", info);
                break;
            default:
                sprintf(info, "%s(other):", info);
                break;
        }
        if (strcmp(c_temp->name,"h264_hlmediacodec") == 0){
            sprintf(info, "%s[%s]\n", info, c_temp->name);
        }
        sprintf(info, "%s[%s]\n", info, c_temp->name);
        c_temp = c_temp->next;
    }
//    AVCodec *codec =avcodec_find_encoder_by_name("h264_hlmediacodec") ;
    return env->NewStringUTF(info);
    }
}