package fr.javafreelance.hibernate.ogm.ehcache.model;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.TableGenerator;

@Entity @Indexed
public class AppliManyToMany {
  @Id
  @GeneratedValue(strategy = GenerationType.TABLE, generator = "appli")
  @TableGenerator(
      name = "appli",
      table = "sequences",
      pkColumnName = "key",
      pkColumnValue = "apply",
      valueColumnName = "seed"
  )
  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  private Long id;

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  private String name;

  @ManyToMany
  @IndexedEmbedded
  public List<Platform> getPlatforms() { return platforms; }
  public void setPlatforms(List<Platform> platforms) { this.platforms = platforms; }
  private List<Platform> platforms=null;

  @Override
  public String toString() {
    String temp =  "AppliManyToMany{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", platforms=[" ;

    for (Platform platform : platforms) {
      String platformLists=null;
      temp+=platform.toString()+" , ";
    }
    temp +="]}";
    return temp;
  }
}
