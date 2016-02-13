Autoprovider
============

Content provider generator for android that uses annotation processing. All helper classes are generated based on a Java Object.

[Documentation](https://github.com/workarounds/autoprovider/wiki) is WIP. A sample app is provided. 

Download
--------
Gradle:
```groovy
buildscript {
  dependencies {
    classpath 'com.neenbedankt.gradle.plugins:android-apt:1.8'
  }
}

apply plugin: 'com.neenbedankt.android-apt'

dependencies {
  compile 'in.workarounds.autoprovider:autoprovider:0.0.1'
  apt 'in.workarounds.autoprovider:autoprovider-compiler:0.0.1'
}
```

License
-------

    Copyright 2015 Workarounds

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


