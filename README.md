# About [![Build Status](https://travis-ci.org/dernasherbrezon/r2weatherClient.svg?branch=master)](https://travis-ci.org/dernasherbrezon/r2weatherClient) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ru.r2cloud%3Ar2weatherClient&metric=alert_status)](https://sonarcloud.io/dashboard?id=ru.r2cloud%3Ar2weatherClient)

Java client for sending data to [r2weather](https://r2weather.ru) service.

# Usage

1. Register at [https://r2weather.ru](https://r2weather.ru)

2. Add maven dependency:

```xml
<dependency>
  <groupId>ru.r2cloud</groupId>
  <artifactId>r2weatherClient</artifactId>
  <version>1.0</version>
</dependency>
```

3. Setup client and make a request:

```java
BmeClient client = new R2WeatherClient("https://r2weather.ru", apiKey, 30000);
client.upload(file, receptionTime);
```

Please note ```file``` should contain serialized VCDU frames from Meteor-M.