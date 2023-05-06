package main

import (
	"GoProject/database"
	"GoProject/routes"
	_ "github.com/go-sql-driver/mysql"
	"github.com/labstack/echo"
	_ "github.com/labstack/echo"
	"github.com/labstack/echo/middleware"
	_ "github.com/labstack/echo/middleware"
)

func main() {

	db := database.Initialize()
	database.Migrate(db)

	e := echo.New()
	e.Use(middleware.Logger())
	e.Use(middleware.Recover())

	e.GET("/fetch-values", routes.GetTemplate(db))

	e.Start(":9000")
}
