# FFmpegTranscoding
>[音视频文章汇总](https://www.jianshu.com/p/167b605add32)

##接到需求，做一个iOS和Android两端的编码测试工具，可选编码器，分辨率，帧率，码率控制ABR或CBR，GOP进行转码,查看软编码libx264和硬编码MediaCodec的编码效率和画质以及是否少帧，具体如下:
![0](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501163338.jpg)
![1.gif](https://upload-images.jianshu.io/upload_images/4193251-60f1c53402815759.gif?imageMogr2/auto-orient/strip)

Android效果图
![ffmepg_transcode](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501104013.jpg)
iOS效果图
![ios_ffmpeg_transcode](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501162021.jpg)
可以用ffmpeg自带的ffmpeg.c中的main函数来执行上面的所选参数，iOS端，ffmpeg是支持VideoToolBox硬编码h264和h265,直接传入所选参数即可执行，问题是Android端ffmpeg并不支持MediaCodec硬编码
###1.Android端，通过查看ffmpeg官网发现，ffmpeg只支持mediacodec硬解码，并不支持mediacodec硬编码，但目前Android手机是支持硬编码的，必须自己修改ffmpeg源码将MediaCodec硬编码添加到ffmpeg源码中，如何给ffmpeg添加codec呢？
![ffmpeg_mediacodec_wiki](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501101655.jpg)
查看[官网](https://wiki.multimedia.cx/index.php/FFmpeg_codec_HOWTO),大致分为五步
####A.查看libavcodec/avcodec.h中AVCodec结构体，知道我们新加的MediaCodec编码器有哪些属性,name,type,id,pix_fmts等
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501114213.jpg)
####B.编写自己的编码器MediaCodec,通过宏定义，取名h264_hlmediacodec,hevc_hlmediacodec分别代表h264和h265的编码器名称，根据此name可以找到编码器进行编码
```
// receive_packet modify to encode2
#define DECLARE_HLMEDIACODEC_ENC(short_name, full_name, codec_id, codec_type)                           \
    DECLARE_HLMEDIACODEC_VCLASS(short_name)                                                             \
    AVCodec ff_##short_name##_hlmediacodec_encoder = {                                                  \
        .name = #short_name "_hlmediacodec",                                                            \
        .long_name = full_name " (Ffmpeg MediaCodec NDK)",                                              \
        .type = codec_type,                                                                             \
        .id = codec_id,                                                                                 \
        .priv_class = &ff_##short_name##_hlmediacodec_enc_class,                                        \
        .priv_data_size = sizeof(HLMediaCodecEncContext),                                               \
        .init = hlmediacodec_encode_init,                                                               \
        .encode2 = hlmediacodec_encode_receive_packet,                                                  \
        .close = hlmediacodec_encode_close,                                                             \
        .capabilities = AV_CODEC_CAP_DELAY,                                                             \
        .caps_internal = FF_CODEC_CAP_INIT_THREADSAFE | FF_CODEC_CAP_INIT_CLEANUP,                      \
        .pix_fmts = (const enum AVPixelFormat[]){AV_PIX_FMT_NV12, AV_PIX_FMT_YUV420P, AV_PIX_FMT_NONE}, \
    };
#ifdef CONFIG_H264_HLMEDIACODEC_ENCODER
DECLARE_HLMEDIACODEC_ENC(h264, "H.264", AV_CODEC_ID_H264, AVMEDIA_TYPE_VIDEO)
#endif
#ifdef CONFIG_HEVC_HLMEDIACODEC_ENCODER
DECLARE_HLMEDIACODEC_ENC(hevc, "H.265", AV_CODEC_ID_HEVC, AVMEDIA_TYPE_VIDEO)
#endif
```
####C.libavcodec/avcodec.h中要有自己的编码器的id，上面传入的AV_CODEC_ID_H264，AV_CODEC_ID_HEVC在avcodec.h中本来就有
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501120516.jpg)
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501120552.jpg)
####D.libavcodec/allcodecs.c中导出新添加的编码器ff_h264_hlmediacodec_encoder,ff_hevc_hlmediacodec_encoder,这样获取所有的编码器能输出ff_h264_hlmediacodec_encoder和ff_hevc_hlmediacodec_encoder
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501121337.jpg)
####E.libavcodec/Makefile中添加新加的文件,编译到ffmpeg库中,编译的时候才会将这些新增的文件添加到ffmpeg库中
```
OBJS-$(CONFIG_HLMEDIACODEC)            += hlmediacodec.o hlmediacodec_codec.o
OBJS-$(CONFIG_AAC_HLMEDIACODEC_DECODER) += hlmediacodec_dec.o
OBJS-$(CONFIG_MP3_HLMEDIACODEC_DECODER) += hlmediacodec_dec.o
OBJS-$(CONFIG_H264_HLMEDIACODEC_DECODER) += hlmediacodec_dec.o
OBJS-$(CONFIG_H264_HLMEDIACODEC_ENCODER) += hlmediacodec_enc.o
OBJS-$(CONFIG_HEVC_HLMEDIACODEC_DECODER) += hlmediacodec_dec.o
OBJS-$(CONFIG_HEVC_HLMEDIACODEC_ENCODER) += hlmediacodec_enc.o
OBJS-$(CONFIG_MPEG4_HLMEDIACODEC_DECODER) += hlmediacodec_dec.o
OBJS-$(CONFIG_VP8_HLMEDIACODEC_DECODER) += hlmediacodec_dec.o
OBJS-$(CONFIG_VP9_HLMEDIACODEC_DECODER) += hlmediacodec_dec.o
SKIPHEADERS-$(CONFIG_HLMEDIACODEC)     += hlmediacodec.h hlmediacodec_codec.h
```
###2.编译的时候可以直接执行原始脚本编译嘛？答案是不是能的，需要修改脚本，我们需要在configure中打开硬件加速和新增的MediaCodec编码器,并且在链接外部库中新增链接libmediandk.so，如果不添加，则会编译报错，找不到MediaCodec的库，
--enable-mediacodec \
--enable-hlmediacodec \
--enable-hwaccels  \
--enable-decoder=h264_mediacodec \
--enable-encoder=h264_mediacodec \
--enable-decoder=hevc_mediacodec \
--enable-decoder=mpeg4_mediacodec \
--enable-encoder=mpeg4_mediacodec \
--enable-hwaccel=h264_mediacodec \
--enable-encoder=h264_hlmediacodec \
最后链接ndk中的libmediandk.so库文件，通过制定libmediandk.so库路径，这一步的实质是就是编译的时候再Mac环境下模拟出Android MediaCodec的硬编码环境
```
#!/bin/bash

echo ">>>>>>>>> 编译ffmpeg <<<<<<<<"

#NDK路径.
export NDK=/Users/cloud/Library/android-ndk-r20b
TOOLCHAIN=$NDK/toolchains/llvm/prebuilt/darwin-x86_64

#如果只需要单独的ffmpeg，不需要依赖x264，去掉$ADD_H264_FEATURE这句就可以了；
#如果你需要的是动态库，--enable-static 改为 --disable-static，--disable-shared 改为 --enable-shared

function build_android
{

echo "开始编译 $CPU"

./configure \
--prefix=$PREFIX \
--enable-neon  \
--enable-mediacodec \
--enable-hlmediacodec \
--enable-hwaccels  \
--enable-decoder=h264_mediacodec \
--enable-encoder=h264_mediacodec \
--enable-decoder=hevc_mediacodec \
--enable-decoder=mpeg4_mediacodec \
--enable-encoder=mpeg4_mediacodec \
--enable-hwaccel=h264_mediacodec \
--enable-encoder=h264_hlmediacodec \
--enable-gpl   \
--enable-postproc \
--enable-avresample \
--enable-avdevice \
--enable-pic \
--disable-shared \
--enable-debug \
--disable-yasm \
--enable-zlib \
--disable-bzlib \
--disable-iconv \
--disable-optimizations \
--disable-stripping \
--enable-small \
--enable-jni \
--enable-static \
--disable-doc \
--enable-ffmpeg \
--enable-ffplay \
--enable-ffprobe \
--disable-doc \
--disable-symver \
--cross-prefix=$CROSS_PREFIX \
--target-os=android \
--arch=$ARCH \
--cpu=$CPU \
--cc=$CC \
--cxx=$CXX \
--enable-cross-compile \
--sysroot=$SYSROOT \
--extra-cflags="-Os -fpic $OPTIMIZE_CFLAGS" \
--extra-ldflags="$ADDI_LDFLAGS" \
$ADD_H264_FEATURE \
$ADD_FDK_AAC_FEATURE \
$ADD_MEDIA_NDK_SO


make clean
make -j8
make install

echo "编译完成 $CPU"

}

#x264库所在的位置，ffmpeg 需要链接 x264
X264_LIB_DIR=/Users/cloud/Documents/iOS/ego/FFmpeg/Android_sh/x264-snapshot-20191217-2245-stable/android/arm64-v8a;
FDK_AAC_LIB_DIR=/Users/cloud/Documents/iOS/ego/FFmpeg/Android_sh/fdk-aac-2.0.2/android/armv8-a;

#x264的头文件地址
X264_INC="$X264_LIB_DIR/include"
FDK_AAC_INC="$FDK_AAC_LIB_DIR/include"

#x264的静态库地址
X264_LIB="$X264_LIB_DIR/lib"
FDK_AAC_LIB="$FDK_AAC_LIB_DIR/lib"

#libmediandk.so路径
MEDIA_NDK_LIB=$TOOLCHAIN/sysroot/usr/lib/aarch64-linux-android/21

ADD_H264_FEATURE="--enable-gpl \
    --enable-libx264 \
    --enable-encoder=libx264 \
    --extra-cflags=-I$X264_INC $OPTIMIZE_CFLAGS \
    --extra-ldflags=-L$X264_LIB $ADDI_LDFLAGS "
    
ADD_FDK_AAC_FEATURE="--enable-libfdk-aac \
    --enable-nonfree \
    --extra-cflags=-I$FDK_AAC_INC $OPTIMIZE_CFLAGS \
    --extra-ldflags=-L$FDK_AAC_LIB $ADDI_LDFLAGS "
    
ADD_MEDIA_NDK_SO="--extra-ldflags=-L$MEDIA_NDK_LIB \
--extra-libs=-lmediandk "

#ADD_H264_FDK_AAC_FEATURE="--enable-encoder=aac \
#    --enable-decoder=aac \
#    --enable-gpl \
#    --enable-encoder=libx264 \
#    --enable-libx264 \
#    --enable-libfdk-aac \
#    --enable-encoder=libfdk-aac \
#    --enable-nonfree \
#    --extra-cflags=-I$X264_INC -I$FDK_AAC_INC \
#    --extra-ldflags=-lm -L$X264_LIB -L$FDK_AAC_LIB $ADDI_LDFLAGS "
#armv8-a
ARCH=aarch64
CPU=armv8-a
API=21
CC=$TOOLCHAIN/bin/aarch64-linux-android$API-clang
CXX=$TOOLCHAIN/bin/aarch64-linux-android$API-clang++
SYSROOT=$NDK/toolchains/llvm/prebuilt/darwin-x86_64/sysroot
CROSS_PREFIX=$TOOLCHAIN/bin/aarch64-linux-android-
PREFIX=$(pwd)/android/$CPU
#OPTIMIZE_CFLAGS="-mfloat-abi=softfp -mfpu=vfp -marm -march=$CPU "
build_android
```
执行脚本命令同时输出log文件方便排错```sh build_arm64.sh > /Users/cloud/Desktop/0.log```，编译成功生成.a静态库，我这儿是将armv7和arm64分开执行的，也分开合并成.so文件
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501202814.jpg)
执行合并.so的脚本union_ffmpeg_so_armv8.sh，将libx264,fdk-aac和ffmpeg中的.a合并为libffmpeg.so文件
```
echo "开始编译ffmpeg so"

#NDK路径.
export NDK=/Users/cloud/Library/android-ndk-r20b

PLATFORM=$NDK/platforms/android-21/arch-arm64
TOOLCHAIN=$NDK/toolchains/aarch64-linux-android-4.9/prebuilt/darwin-x86_64
TOOL=$NDK/toolchains/llvm/prebuilt/darwin-x86_64

PREFIX=$(pwd)

#如果不需要依赖x264，去掉/usr/x264/x264-master/android/armeabi-v7a/lib/libx264.a \就可以了

$TOOLCHAIN/bin/aarch64-linux-android-ld \
-rpath-link=$PLATFORM/usr/lib \
-L$PLATFORM/usr/lib \
-L$PREFIX/lib \
-soname libffmpeg.so -shared -nostdlib -Bsymbolic --whole-archive --no-undefined -o \
$PREFIX/libffmpeg.so \
    libavcodec.a \
    libavfilter.a \
    libswresample.a \
    libavformat.a \
    libavutil.a \
    libpostproc.a \
    libswscale.a \
    libavresample.a \
    libavdevice.a \
    /Users/cloud/Documents/iOS/ego/FFmpeg/Android_sh/x264-snapshot-20191217-2245-stable/android/arm64-v8a/lib/libx264.a \
    /Users/cloud/Documents/iOS/ego/FFmpeg/Android_sh/fdk-aac-2.0.2/android/armv8-a/lib/libfdk-aac.a \
    -lc -lm -lz -ldl -llog --dynamic-linker=/system/bin/linker \
    $TOOLCHAIN/lib/gcc/aarch64-linux-android/4.9.x/libgcc.a \
    $TOOL/sysroot/usr/lib/aarch64-linux-android/21/libmediandk.so \

echo "完成编译ffmpeg so"
```
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501203653.jpg)
同理再生成armv7架构的so，拖入到Andriod工程中，就可以执行通过ffmpeg执行Android硬编码了,比如具体命令
ffmpeg -i MyHeart.mp4 -c:a aac -c:v h264_hlmediacodec output.mp4
ffmpeg -i MyHeart.mp4 -c:a aac -c:v hevc_hlmediacodec output.mp4
ffmpeg -i MyHeart.mp4 -c:a aac -c:v libx264 output.mp4
当然后面可以添加更改分辨率，帧率，码率，gop,ABR和CBR的参数配置，不同的参数输出的结果不一致
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501204137.jpg)
![](https://raw.githubusercontent.com/yanyi0/MWeb-Images/master/20220501204533.jpg)
先将我编号的工程传到github上面，[Demo地址](https://github.com/yanyi0/FFmpegTranscoding/tree/master)





