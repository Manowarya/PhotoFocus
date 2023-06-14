package main

import (
	"GoProject/database"
	_ "GoProject/docs"
	"GoProject/routes"
	_ "github.com/go-sql-driver/mysql"
	"github.com/labstack/echo/v4"
	"github.com/labstack/echo/v4/middleware"
	_ "github.com/labstack/echo/v4/middleware"
	echoSwagger "github.com/swaggo/echo-swagger"
	"github.com/swaggo/swag"
	"net/http"
	_ "net/http"
	"regexp"
)

// @title PhotoFocus API
// @version 1.0.5

// @host photofocus-production.up.railway.app

func allowOrigin(origin string) (bool, error) {
	// In this example we use a regular expression but we can imagine various
	// kind of custom logic. For example, an external datasource could be used
	// to maintain the list of allowed origins.
	return regexp.MatchString(`^https:\/\/labstack\.(net|com)$`, origin)
}

func main() {

	db := database.Initialize()
	database.Migrate(db)

	e := echo.New()
	e.Use(middleware.Logger())
	e.Use(middleware.Recover())
	e.Use(middleware.CORS())

	e.GET("/swagger/*", echoSwagger.WrapHandler)

	e.GET("/swagger.json", func(c echo.Context) error {
		doc, _ := swag.ReadDoc()
		return c.JSONBlob(http.StatusOK, []byte(doc))
	})

	e.GET("/get-templates/:id", routes.GetTemplate(db))
	e.POST("/save-template", routes.SaveTemplate(db))
	e.POST("/update-template", routes.UpdateTemplate(db))
	e.POST("/delete-template", routes.DeleteTemplate(db))
	e.POST("/verification", routes.VerificationEmail(db))
	e.POST("/register", routes.RegisterUser(db))
	e.POST("/authorization", routes.AuthorizationUser(db))

	e.Start(":9000")
}
