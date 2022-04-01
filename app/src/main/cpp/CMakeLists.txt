# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.6.1)

# Declares and names the project.

project("ffmpegtranscoding")

# 支持gnu++11
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=gnu++11")

# 定义源码所在目录
#aux_source_directory(./src/main/cpp SRC)
#aux_source_directory(./src/main/cpp/ffmpeg SRC_FFMPEG)
# 将 SRC_FFMPEG 添加到 SRC 中
#list(APPEND SRC ${SRC_FFMPEG})

# 1. 定义so库和头文件所在目录，方面后面使用 设置ffmpeg库所在路径的目录
#CMAKE_SOURCE_DIR=/Users/zego/Documents/FFmpeg/AndroidExamples/FFmpegTranscoding/app/src/main/cpp/
set(distribution_DIR ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI})
set(ffmpeg_head_dir ${CMAKE_SOURCE_DIR})
# 2. 添加头文件目录
include_directories(${ffmpeg_head_dir}/include)
# 添加头文件路径
include_directories(
        ./
        ./ffmpeg
        ./include
)

# 添加libffmpeg.so库
add_library( ffmpeg
        SHARED
        IMPORTED )
# 指定libffmpeg.so库的位置
set_target_properties( ffmpeg
        PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libffmpeg.so )
# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
# 配置目标so库编译信息
add_library( # Sets the name of the library.
        ffmpegtranscoding

        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        native-lib.cpp
        ffmpeg_cmd.c
        ffmpeg_thread.c
        ./ffmpeg/cmdutils.c
        ./ffmpeg/ffmpeg.c
        ./ffmpeg/ffmpeg_cuvid.c
        ./ffmpeg/ffmpeg_filter.c
        ./ffmpeg/ffmpeg_hw.c
        ./ffmpeg/ffmpeg_opt.c
        )

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.
#查找日志库
find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        ffmpegtranscoding
        ffmpeg
        -landroid     # native_window
        -ljnigraphics # bitmap
        -lOpenSLES    # openSLES
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})