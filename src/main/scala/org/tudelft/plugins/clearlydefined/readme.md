Clearly Defined Codefeedr plugin
=======================================
Clearly defined is on a mission to help improve Free and Open-Source Software (FOSS) by providing users
with necessary information on these project, for example "What license does this project have?"

**What is it?**
--------
This plugin aggregates Clearly defined information data using CodeFeedr's pipeline abstraction.

**How do you run this and get data of interest?**
-------
The Clearly defined plugin is a bit different in that it doesn't represent the transformation as an edge in 
CodeFeedr, but in stead in one input stage fetches both the datastream of updates and the updates itself.

To see output for Clearly Defined manually extend an exitStage yourself, overriding `def main` with a
print statement and use that as an edge to build a CodeFeedr pipeline. Or choose a JsonExitStage, providing the type of
output and use `startMock()` to see some output.

**What Data is being tracked?**
-------------------------------
The Clearly Defined plugin for CodeFeedr is keeping track of the following data:


*ClearlyDefinedRelease* 

|field names|type|
|:-----|:----|      
|described| CDDescribed|
|licensed| CDLicensed|
|coordinates| CDCoordinates|
|_meta| CD_meta|
|scores| CDScores|
 
*CDDescribed*

|field names|type|
|:-----|:----|      
|releaseDate| String|
|urls| CDDescribedUrls|
|projectWebsite| Option[String]|
|issueTracker| Option[String]|
|hashes| CDDescribedHashes|
|files| Int|
|tools| List[String]|
|toolScore| CDDescribedToolScore|
|sourceLocation| Option[CDDescribedSourceLocation]|
|score| CDDescribedScore|

*CDDescribedUrls* 

|field names|type|
|:-----|:----|
|registry| String|
|version| String|
|download| String|

*CDDescribedHashes* 

|field names|type|
|:-----|:----|
|gitSha| Option[String]|
|sha1| Option[String]|
|sha256| Option[String]|

*CDDescribedToolScore*

|field names|type|
|:-----|:----|
|total| Int|
|date| Int|
|source| Int|

*CDDescribedSourceLocation*

|field names|type|
|:-----|:----| 
|locationType| String|
|provider| String|
|namespace| String|
|name| String|
|revision| String|
|url| String|

*CDDescribedScore* 

|field names|type|
|:-----|:----|
|total| Int|
|date| Int|
|source| Int|

*CDLicensed* 

|field names|type|
|:-----|:----|
|declared| Option[String]|
|toolScore| CDLicensedToolScore|
|facets| CDLicensedFacets|
|score| CDLicensedScore|

                              
*CDLicensedToolScore* 

|field names|type|
|:-----|:----|
|total| Int|
|declared| Int|
|discovered| Int|
|consistency| Int|
|spdx| Int|
|texts| Int|

*CDLicensedFacets*

|field names|type|
|:-----|:----|
|core| CDLFCore|

*CDLFCore* 

|field names|type|
|:-----|:----|
|attribution| CDLFCoreAttribution|
|discovered| CDLFCoreDiscovered|
|files| Int|

*CDLFCoreAttribution* 

|field names|type|
|:-----|:----|
|unknown| Int|
|parties| Option[List[String]]|

*CDLFCoreDiscovered* 

|field names|type|
|:-----|:----|
|unknown| Int|
|expressions| List[String]|

*CDLicensedScore*
 
|field names|type|
|:-----|:----|
|total| Int|
|declared| Int|
|discovered| Int|
|consistency| Int|
|spdx| Int|
|texts| Int|

*CDCoordinates*
 
|field names|type|
|:-----|:----|
|\`type\`| String|
|provider| String|
|name| String|
|namespace| Option[String]|
|revision| String|

*CD_meta*
 
|field names|type|
|:-----|:----|
|schemaVersion| String|
|updated| String|

*CDScores*
 
|field names|type|
|:-----|:----|
|effective| Int|
|tool| Int|