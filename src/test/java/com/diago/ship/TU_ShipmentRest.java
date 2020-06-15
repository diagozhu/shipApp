package com.diago.ship;

import com.diago.ship.dao.RootShipment;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.module.mockmvc.RestAssuredMockMvc;
import com.jayway.restassured.module.mockmvc.response.MockMvcResponse;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.concurrent.TimeUnit;

import static com.jayway.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.*;

/**
 * 请关系ShipApplication:8008,再尝试跑此测试。
 * Run this should stop the Application Server.
 * <p>
 * 测试前请修改spring-mvc.xml，跳过前端认证检查
 * Before test, change spring-mvc.xml to skip the auth check.
 * <p>
 * 测试所有实体API控制器，无需启动Web容器，不会产生脏数据, 运行速度比WEB方式提高一倍。
 * Test all entity API controller, No need Web container, no dirty data, half time of web way.
 *
 * @author 朱佳成  diago@yeah.net 13818118656
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ShipApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Transactional
public class TU_ShipmentRest {

    @Autowired
    private WebApplicationContext wac;

    @Before
    public void before() {
        RestAssuredMockMvc.webAppContextSetup(wac);
    }

    @After
    public void after() {
        RestAssuredMockMvc.reset();
    }

    @Test
    public void testGreetApi() throws JsonProcessingException {
        MockMvcResponse rsp;

        // static greet api test
        rsp = given().when().get("/api/v1/shipment/greet").then().statusCode(200).body("errCod", equalTo(0))
                .extract().response();
        String word = rsp.jsonPath().getString("rows[0]");
        Assertions.assertThat(word).isEqualTo("Hello World!!!");
    }

    /**
     * This operation applies to trade. When trade quantity is changed, all shipment quantities should
     * be updated proportionally
     * - 1) There is 1 root shipment 6000kg with 3 child standard shipments id:1/2/3 weight:1000/2000/3000kg
     * Merge 2&3 with total weight 5000.
     * - 2) If there are some merged shipments on the root shipment. Find and re-accumulate them.
     * 1.rootShipment.changeWeight("12000") //Right! new weight:2000/4000/6000kg, mergeShipment(2&3):10000kg
     * 2.rootShipment.changeWeight("2000")  //Right! new weight:333/666/1001kg (The biggest part will plus
     * remainder, if they are same, max id will plus remainder.). merge("2,3"):1667kg
     */
    @Test
    public void testShipmentSplitMergeChangeWeight() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        MockMvcResponse rsp;

        // 1) Create Root Shipment with 6000kg
        Integer rootShipId = null;
        RootShipment rootShip = new RootShipment();
        rootShip.setWeight(6000);
        rsp = given().contentType(ContentType.JSON).body(mapper.writeValueAsString(rootShip)).when()
                .post("/api/v1/rootShipment").then().statusCode(200)
                .body("errCod", equalTo(0), "rows[0].id", not(""))
                .time(lessThan(1L), TimeUnit.SECONDS).extract().response();
        rootShipId = rsp.jsonPath().getInt("rows[0].id");
        System.out.println("NEW_ROOT_SHIPMENT_ID(): " + rootShipId);
        // Verification
        given().when().get("/api/v1/rootShipment/{id}", rootShipId).then().statusCode(200)
                .body("errCod", equalTo(0),
                        "rows[0].weight", is(6000));

        // 2) Split the Root Shipment to 1000/2000/3000 kg;
        given().when().get("/api/v1/shipment/split/{id}/1000,2000,3000", rootShipId).then().statusCode(200)
                .body("errCod", equalTo(0));
        // Verify split standard shipment weight is 1000/2000/3000 kg.
        rsp = given().when().get("/api/v1/rootShipment/{id}", rootShipId).then().statusCode(200)
                .body("errCod", equalTo(0),
                        "rows[0].spiltShipments[0].weight", is(1000),
                        "rows[0].spiltShipments[1].weight", is(2000),
                        "rows[0].spiltShipments[2].weight", is(3000))
                .time(lessThan(1L), TimeUnit.SECONDS).extract().response();
        // get split shipment id for next part merge test.
        int splitShipId1 = rsp.jsonPath().getInt("rows[0].spiltShipments[0].id");
        int splitShipId2 = rsp.jsonPath().getInt("rows[0].spiltShipments[1].id");
        int splitShipId3 = rsp.jsonPath().getInt("rows[0].spiltShipments[2].id");

        // 3) Merge split standard shipment 2/3 with total weight 5000 kg.
        rsp = given().when().get("/api/v1/shipment/merge/{id2},{id3}", splitShipId2, splitShipId3).then().statusCode(200)
                .body("errCod", equalTo(0)).extract().response();
        int mergeShipId = rsp.jsonPath().getInt("rows[0]");
        // Verify merged shipment weight:5000. Merged from standard shipments weight is 2000/3000 kg.
        given().when().get("/api/v1/mergedShipment/{id}", mergeShipId).then().statusCode(200)
                .body("errCod", equalTo(0),
                        "rows[0].weight", is(5000),
                        "rows[0].mergedFromShipments[0].weight", is(2000),
                        "rows[0].mergedFromShipments[1].weight", is(3000))
                .time(lessThan(1L), TimeUnit.SECONDS);

        // 4) rootShipment.changeWeight("12000") //Right! new weight:2000/4000/6000kg, mergeShipment(2&3):10000kg
        given().when().get("/api/v1/shipment/changeWeight/{id}/12000", rootShipId).then().statusCode(200)
                .body("errCod", equalTo(0));
        // Verify split standard shipment weight is 1000/2000/3000 kg.
        rsp = given().when().get("/api/v1/rootShipment/{id}", rootShipId).then().statusCode(200)
                .body("errCod", equalTo(0),
                        "rows[0].weight", is(12000),
                        "rows[0].spiltShipments[0].weight", is(2000),
                        "rows[0].spiltShipments[1].weight", is(4000),
                        "rows[0].spiltShipments[2].weight", is(6000))
                .time(lessThan(1L), TimeUnit.SECONDS).extract().response();
        // Verify merged shipment of 2&3 weight:10000 kg.
        given().when().get("/api/v1/mergedShipment/{id}", mergeShipId).then().statusCode(200)
                .body("errCod", equalTo(0),
                        "rows[0].weight", is(10000))
                .time(lessThan(1L), TimeUnit.SECONDS);

        // 5) rootShipment.changeWeight("2000")  //Right! new weight:333/666/1001kg (The biggest part will plus
        //    remainder, if they are same, max id will plus remainder.). merge("2,3"):1667kg.
        given().when().get("/api/v1/shipment/changeWeight/{id}/2000", rootShipId).then().statusCode(200)
                .body("errCod", equalTo(0));
        // Verify split standard shipment weight is 333/666/1001 kg.
        rsp = given().when().get("/api/v1/rootShipment/{id}", rootShipId).then().statusCode(200)
                .body("errCod", equalTo(0),
                        "rows[0].weight", is(2000),
                        "rows[0].spiltShipments[0].weight", is(333),
                        "rows[0].spiltShipments[1].weight", is(666),
                        "rows[0].spiltShipments[2].weight", is(1001))
                .time(lessThan(1L), TimeUnit.SECONDS).extract().response();
        // Verify merged shipment of 2&3 weight:1667 kg.
        given().when().get("/api/v1/mergedShipment/{id}", mergeShipId).then().statusCode(200)
                .body("errCod", equalTo(0),
                        "rows[0].weight", is(1667))
                .time(lessThan(1L), TimeUnit.SECONDS);
    }
}