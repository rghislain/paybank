package com.paybank.hexagonal.tests;

import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;
import org.springframework.test.context.TestPropertySource;

public class VirementSimulation extends Simulation {

    HttpProtocolBuilder httpProtocol = http
        .baseUrl("http://localhost:8080");

    ScenarioBuilder scn = scenario("Scénario Charge Virement")
        .exec(http("Requete Virement")
            .post("/api/virements")
            .header("Content-Type", "application/json")
            .body(StringBody("{\"montant\": 100}"))
        );

    {
        setUp(
            scn.injectOpen(atOnceUsers(50))
        ).protocols(httpProtocol);
    }
}