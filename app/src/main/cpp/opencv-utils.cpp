#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <thread>

void myFlip(Mat src) {
    flip(src, src, 0);
}
void myBlur(Mat src, float sigma) {
    GaussianBlur(src, src, Size(), sigma);
}
void myNoise(Mat image, float sigma) {
    std::vector<Mat> channels;
    split(image, channels);

    for (Mat& channel : channels) {
        Mat noise(channel.size(), CV_32F);
        randn(noise, 0.0, sigma*3);

        Mat blurred;
        GaussianBlur(noise, blurred, Size(5, 5), 0);

        Mat result;
        add(channel, blurred, result, Mat(), CV_8U);

        channel = result;
    }
    merge(channels, image);
}

void myBright(Mat image, float sigma) {
    Mat dst;
    float alpha = 1.0f, beta = sigma*3;

    image.convertTo(dst, -1, alpha,beta);
    convertScaleAbs(dst, image);
}