package fr.javafreelance.hibernate.ogm.ehcache;


import fr.javafreelance.hibernate.ogm.ehcache.model.Appli;
import fr.javafreelance.hibernate.ogm.ehcache.model.Platform;
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
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.transaction.TransactionManager;

import static org.hibernate.search.query.ObjectLookupMethod.SKIP;

/**
 * Test with Many To One - Unidirectionnal
 * @author : Mathilde Lemee
 */

public class InitData {

  private static final String JBOSS_TM_CLASS_NAME = "com.arjuna.ats.jta.TransactionManager";
  private static final Log logger = LoggerFactory.make();


  public static void main(String[] args) {
    TransactionManager transactionManager = getTransactionManager();
    EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("ogm-ehcache");

    try {
      transactionManager.begin();
      logger.info("*** Initialize data ****");
      EntityManager entityManager = entityManagerFactory.createEntityManager();
      Platform itunes = new Platform();
      itunes.setName("iTunes");

      Platform play = new Platform();
      play.setName("play");
      entityManager.persist(itunes);
      entityManager.persist(play);

      for (int i = 0; i < 10; i++) {
        Appli learnAnimals = new Appli();
        learnAnimals.setName("Nom de mon apps " + i);
        if (i % 2 == 0) {
          learnAnimals.setPlatform(itunes);
        } else {
          learnAnimals.setPlatform(play);
        }
        entityManager.persist(learnAnimals);
      }


      entityManager.flush();
      entityManager.close();
      transactionManager.commit();

      transactionManager.begin();
      logger.info("***** searching by id 5 with find *****");

      entityManager = entityManagerFactory.createEntityManager();
      Appli appli = entityManager.find(Appli.class, 5l);
      logger.infof("Found appli %s ", appli.toString());
      entityManager.flush();
      entityManager.close();
      transactionManager.commit();

      logger.info("***** searching by id 6 with createQuery *****");
      transactionManager.begin();
      entityManager = entityManagerFactory.createEntityManager();
      Session session = (Session)entityManager.getDelegate();
      Query query = session
          .createQuery("from Appli a where a.id = :id")
          .setLong("id", 6l);
      // List<Appli> applis = query.list();
      Appli appli45 = (Appli)query.uniqueResult();
      logger.infof("Found appli %s ", appli45.toString());

      entityManager.flush();
      entityManager.close();
      transactionManager.commit();
      transactionManager.begin();
      entityManager = entityManagerFactory.createEntityManager();
      session = (Session)entityManager.getDelegate();

      /** DOES NOT WORK
       query = session
       .createQuery("from Appli a where a.platform.name = :name")
       .setString("name", "play");
       */

      //Add full-text superpowers to any EntityManager:
      logger.info("***** searching by id 7 with Lucene Query *****");
      FullTextSession fullTextSession = Search.getFullTextSession(session);

      QueryBuilder queryBuilder = fullTextSession.getSearchFactory()
          .buildQueryBuilder()
          .forEntity(Appli.class)
          .get();
      org.apache.lucene.search.Query luceneQuery = queryBuilder.keyword().onField("id").matching("7").createQuery();


      FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery, Appli.class);
      fullTextQuery.initializeObjectsWith(SKIP,
          DatabaseRetrievalMethod.FIND_BY_ID);

      List<Appli> resultList = fullTextQuery.list();
      for (Appli appli1 : resultList) {
        logger.infof("Found appli %s ", appli1.toString());
      }

      logger.info("***** search complex with Lucene Query *****");
      entityManager.flush();
      entityManager.close();
      transactionManager.commit();
      transactionManager.begin();
      entityManager = entityManagerFactory.createEntityManager();
      session = (Session)entityManager.getDelegate();
      fullTextSession = Search.getFullTextSession(session);

      queryBuilder = fullTextSession.getSearchFactory()
          .buildQueryBuilder()
          .forEntity(Appli.class)
          .get();

      luceneQuery = queryBuilder.keyword().onField("platform.name").matching("play").createQuery();

      fullTextQuery = fullTextSession.createFullTextQuery(luceneQuery, Appli.class);
      fullTextQuery.initializeObjectsWith(SKIP,
          DatabaseRetrievalMethod.FIND_BY_ID);

      resultList = fullTextQuery.list();
      for (Appli appli1 : resultList) {
        logger.infof("Found appli %s ", appli1.toString());
      }
      entityManager.flush();
      entityManager.close();
      transactionManager.commit();
      entityManagerFactory.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("OUT");
  }

  public static TransactionManager getTransactionManager() {
    try {
      Class<?> tmClass = InitData.class.getClassLoader().loadClass(JBOSS_TM_CLASS_NAME);
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