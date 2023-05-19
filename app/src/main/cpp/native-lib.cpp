#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include "opencv-utils.h"

void bitmapToMat(JNIEnv *env, jobject bitmap, Mat& dst, jboolean needUnPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = nullptr;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, COLOR_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cvtColor(tmp, dst, COLOR_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return;
    }
}

void matToBitmap(JNIEnv* env, const Mat& src, jobject bitmap, jboolean needPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = nullptr;

    try {
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width == (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
                if(needPremultiplyAlpha) cvtColor(src, tmp, COLOR_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
                cvtColor(src, tmp, COLOR_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
                cvtColor(src, tmp, COLOR_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
                cvtColor(src, tmp, COLOR_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    }  catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_PhotoFocus_EditImageActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myBlur(JNIEnv *env, jobject, jobject bitmap_in,
                                                   jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myBlur(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myNoise(JNIEnv *env, jobject, jobject bitmap_in,
                                                     jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myNoise(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myTone(JNIEnv *env, jobject, jobject bitmap_in,
                                                     jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myTone(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_mySaturation(JNIEnv *env, jobject, jobject bitmap_in,
                                                       jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    mySaturation(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myBright(JNIEnv *env, jobject, jobject bitmap_in,
                                                      jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myBright(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myExposition(JNIEnv *env, jobject, jobject bitmap_in,
                                                       jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myExposition(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myContrast(JNIEnv *env, jobject, jobject bitmap_in,
                                                           jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myContrast(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myVignette(JNIEnv *env, jobject, jobject bitmap_in,
                                                         jobject bitmap_out, jfloat sigma) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myVignette(src, sigma);
    matToBitmap(env, src, bitmap_out, false);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_PhotoFocus_EditImageActivity_myAutocorrect(JNIEnv *env, jobject, jobject bitmap_in,
                                                         jobject bitmap_out) {
    Mat src;
    bitmapToMat(env, bitmap_in, src, false);
    myAutocorrect(src);
    matToBitmap(env, src, bitmap_out, false);
}