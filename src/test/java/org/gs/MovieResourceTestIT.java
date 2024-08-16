package org.gs;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.when;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.core.MediaType;
import org.junit.jupiter.api.*;

@QuarkusTest
@Tag("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MovieResourceTestIT {
  private static final Long ERROR_ID = 999L;

  @Test
  @Order(1)
  void getAll() {
    given()
      .when().get("/movies")
      .then()
      .statusCode(200) //Verifica se o código é 200-OK
      .body("size()", equalTo(2)) //Verifica o tamanho da lista
      .body("[0].title", equalTo("FirstMovie")); //Verifica title do primeiro movie
  }

  @Test
  @Order(1)
  void getById() {
    given()
      .when().get("/movies/1")
      .then()
      .statusCode(200) //Verifica se o código é 200-OK
      .body("title", equalTo("FirstMovie")); //Verifica title com o id 1
  }

  @Test
  @Order(1)
  void getByIdKO() {
    given()
      .when().get("/movies/99") //Busca ID que não existe
      .then()
      .statusCode(404); //Verifica se o código é NOT_Found
  }

  @Test
  @Order(1)
  void getByTitle() {
    given()
      .when().get("/movies/title/FirstMovie")
      .then()
      .statusCode(200) //Verifica se o código é 200-OK
      .body("title", equalTo("FirstMovie"));
  }

  @Test
  @Order(1)
  void getByTitleKO() {
    given()
      .when().get("/movies/title/First") //Busca por title inexistente
      .then()
      .statusCode(404); //Verifica se o código é NOT_Found
  }

  @Test
  @Order(2)
  void getByCountry() {
    given()
      .when().get("/movies/country/Planet")//Busca por title inexistente
      .then()
      .statusCode(200) //Verifica se o código é 200-OK
      .body("size()", equalTo(2)) //Verifica o tamanho do retorno
      .body("[0].country", equalTo("Planet"))//Compara os resultados
      .body("[1].country", equalTo("Planet"));//Compara os resultados
  }

  @Test
  @Order(2)
  void getByCountryKO() {
    given()
      .when().get("/movies/country/Planeta")//Busca por country inexistente
      .then()
      .statusCode(200) //Verifica se o código é 200-OK
      .body("size()", equalTo(0));//Verifica a lista vazia
  }

  @Test
  @Order(3)
  void create() {
    var createMovie = new Movie();
    createMovie.setTitle("Transformers");
    createMovie.setCountry("Estados Unidos");
    Response response = 
    given()
      .contentType(ContentType.JSON)
      .body(createMovie)
      .when()
      .post("/movies")
      .then()
      .statusCode(201) //Valida se o status é created
      .extract()
      .response();

      //Valida se createMovie foi criado
      String location = response.getHeader("Location");
      assertTrue(location.contains("/movies/"));

      //Valida se foi adicionado corretamente no bd
      long novoId = Long.valueOf(location.substring(location.lastIndexOf("/") + 1));

      given()
      .when()
      .get("/movies/" + novoId)
      .then()
      .statusCode(200) 
      .body("title", equalTo("Transformers"));
  }

  @Test
  @Order(4)
  void updateById() {
    var update = new Movie();
    update.setTitle("Novo");
    
    //ID para o filme que vai ser atualizado
    Long id = 1L;

    //Manda a requisicao PUT("Atualizar") com o ID
    given()
      .contentType(ContentType.JSON)
      .body(update)
      .when()
      .put("/movies/" + id)
      .then()
      .statusCode(200) //Valida se o status é OK
      .body("title", equalTo("Novo"));
    
      //Valida se realmente atualizou o filme
      given()
      .when()
      .get("/movies/" + id)
      .then()
      .statusCode(200) //Valida se o status é OK
      .body("title", equalTo("Novo"));
  }

  @Test
  @Order(4)
  void updateByIdKO() {
    var update = new Movie();
    update.setTitle("Spider Man");
    
    //ID que não existe para o filme que vai ser atualizado
    Long id = ERROR_ID;

    //Manda a requisicao PUT("Atualizar") com o ID
    given()
      .contentType(ContentType.JSON)
      .body(update)
      .when()
      .put("/movies/" + ERROR_ID)
      .then()
      .statusCode(404); //Valida se o status é NOT_Found
  }

  @Test
  @Order(5)
  void deleteById() {
    Long id = 1L;

     given()
      .when()
      .delete("/movies/" + id)
      .then()
      .statusCode(204); //Valida se o status é No_CONTENT
  }

  @Test
  @Order(5)
  void deleteByIdKO() {
    given()
      .when()
      .delete("/movies/" + ERROR_ID)
      .then()
      .statusCode(404); //Valida se o status é Not_FOUND
  }
}
