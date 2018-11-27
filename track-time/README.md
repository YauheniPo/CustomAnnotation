# CustomAnnotation

_**mvn deploy**_ - in the project (TrackTime) to console for deploy in the JAR (target/mvn-repo/all_files(.sha1, .md5, .pom, .jar))

token in .m2/settings.xml from gitaccount-settings-developer settings privileges: 
repo - Full control of private repositories 
repo:status - Access commit status 
repo_deployment - Access deployment status 
public_repo - Access public repositories 
repo:invite
notifications - Access notifications 
user - :read :email - Access user email addresses (read-only) :follow - Follow and unfollow users

for deploy - in .m/settings.xml

```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>[username]</username>
      <password>[token]</password>
    </server>
  </servers>
</settings>
```

or mvn --encrypt-master-password [password] and create .m2/settings-security.xml 
```
<settingsSecurity>
  <master>{qJCIDnXf8HNInZwx+mI1eLvWKziDQiwddw8=}</master>
</settingsSecurity>
```
than mvn --encrypt-password [password]
```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>github</id>
      <username>[username]</username>
      <password>{jhkjut78ttggk}</password>
    </server>
  </servers>
</settings>
```

```
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.cloudogu.blog</groupId>
    <artifactId>main</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>1.8</java.version>
        <github.global.server>github</github.global.server>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <repositories>
        <repository>
            <id>epam-popovich-log</id>
            <name>repo-maven-popovich</name>
            <url>https://github.com/YauheniPo/git_jar_for_maven/tree/master/</url>
            <!--<snapshots>-->
                <!--<enabled>true</enabled>-->
                <!--<updatePolicy>always</updatePolicy>-->
            <!--</snapshots>-->
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>epam.popovich.annotation</groupId>
            <artifactId>log</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.6.1</version>
                <configuration>
                    <showWarnings>true</showWarnings>
                </configuration>
              </plugin>
        </plugins>
    </build>
</project>
```