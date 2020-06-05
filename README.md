
# GWALI: Detecting and Localizing Internationalization Presentation Failures in Web Applications

Web applications can be easily made available to an international audience by leveraging frameworks and tools for automatic translation and localization. However, these automated changes can distort the appearance of web applications since it is challenging for developers to design their websites to accommodate the expansion and contraction of text after it is translated to another language. We refer to these distortions as Internationalization Presentation Failures (IPFs). To address this problem, we propose, *GWALI*, a tool for automatically detecting and localizing IPFs in web applications. More algorithmic details of GWALI can be found in our paper:
```
Detecting and Localizing Internationalization Presentation Failures in Web Applications
Abdulmajeed Alameer, Sonal Mahajan, William G. J. Halfond
In Proceedings of the 11th IEEE International Conference on Software Testing, Verification and Validation (ICST). April 2016.
```
## Evaluation Data
#### Subjects:
The 23 real-world web pages used in the evaluation of GWALI can be found [here](https://github.com/USC-SQL/ifix/tree/master/subjects).


## Configuration
To run GWALI Successfully, you need to configure the following file:

gwali/src/main/java/edu/usc/config/Config.java

Change the following **constants**:

 1. FIREFOX_LOCATION: to the location of Firefox version 46.0.1 binary
 2. DRIVER_LOCATION: to the location of the gecko driver


## Questions
In case of any questions you can email at halfond [at] usc {dot} edu
