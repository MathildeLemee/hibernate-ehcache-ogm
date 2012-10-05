package fr.javafreelance.hibernate.ogm.ehcache.model;

import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.TableGenerator;

@Entity @Indexed
public class Appli {
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

  @ManyToOne
  @IndexedEmbedded
  public Platform getPlatform() { return platform; }
  public void setPlatform(Platform platform) { this.platform = platform; }
  private Platform platform;

  @Override
  public String toString() {
    return "Appli{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", platform=" + platform +
           '}';
  }
}
