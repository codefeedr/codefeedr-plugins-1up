Cargo Codefeedr plugin
=======================================

**What is it?**
--------
This plugin aggregates Cargo crates data using CodeFeedr's pipeline abstraction.

**How do you run this and get data of interest?**
-------
The Cargo plugin is a bit different from the normal CodeFeedr pipeline abstraction. 
In stead of fetching an input (let's call that node A), buffering (edge e_1), outputting to a Datastream (node B) 
The plugin outputs this information to a Datastream[CrateRelease]
running a Cargo stage processes both an update stream of packages and fetches all information of each updated package

So in order to see To see output for Cargo manually extend an exitStage yourself, overriding `def main` with a
print statement and use that as an edge to build a CodeFeedr pipeline. Or choose a JsonExitStage, providing the type of
output and use `startMock()` to see some output on your terminal. `startLocal` is also possible but requires a working Kafka/Zookeeper instance
on your computer.

**What Data is being tracked?**
-------------------------------
The Cargo plugin for CodeFeedr is keeping track of the following data:

**CrateRelease**

|field names|type|role|
|:-----|:----|:---|  
|crate| Crate||
|versions| List[CrateVersion]||
|keywords| List[CrateKeyword]||
|categories| List[CrateCategory]||

**Crate**
 
|field names|type|role|
|:-----|:----|:---|  
|*id*| String|PK|
|name| String||
|updated_at| Date||
|versions| List[Int]||
|keywords| List[String]||
|categories| List[String]||
|//badges| Option[List[String]]|| Unimportant information / Too extensive| See natvis-pdbs
|created_at| Date||
|downloads| Int|| // Assuming no more than 2B downloads
|recent_downloads| Option[Int]||
|max_version| String||
|description| String||
|homepage| Option[String]||
|documentation| Option[String]||
|repository| Option[String]||
|links| CrateLinks||
|exact_match| Boolean||

**CrateLinks**

|field names|type|role|
|:-----|:----|:---|
|*crateId*| String | FK|
|version_downloads| String||
|versions| Option[String]||
|owners| String||
|owner_team| String||
|owner_user| String||
|reverse_dependencies| String||

**CrateVersion**

|field names|type|role|
|:-----|:----|:---|
|id| Int|PK|
|crate| String|PK|
|num| String||
|dl_path| String||
|readme_path| String||
|updated_at| Date||
|created_at| Date||
|downloads| Int||
|features| CrateVersionFeatures||
|yanked| Boolean||
|license| String||
|links| CrateVersionLinks||
|crate_size| Option[Int]||
|published_by| Option[CrateVersionPublishedBy]||

**CrateVersionFeatures**

|field names|type|role|
|:-----|:----|:---|
|versionId | String | FK | 
|crate | String | FK|

**CrateVersionLinks**

|field names|type|role|
|:-----|:----|:---|
|versionId | String | FK | 
|crate | String | FK|
|dependencies| String||
|version_downloads| String||
|authors| String||

**CrateVersionPublishedBy**

|field names|type|role|
|:-----|:----|:---|
|versionId | String | FK | 
|crate | String | FK|
|id| Int||
|login| String||
|name| Option[String]||
|avatar| String||
|url| String||

**CrateKeyword**

|field names|type|role|
|:-----|:----|:---|
|id| String|PK|
|crate | String | FK|
|keyword| String||
|created_at| String||
|crates_cnt| Int||

**CrateCategory**

|field names|type|role|
|:-----|:----|:---|
|id| String|PK|
|crate | String | FK|
|category| String||
|slug| String||
|description| String||
|created_at| String||
|crates_cnt| Int||

TODO
====
- [ ] review comments from table **Crate** & update where necessary
- [ ] review this whole document
- [ ] implement parsing badges field from table **Crate**
- [ ] create image for table overview