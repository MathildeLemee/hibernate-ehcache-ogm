package fr.javafreelance.hibernate.ogm.ehcache;


import fr.javafreelance.hibernate.ogm.ehcache.model.Platform;
import fr.javafreelance.hibernate.ogm.ehcache.model.AppliManyToMany;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.ogm.util.impl.Log;
import org.hibernate.ogm.util.impl.LoggerFactory;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.DatabaseRetrievalMethod;
import org.hibernate.search.query.dsl.QueryBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

import static org.hibernate.search.query.ObjectLookupMethod.SKIP;

/**
 * Test with Many To Many - Unidirectionnal
 * @author : Mathilde Lemee
 */

public class InitDataManyToMany {

  private static final String JBOSS_TM_CLASS_NAME = "com.arjuna.ats.jta.TransactionManager";
  private static final Log logger = LoggerFactory.make();


  public static void main(String[] args) {
    TransactionManager tm = getTransactionManager();
    //build the EntityManagerFactory as you would build in in Hibernate Core
    EntityManagerFactory emf = Persistence.createEntityManagerFactory("ogm-ehcache");

    try {
      tm.begin();

      logger.info("*** Initialize data ****");

      EntityManager em = emf.createEntityManager();

      Platform itunes = new Platform();
      itunes.setName("iTunes");

      Platform play = new Platform();
      play.setName("play");
      em.persist(itunes);
      em.persist(play);

      List<Platform> bothPlatforms = new ArrayList<Platform>();
      bothPlatforms.add(itunes);
      bothPlatforms.add(play);

      List<Platform> playPlatforms = new ArrayList<Platform>();
      playPlatforms.add(play);

      List<Platform> itunesPlatforms = new ArrayList<Platform>();
      itunesPlatforms.add(itunes);


      for (int i = 0; i < 10; i++) {
        AppliManyToMany learnAnimals = new AppliManyToMany();
        learnAnimals.setName("Nom de mon apps " + i);
        if (i % 3 == 0) {
          learnAnimals.setPlatforms(playPlatforms);
        } else if (i % 2 == 0) {
          learnAnimals.setPlatforms(itunesPlatforms);
        } else {
          learnAnimals.setPlatforms(bothPlatforms);
        }
        em.persist(learnAnimals);

      }

      em.flush();
      em.close();
      tm.commit();

      tm.begin();
      logger.info("***** searching by id 5 with find *****");

      em = emf.createEntityManager();
      AppliManyToMany appli = em.find(AppliManyToMany.class, 5l);
      logger.infof("Found appli %s ", appli.toString());
      em.flush();
      em.close();
      tm.commit();

      logger.info("***** searching by id 6 with createQuery *****");
      tm.begin();
      em = emf.createEntityManager();
      Session session = (Session)em.getDelegate();
      Query query = session
          .createQuery("from AppliManyToMany a where a.id = :id")
          .setLong("id", 6l);
      // List<Appli> applis = query.list();
      AppliManyToMany appli45 = (AppliManyToMany)query.uniqueResult();
      final List<Platform> platforms = appli45.getPlatforms();
      logger.infof("Found appli %s ", appli45.toString());
      em.flush();
      em.close();
      tm.commit();


      /** DOES NOT WORK
       query = session
       .createQuery("from AppliManyToMany a where a.platform.name = :name")
       .setString("name", "play");
       */
      tm.begin();
      em = emf.createEntityManager();
      session = (Session)em.getDelegate();

      //Add full-text superpowers to any EntityManager:
      logger.info("***** searching by id 7 with Lucene Query *****");
      FullTextSession ftem = Search.getFullTextSession(session);

      QueryBuilder b = ftem.getSearchFactory()
          .buildQueryBuilder()
          .forEntity(AppliManyToMany.class)
          .get();
      org.apache.lucene.search.Query lq = b.keyword().onField("id").matching("7").createQuery();


      FullTextQuery ftQuery = ftem.createFullTextQuery(lq, AppliManyToMany.class);
      ftQuery.initializeObjectsWith(SKIP,
          DatabaseRetrievalMethod.FIND_BY_ID);

//List all matching Hypothesis:
      List<AppliManyToMany> resultList = ftQuery.list();
      for (AppliManyToMany appli1 : resultList) {
        logger.infof("Found appli %s ", appli1.toString());
      }
      em.flush();
      em.close();
      tm.commit();
      tm.begin();
      em = emf.createEntityManager();
      session = (Session)em.getDelegate();


      logger.info("***** search complex with Lucene Query *****");
      ftem = Search.getFullTextSession(session);//getFullTextEntityManager(em);

//Optionally use the QueryBuilder to simplify Query definition:
      b = ftem.getSearchFactory()
          .buildQueryBuilder()
          .forEntity(AppliManyToMany.class)
          .get();

//Create a Lucene Query:
      lq = b.keyword().onField("platforms.name").matching("itunes").createQuery();

      ftQuery = ftem.createFullTextQuery(lq, AppliManyToMany.class);
      ftQuery.initializeObjectsWith(SKIP,
          DatabaseRetrievalMethod.FIND_BY_ID);

//List all matching Hypothesis:
      resultList = ftQuery.list();
      for (AppliManyToMany appli1 : resultList) {
        logger.infof("Found appli %s ", appli1.toString());
      }
      em.flush();
      em.close();
      tm.commit();
      emf.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("OUT");
  }

  public static TransactionManager getTransactionManager() {
    try {
      Class<?> tmClass = InitDataManyToMany.class.getClassLoader().loadClass(JBOSS_TM_CLASS_NAME);
      return (TransactionManager)tmClass.getMethod("transactionManager").invoke(null);
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    } catch (NoSuchMethodException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    return null;
  }
}