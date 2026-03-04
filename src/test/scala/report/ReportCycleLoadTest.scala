package report
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import com.microsoft.azure.functions.HttpStatus

class ReportCycleLoadTest extends Simulation {

        val httpProtocol = http
                .baseUrl("http://localhost:7071/api")
                .acceptHeader("application/json")

        val scn = scenario("Report Lifecycle Test")
                .during(30 seconds) {
                        exec(_.set("reportId", java.util.UUID.randomUUID().toString()))
                        .exec(http("Call Report Request")
                                .post("/report")
                                .header("accept", "application/json")
                                .header("Content-Type", "application/json")
                                .body(StringBody("""{"reportId": "#{reportId}", "reportType": "simple","title": "Test Report"}"""))
                                .check(status.is(HttpStatus.ACCEPTED.value),
                                        jsonPath("$.reportId").is("#{reportId}"),
                                        jsonPath("$.status").is("PENDING"),
                                        jsonPath("$.executionId").exists,
                                        jsonPath("$.executionId").saveAs("executionId")))
                        .pause(100 milliseconds, 200 milliseconds)

                        // .exec(_.set("poolingComplete", false))
                        // .asLongAs(session => !session("poolingComplete").as[Boolean]) {
                        //         exec(http("Check Report Status")
                        //                 .get("/report/${executionId}")
                        //                 .header("accept", "application/json")
                        //                 .check(
                        //                         status.in(200, 201), // Apenas 200 ou 201 são aceitos
                        //                         doIf(status.is(201)) {
                        //                                 // Quando recebe 201, valida filePath e finaliza pooling
                        //                                 jsonPath("$.filePath").exists
                        //                                         .transform(filePath => {
                        //                                                 // Validar se é uma URL válida
                        //                                                 val urlPattern = "^https?://.*".r
                        //                                                 urlPattern.findFirstIn(filePath.toString) match {
                        //                                                         case Some(_) => true
                        //                                                         case None => throw new RuntimeException(s"Invalid URL in filePath: $filePath")
                        //                                                 }
                        //                                         })
                        //                                         .saveAs("validUrl")
                        //                         }
                        //                 ))
                        //         .exec(session => {
                        //                 val statusCode = session("gatling.http.status").asOption[Int].getOrElse(0)
                        //                 statusCode match {
                        //                         case 201 => 
                        //                                 // Report processado com sucesso
                        //                                 session.set("poolingComplete", true)
                        //                         case 200 => 
                        //                                 // Continue pooling
                        //                                 session
                        //                         case _ => 
                        //                                 // Qualquer outro código é erro
                        //                                 session.set("poolingComplete", true).markAsFailed
                        //                 }
                        //         })
                        //         .doIf(session => !session("poolingComplete").as[Boolean]) {
                        //                 pause(250 milliseconds) // Aguardar 250ms antes da próxima tentativa
                        //         }
                        // }
                        // .pause(100 milliseconds, 200 milliseconds)

                }

        setUp(scn.inject(
                rampUsers(3) during (5 seconds)
        )).protocols(httpProtocol)
}