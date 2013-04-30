package global

import models._
import play.api._
import play.api.Play.current

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }

}