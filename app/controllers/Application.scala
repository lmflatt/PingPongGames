package controllers

import models._
import org.joda.time.DateTime
import play.api._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.api.Play.current
import play.api.mvc.BodyParsers._
import play.api.libs.json.Json
import play.api.libs.json.Json._

import scala.collection.mutable

object Application extends Controller{

  //create an instance of the table
  val games = TableQuery[GamesTable] //see a way to architect your app in the computers-database-slick sample

  //JSON read/write macro
  implicit val gameFormat = Json.format[Game]

//  def index = DBAction { implicit rs =>
//    val formattedGames = List[FormattedGame]()
//    games.list.map { dbGame =>
//      val players = Array(dbGame.player_1, dbGame.player_2)
//      val scores = Array(dbGame.score_1, dbGame.score_2)
//      val game = new FormattedGame(dbGame.id, players, scores, dbGame.dateTime)
//    }
//
//    Ok(formattedGames)
//  }

//  val gameForm = Form(
//    mapping(
//      "name" -> text(),
//      "color" -> text()
//    )(Game.apply)(Game.unapply)
//  )

//  def insert = DBAction { implicit rs =>
//    val cat = catForm.bindFromRequest.get
//    cats.insert(cat)
//
//    Redirect(routes.Application.index)
//  }

  def jsonFindAll = DBAction { implicit rs =>
      //define new list, and fill it with items modeled after sender contract
      val formattedGames = games.list.map { dbGame =>
        dbGameToFormattedGame(dbGame)
      }
      Ok(Json.obj("games" -> toJson(formattedGames))).withHeaders(
        "Access-Control-Allow-Origin" -> "*",
        "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
        "Access-Control-Allow-Headers" -> "Accept, Origin, Content-type, X-Json, X-Prototype-Version, X-Requested-With",
        "Access-Control-Allow-Credentials" -> "true",
        "Access-Control-Max-Age" -> (60 * 60 * 24).toString
      )
    }

  def jsonInsert = DBAction(parse.json) { implicit rs =>
      rs.request.body.validate[Game].map { game =>
        val dateTime = DateTime.now
        val dbGame = new DBGame(None, game.player_1, game.player_2, game.score_1, game.score_2, dateTime.toString)
        games.insert(dbGame)
        val formattedGame = dbGameToFormattedGame(dbGame)
        Ok(Json.obj("game" -> toJson(formattedGame))).withHeaders(
          "Access-Control-Allow-Origin" -> "*",
          "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
          "Access-Control-Allow-Headers" -> "Accept, Origin, Content-type, X-Json, X-Prototype-Version, X-Requested-With",
          "Access-Control-Allow-Credentials" -> "true",
          "Access-Control-Max-Age" -> (60 * 60 * 24).toString
        )
      }.getOrElse(BadRequest("invalid json"))
    }

  def options(path: String) = Action {
    Ok("").withHeaders(
      "Access-Control-Allow-Origin" -> "*",
      "Access-Control-Allow-Methods" -> "GET, POST, PUT, DELETE, OPTIONS",
      "Access-Control-Allow-Headers" -> "Accept, Origin, Content-type, X-Json, X-Prototype-Version, X-Requested-With",
      "Access-Control-Allow-Credentials" -> "true",
      "Access-Control-Max-Age" -> (60 * 60 * 24).toString
    )
  }

  def dbGameToFormattedGame (dbGame: DBGame): FormattedGame = {
    val players = Array(dbGame.player_1, dbGame.player_2)
    val scores = Array(dbGame.score_1, dbGame.score_2)
    new FormattedGame(dbGame.id, players, scores, dbGame.dateTime.substring(0,10))
  }
}
