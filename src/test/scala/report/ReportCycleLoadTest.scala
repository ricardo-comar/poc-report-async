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
                        .pause(200 milliseconds, 300 milliseconds)

                        .exec(_.set("poolingComplete", false))
                        .asLongAs(session => !session("poolingComplete").as[Boolean]) {
                                exec(http("Check Report Status")
                                        .get("/report/${executionId}")
                                        .header("accept", "application/json")
                                        .check(
                                                status.in(200, 201), // Apenas 200 ou 201 são aceitos
                                                checkIf(session => session("gatling.http.status").asOption[Int].contains(201)) {
                                                        // Quando recebe 201, salva filePath para validação posterior
                                                        jsonPath("$.filePath").saveAs("validUrl")
                                                }
                                        )
                                )
                                .exec(session => {
                                        val statusCode = session("gatling.http.status").asOption[Int].getOrElse(0)
                                        statusCode match {
                                                case 201 => 
                                                        session("validUrl").asOption[String] match {
                                                            case Some(url) if "^https?://.*".r.findFirstIn(url).isDefined =>
                                                                session.set("poolingComplete", true)
                                                            case Some(bad) =>
                                                                throw new RuntimeException(s"Invalid URL in filePath: $bad")
                                                            case None =>
                                                                session.set("poolingComplete", true).markAsFailed
                                                        }
                                                case 200 => 
                                                        session
                                                case _ => 
                                                        session.set("poolingComplete", true).markAsFailed
                                        }
                                })
                                .doIf(session => !session("poolingComplete").as[Boolean]) {
                                        pause(500 milliseconds) 
                                }
                        }
                        .pause(200 milliseconds, 500 milliseconds)

                }

        setUp(scn.inject(
                rampUsers(3) during (5 seconds)
        )).protocols(httpProtocol)
}