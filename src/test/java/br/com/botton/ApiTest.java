package br.com.botton;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password=","spring.datasource.driver-class-name=org.h2.Driver"})
class ApiTest {
 @Autowired Api api; @Autowired JdbcTemplate db;
 @BeforeEach void clear(){db.update("DELETE FROM requirements");db.update("DELETE FROM orders");db.update("DELETE FROM supplies");}
 long supply(long stock){return api.addSupply(new Api.Supply("Chapa 58mm","un.",stock,20,200)).get("id");}
 long order(int qty,Api.Ingredient... items){return api.addOrder(new Api.Demand("Ana","58mm",qty,LocalDate.now().plusDays(3),List.of(items))).get("id");}
 @SuppressWarnings("unchecked") Map<String,Object> first(){return ((List<Map<String,Object>>)api.state().get("supplies")).get(0);}
 @Test void reservesAndCompletesExactlyOnce(){long s=supply(100);long o=order(90,new Api.Ingredient(s,1));assertEquals("BAIXO",first().get("alert"));assertEquals(10L,first().get("available"));api.finish(o,"complete");assertEquals(10L,first().get("available"));assertThrows(ResponseStatusException.class,()->api.finish(o,"complete"));assertEquals(10L,first().get("available"));}
 @Test void cancellationReleasesReservations(){long s=supply(100);long o=order(120,new Api.Ingredient(s,1));assertEquals("FALTA",first().get("alert"));api.finish(o,"cancel");assertEquals(100L,first().get("available"));assertEquals("OK",first().get("alert"));}
 @Test void failedCompletionRollsBackAllMaterials(){long a=supply(100),b=supply(1);long o=order(10,new Api.Ingredient(a,1),new Api.Ingredient(b,1));assertThrows(ResponseStatusException.class,()->api.finish(o,"complete"));assertEquals(100L,db.queryForObject("SELECT stock FROM supplies WHERE id=?",Long.class,a));assertEquals("ABERTO",db.queryForObject("SELECT status FROM orders WHERE id=?",String.class,o));}
 @Test void stockAndThresholdValidation(){long s=supply(300);assertEquals("EXCESSO",first().get("alert"));assertThrows(ResponseStatusException.class,()->api.move(s,new Api.Movement(-301)));assertThrows(ResponseStatusException.class,()->api.addSupply(new Api.Supply("x","un",0,50,10)));assertEquals(300L,first().get("available"));}
}
