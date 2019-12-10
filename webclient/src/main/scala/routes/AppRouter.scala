/*
 * Copyright 2019 Roberto Leibman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package routes

import java.time.LocalDate

import components.AbstractComponent
import japgolly.scalajs.react.extra.router.StaticDsl.RouteB
import japgolly.scalajs.react.extra.router._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo, ReactMouseEventFrom, ScalaComponent}
import org.scalajs.dom._
import org.scalajs.dom.ext.{Ajax, AjaxException}
import org.scalajs.dom.raw.HTMLAnchorElement
import pages.MainPage
import upickle.default.read

import scala.concurrent.Future

object AppRouter extends AbstractComponent {

  import scala.concurrent.ExecutionContext.Implicits.global

  case class State()

  sealed trait AppPageData

  case object MainPageData extends AppPageData

  private def setEH(c: RouterCtl[AppPageData], target: AppPageData) = {
    (event: ReactMouseEventFrom[HTMLAnchorElement], data: MenuItemProps) =>
      c.setEH(target)(event)
  }

  private def layout(page: RouterCtl[AppPageData], resolution: Resolution[AppPageData]) = {
    assert(page != null)
    <.div(
      ^.height := 100.pct,
      <.div(
        ^.height := 100.pct,
        ^.className := "full height",
        ^.display := "flex",
        ^.flexDirection := "row",
        <.div(
          ^.height := 100.pct,
          ^.className := "no-print",
          ^.flex := "0 0  auto",
          ^.position := "relative",
          Menu(vertical = true)(
              MenuItem(
                active = resolution.page == MainPageData,
                onClick = { (event: ReactMouseEventFrom[HTMLAnchorElement], data: MenuItemProps) =>
                  page.setEH(MainPageData)(event)
                }
              )("Main Page")
            )
          )
        ),
        <.div(^.flex := "1 1  auto", resolution.render())
      )
  }

  private val config: RouterConfig[AppPageData] = RouterConfigDsl[AppPageData].buildConfig { dsl =>
    import dsl._

    val seqInt = new RouteB[Seq[Int]](
      regex = "(-?[\\d,]+)",
      matchGroups = 1,
      parse = { groups =>
        Some(groups(0).split(",").map(_.toInt))
      },
      build = _.mkString(",")
    )

    val dateRange = new RouteB[(LocalDate, LocalDate)](
      regex = "\\((.*),(.*)\\)",
      matchGroups = 2,
      parse = { groups =>
        Some((LocalDate.parse(groups(0)), LocalDate.parse(groups(1))))
      },
      build = { tuple =>
        s"(${tuple._1.toString},${tuple._2.toString})"
      }
    )

    (trimSlashes
    | staticRoute("#newRecipe", MainPageData) ~> renderR(ctrl => MainPage()))
      .notFound(redirectToPage(MainPageData)(Redirect.Replace))
      .renderWith(layout)
  }
  private val baseUrl = BaseUrl.fromWindowOrigin_/
  //    if (dom.window.location.hostname == "localhost")
  //    BaseUrl.fromWindowOrigin_/
  //    else
  //      BaseUrl.fromWindowOrigin / "mealorama/"

  val router = Router.apply(baseUrl, config)
}
