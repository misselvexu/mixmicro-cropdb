package xyz.vopen.framework.cropdb.samples;

import lombok.*;
import xyz.vopen.framework.cropdb.CropDB;
import xyz.vopen.framework.cropdb.collection.CropCollection;
import xyz.vopen.framework.cropdb.collection.Document;
import xyz.vopen.framework.cropdb.collection.DocumentCursor;
import xyz.vopen.framework.cropdb.common.WriteResult;
import xyz.vopen.framework.cropdb.common.mapper.JacksonMapperModule;
import xyz.vopen.framework.cropdb.index.IndexType;
import xyz.vopen.framework.cropdb.repository.ObjectRepository;
import xyz.vopen.framework.cropdb.repository.annotations.Entity;
import xyz.vopen.framework.cropdb.repository.annotations.Id;
import xyz.vopen.framework.cropdb.repository.annotations.Index;
import xyz.vopen.framework.cropdb.rocksdb.RocksDBModule;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import static xyz.vopen.framework.cropdb.collection.Document.createDocument;
import static xyz.vopen.framework.cropdb.filters.FluentFilter.where;

/**
 * {@link CropDBSample} Definition
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 * @version ${project.version} - 2021/12/18
 */
public class CropDBSample {

  public static void main(String[] args) {

    RocksDBModule storeModule = RocksDBModule.withConfig().filePath("/tmp/test.db").build();

    CropDB db =
        CropDB.builder()
            .loadModule(storeModule)
            .loadModule(new JacksonMapperModule()) // optional
            .openOrCreate("user", "password");

    // Create a Nitrite Collection
    CropCollection collection = db.getCollection("test");

    // Create an Object Repository
    ObjectRepository<Employee> repository = db.getRepository(Employee.class);

    // create a document to populate data
    Document doc =
        createDocument("firstName", "John")
            .put("lastName", "Doe")
            .put("birthDay", new Date())
            .put("data", new byte[] {1, 2, 3})
            .put(
                "fruits",
                new ArrayList<String>() {
                  {
                    add("apple");
                    add("orange");
                    add("banana");
                  }
                })
            .put("note", "a quick brown fox jump over the lazy dog");

    // insert the document
    WriteResult writeResult = collection.insert(doc);
    System.out.println("affected count = " + writeResult.getAffectedCount());

    // find a document
    DocumentCursor cursor =
        collection.find(where("firstName").eq("John").and(where("lastName").eq("Doe")));

    Document document = cursor.firstOrNull();
    // update the document
    collection.update(where("firstName").eq("John"), createDocument("lastName", "Wick"));

    // remove the document
    collection.remove(document);

    // insert an object in repository
    Employee emp = new Employee();
    emp.setEmpId(124589);
    emp.setFirstName("John");
    emp.setLastName("Doe");

//    repository.insert(emp);
  }

  // ~~ entity

  @Getter
  @Setter
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  @Entity(
      value = "retired-employee", // entity name (optional),
      indices = {
        @Index(value = "firstName", type = IndexType.NON_UNIQUE),
        @Index(value = "lastName", type = IndexType.NON_UNIQUE),
        @Index(value = "note", type = IndexType.FULL_TEXT),
      })
  public static class Employee implements Serializable {
    // provides id field to uniquely identify an object inside an ObjectRepository
    @Id private long empId;
    private Date joinDate;
    private String firstName;
    private String lastName;
    private String note;
  }
}
