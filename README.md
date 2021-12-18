# Mixmicro Crop Database

Crop database is an open source nosql embedded document store written in Java. It has MongoDB like API. It supports both in-memory and file based persistent
store.

CropDB is an embedded database ideal for desktop, mobile or small web applications.

**It features**:

- Schemaless document collection and object repository
- In-memory / file-based store
- Pluggable storage engines - mvstore, mapdb, rocksdb
- ACID transaction
- Schema migration
- Indexing
- Full text search
- Both way replication via Crop DataGate server
- Very fast, lightweight and fluent API
- Android compatibility (API Level 19)

## Kotlin Extension

Crop has a kotlin extension called **Potassium Crop** for kotlin developers.
Visit [here](https://github.com/misselvexu/mixmicro-cropdb/tree/rebuild/potassium-corpdb) for more details.

## Getting Started with Crop

### How To Install

To use Crop in any Java application, first add the cropdb bill of materials, then add required dependencies:

**Maven**

```xml

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>xyz.vopen.framework</groupId>
            <artifactId>cropdb-bom</artifactId>
            <version>4.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
<dependency>
    <groupId>xyz.vopen.framework</groupId>
    <artifactId>cropdb</artifactId>
</dependency>

<dependency>
    <groupId>xyz.vopen.framework</groupId>
    <artifactId>cropdb-mvstore-adapter</artifactId>
</dependency>
</dependencies>
```

**Gradle**

```groovy

implementation(platform("xyz.vopen.framework:cropdb-bom:4.0.0-SNAPSHOT"))

implementation 'xyz.vopen.framework:cropdb'
implementation 'xyz.vopen.framework:cropdb-mvstore-adapter'

```

### Quick Examples

**Initialize Database**

```java
// create a mvstore backed storage module
MVStoreModule storeModule=MVStoreModule.withConfig()
    .filePath("/tmp/test.db")  // for android - .filePath(getFilesDir().getPath() + "/test.db")
    .compress(true)
    .build();

// or a rocksdb based storage module
    RocksDBModule storeModule=RocksDBModule.withConfig()
    .filePath("/tmp/test.db")
    .build();


// initialization using builder
    Crop db=Crop.builder()
    .loadModule(storeModule)
    .loadModule(new JacksonMapperModule())  // optional
    .openOrCreate("user","password");

```

**Create a Collection**

```java
// Create a Crop Collection
CropCollection collection=db.getCollection("test");

// Create an Object Repository
    ObjectRepository<Employee> repository=db.getRepository(Employee.class);

```

**Annotations for POJO**

```java

@Entity(value = "retired-employee",     // entity name (optional), 
    indices = {
        @Index(value = "firstName", type = IndexType.NonUnique),
        @Index(value = "lastName", type = IndexType.NonUnique),
        @Index(value = "note", type = IndexType.Fulltext),
    })
public class Employee implements Serializable {
    // provides id field to uniquely identify an object inside an ObjectRepository
    @Id
    private long empId;
    private Date joinDate;
    private String firstName;
    private String lastName;
    private String note;

    // ... public getters and setters
}

```

**CRUD Operations**

```java

// create a document to populate data
Document doc=createDocument("firstName","John")
    .put("lastName","Doe")
    .put("birthDay",new Date())
    .put("data",new byte[]{1,2,3})
    .put("fruits",new ArrayList<String>(){{add("apple");add("orange");add("banana");}})
    .put("note","a quick brown fox jump over the lazy dog");

// insert the document
    collection.insert(doc);

// find a document
    collection.find(where("firstName").eq("John").and(where("lastName").eq("Doe"));

// update the document
    collection.update(where("firstName").eq("John"),createDocument("lastName","Wick"));

// remove the document
    collection.remove(doc);

// insert an object in repository
    Employee emp=new Employee();
    emp.setEmpId(124589);
    emp.setFirstName("John");
    emp.setLastName("Doe");

    repository.insert(emp);

```

**Create Indices**

```java

// create document index
collection.createIndex("firstName",indexOptions(IndexType.NonUnique));
    collection.createIndex("note",indexOptions(IndexType.Fulltext));

// create object index. It can also be provided via annotation
    repository.createIndex("firstName",indexOptions(IndexType.NonUnique));

```

**Query a Collection**

```java

DocumentCursor cursor=collection.find(
    where("firstName").eq("John")               // firstName == John
    .and(
    where("data").elemMatch("$".lt(4))      // AND elements of data array is less than 4
    .and(
    where("note").text("quick")     // AND note field contains string 'quick' using full-text index
    )
    )
    );

    for(Document document:cursor){
    // process the document
    }

// get document by id
    Document document=collection.getById(cropId);

// query an object repository and create the first result
    Cursor<Employee> cursor=repository.find(where("firstName").eq("John"));
    Employee employee=cursor.firstOrNull();

```

**Transaction**

```java
try(Session session=db.createSession()){
    Transaction transaction=session.beginTransaction();
    try{
    CropCollection txCol=transaction.getCollection("test");

    Document document=createDocument("firstName","John");
    txCol.insert(document);

    transaction.commit();
    }catch(TransactionException e){
    transaction.rollback();
    }
    }


```

**Schema Migration**

```java

Migration migration1=new Migration(Constants.INITIAL_SCHEMA_VERSION,2){
@Override
public void migrate(Instruction instructions){
    instructions.forDatabase()
    // make a non-secure db to secure db
    .addPassword("test-user","test-password");

    // create instructions for existing repository
    instructions.forRepository(OldClass.class,null)

    // rename the repository (in case of entity name changes)
    .renameRepository("migrated",null)

    // change datatype of field empId from String to Long and convert the values
    .changeDataType("empId",(TypeConverter<String, Long>)Long::parseLong)

    // change id field from uuid to empId
    .changeIdField("uuid","empId")

    // delete uuid field
    .deleteField("uuid")

    // rename field from lastName to familyName
    .renameField("lastName","familyName")

    // add new field fullName and add default value as - firstName + " " + lastName
    .addField("fullName",document->document.get("firstName",String.class)+" "
    +document.get("familyName",String.class))

    // drop index on firstName
    .dropIndex("firstName")

    // drop index on embedded field literature.text
    .dropIndex("literature.text")

    // change data type of embedded field from float to integer and convert the values 
    .changeDataType("literature.ratings",(TypeConverter<Float, Integer>)Math::round);
    }
    };

    Migration migration2=new Migration(2,3){
@Override
public void migrate(Instruction instructions){
    instructions.forCollection("test")
    .addField("fullName","Dummy Name");
    }
    };

    MVStoreModule storeModule=MVStoreModule.withConfig()
    .filePath("/temp/employee.db")
    .compressHigh(true)
    .build();

    db=Crop.builder()
    .loadModule(storeModule)

    // schema versioning is must for migration
    .schemaVersion(2)

    // add defined migration paths
    .addMigrations(migration1,migration2)
    .openOrCreate();

```

**Automatic Replication**

```java

CropCollection collection=db.getCollection("products");

    Replica replica=Replica.builder()
    .of(collection)
    // replication via websocket (ws/wss)
    .remote("ws://127.0.0.1:9090/datagate/john/products")
    // user authentication via JWT token
    .jwtAuth("john","eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c")
    .create();

    replica.connect();

```

**Import/Export Data**

```java
// Export data to a file
Exporter exporter=Exporter.of(db);
    exporter.exportTo(schemaFile);

//Import data from the file
    Importer importer=Importer.of(db);
    importer.importFrom(schemaFile);

```

More details are available in the reference document.

## Release Notes

Release notes are available [here](https://github.com/misselvexu/mixmicro-cropdb/releases).

## Documentation

<table>
<colgroup>
<col style="width: 50%" />
<col style="width: 50%" />
</colgroup>
<thead>
<tr class="header">
<th>Reference</th>
<th>API</th>
</tr>
</thead>
<tbody>
<tr class="odd">
<td><p><a href="http://www.dizitart.org/cropdb-database">Document</a></p></td>
<td><p><a href="https://javadoc.io/doc/xyz.vopen.framework/cropdb">JavaDoc</a></p></td>
</tr>
</tbody>
</table>

## Build

To build and test Crop

```shell script

git clone https://github.com/misselvexu/mixmicro-cropdb.git
cd mixmicro-cropdb
./gradlew build

```

## Bugs / Feature Requests

Think you’ve found a bug? Want to see a new feature in the Crop? Please open an issue [here](https://github.com/misselvexu/mixmicro-cropdb/issues). But before
you file an issue please check if it is already existing or not.
