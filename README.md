# Burne

Burne is Reflective File Download(RFD) vulnerability detection tool written in Java

## Description
A friendly Java based CLI tool to find RFD vulnerable links in a web application.  
Accepts a URL in input and prints all RFD vulnerable links in the provided domain. Written by Syed for the love of lesser known vulnerabilities.

## Features

  - Proxy Support
  - Multi-Threading
  - Relative Path Resolution
  - Domain Level Restriction

## Prerequisites 

  - Have Java 6 or above installed
  - Requires Jsoup (already included in jar, no action required)


## Usage
Grab a copy of Burne.jar and put in some where, say d drive. Summon the command prompt, navigate to the path where you've put the jar and use the following syntax to execute Burne:   
```java
java -jar Burne.jar <target url>(required) <proxy>(optional)
```
For example: 

```java
D:\> java -jar Burne.jar "https://www.github.com/"
```



## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)
