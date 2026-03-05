package report
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._
import scala.sys.process._
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
                                        jsonPath("$.executionId").exists.saveAs("executionId")))
                        .pause(200 milliseconds, 300 milliseconds)

                        .exec(_.set("poolingComplete", false))
                        .asLongAs(session => !session("poolingComplete").as[Boolean]) {
                                exec(http("Check Report Status")
                                        .get("/report/${executionId}")
                                        .header("accept", "application/json")
                                        .check(
                                                status.in(HttpStatus.OK.value, HttpStatus.CREATED.value), 
                                                checkIf(session => session("gatling.http.status").asOption[Int].contains(HttpStatus.CREATED.value)) {
                                                        jsonPath("$.filePath").saveAs("validUrl")
                                                }
                                        )
                                )
                                .exec(session => {
                                        val statusCode = HttpStatus.valueOf(session("gatling.http.status").asOption[Int].getOrElse(0))
                                        statusCode match {
                                                case HttpStatus.CREATED => 
                                                        session("validUrl").asOption[String] match {
                                                            case Some(url) if "^https?://.*".r.findFirstIn(url).isDefined =>
                                                                session.set("poolingComplete", true)
                                                            case Some(bad) =>
                                                                throw new RuntimeException(s"Invalid URL in filePath: $bad")
                                                            case None =>
                                                                session.set("poolingComplete", true).markAsFailed
                                                        }
                                                case HttpStatus.OK => 
                                                        session
                                                case _ => 
                                                        session.set("poolingComplete", true).markAsFailed
                                        }
                                })
                                .doIf(session => !session("poolingComplete").as[Boolean]) {
                                        pause(500 milliseconds) 
                                }
                        }
                        // .pause(200 milliseconds, 500 milliseconds)
                        // .exec(session => {
                        //         val executionId = session("executionId").as[String]
                        //         println(s"""executionId: #{executionId} """)
                        //         // val scriptOutput = s"""az storage blob delete --container-name "reports-generated" --name "generated/report-#{executionId}.pdf"""".!
                        //         val scriptOutput = s"""echo "generated/report-#{executionId}.pdf"""".!
                        //         println(s"""blob deleted: #{scriptOutput} """)
                        //         session
                        // })
                        // .pause(500 milliseconds)
                        // .exec(http("Check Report Status after blob deletion")
                        //         .get("/report/${executionId}")
                        //         .header("accept", "application/json")
                        //         .check(status.is(HttpStatus.NO_CONTENT.value))
                        // )
                }

        setUp(scn.inject(
                rampUsers(3) during (5 seconds)
        )).protocols(httpProtocol)
}