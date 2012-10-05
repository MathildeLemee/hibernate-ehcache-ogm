package fr.javafreelance.hibernate.ogm.ehcache.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity  @Indexed
public class Platform {

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name="uuid", strategy="uuid2")
  public String getId() { return id; }
  public void setId(String id) { this.id = id; }
  private String id;

  @Field
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  private String name;

  @Override
  public String toString() {
    return "Platform="+ name;
  }
}