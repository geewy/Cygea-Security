# Cygea-Security

## An application for an Android Malware Detector

**version 2.0-alpha** -- **Release date : 2017-02-16**

**Authors :** Marie-Kerguelen Sido <mariek.sido@netc.fr>

Cygea Security is an open source project. The Android application aims to list the applications downloaded on a phone and show for each of them the result of a malware analysis performed by our neural network. The application has either been already analysed and Cygea just saw the result of it, or not. In this case, the basic information of the application is sent to the crawler to be downloaded and then analysed after what the report is generated and print on the phone. If the crawler does not find the application on the Google PlayStore, the application send to him the apk file of the application. The user can also connect itself to the website. Cygea Security has been the subject of a scientific publication, see <http://www.scitepress.org/Papers/2018/67277/67277.pdf> for more information and see <https://github.com/AlexandreDey/cygea-playstore-crawler> for the source code of our crawler.

### Requirements

This code only requiere Android Studio and an emulator.

The pictures used have to be changed. The ip addresses also has to be settled so that you application will be able to join your server and database.

The stop of an aplication's download to analyse it or before its installation is not pushed here.

### Todo

* Comment properly the code
* 


