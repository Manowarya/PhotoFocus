#include "opencv-utils.h"
#include <opencv2/imgproc.hpp>
#include <thread>

void myFlip(Mat src) {
    flip(src, src, 0);
}
void myBlur(Mat src, float sigma) {
    GaussianBlur(src, src, Size(), sigma);
}
void colorGrainEffectThread(Mat& image, int start_row, int end_row, float grain_ratio) {
    int cols = image.cols;

    srand(time(NULL)); // инициализация генератора случайных чисел

    Mat gray_image;
    cvtColor(image, gray_image, COLOR_BGR2GRAY); // преобразуем изображение в grayscale

    for (int row = start_row; row < end_row; row++) {
        for (int col = 0; col < cols; col++) {
            if ((float) rand() / RAND_MAX < grain_ratio) { // проверяем, нужно ли зернистить этот пиксель
                int noise_value = rand() % 256; // случайное значение шума
                gray_image.at<uchar>(row, col) = noise_value;
            }
        }
    }

    cvtColor(gray_image, image, COLOR_GRAY2BGR); // преобразуем изображение обратно в цветное
}

void myNoise(Mat image, float sigma) {
    /*Mat noise(src.size(),src.type());
    cv::randn(noise, 12, sigma*10); //mean and variance
    src += noise;*/

    std::vector<cv::Mat> channels;
    cv::split(image, channels);

    for (cv::Mat& channel : channels) {
        cv::Mat noise(channel.size(), CV_32F);
        cv::randn(noise, 0.0, sigma);

        cv::Mat blurred;
        cv::GaussianBlur(noise, blurred, cv::Size(5, 5), 0);

        cv::Mat result;
        cv::addWeighted(channel, 1.0, blurred, 1.0, 0.0, result, channel.type());

        cv::normalize(result, result, 0, 255, cv::NORM_MINMAX, CV_8U);
        channel = result;
    }

    cv::merge(channels, image);
}