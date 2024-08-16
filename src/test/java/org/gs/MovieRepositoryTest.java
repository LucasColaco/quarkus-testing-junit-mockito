package org.gs;

import static org.junit.jupiter.api.Assertions.*;

import io.quarkus.test.junit.QuarkusTest;
import java.util.List;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;

@QuarkusTest
class MovieRepositoryTest {

  @Inject
  MovieRepository movieRepository;

  @Test
  @Transactional
  void findByCountryOK() {
    //Insere dados para teste
    var movie = new Movie();
    movie.setCountry("Olá Mundo");
    //Persiste a entity
    movieRepository.persist(movie);
    //Chama o método, passando como parametro country
    var movies = movieRepository.findByCountry("Olá Mundo");
    //Verifica se está vazio
    assertEquals(1, movies.size());
    //Verifica se a query retornou o resultado esperado
    assertEquals("Olá Mundo", movies.get(0).getCountry());
  }

  @Test
  void findByCountryKO() {
    var movie = movieRepository.findByCountry("");
    assertTrue(movie.isEmpty());
  }
}
