#pragma once

#include <opencv2/core.hpp>

using namespace cv;

void myFlip(Mat image);
void myBlur(Mat image, float sigma);
void myNoise(Mat src, float sigma);
void myTone(Mat& src, float sigma);
void myBright(Mat src, float sigma);
