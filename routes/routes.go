package routes

import (
	"GoProject/database/model"
	"database/sql"
	"github.com/labstack/echo"
	"net/http"
)

func GetTemplate(db *sql.DB) echo.HandlerFunc {
	return func(c echo.Context) error {
		prices, err := model.GetTemplates(db, "1")
		if err != nil {
			return c.JSON(http.StatusBadGateway, err)
		}
		return c.JSON(http.StatusOK, prices)
	}
}
