# Chatbot Library

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [General Steps for Building & Publishing the Chatbot Library](#general-steps-for-building--publishing-the-chatbot-library)
  - [Publishing to Github Packages](#publishing-to-github-packages)
  - [Publishing to Maven Central](#publishing-to-maven-central)
  - [Publishing to a Local Repository](#publishing-to-a-local-repository)
- [Importing the Chatbot Library](#importing-the-chatbot-library)
  - [Importing from Github Packages](#importing-from-github-packages)
  - [Importing from Maven Central](#importing-from-maven-central)
  - [Importing from a Local Repository](#importing-from-a-local-repository)

## Overview
The Chatbot Library is a versatile and modular component of the ACCESS AI project, designed to manage and customize chatbot entities. It allows for easy integration and replacement of various elements, ensuring flexibility and adaptability. This library is particularly useful for students as it provides tailored information, reduces reliance on public large language models, and offers guidance based on specific educational contexts.

## Prerequisites
In order to run the Chatbot, the following environment variables are needed:
- `MISTRAL_API_KEY` is the API key for the Mistral API.
- `MISTRAL_EMBEDDING_MODEL` is the URL of the Mistral embedding model.
- `MISTRAL_EMBEDDING_HOST` is the host of the Mistral embedding model.
- `MISTRAL_LLM_MODEL` is the URL of the Mistral LLM model.
- `MISTRAL_LLM_HOST` is the host of the Mistral LLM model.
- `VECTOR_STORE_HOST` is the host of the vector store.
- `SIMILARITY_SCORE_THRESHOLD` is the similarity score threshold for the vector store.
- `TOP_K_RESULTS` is the number of top results to return from the vector store.
- `CHATBOT_DB_URL` is the URL of the chatbot database.
- `CHATBOT_DB_USER` is the username of the chatbot database.
- `CHATBOT_DB_PASSWORD` is the password of the chatbot database.

## General Steps for Building & Publishing the Chatbot Library

### 1. Add the maven-publish plugin to the build.gradle file
Add the following code to the build.gradle file in the root directory of the project:
```kotlin
{...}
plugins {
    {...}
    // Apply the maven-publish plugin to publish your library.
    id("maven-publish")
    {...}
}
{...}
```

### 2. Add the publishing configuration to the build.gradle file
Add the following code to the build.gradle file in the root directory of the project:
```kotlin
{...}
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = "ch.uzh.ifi" // replace with your preferred group ID
            artifactId = "access-chatbot" // replace with your preferred artifact ID
            version = "1.0.0" // replace with the chatbot version
        }
    }
    {...}
}
{...}
```

### 3. Choose a Publishing Method
Before building and publishing the library, decide on the publishing method. Refer to the specific sections below for guidance on publishing to:
- [A Local Repository](#publishing-to-a-local-repository) (default)
- [Github Packages](#publishing-to-github-packages)
- [Maven Central](#publishing-to-maven-central)

### 3. Build and Publish the Library
After configuring, use the following command to build and publish the library:
```bash
./gradlew publish
```

## Publishing to Github Packages

### 1. Create a Personal Access Token
1. Go to your github account settings
2. Click on Developer settings
3. Click on Personal access tokens
4. Click on Generate new token
5. Select the following scopes:
    - read:packages
    - write:packages
    - delete:packages
6. Click on Generate token

### 2. Add the Personal Access Token to the gradle.properties file
Add the following line to the `gradle.properties` file in the root directory of the project:
```properties
gpr.github_user=USERNAME
gpr.github_token=PERSONAL_ACCESS_TOKEN
```
Replace USERNAME with your github username and PERSONAL_ACCESS_TOKEN with the generated token.

### 3. Add the Github Repository to the build.gradle file
Add the following code to the build.gradle file in the root directory of the project:
```kotlin
{...}
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/USERNAME/REPOSITORY")
        credentials {
            username = project.findProperty("gpr.github_user") as String?
            password = project.findProperty("gpr.github_token") as String?
        }
    }
    {...}
}
{...}
```
Replace USERNAME with your github username and REPOSITORY with the name of the repository. For example, if the repository is at the following github link `https://github.com/example-organisation-name-or-username/Chatbot` then the maven github repository url would be `https://maven.pkg.github.com/example-organisation-name-or-username/Chatbot`.

## Publishing to Maven Central

### Prerequisites
- Sonatype Account: create a [Sonatype JIRA](https://issues.sonatype.org/secure/Dashboard.jspa) account if you don't have one already

### 1. Add the Sonatype credentials to the gradle.properties file
Add the following lines to the `gradle.properties` file in the root directory of the project
```properties
signing.gnupg.keyName=KEY_NAME
signing.gnupg.passphrase=KEY_PASSPHRASE
signing.gnupg.secretKeyRingFile=KEY_FILE
sonatypeUsername=SONATYPE_USERNAME
sonatypePassword=SONATYPE_PASSWORD
```
Replace KEY_NAME with the name of your GPG key, KEY_PASSPHRASE with the passphrase of your GPG key, KEY_FILE with the path to your GPG key file, SONATYPE_USERNAME with your Sonatype username and SONATYPE_PASSWORD with your Sonatype password.

### 2. Add the Sonatype Repository and signing plugin to the build.gradle file
```kotlin
{...}
plugins {
    id("signing")
}
{...}
repositories {
    mavenCentral()
    maven {
        name = "SonatypeSnapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    maven {
        name = "SonatypeStaging"
        url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
    }
    {...}
}
{...}
signing {
    useGpgCmd()
    sign(publishing.publications)
}
```

## Publishing to a Local Repository

### 1. Modify the publishing configuration in the build.gradle file
Add the following code to the build.gradle file in the root directory of the project
```kotlin
{...}
publishing {
    {...}
    repositories {
        maven {
            url = uri("${buildDir}/repo")
        }
    }
    {...}
}
{...}
```

## Importing the Chatbot Library

### 1. Choose an Import Method
- [Local Repository](#importing-from-a-local-repository) (default)
- [Github Packages](#importing-from-github-packages)
- [Maven Central](#importing-from-maven-central)

### 2. Add the Chatbot Library to the build.gradle file
Add the following code to the build.gradle file in the root directory of the project:
```kotlin
{...}
dependencies {
    implementation("ch.uzh.ifi:access-chatbot:1.0.0")
    {...}
}
{...}
```

## Importing from Github Packages

### 1. Add the Personal Access Token to the gradle.properties file
Add the following line to the `gradle.properties` file in the root directory of the project:
```properties
gpr.github_user=USERNAME
gpr.github_token=PERSONAL_ACCESS_TOKEN
```

### 2. Add the Github Repository to the build.gradle file
Add the following code to the build.gradle file in the root directory of the project:
```kotlin
{...}
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("MAVEN_GITHUB_URL")
        credentials {
            username = project.findProperty("gpr.github_user") as String?
            password = project.findProperty("gpr.github_token") as String?
        }
    }
    {...}
}
{...}
```
Replace MAVE_GITHUB_URL with the maven github repository url you used to publish the library. In the example above, the maven github repository url is `https://maven.pkg.github.com/example-organisation-name-or-username/Chatbot`.

## Importing from Maven Central

### 1. Add the Sonatype Repository to the build.gradle file
Add the following code to the build.gradle file in the root directory of the project:
```kotlin
{...}
repositories {
    mavenCentral()
    maven {
        name = "SonatypeSnapshots"
        url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
    }
    {...}
}
{...}
```

## Importing from a Local Repository

### 1. Modify the publishing configuration in the build.gradle file
Add the following code to the build.gradle file in the root directory of the project:
```kotlin
{...}
repositories {
    maven {
        url = uri("${buildDir}/repo")
    }
    {...}
}
{...}
```