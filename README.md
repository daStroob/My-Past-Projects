# My-Past-Projects
<p>by Dario Strübin <br>
This Folder contains a small selection of material about seven past projects of mine.</p>
<br>

<p><b>Classification of Waste using simulated Data</b><br>

<div class="nav3">
    <img src="/7%20-%20Classification%20of%20waste%20using%20simulated%20data/randomization_animation.gif" width="40.8%">
    <img src="/7%20-%20Classification%20of%20waste%20using%20simulated%20data/multilabel_detection.gif" width="56%">
</div>    

For my master thesis I am currently working on image detection and multilabel classification of municipal waste. To overcome the huge costs associated with the labeling of complex and cluttered images of waste, I am developing a pipeline for the generation of synthetic data. 3D scanned objects can be fed into this pipeline, which will then add a number of randomization steps and finally take a high-quality render in Blender (see animation on the left). Any number of completely annotated images can then be generated through this process, which drastically reduces the amount of labeled real-world data to train a neural network. The project is done in colaboration with the autonomous river cleanup project, with the aim of using the resulting neural network to detect litter on a conveyor belt, pick it up with robotic arms and segregate it for recycling purposes.

<p><b>Origami Delta Manipulator for Aerial Interaction</b><br>
For my semester project I worked on the development of a lightweight origami delta manipulator for aerial interaction. This manipulator uses a composite of flexible and rigid materials, achieving a folding structure without the use of any conventional joints. My primary work consisted in improving the controls and modeling the inverse kinematics of the system. This resulted in the manipulator's ability to pinpoint a desired point in 3d space and compensate for drone movement. Additionally the compliant nature of the manipulator was analyzed and exploited for soft interaction tasks.
    
<div class="nav3">
    <img src="/6%20-%20Origami%20Delta%20Aerial%20Manipulator/compensation.gif" width="70%">
    <img src="/6%20-%20Origami%20Delta%20Aerial%20Manipulator/heart.gif" width="28%">
</div>    

<p><b>Scewo Internship</b><br>
During a six months long industrial internship at the startup company <i>Scewo</i> I worked on the development of a balancing and stairclimbing wheelchair as a software engineer. The main part of the job was contributing to the software running the wheelchair, for example the stair-detection using time of flight sensors. Besides that I developed a graphical interface in C++ and Qt to plot and display information about the wheelchair and change settings. This interface was then used by the team to help during development, testing and debugging.
    
<div class="nav3">
    <img src="/5%20-%20Scewo%20Internship/Scewo%20Demo%20GIF.gif" width="49%">
    <img src="/5%20-%20Scewo%20Internship/Scewo%20GUI%20GIF.gif" width="49%">
</div>

<br>
<p><b>Any Robot Controller Interface:</b><br>
For my bachelor's thesis I worked on a new controller interface for the quadrupedal robot <i>Anymal</i>. The aim was to develop an interface that was intuitive and easy to use. I started by reading papers on the topic of human-robot interaction and interface design. Then I developed a touch interface, enabling the user to access Anymal's main functionalities in an intuitive and effective manner, as well as a new widget to place individual feet of the robot on a selected location on the map.<br>
I included in this repository the written report, documenting the project and my progress.</p>

<div class="nav3">
    <img src="/1%20-%20ARCI/ARCI_overview.png" width="43%">
    <img src="/1%20-%20ARCI/Foot%20Placer%20Preview.gif" width="54%">
</div>

<br/>
<p><b>Arcade Cabinet:</b><br>
For my graduation work at the Gymnasium I built an Arcade Cabinet. This involved planning and building the cabinet itself, connecting and reading input devices through an Arduino, programming the software in Java and finally design and code some videogames.<br>
I wrote a complete documentation of the project (in German), which I included in this folder. I also included the code of the main arcade software and of the Arduino input handling.</p>

<p align="center">
    <img width="35%" src="3%20-%20Arcade%20Cabinet/Arcade%20Cabinet.jpg">
    <img width="60%" src="3%20-%20Arcade%20Cabinet/arcade%20demo%20GIF.gif"> 
</p>

<br>
<p><b>Pixel Art Display:</b><br>
This started as a private project of mine which I did for fun. I wanted to build a small 16x16 pixels display to show old school pixel art. An Arduino inside reads sequences of images on an SD card and then displays them on multiple rows of LED-strips. The case was all built by lasercut MDF pieces and a 3D printed frame to cleanly separate the individual pixels.<br>
Later on I was asked to build a larger panel to use as prop in a short film and music video. To build this panel with a width of 1.5 meters I redesigned the frame to be built entirely by lasercutter and switched the Arduino for a Teensy, which is able to output to multiple LED-rows in parallel to handle the 36x36 pixels. Any video file could then be fed to the teensy via a usb serial connection.
I included the code which runs on the Arduino of the small panel as well as some fotos of the building process.</p>

<p align="center">
    <img src="/4%20-%20Pixel%20Art%20Display/Pixel%20Art%20Display.gif" width="35%">
    <img src="/4%20-%20Pixel%20Art%20Display/LED%20Panel%20Musicvideo%20GIF.gif" width="62.2%">
</p>

<br>
<p><b>Innovation Project 2017:</b><br>
As part of my mechanical engineering studies at ETH Zürich I participated in the innovation project together with a small team of five people. In the innovation project, over 90 teams are challenged to find the best solution for this year’s task. Therefore, we had to build a robot which can conduct research through collecting probes on a fictional asteroid. As an additional challenge, no wheeled robots were allowed.<br>
My primary responsibility was the mechatronics section and the writing of the control code in Labview.</p>

<div class="nav3">
    <img src="/2%20-%20Innovation%20Project%202017/Innovation%20Project-1.jpg" width="49%">
    <img src="/2%20-%20Innovation%20Project%202017/Innovation%20Project-4.jpg" width="49%">
</div>
