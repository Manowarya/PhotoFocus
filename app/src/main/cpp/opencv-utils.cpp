#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <thread>

void myFlip(Mat image) {
    flip(image, image, 0);
}
void myBlur(Mat image, float sigma) {
    GaussianBlur(image, image, Size(), sigma);
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

void myTone(Mat& image, float sigma)
{
    cvtColor(image, image, cv::COLOR_BGR2HSV);

    for (int i = 0; i < image.rows; i++)
    {
        for (int j = 0; j < image.cols; j++)
        {
            Vec3b& pixel = image.at<Vec3b>(i, j);
            int hue = static_cast<int>(pixel[0] + sigma);

            if (hue < 0)
                hue += 180;
            else if (hue > 179)
                hue -= 180;

            pixel[0] = static_cast<uchar>(hue);
        }
    }
    cvtColor(image, image, COLOR_HSV2BGR);
}

void mySaturation(Mat& image, float sigma) {
    cvtColor(image, image, COLOR_BGR2HSV);

    std::vector<Mat> channels;
    split(image, channels);

    channels[1] *= sigma;

    merge(channels, image);

    cvtColor(image, image, COLOR_HSV2BGR);
}


void myBright(Mat image, float sigma) {
    Mat dst;
    float alpha = 1.0f, beta = sigma*3;

    image.convertTo(dst, -1, alpha,beta);
    convertScaleAbs(dst, image);
}

void myExposition(Mat& image, float sigma) {
    Mat lookup_table(1, 256, CV_8U);

    for (int i = 0; i < 256; i++) {
        int adjusted_value = saturate_cast<uchar>(i * sigma);
        lookup_table.at<uchar>(i) = adjusted_value;
    }

    LUT(image, lookup_table, image);
}

void myContrast(Mat& image, float sigma) {
    image.convertTo(image, -1, sigma, 0);
}