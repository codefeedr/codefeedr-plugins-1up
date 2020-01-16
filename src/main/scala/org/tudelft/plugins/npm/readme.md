NPM Codefeedr plugin
=======================================

**What is it?**
--------
This plugin aggregates NPM package data using CodeFeedr's pipeline abstraction.

**How do you run this and get data of interest?**
-------
Using CodeFeedr's pipeline abstraction, you add an edge to the pipeline by:
- creating a new NpmReleaseStage (to get the updatestream)
- create a new NpmReleaseExtStage (to retrieve data of interest for each individual package from the update stream)
- adding an edge to the pipeline from source to extended release
- finally, based on your needs, you either add an exit Stage which writes output to JSON, or 
an SQLStage, which enables to query the datastream[NPMRelease] using streaming SQL.


**What Data is being tracked?**
-------------------------------
The NPM plugin for CodeFeedr is keeping track of the following data:

*NPMRELEASE*

|field names|type|
|:-----|:----|         
|name           | String |
|retrieveDate   | Date (using ingestion time| |

 *NPMRELEASEEXT*
 
|field names|type|
|:-----|:----|    
|name           | String |
|retrieveDate   | Date|
|project        | NpmProject|

 *NpmProject*|
 
|field names|type|
|:-----|:----|
|_id             | String|
|_rev            | Option[String]|
|name            | String|
|author          | Option[PersonObject]|
|contributors    | Option[List[PersonObject]]|
|description     | Option[String]|
|homepage        | Option[String]|
|keywords        | Option[List[String]]|
|license         | Option[String]|
|dependencies    | Option[List[Dependency]]|
|maintainers     | List[PersonObject]|
|readme          | String|
|readmeFilename  | String|
|bugs            | Option[Bug]|
|bugString       | Option[String]|
|repository      | Option[Repository]|
|time            | TimeObject|

*Dependency*
  
|field names|type|
|:-----|:----|
|packageName | String|
|version     | String|

*PersonObject*

|field names|type|
|:-----|:----|
|name  | String|
| email | Option[String]|
| url   | Option[String]|

*Repository*

|field names|type|
|:-----|:----|
|`type`    | String|
|url       | String|
|directory | Option[String]|

*Bug*

|field names|type|
|:-----|:----|
|url   | Option[String]|
| email | Option[String]|

*TimeObject*

|field names|type|
|:-----|:----|
|created  | String|
|modified | Option[String]|