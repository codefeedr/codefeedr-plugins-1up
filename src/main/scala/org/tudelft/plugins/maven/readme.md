Maven Codefeedr plugin
=======================================

**What is it?**
--------
This plugin aggregates Maven package data using CodeFeedr's pipeline abstraction.

**How do you run this and get data of interest?**
-------
Using CodeFeedr's pipeline abstraction, you add an edge to the pipeline by:
- creating a new MavenReleasesStage (to get the updatestream)
- create a new MavenReleasesExtStage (to retrieve data of interest for each individual package from the update stream)
- adding an edge to the pipeline from source to extended release
- finally, based on your needs, you either add an exit Stage which writes output to JSON, or 
an SQLStage, which enables to query the datastream[MavenRelease] using streaming SQL.


**What Data is being tracked?**
-------------------------------
The Maven plugin for CodeFeedr is keeping track of the following data:

*MavenRelease*

|field names|type|
|:-----|:----|
|title | String|
|link | String|
|description | String|
|pubDate | Date|
|guid | Guid|

 *Guid*
 
|field names|type|
|:-----|:----|
|tag | String|

*MavenReleaseExt*
  
|field names|type|
|:-----|:----|
|title | String|
|link | String|
|description | String|
|pubDate | Date|
|guid | Guid|
|project | MavenProject|


*MavenProject*

|field names|type|
|:-----|:----|
|modelVersion | String|
|groupId | String|
|artifactId | String|
|version | String|
|parent | Option[Parent]|
|dependencies | Option[List[Dependency]]|
|licenses | Option[List[License]]|
|repositories | Option[List[Repository]]|
|organization | Option[Organization]|
|packaging | Option[String]|
|issueManagement | Option[IssueManagement]|
|scm | Option[SCM]|

*SCM*

|field names|type|
|:-----|:----|
|connection | String|
|developerConnection | Option[String]|
|tag | Option[String]|
|url | String|

*Organization*

|field names|type|
|:-----|:----|
|name | String|
|url | String|

*IssueManagement*

|field names|type|
|:-----|:----|
|system | String|
|url | String|

*Parent*

|field names|type|
|:-----|:----|
|groupId | String|
|artifactId | String|
|version | String|
|relativePath | Option[String]|

*License*

|field names|type|
|:-----|:----|
|name | String|
|url | String|
|distribution | String|
|comments | Option[String]|


*Repository*

|field names|type|
|:-----|:----|
|id | String|
|name | String|
|url | String|

*Dependency*

|field names|type|
|:-----|:----|  
|groupId | String|
|artifactId | String|
|version | Option[String]|
|`type` | Option[String]|
|scope | Option[String]|
|optional | Option[Boolean]|         