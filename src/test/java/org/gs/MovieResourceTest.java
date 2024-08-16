package org.gs;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.management.Query;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

@QuarkusTest
class MovieResourceTest {
  private static final Long ERROR_ID = 999L;

  @InjectMock
  MovieRepository movieRepository;

  @Inject
  MovieResource movieResource;

  private Movie movie;

  @BeforeEach
  void setUp() {
    movie = new Movie();
    movie.setId(1L);
    movie.setTitle("Olá Mundo");
    movie.setCountry("Brasil");
  }

  @Test
  void getAll() {
    //Cria uma lista imutável
    List<Movie> moviesMock = Collections.singletonList(movie);
    //Cria mock para simular o getAll()
    Mockito.when(movieRepository.listAll()).thenReturn(moviesMock);
    //Simula a reposta contendo o status
    Response response = movieResource.getAll();
    //Verifica se o status é 200-OK
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  void getByIdOK() {
    //Cria o mock de acordo com o return do getById()
    Mockito.when(movieRepository.findByIdOptional(1L)).thenReturn(Optional.of(movie));
    Response response = movieResource.getById(1L);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    //Verifica se o filme retornado é o esperado
    assertEquals(movie, response.getEntity());
  }

  @Test
  void getByIdKO() {
    Mockito.when(movieRepository.findByIdOptional(ERROR_ID)).thenReturn(Optional.empty());
    Response response = movieResource.getById(ERROR_ID);
    //Verifica se o status é 404-NOT_FOUND
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  void getByTitleOK() {
    //Manipula consultas e entidades
    PanacheQuery<Movie> query = mock(PanacheQuery.class);
    Mockito.when(movieRepository.find("title", "Olá Mundo")).thenReturn(query);
    Mockito.when(query.singleResultOptional()).thenReturn(Optional.of(movie));
    Response response = movieResource.getByTitle("Olá Mundo");
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    assertEquals(movie, response.getEntity());
  }

  @Test
  void getByTitleKO() {
    PanacheQuery<Movie> query = mock(PanacheQuery.class);
    Mockito.when(movieRepository.find("title", "Mundo")).thenReturn(query);
    Mockito.when(query.singleResultOptional()).thenReturn(Optional.empty());
    Response response = movieResource.getByTitle("Mundo");
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  void getByCountry() {
    List<Movie> moviesMock = Arrays.asList(movie);
    Mockito.when(movieRepository.findByCountry("Brasil")).thenReturn(moviesMock);
    Response response = movieResource.getByCountry("Brasil");
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
  }

  @Test
  void createOK() {
    Mockito.when(movieRepository.isPersistent(movie)).thenReturn(true);
    Mockito.doNothing().when(movieRepository).persist(movie);
    Response response = movieResource.create(movie);
    assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
    assertEquals(URI.create("/movies/" + movie.getId()), response.getLocation());
  }

  @Test
  void createKO() {
    Mockito.when(movieRepository.isPersistent(movie)).thenReturn(false);
    Mockito.doNothing().when(movieRepository).persist(movie);
    Response response = movieResource.create(movie);
    assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
  }

  @Test
  void updateByIdOK() {
    Mockito.when(movieRepository.findByIdOptional(1L)).thenReturn(Optional.of(movie));
    Movie updateMovie = new Movie();
    updateMovie.setTitle("Olá");
    Response response = movieResource.updateById(1L, updateMovie);
    assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    Movie resultado = (Movie) response.getEntity();
    assertEquals("Olá", resultado.getTitle());
  }

  @Test
  void updateByIdKO() {
    Mockito.when(movieRepository.findByIdOptional(1L)).thenReturn(Optional.empty());
    Movie updateMovie = new Movie();
    updateMovie.setTitle("Error");
    Response response = movieResource.updateById(1L, updateMovie);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }

  @Test
  void deleteByIdOK() {
    Mockito.when(movieRepository.deleteById(1L)).thenReturn(true);
    Response response = movieResource.deleteById(1L);
    assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
  }

  @Test
  void deleteByIdKO() {
    Mockito.when(movieRepository.deleteById(1L)).thenReturn(false);
    Response response = movieResource.deleteById(1L);
    assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
  }
}
