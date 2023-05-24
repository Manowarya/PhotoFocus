package main

import (
	"GoProject/database"
	"GoProject/routes"
	_ "github.com/go-sql-driver/mysql"
	"github.com/labstack/echo"
	_ "github.com/labstack/echo"
	"github.com/labstack/echo/middleware"
	_ "github.com/labstack/echo/middleware"
	_ "net/http"
)

func main() {

	db := database.Initialize()
	database.Migrate(db)

	e := echo.New()
	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.GET("/fetch-values", routes.GetTemplate(db))
	e.POST("/save-template", routes.SaveTemplate(db))
	e.POST("/verification", routes.VerificationEmail(db))
	e.POST("/register", routes.RegisterUser(db))
	e.POST("/authorization", routes.AuthorizationUser(db))

	e.Start(":9000")
}
