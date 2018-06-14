# Purpose

Evaluate handwritten mathematical expression on android appplication.

# Implementation Details

Please notice any error or possible improvements of our project.
Gyu Jin Choi(paganinist@gmail.com)


## Machine Learning
### Machine Learning Library

We build our hand-written letter recogition model using **Tensorflow** library.

To predict samples on android environment using tensorflow(on android application, learning process will not be included further after and now...... maybe?), [nightly-android build 465](https://ci.tensorflow.org/view/Nightly/job/nightly-android/465/) version is included in our project.

Our development tool is android studio, and initial setting with [This Article](https://omid.al/posts/2017-02-20-Tutorial-Build-Your-First-Tensorflow-Android-App.html) has no problem to us.

### Dataset

On this project, included symbols are digits, and four arithmetic operators(+, x, /, -)

Combination of datasets listed below

1. [MNIST](http://yann.lecun.com/exdb/mnist/)
2. [Kaggle Handwritten Math Symbol Dataset](https://www.kaggle.com/xainano/handwrittenmathsymbols)
3. [ICFHR 2016 CROHME](http://ivc.univ-nantes.fr/CROHME/index.php#Tasks)

### Dataset Augmentation Details

Combination of methods listed below

1. Gaussian noise (sigma 0.0 to 3 with 0.5 interval)
2. Salt&Pepper noise (0.00 to 0.5 with 0.1 interval)
3. Rotation (-45 degrees to 45 degree with 10 degree interval)
4. Translation
5. Scale (Y scale 0.0 to 3.0 and X scale 0.0 to 3.0 repectively)

### Network Design

Modified ConvNet inpired by this [paper](https://pdfs.semanticscholar.org/4dbc/68cf2e14155edb6da0def30661aca8c96c22.pdf)

### Validation

Training : Test = 8 : 2 ratio.
Final test set performance is 98.2%.

## Mathemathical string expression evaluation

We evaluate reconized math symbol character sequence using modified shunting yard algorithm.
